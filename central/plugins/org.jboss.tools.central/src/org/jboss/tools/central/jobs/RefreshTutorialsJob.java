/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.project.examples.model.ProjectFix;

/**
 * 
 * @author snjeza
 *
 */
public class RefreshTutorialsJob extends Job {

	private Exception exception;
	private Map<ProjectExampleCategory,List<ProjectExample>> tutorialCategories;

	private List<ProjectExample> wizardProjects;

	public static RefreshTutorialsJob INSTANCE = new RefreshTutorialsJob();
	
	private RefreshTutorialsJob() {
		super("Refreshing JBoss Tutorials...");
		setPriority(LONG);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		List<ProjectExampleCategory> categories = ProjectExampleUtil.getProjects(monitor);
		wizardProjects = ProjectExampleUtil.getProjectsByTags(categories, "wizard");
		List<ProjectExample> tutorials = ProjectExampleUtil.getProjectsByTags(categories, "central");
		if (tutorialCategories == null) {
		  tutorialCategories = new HashMap<ProjectExampleCategory, List<ProjectExample>>();
		} else {
		  tutorialCategories.clear();
		}
		
		for (ProjectExample project : tutorials) {
		  if (canBeImported(project)){
				List<ProjectFix> unsatisfiedFixes = new ArrayList<ProjectFix>();
				List<ProjectFix> fixes = project.getFixes();
				project.setUnsatisfiedFixes(unsatisfiedFixes);
				for (ProjectFix fix : fixes) {
					if (!ProjectExamplesActivator.canFix(project, fix)) {
						unsatisfiedFixes.add(fix);
					}
				}
  			ProjectExampleCategory category = project.getCategory();
  			List<ProjectExample> projects = tutorialCategories.get(category);
  			if (projects == null) {
  			  projects = new ArrayList<ProjectExample>();
  			  tutorialCategories.put(category, projects);
  			}
  			projects.add(project);
			}
		}
		
		return Status.OK_STATUS;
	}

	private boolean canBeImported(ProjectExample project) {
		return ProjectExamplesActivator.getDefault()
				.getImportProjectExample(project.getImportType()) != null;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Map<ProjectExampleCategory,List<ProjectExample>> getTutorialCategories() {
		return tutorialCategories;
	}

    public List<ProjectExample> getWizardProjects() {
        return wizardProjects;
    }

	@Override
	public boolean belongsTo(Object family) {
		return family == JBossCentralActivator.JBOSS_CENTRAL_FAMILY;
	}
}
