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
package org.jboss.tools.central.reddeer.wizards;

import org.jboss.reddeer.common.logging.Logger;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.jface.wizard.WizardDialog;
import org.jboss.reddeer.swt.api.Button;
import org.jboss.reddeer.swt.condition.ShellIsActive;
import org.jboss.reddeer.swt.impl.browser.InternalBrowser;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.tools.central.reddeer.api.JavaScriptHelper;
import org.jboss.tools.central.reddeer.projects.ArchetypeProject;
import org.jboss.tools.central.reddeer.wait.CentralIsLoaded;

/**
 * 
 * @author rhopp
 * @contributor jkopriva@redhat.com
 *
 */


public class JBossCentralProjectWizard extends WizardDialog {

	protected final static Logger log = Logger
			.getLogger(JBossCentralProjectWizard.class);
	private ArchetypeProject project;

	public JBossCentralProjectWizard(ArchetypeProject project) {
		this.project = project;
	}

	public void open() {
		new WaitUntil(new CentralIsLoaded(),TimePeriod.LONG);
		JavaScriptHelper.getInstance().setBrowser(new InternalBrowser());
		JavaScriptHelper.getInstance().clickWizard(project.getName());
		new DefaultShell("New Project Example");
	}

	/**
	 * Finishes this wizard and returns {@link NewProjectExamplesReadyPage} for
	 * user to validate that shell (for example for opening of cheatsheet).
	 * 
	 */

	public NewProjectExamplesReadyPage finishAndWait() {
		return finishAndWait(TimePeriod.getCustom(1800));
	}

	/**
	 * Finishes this wizard with custom timeout and returns {@link NewProjectExamplesReadyPage} for
	 * user to validate that shell (for example for opening of cheatsheet).
	 * @param customTimeout
	 */

	public NewProjectExamplesReadyPage finishAndWait(TimePeriod customTimeout) {
		log.info("Finish example wizard");

		DefaultShell shell = new DefaultShell();
		Button button = new PushButton("Finish");
		button.click();
		new WaitWhile(new ShellIsActive(shell), customTimeout);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		return new NewProjectExamplesReadyPage(project);
	}
	
	/**
	 * Finishes this wizard without posibility to check
	 * NewProjectExamplesReadyPage.
	 */

	public void finish() {
		NewProjectExamplesReadyPage projectReadyPage = finishAndWait();
		projectReadyPage.finish();
	}

}
