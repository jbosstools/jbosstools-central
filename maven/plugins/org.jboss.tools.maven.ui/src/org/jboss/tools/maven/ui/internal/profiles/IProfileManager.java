package org.jboss.tools.maven.ui.internal.profiles;

import java.util.List;
import java.util.Map;

import org.apache.maven.model.Profile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public interface IProfileManager {
	
	Map<Profile, Boolean> getAvailableProfiles(IMavenProjectFacade project) throws CoreException;
	
	Map<Profile, Boolean> getAvailableSettingProfiles() throws CoreException;
	
	void updateActiveProfiles(IMavenProjectFacade mavenProjectFacade,
			List<String> profiles, boolean isOffline, boolean isForceUpdate)
			throws CoreException; 
}
