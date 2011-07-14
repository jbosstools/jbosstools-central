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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.dialog.MarkerDialog;
import org.jboss.tools.project.examples.fixes.SeamRuntimeFix;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.IImportProjectExample;
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
				File file = null;
				while (iterator.hasNext()) {
					Object object = iterator.next();
					if (object instanceof Project) {
						Project project = (Project) object;
						if (project.isURLRequired()) {
							String urlString = project.getUrl();
							String name = project.getName();
							URL url = null;
							try {
								url = new URL(urlString);
							} catch (MalformedURLException e) {
								ProjectExamplesActivator.log(e);
								continue;
							}
							file = ProjectUtil.getProjectExamplesFile(url, name,
											".zip", monitor); //$NON-NLS-1$
							if (file == null) {
								return Status.CANCEL_STATUS;
							}
						}
						projects.add(project);
						files.add(file);
					}
				}
				try {
					int i = 0;
					setName(Messages.NewProjectExamplesWizard_Importing);
					for (final Project project : projects) {
						if (project.getImportType() == null) {
							importProject(project, files.get(i++), monitor);
							ProjectExamplesActivator.fix(project, monitor);
						} else {
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
							importProjectExample.importProject(project, files.get(i++), monitor);
							importProjectExample.fix(project, monitor);
						}
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
						updatePerspective();
						ProjectExamplesActivator.waitForBuildAndValidation
								.schedule();
						ProjectExamplesActivator.waitForBuildAndValidation
								.join();
					} catch (InterruptedException e) {
						return;
					}
					if (showQuickFix  && projects != null && projects.size() > 0) {
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
			updatePerspective();
			openWelcome();
		}
		workspaceJob.schedule();
		return true;
	}

	protected void updatePerspective() {
		if (projects == null || projects.size() != 1) {
			return;
		}
		final String perspectiveId = projects.get(0).getPerspectiveId();
		if (perspectiveId == null || perspectiveId.length() <= 0) {
			return;
		}
		// Retrieve the new project open perspective preference setting
		String perspSetting = PrefUtil.getAPIPreferenceStore().getString(
				IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE);

		String promptSetting = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore().getString(
						IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);

		// Return if do not switch perspective setting and are not prompting
		if (!(promptSetting.equals(MessageDialogWithToggle.PROMPT))
				&& perspSetting.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE)) {
			return;
		}
		
		// Map perspective id to descriptor.
		IPerspectiveRegistry reg = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		// leave this code in - the perspective of a given project may map to
		// activities other than those that the wizard itself maps to.
		final IPerspectiveDescriptor finalPersp = reg
				.findPerspectiveWithId(perspectiveId);
		if (finalPersp != null && finalPersp instanceof IPluginContribution) {
			IPluginContribution contribution = (IPluginContribution) finalPersp;
			if (contribution.getPluginId() != null) {
				IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
						.getWorkbench().getActivitySupport();
				IActivityManager activityManager = workbenchActivitySupport
						.getActivityManager();
				IIdentifier identifier = activityManager
						.getIdentifier(WorkbenchActivityHelper
								.createUnifiedId(contribution));
				Set idActivities = identifier.getActivityIds();

				if (!idActivities.isEmpty()) {
					Set enabledIds = new HashSet(activityManager
							.getEnabledActivityIds());

					if (enabledIds.addAll(idActivities)) {
						workbenchActivitySupport
								.setEnabledActivityIds(enabledIds);
					}
				}
			}
		} else {
			IDEWorkbenchPlugin.log("Unable to find perspective " //$NON-NLS-1$
					+ perspectiveId
					+ " in NewProjectExamplesWizard.updatePerspective"); //$NON-NLS-1$
			return;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					switchPerspective(perspectiveId, finalPersp, win);
				}
			});
		} else {
			switchPerspective(perspectiveId, finalPersp, window);
		}
	}

	private void switchPerspective(String perspectiveId,
			IPerspectiveDescriptor finalPersp, IWorkbenchWindow window) {
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IPerspectiveDescriptor currentPersp = page.getPerspective();
				if (currentPersp != null
						&& perspectiveId.equals(currentPersp.getId())) {
					return;
				}
			}
		}

		if (!confirmPerspectiveSwitch(window, finalPersp)) {
			return;
		}

		int workbenchPerspectiveSetting = WorkbenchPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.OPEN_PERSP_MODE);

		if (workbenchPerspectiveSetting == IPreferenceConstants.OPM_NEW_WINDOW) {
			openInNewWindow(finalPersp);
			return;
		}

		replaceCurrentPerspective(finalPersp);
	}

	/**
	 * Prompts the user for whether to switch perspectives.
	 * 
	 * @param window
	 *            The workbench window in which to switch perspectives; must not
	 *            be <code>null</code>
	 * @param finalPersp
	 *            The perspective to switch to; must not be <code>null</code>.
	 * 
	 * @return <code>true</code> if it's OK to switch, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmPerspectiveSwitch(IWorkbenchWindow window,
			IPerspectiveDescriptor finalPersp) {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore();
		String pspm = store
				.getString(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);
		if (!IDEInternalPreferences.PSPM_PROMPT.equals(pspm)) {
			// Return whether or not we should always switch
			return IDEInternalPreferences.PSPM_ALWAYS.equals(pspm);
		}
		String desc = finalPersp.getDescription();
		String message;
		if (desc == null || desc.length() == 0)
			message = NLS.bind(ResourceMessages.NewProject_perspSwitchMessage,
					finalPersp.getLabel());
		else
			message = NLS.bind(
					ResourceMessages.NewProject_perspSwitchMessageWithDesc,
					new String[] { finalPersp.getLabel(), desc });

		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openYesNoQuestion(window.getShell(),
						ResourceMessages.NewProject_perspSwitchTitle, message,
						null /* use the default message for the toggle */,
						false /* toggle is initially unchecked */, store,
						IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);
		int result = dialog.getReturnCode();

		// If we are not going to prompt anymore propogate the choice.
		if (dialog.getToggleState()) {
			String preferenceValue;
			if (result == IDialogConstants.YES_ID) {
				// Doesn't matter if it is replace or new window
				// as we are going to use the open perspective setting
				preferenceValue = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
			} else {
				preferenceValue = IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE;
			}

			// update PROJECT_OPEN_NEW_PERSPECTIVE to correspond
			PrefUtil.getAPIPreferenceStore().setValue(
					IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE,
					preferenceValue);
		}
		return result == IDialogConstants.YES_ID;
	}
	
	/*
	 * (non-Javadoc) Opens a new window with a particular perspective and input.
	 */
	private static void openInNewWindow(IPerspectiveDescriptor desc) {

		// Open the page.
		try {
			PlatformUI.getWorkbench().openWorkbenchWindow(desc.getId(),
					ResourcesPlugin.getWorkspace().getRoot());
		} catch (WorkbenchException e) {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window != null) {
				ErrorDialog.openError(window.getShell(), ResourceMessages.NewProject_errorOpeningWindow,
						e.getMessage(), e.getStatus());
			}
		}
	}
	
	/*
	 * (non-Javadoc) Replaces the current perspective with the new one.
	 */
	private static void replaceCurrentPerspective(IPerspectiveDescriptor persp) {

		// Get the active page.
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}

		// Set the perspective.
		page.setPerspective(persp);
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
				ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(
						sourceFile);

				Enumeration<? extends ZipEntry> entries = sourceFile.entries();
				ZipEntry entry = null;
				List<ZipEntry> filesToImport = new ArrayList<ZipEntry>();
				String prefix = projectName + "/"; //$NON-NLS-1$
				while (entries.hasMoreElements()) {
					entry = entries.nextElement();
					if (entry.isDirectory()) {
						continue;
					}
					if (entry.getName().startsWith(prefix)) {
						filesToImport.add(entry);
					}
				}
				
				structureProvider.setStrip(1);
				ImportOperation operation = new ImportOperation(project.getFullPath(), structureProvider.getRoot(),
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
		ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(
				sourceFile);
		
		Enumeration<? extends ZipEntry> entries = sourceFile.entries();
		ZipEntry entry = null;
		List<ZipEntry> filesToImport = new ArrayList<ZipEntry>();
		String prefix = projectName + "/"; //$NON-NLS-1$
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			if (entry.isDirectory()) {
				continue;
			}
			if (entry.getName().startsWith(prefix)) {
				filesToImport.add(entry);
			}
		}
		
		structureProvider.setStrip(1);
		ImportOperation operation = new ImportOperation(project.getFullPath(), structureProvider.getRoot(),
				structureProvider, OVERWRITE_ALL_QUERY, filesToImport);
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
