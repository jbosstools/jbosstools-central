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
package org.jboss.tools.project.examples.cheatsheet.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.jboss.tools.project.examples.cheatsheet.internal.util.CheatSheetUtil;

/**
 * 
 * <p>Command that opens a file in an editor.</p>
 * 
 * @author snjeza
 *
 */
public class OpenFileInEditorHandler extends AbstractHandler {

	private final static String FILE_PATH = "path"; //$NON-NLS-1$
	private final static String FROM_LINE = "fromLine"; //$NON-NLS-1$
	private final static String TO_LINE = "toLine"; //$NON-NLS-1$
	private final static String EDITOR_ID = "editorID"; //$NON-NLS-1$
	
	/**
	 * Execution of the action
	 * 
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		String path = event.getParameter(FILE_PATH);
		String fromLine = event.getParameter(FROM_LINE);
		String toLine = event.getParameter(TO_LINE);
		String editorID = event.getParameter(EDITOR_ID);
		
		CheatSheetUtil.openFile(path, fromLine, toLine, editorID);
		return null;
	}

}
