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

import java.util.ArrayList;
import java.util.List;

import org.jboss.reddeer.jface.wizard.WizardDialog;
import org.jboss.reddeer.swt.api.Shell;
import org.jboss.reddeer.swt.api.Tree;
import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ShellWithTextIsActive;
import org.jboss.reddeer.core.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.common.exception.RedDeerException;
import org.jboss.reddeer.common.exception.WaitTimeoutExpiredException;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.menu.ShellMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;

public class MavenImportWizard extends WizardDialog {

	public MavenImportWizard() {
		super();
	}

	public void open() {
		new ShellMenu("File", "Import...").select();
		new WaitUntil(new ShellWithTextIsAvailable("Import"));
		new DefaultShell("Import");
		new DefaultTreeItem("Maven", "Existing Maven Projects").select();
		new PushButton("Next >").click();
		new WaitUntil(new ShellWithTextIsAvailable("Import Maven Projects"));
		new DefaultShell("Import Maven Projects");
	}

	@Override
	public void finish() {
		new PushButton("Finish").click();
		
		waitForShellToDisappear();
		
		waitForcheatSheet();
		
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	private void waitForcheatSheet() {
		try {
			new DefaultShell("Found cheatsheet");
			new PushButton("No").click();
		} catch (Exception ex) {
			// project was without cheatsheet; continue.
		}
	}

	private void waitForShellToDisappear(){
			new WaitWhile(new ShellWithTextIsActive("Import Maven Projects"));
		try{
			//try to wait for another shell (with errors after import)
			new WaitUntil(new ShellWithTextIsActive("Import Maven Projects"));
			throw new MavenImportWizardException(new DefaultShell());
		}catch(WaitTimeoutExpiredException ex){
			//everything is fine
		}
	}

	public class MavenImportWizardException extends RedDeerException{
	
		private static final long serialVersionUID = 1L;
		
		List<String> errors;
		
		public MavenImportWizardException(Shell activeShell) {
			//Probalby some error occured while importing
			super("Probalby some error occured while importing");
			errors = extractErrors(activeShell);
			activeShell.close();
		}
		
		public List<String> getErrors() {
			return errors;
		}

		private List<String> extractErrors(Shell activeShell) {
			List<String> list = new ArrayList<String>();
			Tree tree = new DefaultTree(); 
			for (TreeItem item : tree.getAllItems()) {
				list.add(item.getText());
			}
			return list;
		}
		
	}
	
}
