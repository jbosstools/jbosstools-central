package org.jboss.tools.maven.project.examples;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.maven.project.examples.xpl.UpdateMavenProjectJob;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenProjectExamplesActivator extends AbstractUIPlugin {
 
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.project.examples"; //$NON-NLS-1$

	// The shared instance
	private static MavenProjectExamplesActivator plugin;
	
	/**
	 * The constructor
	 */
	public MavenProjectExamplesActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MavenProjectExamplesActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable e, String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		IStatus status = new Status(IStatus.INFO, PLUGIN_ID, message);
		getDefault().getLog().log(status);
	}
	
	public static String getProjectName(MavenProjectInfo projectInfo,
			ProjectImportConfiguration configuration) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		File pomFile = projectInfo.getPomFile();
		Model model = projectInfo.getModel();
		IMaven maven = MavenPlugin.getMaven();
		if (model == null) {
			model = maven.readModel(pomFile);
			projectInfo.setModel(model);
		}

		String projectName = configuration.getProjectName(model);

		File projectDir = pomFile.getParentFile();
		String projectParent = projectDir.getParentFile().getAbsolutePath();

		if (projectInfo.getBasedirRename() == MavenProjectInfo.RENAME_REQUIRED) {
			File newProject = new File(projectDir.getParent(), projectName);
			if (!projectDir.equals(newProject)) {
				projectDir = newProject;
			}
		} else {
			if (projectParent.equals(root.getLocation().toFile()
					.getAbsolutePath())) {
				projectName = projectDir.getName();
			}
		}
		return projectName;
	}

	public static ImageDescriptor getNewWizardImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/new_wiz.png");
	}
	
	public static void updateMavenConfiguration(String projectName, List<String> includedProjects,final IProgressMonitor monitor) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project != null && project.isAccessible()) {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				// ignore
			}
		}
		if (includedProjects.size() > 0) {
			IProject[] selectedProjects = new IProject[includedProjects.size()];
			int i = 0;
			
			for (String selectedProjectName:includedProjects) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProjectName);
				selectedProjects[i++] = project;
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					// ignore
				}
			}
			Job updateJob = new UpdateMavenProjectJob(selectedProjects , true, false);
			updateJob.schedule();
			try {
				updateJob.join();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}
}
