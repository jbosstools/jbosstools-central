package org.jboss.tools.project.examples.cheatsheet.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;

public class OpenPreferencePage extends Action implements ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		if(params == null || params[0] == null) {
			return;
		}
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		PreferenceDialog dialog = WorkbenchPreferenceDialog.createDialogOn(
				shell, params[0]);
		dialog.open();
	}

}
