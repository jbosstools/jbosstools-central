package org.jboss.tools.seam.tutorial.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectUtil;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard;
import org.jboss.tools.seam.tutorial.Activator;

public class ImportProjectExample extends Action implements ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		if(params == null || params[0] == null || params[1] == null || params[2] == null ) {
			return;
		}
		
		Project project = new Project();
		project.setName(params[0]);
		StringTokenizer tokenizer = new StringTokenizer(params[1],",");
		List<String> includedProjects = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			includedProjects.add(tokenizer.nextToken().trim());
		}
		project.setIncludedProjects(includedProjects);
		project.setUrl(params[2]);
		importProject(project);
		
	}

	private void importProject(final Project project) {
		WorkspaceJob workspaceJob = new WorkspaceJob(Messages.NewProjectExamplesWizard_Downloading) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				String urlString = project.getUrl();
				String name = project.getName();
				URL url = null;
				try {
					url = new URL(urlString);
				} catch (MalformedURLException e) {
					ProjectExamplesActivator.log(e);
					return Status.CANCEL_STATUS;
				}
				final File file = ProjectUtil.getProjectExamplesFile(
						url, name, ".zip", monitor); //$NON-NLS-1$
				if (file == null) {
					return Status.CANCEL_STATUS;
				}
				
				setName(Messages.NewProjectExamplesWizard_Importing);
				try {
					NewProjectExamplesWizard.importProject(project, file, monitor);
				} catch (Exception e) {
					IStatus status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,e.getMessage(),e);
					throw new CoreException(status);
				}
				
				return Status.OK_STATUS;
			}
			
		};
		workspaceJob.setUser(true);
		
		workspaceJob.addJobChangeListener(new IJobChangeListener() {

			public void aboutToRun(IJobChangeEvent event) {

			}

			public void awake(IJobChangeEvent event) {

			}

			public void done(IJobChangeEvent event) {
				try {
					ProjectExamplesActivator.waitForBuildAndValidation
							.schedule();
					ProjectExamplesActivator.waitForBuildAndValidation
							.join();
				} catch (InterruptedException e) {
					return;
				}
				List<Project> projects = new ArrayList<Project>();
				projects.add(project);
				List<IMarker> markers = ProjectExamplesActivator
						.getMarkers(projects);
				if (markers != null && markers.size() > 0) {
					NewProjectExamplesWizard.showQuickFix(projects);
				}
				
			}

			public void running(IJobChangeEvent event) {

			}

			public void scheduled(IJobChangeEvent event) {

			}

			public void sleeping(IJobChangeEvent event) {

			}

		});
		workspaceJob.schedule();
	}

	
}
