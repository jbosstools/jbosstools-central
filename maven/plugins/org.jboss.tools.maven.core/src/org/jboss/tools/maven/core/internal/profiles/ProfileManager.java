package org.jboss.tools.maven.core.internal.profiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.SettingsUtils;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.maven.core.profiles.IProfileManager;
import org.jboss.tools.maven.core.profiles.ProfileState;
import org.jboss.tools.maven.core.profiles.ProfileStatus;

public class ProfileManager implements IProfileManager {

	private static final String ARTIFACT_SEPARATOR = ":"; //$NON-NLS-1$
	
	public void updateActiveProfiles(final IMavenProjectFacade mavenProjectFacade, 
									 final List<String> profiles, 
									 final boolean isOffline, 
									 final boolean isForceUpdate, 
									 IProgressMonitor monitor) throws CoreException {
		if (mavenProjectFacade == null) {
			return;
		}
		final IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();

		IProject project = mavenProjectFacade.getProject();
		
		final ResolverConfiguration configuration =configurationManager.getResolverConfiguration(project);

		final String profilesAsString = getAsString(profiles);
		if (profilesAsString.equals(configuration.getActiveProfiles())) {
			//Nothing changed
			return;
		}
		
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
			IMavenProjectFacade facade,
			IProgressMonitor monitor
			) throws CoreException {
		if (facade == null) {
			return Collections.emptyList();
		}
		
		ResolverConfiguration resolverConfiguration = MavenPlugin.getProjectConfigurationManager()
														.getResolverConfiguration(facade.getProject());

		List<String> configuredProfiles = resolverConfiguration.getActiveProfileList();
		
		MavenProject mavenProject = facade.getMavenProject(monitor);
		
		
		List<Model> modelHierarchy = new ArrayList<Model>();
		getModelHierarchy(modelHierarchy, mavenProject.getModel(), new NullProgressMonitor());

		List<Profile> availableProfiles = collectAvailableProfiles(modelHierarchy, new NullProgressMonitor());

		final Map<Profile, Boolean> availableSettingsProfiles = getAvailableSettingProfiles();
		Set<Profile> settingsProfiles = new HashSet<Profile>(availableSettingsProfiles.keySet());
		
		List<ProfileStatus> statuses = new ArrayList<ProfileStatus>();
		
		//First we put user configured profiles
		for (String pId : configuredProfiles) {
			if (StringUtils.isEmpty(pId)) continue;
			boolean isDisabled = pId.startsWith("!");
			String id = (isDisabled)?pId.substring(1):pId;
			ProfileStatus status = new ProfileStatus(id);
			status.setUserSelected(true);
			ProfileState state = isDisabled?ProfileState.Disabled
											:ProfileState.Active;
			status.setActivationState(state);
			
			Profile p = get(id, availableProfiles);
			
			if (p == null){
				p = get(id, settingsProfiles);
				if(p != null){
					status.setAutoActive(availableSettingsProfiles.get(p));
				}
			} 

			status.setSource(findSource(p, modelHierarchy));
			statuses.add(status);
		}
		
		final List<Profile> activeProfiles = mavenProject.getActiveProfiles();
		//Iterate on the remaining project profiles
		addStatuses(statuses, availableProfiles, modelHierarchy, new ActivationPredicate() {
			@Override
			boolean isActive(Profile p) {
				return ProfileManager.this.isActive(p, activeProfiles);
			}
		});

		//Iterate on the remaining settings profiles
		addStatuses(statuses, settingsProfiles, modelHierarchy, new ActivationPredicate() {
			@Override
			boolean isActive(Profile p) {
				return availableSettingsProfiles.get(p);
			}
		});
		return Collections.unmodifiableList(statuses);
	}

	private String findSource(Profile profile, List<Model> modelHierarchy) {
		if (profile != null) {
			if ("settings.xml".equals(profile.getSource())) { //$NON-NLS-1$
				return profile.getSource();
			}
			for (Model m : modelHierarchy) {
				for (Profile p : m.getProfiles()) {
					if(p.equals(profile)) {
						return  //m.getGroupId()+ARTIFACT_SEPARATOR+
								m.getArtifactId()
								//+ (m.getVersion()==null?"":ARTIFACT_SEPARATOR+m.getVersion())//$NON-NLS-1$
								;
					}
				}
			}
		} 
		return "undefined"; //$NON-NLS-1$
	}

	protected List<Profile> collectAvailableProfiles(List<Model> models, IProgressMonitor monitor) throws CoreException {
		List<Profile> profiles = new ArrayList<Profile>();
		for (Model m : models) {
			profiles.addAll(m.getProfiles());
		}
		return profiles;
	}

	protected List<Model> getModelHierarchy(List<Model> models, Model projectModel, IProgressMonitor monitor) throws CoreException {
		if (projectModel == null) {
			return null;
		}
		models.add(projectModel);
		Parent p  = projectModel.getParent();
		if (p != null) {
			Model parentModel = resolvePomModel(p.getGroupId(), p.getArtifactId(), p.getVersion(), monitor);
			if (parentModel != null) {
				getModelHierarchy(models, parentModel, monitor);
			}
		}
		return models;
	}
	
	 private Model resolvePomModel(String groupId, String artifactId, String version, IProgressMonitor monitor)
		      throws CoreException {
	    monitor.subTask(NLS.bind("Resolving {0}:{1}:{2}", new Object[] { groupId, artifactId, version}));

	    IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject(groupId, artifactId, version);
	    IMaven maven = MavenPlugin.getMaven(); 
	    
	    if (facade != null) {
	    	return facade.getMavenProject(monitor).getModel();
	    }
	    
	    List<ArtifactRepository> repositories = maven.getArtifactRepositories();
	    Artifact artifact = maven.resolve(groupId, artifactId, version, "pom", null, repositories, monitor); //$NON-NLS-1$
	    File file = artifact.getFile();
	    if(file == null) {
	      return null;
	    }
	    
	    return maven.readModel(file);
	 }

	private void addStatuses(List<ProfileStatus> statuses, Collection<Profile> profiles, List<Model> modelHierarchy, ActivationPredicate predicate) {
		for (Profile p : profiles) {
			ProfileStatus status = new ProfileStatus(p.getId());
			status.setSource(findSource(p, modelHierarchy));
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
