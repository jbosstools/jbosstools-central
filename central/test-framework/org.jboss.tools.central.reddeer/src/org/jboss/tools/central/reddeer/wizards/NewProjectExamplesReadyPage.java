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

import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ShellWithTextIsActive;
import org.jboss.reddeer.swt.impl.button.CheckBox;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.tools.central.reddeer.projects.Project;

/**
 * This class represents shell displayed after archetype/example was imported.
 * One can check whether there are any errors in imported project or whether
 * readme/cheatsheet should be opened.
 * 
 * @author rhopp
 *
 */

public class NewProjectExamplesReadyPage extends DefaultShell {

	private Project project;

	public NewProjectExamplesReadyPage(Project project) {
		super("New Project Example");
		this.project = project;
	}

	public boolean isQuickFixEnabled() {
		return new CheckBox("Show the Quick Fix dialog").isEnabled();
	}

	public boolean isQuickFixchecked() {
		return new CheckBox("Show the Quick Fix dialog").isChecked();
	}

	/**
	 * This method require project as parameter to correctly match checkbox
	 * string.
	 * 
	 * @param project
	 * @return
	 */
	public boolean isShowReadmeEnabled() {
		return getReadmeCheckbox().isEnabled();
	}

	/**
	 * This method require project as parameter to correctly match checkbox
	 * string.
	 * 
	 * @param project
	 * @return
	 */
	public boolean isShowReadmeChecked() {
		return getReadmeCheckbox().isChecked();
	}

	public void toggleShowReadme(boolean checked) {
		getReadmeCheckbox().toggle(checked);
	}

	public void toggleDoNotShowAgain(boolean checked) {
		new CheckBox("Do not show this page again").toggle(checked);
	}

	/**
	 * Finishes this dialog and waits for all running jobs to complete.
	 */
	public void finish() {
		new PushButton("Finish").click();
		new WaitWhile(new ShellWithTextIsActive("New Project Example"));
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	private CheckBox getReadmeCheckbox() {
		return new CheckBox("Show '/" + project.getProjectName() + "/"
				+ project.getReadmeString() + "' for further instructions");
	}

}
