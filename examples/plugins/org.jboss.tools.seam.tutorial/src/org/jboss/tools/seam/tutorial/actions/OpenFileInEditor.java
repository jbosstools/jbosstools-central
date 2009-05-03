package org.jboss.tools.seam.tutorial.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jboss.tools.common.model.ui.editor.EditorPartWrapper;
import org.jboss.tools.seam.tutorial.Messages;

public class OpenFileInEditor extends Action implements ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		if(params == null || params[0] == null ) {
			return;
		}
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		IPath path = new Path(params[0]);
		IFile file = workspaceRoot.getFile(path);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = null;
		try {
			if (params[3] != null && params[3].trim().length() > 0) {
				String editorID = params[3];
				try {
					editor = IDE.openEditor(page, file, editorID, true);
				} catch (Exception e) {
				}
			}
			if (editor == null) {
				editor = IDE.openEditor(page, file, true);
			}
		} catch (PartInitException e) {
			setStatusMessage(page,NLS.bind(Messages.OpenFileInEditor_Cannot_open, params[0]));
			return;
		}
		ITextEditor textEditor = getTextEditor(editor);
		if (params[1] != null && textEditor != null) {
			try {
				int lineStart = Integer.parseInt(params[1]);
				int lineEnd = lineStart;
				if (params[2] != null) {
					lineEnd = Integer.parseInt(params[2]);
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

	private ITextEditor getTextEditor(IEditorPart editor) {
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
	
	private void setStatusMessage(IWorkbenchPage page,String message) {
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
