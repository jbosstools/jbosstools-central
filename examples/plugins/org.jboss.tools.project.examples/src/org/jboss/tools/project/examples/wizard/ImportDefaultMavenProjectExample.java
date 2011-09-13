package org.jboss.tools.project.examples.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.AbstractImportProjectExample;
import org.jboss.tools.project.examples.model.Project;

public class ImportDefaultMavenProjectExample extends
		AbstractImportProjectExample {

	private static final IOverwriteQuery OVERWRITE_ALL_QUERY = new IOverwriteQuery() {
		public String queryOverwrite(String pathString) {
			return IOverwriteQuery.ALL;
		}
	};

	@Override
	public List<Project> importProject(Project projectDescription, File file,
			IProgressMonitor monitor) throws Exception {
		List<Project> projects = new ArrayList<Project>();
		if (projectDescription.getIncludedProjects() == null) {
			importSingleProject(projectDescription, file, monitor);
			projects.add(projectDescription);
			return projects;
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
						return projects;
					}
					project.delete(true, true, monitor);
				}
				project.create(monitor);
				project.open(monitor);
				ZipFile sourceFile = new ZipFile(file);
				ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(
						sourceFile);

				Enumeration<? extends ZipEntry> entries = sourceFile.entries();
				ZipEntry entry = null;
				List<ZipEntry> filesToImport = new ArrayList<ZipEntry>();
				List<ZipEntry> directories = new ArrayList<ZipEntry>();
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
						workspace.getRoot().getFolder(resourcePath).create(false, true, null);
					} catch (Exception e) {
						ProjectExamplesActivator.log(e);
					}
				}
				reconfigure(project, monitor);
			}
		}
		return projects;
	}
	
	private void importSingleProject(Project projectDescription, File file,
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
				return;
			}
			project.delete(true, true, monitor);
		}
		project.create(monitor);
		project.open(monitor);
		ZipFile sourceFile = new ZipFile(file);
		ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(
				sourceFile);
		
		Enumeration<? extends ZipEntry> entries = sourceFile.entries();
		ZipEntry entry = null;
		List<ZipEntry> filesToImport = new ArrayList<ZipEntry>();
		List<ZipEntry> directories = new ArrayList<ZipEntry>();
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
				workspace.getRoot().getFolder(resourcePath).create(false, true, null);
			} catch (Exception e) {
				ProjectExamplesActivator.log(e);
			}
		}
		reconfigure(project, monitor);
		
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
				IPackageFragmentRoot[] children = ((JavaProject) javaProject).computePackageFragmentRoots(resolvedClasspath,false, null /* no reverse map */);
				info.setChildren(children);
				((JavaProject) javaProject).getPerProjectInfo().rememberExternalLibTimestamps();
			}
		}
	}

}
