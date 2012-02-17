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
import org.jboss.tools.central.jobs.RefreshTutorialsJob;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard2;

public abstract class AbstractJBossCentralProjectWizard extends NewProjectExamplesWizard2 {

	private String exampleName = null; 
	
	public AbstractJBossCentralProjectWizard(String exampleName) {
		super();
		Assert.isNotNull(exampleName);
		this.exampleName = exampleName;
		ProjectExample example = lookupProjectExample();
		initializeProjectExample(example);
	}

	protected ProjectExample lookupProjectExample() {
		
		ProjectExample example = null;
		RefreshTutorialsJob refreshTutorialsJob = RefreshTutorialsJob.INSTANCE;
		List<ProjectExample> wizardProjects = refreshTutorialsJob.getWizardProjects();
		if (wizardProjects == null || wizardProjects.isEmpty()) {
			//FIXME needs to execute refreshTutorialsJob and wait, gracefully
			//wizardProjects = refreshTutorialsJob.getWizardProjects();
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
			if (expl.getSite() != null && "Shared examples".equals(expl.getSite().getName())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addPages() {
		if (getProjectExample() == null) {
			//TODO add error page
		} else {
			super.addPages();
		}
	}
	
	/*
		RunnableLookup lookup = new RunnableLookup(exampleName);
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, lookup);
		} catch (Exception e) {
			JBossCentralActivator.log(e);
		}

	class RunnableLookup implements IRunnableWithProgress {
		
		ProjectExample example;

		public void run(IProgressMonitor monitor) {
			List<ProjectExampleCategory> categories = ProjectExampleUtil.getProjects(monitor);
			for (ProjectExample expl : ProjectExampleUtil.getProjectsByTags(categories, "wizard")) {
				if (matches(expl)) {
					example = expl;
					break;
				}
			}
		}
	}
	 */
}
