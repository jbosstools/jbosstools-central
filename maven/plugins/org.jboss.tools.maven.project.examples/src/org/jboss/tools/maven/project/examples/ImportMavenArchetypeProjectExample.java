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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.maven.project.examples.wizard.ArchetypeExamplesWizard;
import org.jboss.tools.project.examples.model.AbstractImportProjectExample;
import org.jboss.tools.project.examples.model.Project;

/**
 * @author snjeza
 * 
 */
public class ImportMavenArchetypeProjectExample extends
		AbstractImportProjectExample {

	private static final String UNNAMED_PROJECTS = "UnnamedProjects"; //$NON-NLS-1$

	private static final String JBOSS_TOOLS_MAVEN_PROJECTS = "/.JBossToolsMavenProjects"; //$NON-NLS-1$

	private boolean confirm;

	@Override
	public List<Project> importProject(final Project projectDescription, File file,
			IProgressMonitor monitor) throws Exception {
		List<Project> projects = new ArrayList<Project>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootPath = workspaceRoot.getLocation();
		IPath mavenProjectsRoot = rootPath.append(JBOSS_TOOLS_MAVEN_PROJECTS);
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
					for (IProject project : existingProjects) {
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
						MessageDialog.openError(getActiveShell(), "Error",
							"Cannot delete the '" + destination + "' file.");
					}
				});
				return projects;
			}
		}
		monitor.setTaskName("");
		if (monitor.isCanceled()) {
			return projects;
		}

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				ArchetypeExamplesWizard wizard = new ArchetypeExamplesWizard(destination, projectDescription);
				WizardDialog wizardDialog = new WizardDialog(getActiveShell(), wizard);
				wizardDialog.open();
			}
			
		});
		return projects;
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

	private List<IProject> getExistingProjects(final File destination) {
		List<IProject> existingProjects = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (IProject project : projects) {
			if (project != null && project.exists()) {
				File projectFile = project.getLocation().toFile();
				if (projectFile.getAbsolutePath().startsWith(
						destination.getAbsolutePath())) {
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
				builder.append("\nThere is the '" + projects.get(0).getName()
						+ "' project on the destination location:\n\n");
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

	private String getWorkspaceMessage(final List<IProject> existingProjects) {
		StringBuilder builder = new StringBuilder();
		if (existingProjects.size() == 1) {
			builder.append("There is the '" + existingProjects.get(0).getName()
					+ "' project in the workspace.\n\n");
			builder.append("Would you like to delete it?");
		} else {
			builder.append("There are the following projects in the workspace:\n\n");
			for (IProject project : existingProjects) {
				builder.append(project.getName());
				builder.append("\n"); //$NON-NLS-1$
			}
			builder.append("\n"); //$NON-NLS-1$
			builder.append("Would you like to delete them?");
		}
		return builder.toString();
	}

}
