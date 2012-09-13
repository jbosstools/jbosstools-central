/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.wizards;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.jobs.RefreshTutorialsJob;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard2;

public abstract class AbstractJBossCentralProjectWizard extends NewProjectExamplesWizard2 {

	private String exampleName = null; 
	
	public AbstractJBossCentralProjectWizard(String exampleName) {
		super();
		Assert.isNotNull(exampleName, "Project name is null");
		this.exampleName = exampleName;
		ProjectExample example = lookupProjectExample();
		initializeProjectExample(example);
	}

	protected ProjectExample lookupProjectExample() {
		
		ProjectExample example = null;
		RefreshTutorialsJob refreshTutorialsJob = RefreshTutorialsJob.INSTANCE;
		List<ProjectExample> wizardProjects = refreshTutorialsJob.getWizardProjects();
		
		if (wizardProjects == null || wizardProjects.isEmpty()) {
			RunnableLookup lookup = new RunnableLookup();
			try {
				new ProgressMonitorDialog(getShell()).run(true, true, lookup);
			} catch (Exception e) {
				JBossCentralActivator.log(e);
			}
			wizardProjects = refreshTutorialsJob.getWizardProjects();
		}		
		
		
		if (wizardProjects != null) {
			for (ProjectExample expl : wizardProjects) {
				if (matches(expl)) {
					example = expl;
					break;
				}
			}
		}
		return example;
	}
	
	private boolean matches(ProjectExample expl) {
		if (expl != null && exampleName.equals(expl.getName())) {
//			if (expl.getSite() != null && "Shared examples".equals(expl.getSite().getName())){
				return true;
//			}
		}
		return false;
	}
	
	@Override
	public void addPages() {
		if (getProjectExample() == null) {
			//MessageDialog.openError(getShell(), "Wizard Error", "Wizard metadata could not be loaded");
			addPage(new ErrorPage("Failed to load Wizard", "Wizard metadata could not be loaded"));
		} else {
			super.addPages();
		}
	}
	
	class RunnableLookup implements IRunnableWithProgress {
		
		public void run(IProgressMonitor monitor) {
			monitor.setTaskName("Refreshing project examples");
			RefreshTutorialsJob refreshTutorialsJob = RefreshTutorialsJob.INSTANCE;	
			int jobState = refreshTutorialsJob.getState();
			try {
				if (jobState == Job.NONE) {
					refreshTutorialsJob.schedule();
				}
				refreshTutorialsJob.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void initializeDefaultPageImageDescriptor() {
		String imagePath = getWizardBackgroundImagePath();
		ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, 
																		  imagePath);
		setDefaultPageImageDescriptor(desc);
	}
	
	protected String getWizardBackgroundImagePath() {
		return "icons/new_wiz.gif";
	}
	
}
