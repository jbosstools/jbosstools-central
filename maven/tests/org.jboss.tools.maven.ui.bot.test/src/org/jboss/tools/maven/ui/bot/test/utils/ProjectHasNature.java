package org.jboss.tools.maven.ui.bot.test.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class ProjectHasNature extends DefaultCondition{
	
	private IProject project;
	private String natureID; 
	
	public ProjectHasNature(String projectName, String natureID){
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		this.natureID=natureID;
		
	}
	
	public boolean test() throws Exception {
		return project.hasNature(natureID);
	}

	public String getFailureMessage() {
		return "Project "+project+" doesn't not have nature "+natureID;
	}

}
