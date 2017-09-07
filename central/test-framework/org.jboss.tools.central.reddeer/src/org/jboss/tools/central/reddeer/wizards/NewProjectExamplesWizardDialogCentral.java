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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.reddeer.jface.wizard.WizardDialog;
import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.condition.ShellIsActive;
import org.eclipse.reddeer.core.handler.WidgetHandler;
import org.eclipse.reddeer.eclipse.ui.problems.Problem;
import org.eclipse.reddeer.eclipse.ui.views.markers.ProblemsView;
import org.eclipse.reddeer.eclipse.ui.views.markers.ProblemsView.ProblemType;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.label.DefaultLabel;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.uiforms.impl.formtext.DefaultFormText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.jboss.tools.central.reddeer.exception.CentralException;
import org.jboss.tools.central.reddeer.projects.CentralExampleProject;

/**
 * 
 * @author rhopp
 *
 */


public class NewProjectExamplesWizardDialogCentral extends WizardDialog {

	public NewProjectExamplesWizardDialogCentral() {
		new DefaultShell("New Project Example"); //wait for shell to appear
	}
	
	public void finish(String projectName) {
		log.info("Finish wizard");

		String shellText = new DefaultShell().getText();
		Button button = new FinishButton();
		button.click();

		new WaitWhile(new ShellIsActive(shellText), TimePeriod.LONG);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		new DefaultShell("New Project Example");
		CheckBox quickFix = new CheckBox("Show the Quick Fix dialog");
		if (quickFix.isEnabled()){
			//finish dialog and record errors.
			new FinishButton().click();
			ProblemsView problemsView = new ProblemsView();
			problemsView.open();
			List<Problem> problems = problemsView.getProblems(ProblemType.ERROR);
			String errors = "";
			for (Problem problem : problems) {
				errors += "\t"+problem.getDescription()+"\n";
			}
			throw new CentralException("Quick Fix should not be enabled.\n"+errors);
		}
		if (new CheckBox(1).isEnabled()) {
			assertTrue(new CheckBox(1).isChecked());
		}
		assertTrue(new CheckBox("Do not show this page again").isEnabled());
		assertFalse(new CheckBox("Do not show this page again").isChecked());
		new PushButton("Finish").click();
		new WaitWhile(new ShellIsActive("New Project Example"));
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}
	
	public void open(CentralExampleProject project){
		ExampleLabel label = new ExampleLabel(project.getCategory());
		label.hover();
		new DefaultFormText(project.getName()).click();
		new DefaultShell("New Project Example");
	}
	

	private class ExampleLabel extends DefaultLabel {

		public ExampleLabel(String label) {
			super(label);
		}

		public void hover() {
			WidgetHandler.getInstance().notifyWidget(SWT.MouseEnter, getSWTWidget());
		}
	}
	
}
