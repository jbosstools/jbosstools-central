/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.foundation.core.FoundationCorePlugin;
import org.jboss.tools.foundation.core.usage.IUsageTracker;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.ProjectExample;

public class NewProjectExamplesJob extends WorkspaceJob {

	private static final String WORKING_SETS = "workingSets"; //$NON-NLS-1$
	private List<ProjectExample> selectedProjects;
	private List<ProjectExample> projects = new ArrayList<ProjectExample>();
	private IWorkingSet[] workingSets;
	private Map<String, Object> propertiesMap;

	public NewProjectExamplesJob(String name, List<ProjectExample> selectedProjects, IWorkingSet[] workingSets, Map<String, Object> propertiesMap) {
		super(name);
		this.selectedProjects = selectedProjects;
		this.workingSets = workingSets;
		this.propertiesMap = propertiesMap;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		projects .clear();
		for (ProjectExample selectedProject : selectedProjects) {
			boolean success = ProjectExamplesActivator.downloadProject(
					selectedProject, monitor);
			if (success) {
				projects.add(selectedProject);
				FoundationCorePlugin.getDefault().getUsageTrackerService().sendLiveEvent(IUsageTracker.CATEGORY_CENTRAL, IUsageTracker.ACTION_INSTALLED_EXAMPLE, selectedProject.getName());
			} else {
				final String message = "Unable to download the '" + selectedProject.getName() + "' quickstart.\r\nPlease check your Internet connection and/or Proxy Settings";
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						MessageDialog.openError(getShell(), "Error",message); 
					}
				});
				return Status.CANCEL_STATUS;
			}
		}
		try {
			setName(Messages.NewProjectExamplesWizard_Importing);
			for (final ProjectExample project : projects) {
				IImportProjectExample importProjectExample = 
					ProjectExamplesActivator.getDefault().getImportProjectExample(project.getImportType());
				if (importProjectExample == null) {
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							MessageDialogWithToggle.openError(getShell(),
									Messages.NewProjectExamplesWizard_Error, 
									"Cannot import a project of the '" + project.getImportType() + "' type.");
						}
					});
					return Status.CANCEL_STATUS;
				}
				if (importProjectExample.importProject(project, project.getFile(), propertiesMap, monitor)) {
					importProjectExample.fix(project, monitor);			
					ProjectExamplesActivator.fixWelcome(project);
				} else {
					return Status.CANCEL_STATUS;
				}
				if (workingSets == null || workingSets.length == 0) {
					if (propertiesMap != null) {
						Object object = propertiesMap.get(WORKING_SETS);
						if (object instanceof List<?>) {
							List<IWorkingSet> list = (List<IWorkingSet>) object;
							workingSets = list.toArray(new IWorkingSet[0]);
						}
					}
				}
				if (workingSets != null && workingSets.length > 0 && project.getIncludedProjects() != null) {
					for (String projectName:project.getIncludedProjects()) {
						IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
						if (eclipseProject != null && eclipseProject.exists()) {
							PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(eclipseProject, workingSets);
						}
					}
				}
			}
		} catch (final Exception e) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					MessageDialogWithToggle.openError(getShell(),
							Messages.NewProjectExamplesWizard_Error, e.getMessage(), Messages.NewProjectExamplesWizard_Detail, false,
							ProjectExamplesActivator.getDefault()
									.getPreferenceStore(),
							"errorDialog"); //$NON-NLS-1$
				}

			});
			ProjectExamplesActivator.log(e);
		}
		return Status.OK_STATUS;
	}

	protected Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	public List<ProjectExample> getProjects() {
		return projects;
	}

}
