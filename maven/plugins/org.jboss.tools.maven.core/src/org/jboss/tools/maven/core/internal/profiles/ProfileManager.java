package org.jboss.tools.maven.core.internal.profiles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class ProfileManager implements IProfileManager {

	public void updateActiveProfiles(final IMavenProjectFacade mavenProjectFacade, 
									 final List<String> profiles, 
									 final boolean isOffline, 
									 final boolean isForceUpdate, 
									 IProgressMonitor monitor) throws CoreException {
		
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

}
