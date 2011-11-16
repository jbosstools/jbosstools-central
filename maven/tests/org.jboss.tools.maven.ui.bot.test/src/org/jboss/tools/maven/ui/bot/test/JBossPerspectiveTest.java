/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.maven.ui.bot.test;

import junit.framework.TestCase;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.WorkbenchException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alexey Kazakov
 * @author Fred Bricon
 * 
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class JBossPerspectiveTest extends TestCase {

	/**
	 * Tests JBoss perspective has Maven stuff
	 * See https://issues.jboss.org/browse/JBIDE-10146
	 */
	@Test
	public void testJBossPerspective() throws WorkbenchException {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
		bot.menu("Window").menu("Open Perspective").menu("Other...").click();
		SWTBotShell shell = bot.shell("Open Perspective");
	    shell.activate();
	    bot.table().select("JBoss");
	    bot.button("OK").click();
	    SWTBotMenu menu = bot.menu("New");
	    assertNotNull("Maven Project Menu not found", menu.menu("Maven Project"));
	    
	    /*
	    same test in pure junit 
		IWorkbenchPage page = WorkbenchUtils.getWorkbench().getActiveWorkbenchWindow().openPage(JBossPerspectiveFactory.PERSPECTIVE_ID, null);
		assertNotNull(page);
		List<String> shortcuts = Arrays.asList(page.getNewWizardShortcuts());
		String mavenWizardId = "org.eclipse.m2e.core.wizards.Maven2ProjectWizard";
		assertTrue("Have not found "+ mavenWizardId +" in " + JBossPerspectiveFactory.PERSPECTIVE_ID, shortcuts.contains(mavenWizardId));
		*/
	}
}