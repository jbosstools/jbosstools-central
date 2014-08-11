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
package org.jboss.tools.project.examples.cheatsheet.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.jboss.tools.project.examples.cheatsheet.internal.util.CheatSheetUtil;

/**
 * 
 * <p>Action that return the curent project for cheatsheet.</p>
 * 
 * @author snjeza
 *
 */
public class GetProjectForCheatsheetHandler extends AbstractHandler {

	/**
	 * Execution of the action
	 * 
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
				
		IProject project = CheatSheetUtil.getProject();
		if (project != null) {
			return project.getName();
		}
		return ""; //$NON-NLS-1$
	}

}
