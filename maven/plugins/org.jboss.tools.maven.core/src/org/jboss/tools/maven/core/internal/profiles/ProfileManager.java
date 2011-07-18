package org.jboss.tools.maven.core.internal.profiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Profile;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.SettingsUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.jboss.tools.maven.core.profiles.IProfileManager;
import org.jboss.tools.maven.core.profiles.ProfileState;
import org.jboss.tools.maven.core.profiles.ProfileStatus;

public class ProfileManager implements IProfileManager {

	public void updateActiveProfiles(final IMavenProjectFacade mavenProjectFacade, 
									 final List<String> profiles, 
									 final boolean isOffline, 
									 final boolean isForceUpdate, 
									 IProgressMonitor monitor) throws CoreException {
		if (mavenProjectFacade == null) {
			return;
		}
		final IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
		final ResolverConfiguration configuration =configurationManager
				.getResolverConfiguration(mavenProjectFacade.getProject());

		final String profilesAsString = getAsString(profiles);
		if (profilesAsString.equals(configuration.getActiveProfiles())) {
			//Nothing changed
			return;
		}
		
		IProject project = mavenProjectFacade.getProject();
		
		configuration.setActiveProfiles(profilesAsString);
		boolean isSet = configurationManager.setResolverConfiguration(project, configuration);
		if (isSet) {
			MavenUpdateRequest request = new MavenUpdateRequest(project, isOffline, isForceUpdate);
			configurationManager.updateProjectConfiguration(request, monitor);
		}

	}
	
	private String getAsString(List<String> profiles) {
		StringBuilder sb = new StringBuilder();
		boolean addComma = false;
		if (profiles != null){
			for (String p : profiles) {
				if (addComma) {
					sb.append(", "); //$NON-NLS-1$
				}
				sb.append(p);
				addComma = true;
			}
		}
		return sb.toString();
	}
	
	public Map<Profile, Boolean> getAvailableProfiles(IMavenProjectFacade facade) throws CoreException {
		if (facade == null) {
			return Collections.emptyMap();
		}
		List<Profile> modelProfiles = facade.getMavenProject().getModel().getProfiles();
		if (modelProfiles == null || modelProfiles.isEmpty()) {
			return Collections.emptyMap();
		}
		
		ResolverConfiguration resolverConfiguration = MavenPlugin.getProjectConfigurationManager()
														.getResolverConfiguration(facade.getProject());
		
		Map<Profile, Boolean> projectProfiles = new LinkedHashMap<Profile, Boolean>(modelProfiles.size());
		List<Profile> activeProfiles = facade.getMavenProject().getActiveProfiles();
		
		for (Profile p : modelProfiles) {
			boolean isAutomaticallyActivated = isActive(p, activeProfiles) 
											&& !resolverConfiguration.getActiveProfileList().contains(p.getId());
			projectProfiles.put(p, isAutomaticallyActivated);
		}
		return Collections.unmodifiableMap(projectProfiles);
	}


	public Map<Profile, Boolean> getAvailableSettingProfiles() throws CoreException {
		Map<Profile, Boolean> settingsProfiles = new LinkedHashMap<Profile, Boolean>();
		Settings settings = MavenPlugin.getMaven().getSettings();
		List<String> activeProfiles = settings.getActiveProfiles();
		
		for (org.apache.maven.settings.Profile sp : settings.getProfiles()) {
			Profile p = SettingsUtils.convertFromSettingsProfile(sp);
			boolean isAutomaticallyActivated = isActive2(p, activeProfiles);
			settingsProfiles.put(p, isAutomaticallyActivated);
		}
		return Collections.unmodifiableMap(settingsProfiles);
	}

	private boolean isActive(Profile p, List<Profile> activeProfiles) {
		for (Profile activeProfile : activeProfiles) {
			if (activeProfile.getId().equals(p.getId())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isActive2(Profile p, List<String> activeProfiles) {
		for (String activeProfile : activeProfiles) {
			if (activeProfile.equals(p.getId())) {
				return true;
			}
		}
		return false;
	}

	public List<ProfileStatus> getProfilesStatuses(
			IMavenProjectFacade facade) throws CoreException {
		if (facade == null) {
			return Collections.emptyList();
		}
		
		ResolverConfiguration resolverConfiguration = MavenPlugin.getProjectConfigurationManager()
														.getResolverConfiguration(facade.getProject());

		List<String> configuredProfiles = resolverConfiguration.getActiveProfileList();
		
		final List<Profile> activeProfiles = facade.getMavenProject().getActiveProfiles();

		List<Profile> projectProfiles = new ArrayList<Profile>(facade.getMavenProject().getModel().getProfiles());

		final Map<Profile, Boolean> availableSettingsProfiles = getAvailableSettingProfiles();
		Set<Profile> settingsProfiles = new HashSet<Profile>(availableSettingsProfiles.keySet());
		
		List<ProfileStatus> statuses = new ArrayList<ProfileStatus>();
		
		//First we put user configured profiles
		for (String pId : configuredProfiles) {
			if ("".equals(pId.trim())) continue;
			boolean isDisabled = pId.startsWith("!");
			String id = (isDisabled)?pId.substring(1):pId;
			ProfileStatus status = new ProfileStatus(id);
			status.setUserSelected(true);
			ProfileState state = isDisabled?ProfileState.Disabled
											:ProfileState.Active;
			status.setActivationState(state);
			
			Profile p = get(id, projectProfiles);

			if (p == null){
				p = get(id, settingsProfiles);
				if(p != null){
					status.setAutoActive(availableSettingsProfiles.get(p));
				}
			} 

			if (p == null) {
				status.setSource("undefined");
			} else {
				status.setSource(p.getSource());
			}
			statuses.add(status);
		}
		
		//Iterate on the remaining project profiles
		addStatuses(statuses, projectProfiles, new ActivationPredicate() {
			@Override
			boolean isActive(Profile p) {
				return ProfileManager.this.isActive(p, activeProfiles);
			}
		});

		//Iterate on the remaining settings profiles
		addStatuses(statuses, settingsProfiles, new ActivationPredicate() {
			@Override
			boolean isActive(Profile p) {
				return availableSettingsProfiles.get(p);
			}
		});
		return Collections.unmodifiableList(statuses);
	}

	private void addStatuses(List<ProfileStatus> statuses, Collection<Profile> profiles, ActivationPredicate predicate) {
		for (Profile p : profiles) {
			ProfileStatus status = new ProfileStatus(p.getId());
			status.setSource(p.getSource());
			boolean isActive = predicate.isActive(p);
			ProfileState activationState = (isActive)?ProfileState.Active:ProfileState.Inactive;
			status.setAutoActive(isActive);
			status.setActivationState(activationState);
			statuses.add(status);
		}
	}

	private Profile get(String id, Collection<Profile> profiles) {
		Iterator<Profile> ite = profiles.iterator();
		Profile found = null;
		while(ite.hasNext()) {
			Profile p = ite.next(); 
			if(p.getId().equals(id)) {
				found = p;
				ite.remove();
				break;
			}
		}
		return found;
	}

	private abstract class ActivationPredicate {
		abstract boolean isActive(Profile p);
	}
}
