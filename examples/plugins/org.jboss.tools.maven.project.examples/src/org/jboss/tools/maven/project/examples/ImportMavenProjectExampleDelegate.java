/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.jobs.IBackgroundProcessingQueue;
import org.eclipse.m2e.core.project.AbstractProjectScanner;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.actions.OpenMavenConsoleAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.maven.ui.Activator;
import org.jboss.tools.project.examples.internal.UnArchiver;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.project.examples.model.ProjectImportUtil;

/**
 * @author snjeza
 * 
 */
@SuppressWarnings("restriction")
public class ImportMavenProjectExampleDelegate extends AbstractImportMavenProjectDelegate {

	private static final String UNNAMED_PROJECTS = "UnnamedProjects"; //$NON-NLS-1$

	private boolean confirm;

	@Override
	public boolean importProject(ProjectExampleWorkingCopy projectDescription, File file,
			Map<String, Object> propertiesMap, IProgressMonitor monitor) throws Exception {
		IPath rootPath = getLocation();
		IPath mavenProjectsRoot = rootPath;
		String projectName = projectDescription.getName();
		if (projectName == null || projectName.trim().isEmpty()) {
			projectName = UNNAMED_PROJECTS;
		}
		IPath path = mavenProjectsRoot.append(projectName);
		final File destination = new File(path.toOSString());
		if (destination.exists()) {
			final List<IProject> existingProjects = getExistingProjects(destination);
			if (existingProjects.size() > 0) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						String title = existingProjects.size() > 1 ? "Warning: Projects already exist" : "Warning: Project already exists" ;
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
					return false;
				}
			}
			boolean deleted = deleteDirectory(destination, monitor);
			if (monitor.isCanceled()) {
				return false;
			}
			if (!deleted) {
				Display.getDefault().syncExec(new Runnable() {
						
					@Override
					public void run() {
						MessageDialog.openError(getActiveShell(), 
								"Error", "Cannot delete the '" + destination + "' file.");
					}
				});
				return false;
			}
		}
		boolean ok = false;
		if (file.isFile()) {
			UnArchiver unarchiver = UnArchiver.create(file, destination);
			unarchiver.setFilters(projectDescription.getImportFilter());
			ok = unarchiver.extract(monitor);
		}
		else if (file.isDirectory()) {
			destination.mkdirs();
			IFileStore descStore = EFS.getLocalFileSystem().fromLocalFile(destination);
			IFileStore srcStore = EFS.getLocalFileSystem().fromLocalFile(file);
			try {
				srcStore.copy(descStore, EFS.OVERWRITE, monitor);
				ok = true;
			} catch (Exception e) {
				MavenProjectExamplesActivator.log(e);
			}
		}
		monitor.setTaskName("");
		if (monitor.isCanceled()) {
			return false;
		}
		if (!ok) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					MessageDialog.openError(getActiveShell(), 
							"Error", 
							"Cannot extract/copy the archive.");
				}
			});
			return false;
		}
		
		List<String> originalProjects = projectDescription.getIncludedProjects();
		if (originalProjects == null) {
			originalProjects = new ArrayList<>();
		} 
		List<String> includedProjects = new ArrayList<>();
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureSeam = store.getBoolean(Activator.CONFIGURE_SEAM);
		boolean configurePortlet = store.getBoolean(Activator.CONFIGURE_PORTLET);
		boolean configureJSFPortlet = store.getBoolean(Activator.CONFIGURE_JSFPORTLET);
		boolean configureSeamPortlet = store.getBoolean(Activator.CONFIGURE_SEAMPORTLET);
		boolean configureCDI = store.getBoolean(Activator.CONFIGURE_CDI);
		boolean configureHibernate = store.getBoolean(Activator.CONFIGURE_HIBERNATE);
		List<String> projectNames;
		try {
			store.setValue(Activator.CONFIGURE_SEAM, false);
			store.setValue(Activator.CONFIGURE_PORTLET, false);
			store.setValue(Activator.CONFIGURE_JSFPORTLET, false);
			store.setValue(Activator.CONFIGURE_SEAMPORTLET, false);
			store.setValue(Activator.CONFIGURE_CDI, false);
			store.setValue(Activator.CONFIGURE_HIBERNATE, false);
			projectNames = importMavenProjects(destination, projectDescription, monitor);
		} finally {
			store.setValue(Activator.CONFIGURE_SEAM, configureSeam);
			store.setValue(Activator.CONFIGURE_PORTLET, configurePortlet);
			store.setValue(Activator.CONFIGURE_JSFPORTLET, configureJSFPortlet);
			store.setValue(Activator.CONFIGURE_SEAMPORTLET, configureSeamPortlet);
			store.setValue(Activator.CONFIGURE_CDI, configureCDI);
			store.setValue(Activator.CONFIGURE_HIBERNATE, configureHibernate);
		}
		new OpenMavenConsoleAction().run();
		
		if (projectNames != null && !projectNames.isEmpty()) {
			includedProjects.addAll(projectNames);
		} 
		
		waitForMavenJobs(monitor);

		MavenProjectExamplesActivator.updateMavenConfiguration(projectName, includedProjects, monitor);

		//Import standard projects
		if (includedProjects.isEmpty() && new File(destination, ".project").exists()) { //$NON-NLS-1$
			ProjectImportUtil importer = new ProjectImportUtil();
			List<String> names = new ArrayList<>();
			names.add(projectName);
			Collection<IProject> standardProjects = importer.importProjects(path, names, monitor);
			if (standardProjects != null) {
				for (IProject p : standardProjects) {
					includedProjects.add(p.getName());
				}
			}
		}
		if (!originalProjects.isEmpty()) {
			ProjectImportUtil importer = new ProjectImportUtil();
			Collection<IProject> standardProjects = importer.importProjects(path, originalProjects, monitor);
			if (standardProjects != null) {
				for (IProject p : standardProjects) {
					includedProjects.add(p.getName());
				}
			}
		}

		projectDescription.setIncludedProjects(includedProjects);
		
		return true;
	}

	private static void waitForMavenJobs(IProgressMonitor monitor) throws InterruptedException, CoreException {
		Job[] jobs = Job.getJobManager().find(null);
		if (jobs != null) {
			for (Job job : jobs) {
				if (job instanceof IBackgroundProcessingQueue) {
					IBackgroundProcessingQueue queue = (IBackgroundProcessingQueue) job;
					queue.join();
					if (!queue.isEmpty()) {
						IStatus status = queue.run(monitor);
						if (!status.isOK()) {
							throw new CoreException(status);
						}
					}
					if (queue.isEmpty()) {
						queue.cancel();
					}
				}
			}
		}
	  }

	private List<String> importMavenProjects(final File destination,
			final ProjectExample projectDescription, IProgressMonitor monitor) {
		List<String> projectNames = new ArrayList<>();
		try {
			AbstractProjectScanner<MavenProjectInfo> projectScanner = getProjectScanner(destination);
			projectScanner.run(monitor);
			List<MavenProjectInfo> mavenProjects = projectScanner.getProjects();
			List<MavenProjectInfo> infos = new ArrayList<>();
			infos.addAll(mavenProjects);
			addMavenProjects(infos, mavenProjects);
			final List<IProject> existingProjects = new ArrayList<>();
			ProjectImportConfiguration importConfiguration = new ProjectImportConfiguration();
			String profiles = projectDescription.getDefaultProfiles();
			if (profiles != null && profiles.trim().length() > 0) {
				importConfiguration.getResolverConfiguration()
						.setSelectedProfiles(profiles);
			}
			for (MavenProjectInfo info : infos) {
				String projectName = MavenProjectExamplesActivator.getProjectName(info, importConfiguration);
				IProject project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(projectName);
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
					for (IProject project : existingProjects) {
						try {
							project.refreshLocal(IResource.DEPTH_INFINITE,
									monitor);
						} catch (Exception e) {
							// ignore
						}
						project.delete(true, true, monitor);
					}
				} else {
					return projectNames;
				}
			}
			List<String> includedProjects = projectDescription
					.getIncludedProjects();
			if (includedProjects != null && includedProjects.size() > 0) {
				List<MavenProjectInfo> newInfos = new ArrayList<>();
				for (MavenProjectInfo info : infos) {
					Model model = info.getModel();
					if (model != null && model.getArtifactId() != null
							&& model.getArtifactId().trim().length() > 0) {
						for (String includedProject : includedProjects) {
							if (model.getArtifactId().equals(includedProject)) {
								newInfos.add(info);
							}
						}
					}
				}
				infos = newInfos;
			}
			MavenPlugin.getProjectConfigurationManager().importProjects(infos,
					importConfiguration, monitor);
			for (MavenProjectInfo info : infos) {
				Model model = info.getModel();
				if (model != null && model.getArtifactId() != null
						&& model.getArtifactId().trim().length() > 0) {
					
					IMavenProjectFacade f = MavenPlugin.getMavenProjectRegistry().getMavenProject(model.getGroupId(), 
							                                                                      model.getArtifactId(), 
							                                                                      model.getVersion());
					if (f != null && f.getProject() != null) {
						projectNames.add(f.getProject().getName());
					}
				}
			}
		} catch (CoreException ex) {
			MavenProjectExamplesActivator.log(ex,
					"Projects imported with errors");
			return projectNames;
		} catch (InterruptedException e) {
			MavenProjectExamplesActivator.log(e,
					"Projects imported with errors");
			return projectNames;
		}
		return projectNames;
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
				List<MavenProjectInfo> childProjects = new ArrayList<>();
				childProjects.addAll(projects);
				addMavenProjects(infos, childProjects);
			}
		}
		return mavenProjects;
	}

	private AbstractProjectScanner<MavenProjectInfo> getProjectScanner(
			File folder) {
		MavenModelManager modelManager = MavenPlugin.getMavenModelManager();
		return new LocalProjectScanner(Collections.singletonList(folder.getAbsolutePath()), false,
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

	private List<IProject> getExistingProjects(final File destination) {
		List<IProject> existingProjects = new ArrayList<>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project:projects) {
			if (project != null && project.exists()) {
				File projectFile = project.getLocation().toFile();
				File projectParent = projectFile.getParentFile();
				if (projectParent.equals(destination) || projectFile.equals(destination)) {
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
				builder.append("\nThe '" + projects.get(0).getName() + 
						"' project already exist at '" + destination.getAbsolutePath() + "'.\n\n");
				builder.append("Would you like to overwrite this project?");
			} else {
				builder.append("\nThe following projects already exist at '" + destination.getAbsolutePath() + "'.\n\n");
				for (IProject project : projects) {
					builder.append(project.getName());
					builder.append("\n"); //$NON-NLS-1$
				}
				builder.append("\n"); //$NON-NLS-1$
				builder.append("Would you like to overwrite them?");
			}
			builder.append("\n\nThis cannot be undone.");
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
