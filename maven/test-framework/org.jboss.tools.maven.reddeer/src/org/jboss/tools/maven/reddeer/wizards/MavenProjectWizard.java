/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.reddeer.wizards;


import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.condition.ShellIsActive;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.selectionwizard.NewMenuWizard;

public class MavenProjectWizard extends NewMenuWizard{
	
	public static final String SHELL_TEXT="New Maven Project";
	public static final String CATEGORY="Maven";
	public static final String NAME="Maven Project";

	
	public MavenProjectWizard(){
		super(SHELL_TEXT, CATEGORY,NAME);
	}
	
	@Override
	public void finish() {
		String shell = new DefaultShell().getText();
		Button button = new PushButton("Finish");
		button.click();

		new WaitWhile(new ShellIsActive(shell), TimePeriod.LONG);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}
	
	

}
