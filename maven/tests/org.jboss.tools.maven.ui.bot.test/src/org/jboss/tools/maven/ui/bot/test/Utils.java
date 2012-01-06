package org.jboss.tools.maven.ui.bot.test;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.internal.IMavenConstants;

@SuppressWarnings("restriction")
public class Utils {
	
	public static boolean isMavenProject(String projectName) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		return project.hasNature(IMavenConstants.NATURE_ID);
	}
	
}
