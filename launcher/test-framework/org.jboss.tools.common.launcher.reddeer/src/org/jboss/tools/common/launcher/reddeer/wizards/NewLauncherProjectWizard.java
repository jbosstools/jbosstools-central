/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.common.launcher.reddeer.wizards;


import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.dialogs.NewWizard;
import org.eclipse.reddeer.jface.wizard.WizardDialog;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.NextButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.central.reddeer.api.JavaScriptHelper;
import org.jboss.tools.central.reddeer.wait.CentralIsLoaded;


/**
 * 
 * @author zcervink
 *
 */
public class NewLauncherProjectWizard extends WizardDialog {
	
	/**
	 * Opens new application wizard via shell menu File - New.
	 */
	public void openWizardFromShellMenu() {
		new WorkbenchShell().setFocus();
		new NewWizard().open();
		new DefaultShell("New").setFocus(); 
		new DefaultTreeItem("Launcher", "Launcher project").select(); 
		new NextButton().click();
		new DefaultShell("New Launcher project");
	}
	
	/**
	 * Opens new application wizard via link in the Red Hat Central
	 */
	public void openWizardFromCentral() {
		new WaitUntil(new CentralIsLoaded(),TimePeriod.LONG);
		JavaScriptHelper.getInstance().setBrowser(new InternalBrowser());
		JavaScriptHelper.getInstance().clickWizard("Launcher Application");
		new DefaultShell("New Launcher project");
	}
	
	/**
	 * Finishes this wizard
	 */
	@Override
	public void finish() {		
		finish(TimePeriod.getCustom(1800));
	}
	
	/**
	 * Finishes this wizard
	 */
	@Override
	public void finish(TimePeriod period) {
		if (isFinishEnabled()) {
			new FinishButton().click();
			new WaitWhile(new JobIsRunning(), period);
		} else {
			throw new RedDeerException("Finish button is not enabled!");
		}
	}
	
	/**
	 * Returns if the "Finish" button is enabled
	 * @return
	 */
	@Override
	public boolean isFinishEnabled() {
		return new FinishButton().isEnabled();
	}
}
