/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
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
import java.util.List;

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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.ua.ui.editor.cheatsheet.comp.CompCSEditor;
import org.eclipse.pde.internal.ua.ui.editor.cheatsheet.simple.SimpleCSEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
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
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
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
					return getProject(file);
				} catch (MalformedURLException e) {
					Activator.log(e);
				}
			}
		}
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			IEditorPart activeEditor = page.getActiveEditor();
			if (activeEditor instanceof SimpleCSEditor || activeEditor instanceof CompCSEditor) {
				IEditorInput input = activeEditor.getEditorInput();
				if (input instanceof IFileEditorInput) {
					IFile file = ((IFileEditorInput)input).getFile();
					if (file != null && file.exists()) {
						return file.getProject();
					}
				}
			}
		}
		return null;
	}

	public static IProject getProject(File file) {
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
		return null;
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
		String fileName = pathName;
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
		String projectName = name;
		IProject project = wRoot.getProject(projectName);
		if (project == null || !project.isOpen()) {
			return;
		}
		if (path != null) {
			IFile file = wRoot.getFile(new Path(path));
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
	
	/**
	 * Launch a JUnit test and selects a Maven profile optionally
	 * 
	 * @param projectName - project name
	 * @param profile - Maven profile
	 * @param mode - mode
	 * @deprecated This method will be split in the future to separate profile manipulation and junit execution
	 */
	@Deprecated
	public static void launchJUnitTest(String projectName, String profile, String mode) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		IProject project = workspaceRoot.getProject(projectName);
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
					projectNode.put(ACTIVE_PROFILES, profile);
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
	
	public static boolean showCheatsheet(IFile file) {
		try {
			IContentDescription contentDescription = file.getContentDescription();
			IContentType contentType = contentDescription.getContentType();
			if (contentType != null && "org.eclipse.pde.simpleCheatSheet".equals(contentType.getId())) { //$NON-NLS-1$
				CheatSheetView view = ViewUtilities.showCheatSheetView();
				if (view == null) {
					return false;
				}
				IPath filePath = file.getFullPath();
				String id = filePath.lastSegment();
				if (id == null) {
					id = ""; //$NON-NLS-1$
				}
				URL url = file.getLocation().toFile().toURI().toURL();
				view.getCheatSheetViewer().setInput(id, id, url, new DefaultStateManager(), false);
				return true;
			}
		} catch (Exception e) {
			Activator.log(e);
		}
		return false;
	}

	public static void showCheatsheet(final List<IFile> cheatsheets) {
		if (cheatsheets == null || cheatsheets.size() == 0) {
			return;
		}
		String value = ProjectExamplesActivator.getDefault().getShowCheatsheets();
		if (ProjectExamplesActivator.SHOW_CHEATSHEETS_NEVER.equals(value)) {
			return;
		}
		final IFile[] file = new IFile[1];
		file[0] = cheatsheets.get(0);
		if (ProjectExamplesActivator.SHOW_CHEATSHEETS_PROMPT.equals(value) || cheatsheets.size() > 0) {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					file[0] = promptToShowCheatsheets(cheatsheets);
				}

			});
		}
		if (file[0] != null) {
			Display.getDefault().asyncExec(new Runnable() {
				
				public void run() {
					showCheatsheet(file[0]);
				}
			});
			
		}
	}
	
	private static IFile promptToShowCheatsheets(List<IFile> cheatsheets) {
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		String key = ProjectExamplesActivator.SHOW_CHEATSHEETS;
		String value = store.getString(key);
		if (MessageDialogWithToggle.ALWAYS.equals(value) && cheatsheets.size() == 1) {
			return cheatsheets.get(0);
		}
		if (MessageDialogWithToggle.NEVER.equals(value)) {
			return null;
		}
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		if (cheatsheets.size() == 1) {
			String projectName = cheatsheets.get(0).getProject().getName();
			String title = "Found cheatsheet";
			String message = "Do you wish to open the cheatsheet for the '" + projectName + "' project?";
			MessageDialogWithToggle dialog = MessageDialogWithToggle
					.openYesNoQuestion(shell, title, message, null, false,
							store, key);
			int result = dialog.getReturnCode();
			if (result == Window.CANCEL || result == SWT.DEFAULT) {
				throw new OperationCanceledException();
			}
			if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
				return cheatsheets.get(0);
			} 
		} else {
			int kind = MessageDialog.QUESTION;
			String[] buttonLabels = new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL };
			String title = "Found cheatsheets";
			String message = "Please select the cheatsheet you want to show:";
			MyMessageDialogWithToggle dialog = new MyMessageDialogWithToggle(shell, title, 
					null, message, kind,
					buttonLabels, 0, null, false, cheatsheets);	        
	        dialog.setPrefStore(store); 
	        dialog.setPrefKey(key);
	        dialog.open();
	        int result = dialog.getReturnCode();
			if (result == Window.CANCEL || result == SWT.DEFAULT) {
				throw new OperationCanceledException();
			}
			if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
				return dialog.getCheatsheet();
			}
		}
		return null;
	}
	
	private static class MyMessageDialogWithToggle extends MessageDialogWithToggle {

		private List<IFile> cheatsheets;
		private IFile selectedCheatsheet;

		public MyMessageDialogWithToggle(Shell parentShell, String dialogTitle,
				Image image, String message, int dialogImageType,
				String[] dialogButtonLabels, int defaultIndex,
				String toggleMessage, boolean toggleState, List<IFile> cheatsheets) {
			super(parentShell, dialogTitle, image, message, 0 /*dialogImageType*/,
					dialogButtonLabels, defaultIndex, toggleMessage, toggleState);
			setShellStyle(getShellStyle() | SWT.SHEET);
			this.cheatsheets = cheatsheets;
			selectedCheatsheet = cheatsheets.get(0);
		}

		public IFile getCheatsheet() {
			return selectedCheatsheet;
		}

		@Override
		protected Control createCustomArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
	        GridLayout layout = new GridLayout();
	        layout.marginHeight = 0;
	        layout.marginWidth = 0;
	        composite.setLayout(layout);
	        GridData data = new GridData(GridData.FILL_BOTH);
	        data.horizontalSpan = 2;
	        composite.setLayoutData(data);
	        TableViewer viewer = createCheatsheetViewer(composite, cheatsheets);
	        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				
				public void selectionChanged(SelectionChangedEvent event) {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					ISelection sel = event.getSelection();
					if (sel instanceof IStructuredSelection) {
						Object object = ((IStructuredSelection) sel).getFirstElement();
						if (object instanceof IFile) {
							selectedCheatsheet = (IFile) object;
							getButton(IDialogConstants.OK_ID).setEnabled(true);
						}
					}
				}
			});
	        
	        return composite;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		
	}
	
	private static TableViewer createCheatsheetViewer(Composite parent, final List<IFile> cheatsheets) {
		final TableViewer viewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		gd.widthHint = 100;
		viewer.getTable().setLayoutData(gd);
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		
		viewer.setContentProvider(new IStructuredContentProvider() {
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				return cheatsheets.toArray(new IFile[0]);
			}
			public void dispose() {
			}
		});
		
		String[] columnHeaders = {"Project", "Name"};
		
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.setLabelProvider(new CheatsheetLabelProvider(i));
			column.getColumn().setText(columnHeaders[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			
		}
		
		ColumnLayoutData[] layouts= {
				new ColumnWeightData(100,100),
				new ColumnWeightData(60,60)
			};
		
		TableLayout layout = new AutoResizeTableLayout(table);
		for (int i = 0; i < layouts.length; i++) {
			layout.addColumnData(layouts[i]);
		}
		
		viewer.getTable().setLayout(layout);
		
		configureViewer(viewer);

		viewer.setInput(cheatsheets);
		
		return viewer;
	}

	private static void configureViewer(final TableViewer viewer) {
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				ViewerCell cell = viewer.getColumnViewerEditor().getFocusCell();
				if (cell != null && cell.getColumnIndex() == 1) {
					return super.isEditorActivationEvent(event);
				}
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		
		TableViewerEditor.create(viewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}
	
	private static class CheatsheetLabelProvider extends ColumnLabelProvider {
		private int columnIndex;

		public CheatsheetLabelProvider(int i) {
			this.columnIndex = i;
		}

		public String getText(Object element) {
			if (element instanceof IFile) {
				IFile file = (IFile) element;
				switch (columnIndex) {
				case 0:
					return file.getProject().getName();
				case 1:
					return file.getName();
				}
			}
			return null;
		}

	}

}
