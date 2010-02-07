/*************************************************************************************
 * Copyright (c) 2008-2009 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

/**
 * @author snjeza
 * 
 */
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.dialog.MarkerDialog;
import org.jboss.tools.project.examples.fixes.SeamRuntimeFix;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.model.ProjectUtil;

public class NewProjectExamplesWizard extends Wizard implements INewWizard {

	private static final IOverwriteQuery OVERWRITE_ALL_QUERY = new IOverwriteQuery() {
		public String queryOverwrite(String pathString) {
			return IOverwriteQuery.ALL;
		}
	};

	private List<Project> projects = new ArrayList<Project>();
	/**
	 * The workbench.
	 */
	private IWorkbench workbench;

	/**
	 * The current selection.
	 */
	private IStructuredSelection selection;

	private NewProjectExamplesWizardPage page;
	
	private static Shell shell;

	protected static boolean overwrite;

	private WorkspaceJob workspaceJob;

	public NewProjectExamplesWizard() {
		super();
		setWindowTitle(Messages.NewProjectExamplesWizard_New_Project_Example);

	}

	/**
	 * Creates an empty wizard for creating a new resource in the workspace.
	 */

	@Override
	public boolean performFinish() {

		if (page.getSelection() == null || page.getSelection().size() <= 0) {
			return false;
		}
		workspaceJob = new WorkspaceJob(Messages.NewProjectExamplesWizard_Downloading) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				IStructuredSelection selection = page.getSelection();
				Iterator iterator = selection.iterator();
				projects.clear();
				List<File> files = new ArrayList<File>();
				while (iterator.hasNext()) {
					Object object = iterator.next();
					if (object instanceof Project) {
						Project project = (Project) object;
						String urlString = project.getUrl();
						String name = project.getName();
						URL url = null;
						try {
							url = new URL(urlString);
						} catch (MalformedURLException e) {
							ProjectExamplesActivator.log(e);
							continue;
						}
						final File file = ProjectUtil.getProjectExamplesFile(
								url, name, ".zip", monitor); //$NON-NLS-1$
						if (file == null) {
							return Status.CANCEL_STATUS;
						}
						projects.add(project);
						files.add(file);
					}
				}
				try {
					int i = 0;
					setName(Messages.NewProjectExamplesWizard_Importing);
					for (Project project : projects) {
						importProject(project, files.get(i++), monitor);
						fix(project, monitor);
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

		};
		workspaceJob.setUser(true);
		final boolean showQuickFix = page.showQuickFix();

		if (showQuickFix) {
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
					if (showQuickFix) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							
						}
						List<IMarker> markers = ProjectExamplesActivator
								.getMarkers(projects);
						if (markers != null && markers.size() > 0) {
							showQuickFix(projects);
						}
					}
					openWelcome();
				}

				public void running(IJobChangeEvent event) {

				}

				public void scheduled(IJobChangeEvent event) {

				}

