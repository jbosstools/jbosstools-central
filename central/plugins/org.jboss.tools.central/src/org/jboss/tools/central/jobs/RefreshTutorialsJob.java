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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.model.Tutorial;
import org.jboss.tools.central.model.TutorialCategory;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Category;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.model.ProjectUtil;

/**
 * 
 * @author snjeza
 *
 */
public class RefreshTutorialsJob extends Job {

	private Exception exception;
	private Map<String, TutorialCategory> tutorialCategories;
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
		tutorialCategories = JBossCentralActivator.getDefault().getTutorialCategories();
		List<Category> categories = ProjectUtil.getProjects(monitor);
		Collection<TutorialCategory> values = tutorialCategories.values();
		for (TutorialCategory category:values) {
			for (Tutorial tutorial:category.getTutorials()) {
				if (JBossCentralActivator.PROJECT_EXAMPLE_TYPE.equals(tutorial.getType())) {
					String reference = tutorial.getReference();
					String[] projectExamples = reference.split("::");
					Assert.isNotNull(projectExamples);
					Assert.isTrue(projectExamples.length == 2);
					String projectExampleCategory = projectExamples[0];
					String projectExampleName = projectExamples[1];
					Project projectTutorial = null;
					for (Category peCategory : categories) {
						if (projectExampleCategory.equals(peCategory.getName())) {
							for (Project project : peCategory.getProjects()) {
								if (projectExampleName
										.equals(project.getName())) {
									projectTutorial = project;
									break;
								}
							}
						}
						if (projectTutorial != null) {
							List<ProjectFix> unsatisfiedFixes = new ArrayList<ProjectFix>();
							if (projectTutorial != null) {
								List<ProjectFix> fixes = projectTutorial.getFixes();
								projectTutorial.setUnsatisfiedFixes(unsatisfiedFixes);
								for (ProjectFix fix : fixes) {
									if (!ProjectExamplesActivator.canFix(projectTutorial, fix)) {
										unsatisfiedFixes.add(fix);
									}
								}
							}
							
							break;
						}
					}
					if (projectTutorial != null) {
						tutorial.setProjectExamples(projectTutorial);
					} else {
						JBossCentralActivator.log("Invalid Project example:" + tutorial.getId());
					}
				} else {
					// FIXME
				}
			}
		}
		
		return Status.OK_STATUS;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Map<String, TutorialCategory> getTutorialCategories() {
		return tutorialCategories;
	}

}
