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
package org.jboss.tools.project.examples.cheatsheet.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;

/**
 * 
 * <p>Action that opens a preferences page.</p>
 * 
 * @author snjeza
 *
 */
public class OpenPreferencePage extends Action implements ICheatSheetAction {

	/**
	 * Execution of the action
	 * 
	 * @param params
	 *            Array of parameters
	 *            index 0: preferences page id
	 * @param manager
	 *            Cheatsheet Manager
	 */
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
