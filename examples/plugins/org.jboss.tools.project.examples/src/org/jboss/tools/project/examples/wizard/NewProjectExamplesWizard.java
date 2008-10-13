/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

/**
 * @author snjeza
 * 
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectUtil;

public class NewProjectExamplesWizard extends Wizard implements INewWizard {

	private static final IOverwriteQuery OVERWRITE_ALL_QUERY = new IOverwriteQuery() {
		public String queryOverwrite(String pathString) {
			return IOverwriteQuery.ALL;
		}
	};

	/**
	 * The workbench.
	 */
	private IWorkbench workbench;

	/**
	 * The current selection.
	 */
	private IStructuredSelection selection;

	private NewProjectExamplesWizardPage page;

	protected boolean overwrite;

	public NewProjectExamplesWizard() {
		super();
		setWindowTitle("New Project Example");

	}

	/**
	 * Creates an empty wizard for creating a new resource in the workspace.
	 */

	@Override
	public boolean performFinish() {
		
		if (page.getSelection() == null || page.getSelection().size() <= 0) {
			return false;
		}
		WorkspaceJob job = new WorkspaceJob("Downloading...") {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				IStructuredSelection selection= page.getSelection();
				Iterator iterator = selection.iterator();
				List<Project> projects = new ArrayList<Project>();
				List<File> files = new ArrayList<File>();
				while (iterator.hasNext()) {
					Object object = iterator.next();
					if (object instanceof Project) {
						Project project = (Project) object;
						String url = project.getUrl();
						String name = project.getName();
						final File file = ProjectUtil.getProjectExamplesFile(
								url, name, ".zip", monitor);
						if (file == null) {
							return Status.CANCEL_STATUS;
						}
						projects.add(project);
						files.add(file);
					}
				}
				try {
					int i = 0;
					for (Project project:projects) {
						importProject(project, files.get(i++), monitor);
					}
				} catch (final Exception e) {
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							MessageDialogWithToggle.openError(getShell(), "Error", e
									.getMessage(), "Detail", false,
									ProjectExamplesActivator.getDefault()
											.getPreferenceStore(), "errorDialog");							
						}
						
					});
					ProjectExamplesActivator.log(e);
				} 
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();
		//try {
		//	job.join();
		//} catch (InterruptedException e) {
		//	return false;
		//}
		return true;
	}

	private void importProject(Project projectDescription, File file, IProgressMonitor monitor) throws Exception {
		final String projectName = projectDescription.getName();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		if (project.exists()) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					overwrite = MessageDialog.openQuestion(getShell(), "Question", "Overwrite project '" + projectName + "'");
				}
				
			});
			if (!overwrite) {
				return;
			}
			project.delete(true, true, monitor);
		}
		project.create(monitor);
		project.open(monitor);
		ZipFile sourceFile = new ZipFile(file);
		ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(
				sourceFile);

		ImportOperation operation = new ImportOperation(workspace.getRoot().getFullPath(),
				structureProvider.getRoot(), structureProvider,
				OVERWRITE_ALL_QUERY);
		operation.setContext(getShell());
		operation.run(monitor);

	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		initializeDefaultPageImageDescriptor();
	}

	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ProjectExamplesActivator
				.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID,
						"icons/new_wiz.gif");
		setDefaultPageImageDescriptor(desc);
	}

	@Override
	public void addPages() {
		super.addPages();
		page = new NewProjectExamplesWizardPage();
		addPage(page);
	}

}
