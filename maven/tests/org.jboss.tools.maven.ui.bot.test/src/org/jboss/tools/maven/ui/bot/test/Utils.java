package org.jboss.tools.maven.ui.bot.test;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;

@SuppressWarnings("restriction")
public class Utils {
	
	
	public static boolean isMavenProject(String projectName) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		return project.hasNature(IMavenConstants.NATURE_ID);
	}
	
	public static boolean hasNature(String projectName, String natureID) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		return project.hasNature(natureID);
	}
	
	public static void waitForIdle() throws InterruptedException {
		AbstractMavenSWTBotTest.waitForIdle();
	}
	
	public static void waitForShell(SWTUtilExt util, String shellName) throws InterruptedException {
		Thread.sleep(1000);
		while(!util.isShellActive(shellName)){
			Thread.sleep(500);
		}
	}
	
}
