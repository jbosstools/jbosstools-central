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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.dialog.MarkerDialog;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectUtil;

public class NewProjectExamplesWizard extends Wizard implements INewWizard {

	private List<Project> projects = new ArrayList<Project>();
	
	private NewProjectExamplesWizardPage page;
	
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
				while (iterator.hasNext()) {
					Object object = iterator.next();
					if (object instanceof Project) {
						Project project = (Project) object;
						boolean success = ProjectExamplesActivator.downloadProject(project, monitor);
						if (success) {
							projects.add(project);
						}
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
					ProjectExamplesActivator.openWelcome(projects);
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
			ProjectExamplesActivator.openWelcome(projects);
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
	
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
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
	
}
