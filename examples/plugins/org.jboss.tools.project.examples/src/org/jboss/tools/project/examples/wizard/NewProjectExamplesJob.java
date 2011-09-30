package org.jboss.tools.project.examples.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.Project;

public class NewProjectExamplesJob extends WorkspaceJob {

	private List<Project> selectedProjects;
	private List<Project> projects = new ArrayList<Project>();

	public NewProjectExamplesJob(String name, List<Project> selectedProjects) {
		super(name);
		this.selectedProjects = selectedProjects;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		projects .clear();
		for (Project selectedProject : selectedProjects) {
			boolean success = ProjectExamplesActivator.downloadProject(
					selectedProject, monitor);
			if (success) {
				projects.add(selectedProject);
			}
		}
		try {
			setName(Messages.NewProjectExamplesWizard_Importing);
			for (final Project project : projects) {
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
					return Status.OK_STATUS;
				}
				importProjectExample.importProject(project, project.getFile(), monitor);
				importProjectExample.fix(project, monitor);						
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

	public List<Project> getProjects() {
		return projects;
	}

}
