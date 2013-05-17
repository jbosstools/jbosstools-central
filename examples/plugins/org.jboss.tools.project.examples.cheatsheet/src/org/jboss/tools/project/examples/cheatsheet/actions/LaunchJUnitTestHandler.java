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
 * <p>Action that launches a JUnit test and selects a Maven profile optionally.</p>
 * 
 * @author snjeza
 *
 */
public class LaunchJUnitTestHandler extends AbstractHandler {

	private final static String MAVEN_PROFILE = "profile"; //$NON-NLS-1$
	private final static String PROJECT_NAME = "projectName"; //$NON-NLS-1$
	private final static String MODE = "mode"; //$NON-NLS-1$
	
	/**
	 * Execution of the action
	 * 
	 * @param projectName - project name
	 * @param profile - Maven profile
	 * @param mode - launch mode (run or debug)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		String profile = event.getParameter(MAVEN_PROFILE);
		String projectName = event.getParameter(PROJECT_NAME);
		String mode = event.getParameter(MODE);
		
		CheatSheetUtil.launchJUnitTest(projectName, profile, mode);
		return null;
	}

}
