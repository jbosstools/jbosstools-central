package org.jboss.tools.project.examples.cheatsheet.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.jboss.tools.project.examples.cheatsheet.Activator;
import org.jboss.tools.project.examples.cheatsheet.Messages;
import org.osgi.service.prefs.BackingStoreException;

public class LaunchJUnitTest extends Action implements ICheatSheetAction {

	private static final String ACTIVE_PROFILES = "activeProfiles"; //$NON-NLS-1$

	public void run(String[] params, ICheatSheetManager manager) {
		if (params == null || params[0] == null) {
			return;
		}
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		IProject project = workspaceRoot.getProject(params[0]);
		if (project == null || !project.isOpen()) {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			setStatusMessage(page, NLS.bind(Messages.LaunchJunitTest_The_project_does_not_exist, params[0]));
			return;
		}
		if (params[1] != null) {
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences projectNode = projectScope
					.getNode("org.eclipse.m2e.core"); //$NON-NLS-1$
			if (projectNode != null) {
				String activeProfiles = projectNode.get(ACTIVE_PROFILES, null);
				if (!params[1].equals(activeProfiles)) {
					projectNode.put(ACTIVE_PROFILES, activeProfiles);
					try {
						projectNode.flush();
					} catch (BackingStoreException e) {
						Activator.log(e);
					}
				}
			}
		}
		ISelection selection = new StructuredSelection(project);
		JUnitLaunchShortcut launchShortcut = new JUnitLaunchShortcut();
		launchShortcut.launch(selection, ILaunchManager.RUN_MODE);
	}

	private void setStatusMessage(IWorkbenchPage page, String message) {
		IWorkbenchPart activePart = page.getActivePart();
		IWorkbenchPartSite site = activePart.getSite();
		IActionBars actionBar = null;
		if (site instanceof IViewSite) {
			IViewSite viewSite = (IViewSite) site;
			actionBar = viewSite.getActionBars();
		} else if (site instanceof IEditorSite) {
			IEditorSite editorSite = (IEditorSite) site;
			actionBar = editorSite.getActionBars();
		}
		if (actionBar == null) {
			return;
		}
		IStatusLineManager lineManager = actionBar.getStatusLineManager();
		if (lineManager == null) {
			return;
		}
		lineManager.setMessage(message);
	}

}
