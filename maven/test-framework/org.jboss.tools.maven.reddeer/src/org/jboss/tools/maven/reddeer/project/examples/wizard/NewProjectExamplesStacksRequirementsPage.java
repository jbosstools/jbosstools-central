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
package org.jboss.tools.maven.reddeer.project.examples.wizard;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.Group;
import org.eclipse.reddeer.swt.api.Table;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.group.DefaultGroup;
import org.eclipse.reddeer.swt.impl.link.AnchorLink;
import org.eclipse.reddeer.swt.impl.link.DefaultLink;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.jboss.tools.maven.reddeer.project.examples.wait.MavenRepositoryNotFound;
import org.jboss.tools.maven.reddeer.wizards.AddRepositoryDialog;

public class NewProjectExamplesStacksRequirementsPage extends WizardPage{
	
	public NewProjectExamplesStacksRequirementsPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}


	public void setTargetRuntime(String selection){
		new DefaultCombo(0).setSelection(selection);
	}
	
	
	/**
	 * Sets nth target runtime.
	 * @param index
	 */
	public void setTargetRuntime(int index){
		new DefaultCombo(0).setSelection(index);
		new WaitWhile(new JobIsRunning());
		try{
			new WaitUntil(new MavenRepositoryNotFound());
			fail("Maven repository is not present. Link with message: "+new DefaultLink().getText());
		}catch(WaitTimeoutExpiredException ex){
			//Do nothing
		}
	}
	
	
	/**
	 * Toggles blank project checkbox
	 * @param blank
	 */
	public void toggleBlank(boolean blank){
		CheckBox ch = new CheckBox("Create a blank project");
		ch.toggle(blank);
	}
	
	public List<ExampleRequirement> getRequirements(){
		Group reqGroup = new DefaultGroup("Requirements and Recommendations");
		Table r = new DefaultTable(reqGroup);
		List<ExampleRequirement> reqs = new ArrayList<ExampleRequirement>();
		for(int i=0; i< r.rowCount(); i++){
			reqs.add(new ExampleRequirement(r, i));
		}
		return reqs;
	}
	
	public boolean warningIsVisible(){
		return true;
	}
	
	public AddRepositoryDialog addEAPMavenRepositoryUsingWarningLink(){
		new AnchorLink("repository").click();
		new DefaultShell("Edit Maven Repository");
		return new AddRepositoryDialog();
	}
	
}
