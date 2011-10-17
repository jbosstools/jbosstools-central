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
package org.jboss.tools.maven.project.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.AbstractProjectScanner;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.actions.OpenMavenConsoleAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IProgressConstants;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.job.ProjectExamplesJob;
import org.jboss.tools.project.examples.model.AbstractImportProjectExample;
import org.jboss.tools.project.examples.model.Project;

/**
 * @author snjeza
 * 
 */
public class ImportMavenProjectExample extends AbstractImportProjectExample {

	private static final String UNNAMED_PROJECTS = "UnnamedProjects"; //$NON-NLS-1$

	//private static final String JBOSS_TOOLS_MAVEN_PROJECTS = "/.JBossToolsMavenProjects"; //$NON-NLS-1$
	
	private boolean confirm;

	@Override
	public List<Project> importProject(Project projectDescription, File file,
			IProgressMonitor monitor) throws Exception {
		List<Project> projects = new ArrayList<Project>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootPath = workspaceRoot.getLocation();
		IPath mavenProjectsRoot = rootPath;//.append(JBOSS_TOOLS_MAVEN_PROJECTS);
		String projectName = projectDescription.getName();
		if (projectName == null || projectName.isEmpty()) {
			projectName = UNNAMED_PROJECTS;
		}
		IPath path = mavenProjectsRoot.append(projectName);
		final File destination = new File(path.toOSString());
		if (destination.exists()) {
			final List<IProject> existingProjects = getExistingProjects(destination);
			if (existingProjects.size() > 0) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						String title = "Overwrite";
						String msg = getMessage(destination, existingProjects);
						confirm = MessageDialog.openQuestion(getActiveShell(),
							title, msg);
					}
				});
				if (confirm) {
					monitor.setTaskName("Deleting ...");
					for (IProject project:existingProjects) {
						monitor.setTaskName("Deleting " + project.getName());
						project.delete(false, true, monitor);
					}
				} else {
					return projects;
				}
			}
			boolean deleted = deleteDirectory(destination, monitor);
			if (monitor.isCanceled()) {
				return projects;
			}
			if (!deleted) {
				Display.getDefault().syncExec(new Runnable() {
						
					@Override
					public void run() {
						MessageDialog.openError(getActiveShell(), 
								"Error", "Cannot delete the '" + destination + "' file.");
					}
				});
				return projects;
			}
		}
		boolean ok = ProjectExamplesActivator.extractZipFile(file, destination, monitor);
		monitor.setTaskName("");
		if (monitor.isCanceled()) {
			return projects;
		}
		if (!ok) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					MessageDialog.openError(getActiveShell(), 
							"Error", 
							"Cannot extract the archive.");
				}
			});
			return projects;
		}
		
		importMavenProjects(destination);
		return projects;
	}

	private void importMavenProjects(final File destination) {
		Job job = new ProjectExamplesJob("Importing Maven projects") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				setProperty(IProgressConstants.ACTION_PROPERTY,
						new OpenMavenConsoleAction());
				MavenPlugin plugin = MavenPlugin.getDefault();
				try {
					AbstractProjectScanner<MavenProjectInfo> projectScanner = getProjectScanner(destination);
					projectScanner.run(monitor);
					List<MavenProjectInfo> mavenProjects = projectScanner
							.getProjects();
					List<MavenProjectInfo> infos = new ArrayList<MavenProjectInfo>();
					infos.addAll(mavenProjects);
					addMavenProjects(infos, mavenProjects);
					final List<IProject> existingProjects = new ArrayList<IProject>();
					ProjectImportConfiguration importConfiguration = new ProjectImportConfiguration();
					for(MavenProjectInfo info:infos) {
						String projectName = getProjectName(info, importConfiguration);
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
						if (project != null && project.exists()) {
							existingProjects.add(project);
						}
					}
					if (existingProjects.size() > 0) {
						Display.getDefault().syncExec(new Runnable() {
							
							@Override
							public void run() {
								String message = getWorkspaceMessage(existingProjects);
								
								confirm = MessageDialog.openConfirm(getActiveShell(), 
										"Confirmation", message);
							}
						});
						if (confirm) {
							for (IProject project:existingProjects) {
								try {
									project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
								} catch (Exception e) {
									// ignore
								}
								project.delete(true, true, monitor);
							}
						} else {
							return Status.CANCEL_STATUS;
						}
					}
					plugin.getProjectConfigurationManager().importProjects(
							infos, importConfiguration, monitor);
				} catch (CoreException ex) {
					MavenProjectExamplesActivator.log(ex, "Projects imported with errors");
					return ex.getStatus();
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(MavenPlugin.getDefault().getProjectConfigurationManager()
				.getRule());
		job.schedule();
	}

	private List<MavenProjectInfo> addMavenProjects(List<MavenProjectInfo> infos, List<MavenProjectInfo> mavenProjects) {
		if (mavenProjects == null || mavenProjects.isEmpty()) {
			return mavenProjects;
		}
		for (MavenProjectInfo projectInfo:mavenProjects) {
			Collection<MavenProjectInfo> projects = projectInfo.getProjects();
			if (projects != null && !projects.isEmpty()) {
				for(MavenProjectInfo info:projects) {
					infos.add(info);
				}
				List<MavenProjectInfo> childProjects = new ArrayList<MavenProjectInfo>();
				childProjects.addAll(projects);
				addMavenProjects(infos, childProjects);
			}
		}
		return mavenProjects;
	}

	private AbstractProjectScanner<MavenProjectInfo> getProjectScanner(
			File folder) {
		File root = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toFile();
		MavenPlugin mavenPlugin = MavenPlugin.getDefault();
		MavenModelManager modelManager = mavenPlugin.getMavenModelManager();
		return new LocalProjectScanner(root, folder.getAbsolutePath(), false,
				modelManager);
	}
	
	private static Shell getActiveShell() {
		return Display.getDefault().getActiveShell();
	}
	
	private static boolean deleteDirectory(File path, IProgressMonitor monitor) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (monitor.isCanceled()) {
					return false;
				}
				monitor.setTaskName("Deleting " + file);
				if (file.isDirectory()) {
					deleteDirectory(file, monitor);
				} else {
					file.delete();
				}
			}
		}
		return (path.delete());
	}

	private String getProjectName(MavenProjectInfo projectInfo,
			ProjectImportConfiguration configuration) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		File pomFile = projectInfo.getPomFile();
		Model model = projectInfo.getModel();
		IMaven maven = MavenPlugin.getDefault().getMaven();
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

	private List<IProject> getExistingProjects(final File destination) {
		List<IProject> existingProjects = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project:projects) {
			if (project != null && project.exists()) {
				File projectFile = project.getLocation().toFile();
				if (projectFile.getAbsolutePath().startsWith(destination.getAbsolutePath())) {
					existingProjects.add(project);
				}
			}
		}
		return existingProjects;
	}
	
	private String getMessage(final File destination, List<IProject> projects) {
		if (projects.size() > 0) {
			StringBuilder builder = new StringBuilder();
			if (projects.size() == 1) {
				builder.append("\nThere is the '" + projects.get(0).getName() + 
						"' project on the destination location:\n\n");
				builder.append("Would you like to overwrite it?");
			} else {
				builder.append("\nThere are the following projects on the destination location:\n\n");
				for (IProject project : projects) {
					builder.append(project.getName());
					builder.append("\n"); //$NON-NLS-1$
				}
				builder.append("\n"); //$NON-NLS-1$
				builder.append("Would you like to overwrite them?");
			}
			return builder.toString();
		}
		return null;
	}
	
	private String getWorkspaceMessage(
			final List<IProject> existingProjects) {
		StringBuilder builder = new StringBuilder();
		if (existingProjects.size() == 1) {
			builder.append("There is the '" + existingProjects.get(0).getName() + 
					"' project in the workspace.\n\n");
			builder.append("Would you like to delete it?");
		} else {
			builder.append("There are the following projects in the workspace:\n\n");
			for (IProject project:existingProjects) {
				builder.append(project.getName());
				builder.append("\n"); //$NON-NLS-1$
			}
			builder.append("\n"); //$NON-NLS-1$
			builder.append("Would you like to delete them?");
		}
		return builder.toString();
	}

}
