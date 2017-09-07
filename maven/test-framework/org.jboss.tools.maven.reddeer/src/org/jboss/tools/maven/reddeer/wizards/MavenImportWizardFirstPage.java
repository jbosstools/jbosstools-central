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

import java.util.List;

import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.Tree;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsActive;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.combo.LabeledCombo;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.reference.ReferencedComposite;

public class MavenImportWizardFirstPage extends WizardPage{
	
	public MavenImportWizardFirstPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}

	public void setRootDirectory(String path){
		new LabeledCombo("Root Directory:").setText(path);
		new PushButton("Refresh").click();
		new WaitUntil(new ProjectIsLoaded(new DefaultTree()), TimePeriod.LONG);
	}
	
	public void importProject(String path){
		importProject(path,true);
	}
	
	public void importProject(String path, boolean cheatsheet){
		setRootDirectory(path);
		new PushButton("Finish").click();
		new WaitWhile(new ShellIsActive("Import Maven Projects"),TimePeriod.DEFAULT);
		if(cheatsheet){
			try{
				new DefaultShell("Found cheatsheet");
				new PushButton("No").click();
			}catch(Exception ex){
				//project was without cheatsheet; continue.
			}
		}
		new WaitWhile(new JobIsRunning(),TimePeriod.VERY_LONG);
	}
	
	public List<TreeItem> getProjects(){
		DefaultTree tree = new DefaultTree();
		return tree.getAllItems();
	}
	
	
	private class ProjectIsLoaded extends AbstractWaitCondition {

		private Tree tree;
		
		private ProjectIsLoaded(Tree tree) {
			this.tree = tree;
		}
		
		@Override
		public boolean test() {
			return !tree.getItems().isEmpty();
		}

		@Override
		public String description() {
			return "At least one project is loaded";
		}
	}

}