				public void sleeping(IJobChangeEvent event) {

				}

			});
		} else {
			openWelcome();
		}
		workspaceJob.schedule();
		return true;
	}

	private void openWelcome() {
		for(final Project project:projects) {
			if (project.isWelcome()) {
				String urlString = project.getWelcomeURL();
				URL url = null;
				if (urlString.startsWith("/")) { //$NON-NLS-1$
					IPath path = new Path(urlString);
					IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
					if (resource instanceof IFile && resource.isAccessible()) {
						try {
							url = resource.getRawLocationURI().toURL();
						} catch (MalformedURLException e) {
							ProjectExamplesActivator.log(e);
						} 
					} else {
						ProjectExamplesActivator.log(NLS.bind(Messages.NewProjectExamplesWizard_File_does_not_exist,urlString));
					}
				} else {
					try {
						url = new URL(urlString);
					} catch (MalformedURLException e) {
						ProjectExamplesActivator.log(e);
					}
				}
				if (url!=null) {
					final URL finalURL = url;
					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							if (ProjectUtil.CHEATSHEETS.equals(project.getType())) {
								CheatSheetView view = ViewUtilities.showCheatSheetView();
								if (view == null) {
									return;
								}
								IPath filePath = new Path(finalURL.getPath());
								String id = filePath.lastSegment();
								if (id == null) {
									id = ""; //$NON-NLS-1$
								}
								view.getCheatSheetViewer().setInput(id, id, finalURL, new DefaultStateManager(), false);
							} else {
								try {
									IWorkbenchBrowserSupport browserSupport = ProjectExamplesActivator.getDefault().getWorkbench().getBrowserSupport();
									IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
									browser.openURL(finalURL);
								} catch (PartInitException e) {
									ProjectExamplesActivator.log(e);
								}
							}
						}
						
					});
					
				}
			}
		}
	}

	public static void showQuickFix(final List<Project> projects) {

		Display.getDefault().asyncExec(new Runnable() {

			public void run() {

				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();
				Dialog dialog = new MarkerDialog(shell, projects);
				dialog.open();
			}

		});
	}

	public static void fix(Project project, IProgressMonitor monitor) {
		List<ProjectFix> fixes = project.getFixes();
		for (ProjectFix fix:fixes) {
			if (ProjectFix.WTP_RUNTIME.equals(fix.getType())) {
				new WTPRuntimeFix().fix(project, fix, monitor);
			}
			if (ProjectFix.SEAM_RUNTIME.equals(fix.getType())) {
				new SeamRuntimeFix().fix(project, fix, monitor);
			}
		}
	}
	
	
	public static void importProject(Project projectDescription, File file,
			IProgressMonitor monitor) throws Exception {
		if (projectDescription.getIncludedProjects() == null) {
			importSingleProject(projectDescription, file, monitor);
		} else {
			List<String> projects = projectDescription.getIncludedProjects();
			for (final String projectName : projects) {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject project = workspace.getRoot().getProject(projectName);
				if (project.exists()) {
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							overwrite = MessageDialog.openQuestion(getActiveShell(),
									Messages.NewProjectExamplesWizard_Question, NLS.bind(Messages.NewProjectExamplesWizard_OverwriteProject,
										projectName));
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

				Enumeration<? extends ZipEntry> entries = sourceFile.entries();
				ZipEntry entry = null;
				List<ZipEntry> filesToImport = new ArrayList<ZipEntry>();
				String prefix = projectName + "/"; //$NON-NLS-1$
				while (entries.hasMoreElements()) {
					entry = entries.nextElement();
					if (entry.getName().startsWith(prefix)) {
						filesToImport.add(entry);
					}
				}
				//ZipEntry entry = sourceFile.getEntry(projectName);
				
				//List filesToImport = prepareFileList(structureProvider, entry, null);

				ImportOperation operation = new ImportOperation(workspace
						.getRoot().getFullPath(), structureProvider.getRoot(),
						structureProvider, OVERWRITE_ALL_QUERY, filesToImport);
				operation.setContext(getActiveShell());
				operation.run(monitor);
				reconfigure(project, monitor);
			}
		}
	}

	private static Shell getActiveShell() {
		Display display = Display.getDefault();
		shell = null;
		display.syncExec(new Runnable() {

			public void run() {
				shell = Display.getCurrent().getActiveShell();
			}
			
		});
		return shell;
	}
	private static void importSingleProject(Project projectDescription, File file,
			IProgressMonitor monitor) throws CoreException, ZipException,
			IOException, InvocationTargetException, InterruptedException {
		final String projectName = projectDescription.getName();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		if (project.exists()) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					overwrite = MessageDialog.openQuestion(getActiveShell(),
							Messages.NewProjectExamplesWizard_Question, NLS.bind(Messages.NewProjectExamplesWizard_OverwriteProject,
									projectName));
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

		ImportOperation operation = new ImportOperation(workspace.getRoot()
				.getFullPath(), structureProvider.getRoot(), structureProvider,
				OVERWRITE_ALL_QUERY);
		operation.setContext(getActiveShell());
		operation.run(monitor);
		reconfigure(project, monitor);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		initializeDefaultPageImageDescriptor();
	}

	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ProjectExamplesActivator
				.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID,
						"icons/new_wiz.gif"); //$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	@Override
	public void addPages() {
		super.addPages();
		page = new NewProjectExamplesWizardPage();
		addPage(page);
	}

	private static List prepareFileList(IImportStructureProvider structure,
			ZipEntry entry, List list) {
		if (structure == null || entry == null)
			return null;
		if (list == null) {
			list = new ArrayList();
		}
		List son = structure.getChildren(entry);
		if (son == null)
			return list;
		Iterator it = son.iterator();
		while (it.hasNext()) {
			ZipEntry temp = (ZipEntry) it.next();
			if (temp.isDirectory()) {
				prepareFileList(structure, temp, list);
			} else {
				list.add(temp);
			}
		}
		return list;
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
