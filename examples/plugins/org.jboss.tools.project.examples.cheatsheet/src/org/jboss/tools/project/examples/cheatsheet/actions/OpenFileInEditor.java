/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
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
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.jboss.tools.project.examples.cheatsheet.internal.util.CheatSheetUtil;

/**
 * 
 * <p>Action that opens a file in an editor.</p>
 * 
 * @author snjeza
 *
 * @deprecated use the org.jboss.tools.project.examples.cheatsheet.openFileInEditor command
 */
@Deprecated
public class OpenFileInEditor extends Action implements ICheatSheetAction {

	/**
	 * Execution of the action
	 * 
	 * @param params
	 *            Array of parameters
	 *            index 0: the file path,
	 *            index 1,2: the range of the lines that will be selected 
	 * @param manager
	 *            Cheatsheet Manager
	 */
	public void run(String[] params, ICheatSheetManager manager) {
		if(params == null || params[0] == null ) {
			return;
		}
		CheatSheetUtil.openFile(params[0], params[1], params[2], params[3]);
	}
	
}
