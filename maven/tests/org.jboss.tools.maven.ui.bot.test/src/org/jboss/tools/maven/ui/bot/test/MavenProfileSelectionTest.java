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
package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class MavenProfileSelectionTest extends AbstractMavenSWTBotTest {
	
	@Test
	//FIXME Test fail due to Modal Dialog. Need to find a solution
	public void testOpenMavenProfiles() throws Exception {
		
		IProject project = importProject("projects/simple-jar/pom.xml");
		waitForJobsToComplete();
		//Select the project
		final SWTBotView packageExplorer = bot.viewByTitle(PACKAGE_EXPLORER);
		SWTBot innerBot = packageExplorer.bot();
		innerBot.activeShell().activate();
		final SWTBotTree tree = innerBot.tree();
		final SWTBotTreeItem projectItem = tree.getTreeItem(project.getName());
		projectItem.select();
		//Open the profiles dialog 
		projectItem.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT, KeyStroke.getInstance("P"));
		
		//FIXME Either the dialog doesn't open abd the test fails
		//or SWTBot is blocked by the modal dialog
		final SWTBotShell selectDialogShell = bot.shell("Select Maven profiles");
		assertNotNull("selectDialogShell not found", selectDialogShell);
	    assertEquals("Select Maven profiles", selectDialogShell.getText());
	    
	    selectDialogShell.activate();
	    Thread.sleep(5000);
	    //TODO implement real tests
		bot.button("Cancel").click();
	}
}
