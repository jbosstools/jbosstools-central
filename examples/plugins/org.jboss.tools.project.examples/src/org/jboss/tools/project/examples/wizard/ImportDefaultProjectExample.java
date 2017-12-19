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
package org.jboss.tools.project.examples.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.jboss.tools.project.examples.internal.Messages;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.AbstractImportProjectExample;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;

public class ImportDefaultProjectExample extends
		AbstractImportProjectExample {

	private static final IOverwriteQuery OVERWRITE_ALL_QUERY = new IOverwriteQuery() {
		public String queryOverwrite(String pathString) {
			return IOverwriteQuery.ALL;
		}
	};

	@Override
	public boolean importProject(ProjectExampleWorkingCopy projectDescription, File file,
			Map<String, Object> propertiesMap, IProgressMonitor monitor) throws Exception {
		if (projectDescription.getIncludedProjects() == null) {
			boolean ret = importSingleProject(projectDescription, file, monitor);
			return ret;
		} else {
			List<String> projectNames = projectDescription.getIncludedProjects();
			for (final String projectName : projectNames) {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject project = workspace.getRoot().getProject(projectName);
				final boolean[] ret = new boolean[1];
				if (project.exists()) {
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							ret[0] = MessageDialog.openQuestion(getActiveShell(),
									Messages.NewProjectExamplesWizard_Question, NLS.bind(Messages.NewProjectExamplesWizard_OverwriteProject,
										projectName));
						}

					});
					if (!ret[0]) {
						return false;
					}
					project.delete(true, true, monitor);
				}
				createProject(project, monitor);
				project.open(monitor);
				ZipFile sourceFile = new ZipFile(file);
				ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(
						sourceFile);

				Enumeration<? extends ZipEntry> entries = sourceFile.entries();
				ZipEntry entry = null;
				List<ZipEntry> filesToImport = new ArrayList<>();
				List<ZipEntry> directories = new ArrayList<>();
				String prefix = projectName + "/"; //$NON-NLS-1$
				while (entries.hasMoreElements()) {
					entry = entries.nextElement();
					if (entry.getName().startsWith(prefix)) {
						if (!entry.isDirectory()) {
							filesToImport.add(entry);
						} else {
							directories.add(entry);
						}
					}
				}
				
				structureProvider.setStrip(1);
				ImportOperation operation = new ImportOperation(project.getFullPath(), structureProvider.getRoot(),
						structureProvider, OVERWRITE_ALL_QUERY, filesToImport);
				operation.setContext(getActiveShell());
				operation.run(monitor);
				for (ZipEntry directory:directories) {
					IPath resourcePath = new Path(directory.getName());
					try {
						if (resourcePath.segmentCount() > 1 && !workspace.getRoot().getFolder(resourcePath).exists()) {
							workspace.getRoot().getFolder(resourcePath).create(false, true, null);
						}
					} catch (Exception e) {
						ProjectExamplesActivator.log(e);
						return false;
					}
				}
				reconfigure(project, monitor);
			}
		}
		return true;
	}
	
	private void createProject(IProject project, IProgressMonitor monitor) throws CoreException {
		IPath location = getLocation();
		if (!Platform.getLocation().equals(location)) {
			IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
			desc.setLocation(location.append(project.getName()));
			project.create(desc, monitor);
		} else
			project.create(monitor);
	}

	private boolean importSingleProject(ProjectExample projectDescription, File file,
			IProgressMonitor monitor) throws CoreException, ZipException,
			IOException, InvocationTargetException, InterruptedException {
		final String projectName = projectDescription.getName();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		final boolean[] ret = new boolean[1];
		if (project.exists()) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					ret[0] = MessageDialog.openQuestion(getActiveShell(),
							Messages.NewProjectExamplesWizard_Question, NLS.bind(Messages.NewProjectExamplesWizard_OverwriteProject,
									projectName));
				}

			});
			if (!ret[0]) {
				return false;
			}
			project.delete(true, true, monitor);
		}
		createProject(project, monitor);
		project.open(monitor);
		ZipFile sourceFile = new ZipFile(file);
		ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(
				sourceFile);
		
		Enumeration<? extends ZipEntry> entries = sourceFile.entries();
		ZipEntry entry = null;
		List<ZipEntry> filesToImport = new ArrayList<>();
		List<ZipEntry> directories = new ArrayList<>();
		String prefix = projectName + "/"; //$NON-NLS-1$
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			if (entry.getName().startsWith(prefix)) {
				if (!entry.isDirectory()) {
					filesToImport.add(entry);
				} else {
					directories.add(entry);
				}
			}
		}
		
		structureProvider.setStrip(1);
		ImportOperation operation = new ImportOperation(project.getFullPath(), structureProvider.getRoot(),
				structureProvider, OVERWRITE_ALL_QUERY, filesToImport);
		operation.setContext(getActiveShell());
		operation.run(monitor);
		for (ZipEntry directory:directories) {
			IPath resourcePath = new Path(directory.getName());
			try {
				if (resourcePath.segmentCount() > 1 && !workspace.getRoot().getFolder(resourcePath).exists()) {
					workspace.getRoot().getFolder(resourcePath).create(false, true, null);
				}
			} catch (Exception e) {
				ProjectExamplesActivator.log(e);
				return false;
			}
		}
		reconfigure(project, monitor);
		return true;
	}

	private static Shell getActiveShell() {
		Display display = Display.getDefault();
		final Shell[] ret = new Shell[1];
		display.syncExec(new Runnable() {

			public void run() {
				ret[0] = Display.getCurrent().getActiveShell();
			}
			
		});
		return ret[0];
	}

	private static void reconfigure(IProject project, IProgressMonitor monitor) throws CoreException {
		if (project == null || !project.exists() || !project.isOpen() || !project.hasNature(JavaCore.NATURE_ID)) {
			return;
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject != null && javaProject.exists() && javaProject.isOpen() && javaProject instanceof JavaProject) {
			Object object = ((JavaProject) javaProject).getElementInfo();
			if (object instanceof OpenableElementInfo) {
				// copied from JavaProject.buildStructure(...)
				OpenableElementInfo info = (OpenableElementInfo) object;
				IClasspathEntry[] resolvedClasspath = ((JavaProject) javaProject).getResolvedClasspath();
				IPackageFragmentRoot[] children = ((JavaProject) javaProject).computePackageFragmentRoots(resolvedClasspath,false, false, null /* no reverse map */);
				info.setChildren(children);
				((JavaProject) javaProject).getPerProjectInfo().rememberExternalLibTimestamps();
			}
		}
	}

}
