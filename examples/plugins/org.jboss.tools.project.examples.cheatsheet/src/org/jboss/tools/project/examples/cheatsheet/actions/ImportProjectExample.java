/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.cheatsheet.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.cheatsheet.Activator;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;

/**
 * 
 * <p>Action that imports project examples to workspace.</p>
 * 
 * @author snjeza
 *
 */
public class ImportProjectExample extends Action implements ICheatSheetAction {

	/**
	 * Execution of the action
	 * 
	 * @param params
	 *            Array of parameters
	 *            index 0: projectName,
	 *            index 1: included projects, 
	 *            index 2: the URL of the project example
	 * @param manager
	 *            Cheatsheet Manager
	 */
	public void run(String[] params, ICheatSheetManager manager) {
		if(params == null || params[0] == null || params[1] == null || params[2] == null ) {
			return;
		}
		
		ProjectExampleWorkingCopy project = new ProjectExampleWorkingCopy();
		project.setName(params[0]);
		StringTokenizer tokenizer = new StringTokenizer(params[1],","); //$NON-NLS-1$
		List<String> includedProjects = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			includedProjects.add(tokenizer.nextToken().trim());
		}
		project.setIncludedProjects(includedProjects);
		project.setUrl(params[2]);
		importProject(project);
		
	}

	private void importProject(final ProjectExampleWorkingCopy project) {
		WorkspaceJob workspaceJob = new WorkspaceJob(Messages.NewProjectExamplesWizard_Downloading) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				ProjectExamplesActivator.downloadProject(project, monitor);
				
				setName(Messages.NewProjectExamplesWizard_Importing);
				try {
					IImportProjectExample importProjectExample = ProjectExamplesActivator.getDefault().getImportProjectExample(project.getImportType());
					if (importProjectExample.importProject(project, project.getFile(), new HashMap<String, Object>(), monitor)) {
						UsageEventType createProjectEvent = ProjectExamplesActivator.getDefault().getCreateProjectFromExampleEventType();
						UsageReporter.getInstance().trackEvent(createProjectEvent.event(project.getExampleId()));
						
						importProjectExample.fix(project, monitor);
					}
				} catch (Exception e) {
					IStatus status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,e.getMessage(),e);
					throw new CoreException(status);
				}
				
				return Status.OK_STATUS;
			}
			
		};
		workspaceJob.setUser(true);
		
		workspaceJob.addJobChangeListener(new JobChangeAdapter() {

			public void done(IJobChangeEvent event) {
				try {
					ProjectExamplesActivator.waitForBuildAndValidation
							.schedule();
					ProjectExamplesActivator.waitForBuildAndValidation
							.join();
				} catch (InterruptedException e) {
					return;
				}
				ProjectExamplesActivator.showReadyWizard(Collections.singletonList(project));
			}
		});
		workspaceJob.schedule();
	}

	
}
