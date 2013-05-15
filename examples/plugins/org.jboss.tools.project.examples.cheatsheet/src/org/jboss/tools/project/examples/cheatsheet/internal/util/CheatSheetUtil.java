/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.cheatsheet.internal.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.actions.RunOnServerAction;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.common.model.ui.editor.EditorPartWrapper;
import org.jboss.tools.project.examples.cheatsheet.Activator;
import org.jboss.tools.project.examples.cheatsheet.Messages;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * @author snjeza
 *
 */
public class CheatSheetUtil {
	
	private static final String ACTIVE_PROFILES = "activeProfiles"; //$NON-NLS-1$

	public static IProject getProject() {
		CheatSheetView view = ViewUtilities.showCheatSheetView();
		if (view != null && view.getContent() != null) {
			String href = view.getContent().getHref();
			if (href != null) {
				try {
					URL url = new URL(href);
					File file = null;
					if (ProjectExampleUtil.PROTOCOL_FILE.equals(url.getProtocol())) {
						try {
							file = new File(new URI(url.toExternalForm()));
						} catch (Exception e) {
							file = new File(url.getFile());
						}
					}
					if (file != null && file.exists()) {
						IWorkspace workspace= ResourcesPlugin.getWorkspace();    
						IPath location= Path.fromOSString(file.getAbsolutePath());
						if (location != null) {
							IFile iFile= workspace.getRoot().getFileForLocation(location);
							if (iFile != null) {
								return iFile.getProject();
							}
						}
					}
				} catch (MalformedURLException e) {
					Activator.log(e);
				}
			}
		}
		return null;
	}
	
	public static String replaceProjectName(String param) {
		if (param == null) {
			return null;
		}
		String replace = param;
		IProject project = CheatSheetUtil.getProject();
		if (project != null) {
			String projectName = project.getName();
			if (projectName != null) {
				replace = param.replace("{project}", projectName); //$NON-NLS-1$
			}
		}
		return replace;
	}
	
	public static ITextEditor getTextEditor(IEditorPart editor) {
		if (editor instanceof ITextEditor) {
			return (ITextEditor) editor;
		}
		if (editor instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditor = (MultiPageEditorPart) editor;
			IEditorPart[] editors = multiPageEditor.findEditors(editor.getEditorInput());
			for (int i = 0; i < editors.length; i++) {
				if (editors[i] instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) editors[i];
					if (textEditor.getDocumentProvider() != null) {
						return (ITextEditor) editors[i];
					}
				}
			}
		}
		if (editor instanceof EditorPartWrapper) {
			EditorPartWrapper wrapper = (EditorPartWrapper) editor;
			IEditorPart nestedEditor = wrapper.getEditor();
			return getTextEditor(nestedEditor);
		}
		return null;
	}
	
	private static void setStatusMessage(IWorkbenchPage page,String message) {
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

	public static void openFile(String pathName, String fromLine, String toLine, String editorID) {
		String fileName = CheatSheetUtil.replaceProjectName(pathName);
		IPath path = new Path(fileName);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFile file = workspaceRoot.getFile(path);
		if (!file.exists()) {
			setStatusMessage(page,NLS.bind(Messages.OpenFileInEditor_Cannot_open, path));
			return;
		}
		IEditorPart editor = null;
		try {
			if (editorID != null && editorID.trim().length() > 0) {
				try {
					editor = IDE.openEditor(page, file, editorID, true);
				} catch (Exception e) {
				}
			}
			if (editor == null) {
				editor = IDE.openEditor(page, file, true);
			}
		} catch (PartInitException e) {
			setStatusMessage(page,NLS.bind(Messages.OpenFileInEditor_Cannot_open, path));
			return;
		}
		ITextEditor textEditor = CheatSheetUtil.getTextEditor(editor);
		if (fromLine != null && textEditor != null) {
			try {
				int lineStart = Integer.parseInt(fromLine);
				int lineEnd = lineStart;
				if (toLine != null) {
					lineEnd = Integer.parseInt(toLine);
				}
				IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
				IRegion lineInfoStart = document.getLineInformation(lineStart-1);
				IRegion lineInfoEnd = document.getLineInformation(lineEnd-1);
				textEditor.selectAndReveal(lineInfoStart.getOffset(), lineInfoEnd.getOffset() - lineInfoStart.getOffset() + lineInfoEnd.getLength());
			} catch (Exception e) {
				setStatusMessage(page, e.getLocalizedMessage());
			}
		}
	}

	public static void runOnServer(String name, String path) {
		IWorkspaceRoot wRoot = ResourcesPlugin.getWorkspace().getRoot();
		String projectName = CheatSheetUtil.replaceProjectName(name);
		IProject project = wRoot.getProject(projectName);
		if (project == null || !project.isOpen()) {
			return;
		}
		if (path != null) {
			IFile file = wRoot.getFile(new Path(CheatSheetUtil.replaceProjectName(path)));
			if (file != null && file.exists()) {
				try {
					SingleDeployableFactory.makeDeployable(file.getFullPath());
					IServer[] deployableServersAsIServers = ServerConverter
							.getDeployableServersAsIServers();
					if (deployableServersAsIServers.length == 1) {
						IServer server = deployableServersAsIServers[0];
						IServerWorkingCopy copy = server.createWorkingCopy();
						IModule[] modules = new IModule[1];
						modules[0] = SingleDeployableFactory.findModule(file
								.getFullPath());
						copy.modifyModules(modules, new IModule[0],
								new NullProgressMonitor());
						IServer saved = copy.save(false,
								new NullProgressMonitor());
						saved.publish(IServer.PUBLISH_INCREMENTAL,
								new NullProgressMonitor());
					}
				} catch (CoreException e) {
					IStatus status = new Status(IStatus.INFO,Activator.PLUGIN_ID,e.getMessage(),e);
					Activator.getDefault().getLog().log(status);
				}
			}
		}
		IAction action = new RunOnServerAction(project);
		action.run();
	}
	
	public static void launcJUnitTest(String projectName, String profile, String mode) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		IProject project = workspaceRoot.getProject(CheatSheetUtil.replaceProjectName(projectName));
		if (project == null || !project.isOpen()) {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			setStatusMessage(page, NLS.bind(Messages.LaunchJunitTest_The_project_does_not_exist, projectName));
			return;
		}
		if (profile != null) {
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences projectNode = projectScope
					.getNode("org.eclipse.m2e.core"); //$NON-NLS-1$
			if (projectNode != null) {
				String activeProfiles = projectNode.get(ACTIVE_PROFILES, null);
				if (!profile.equals(activeProfiles)) {
					projectNode.put(ACTIVE_PROFILES, CheatSheetUtil.replaceProjectName(profile));
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
		if (mode == null) {
			mode = ILaunchManager.RUN_MODE;
		}
		if (!ILaunchManager.RUN_MODE.equals(mode) && !ILaunchManager.DEBUG_MODE.equals(mode)) {
			mode = ILaunchManager.RUN_MODE;
		}
		launchShortcut.launch(selection, mode);
	}

}
