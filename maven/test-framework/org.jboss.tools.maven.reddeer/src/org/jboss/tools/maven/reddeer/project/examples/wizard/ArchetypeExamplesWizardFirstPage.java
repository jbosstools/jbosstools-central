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

import org.jboss.reddeer.jface.wizard.WizardPage;
import org.jboss.reddeer.swt.impl.button.CheckBox;
import org.jboss.reddeer.swt.impl.combo.LabeledCombo;

public class ArchetypeExamplesWizardFirstPage extends WizardPage{
	
	public String getProjectName(){
		return new  LabeledCombo("Project name").getText();
	}
	
	public String getPackage(){
		return new  LabeledCombo("Package").getText();
	}
	
	public boolean isUseDefaultWorkspace(){
		return new CheckBox("Use default Workspace location").isChecked();
	}
	
	public String getWorkspaceLocation(){
		return new LabeledCombo("Location:").getText();
	}
	
	public void setProjectName(String projectName){
		new LabeledCombo("Project name").setText(projectName);
	}
	
	public void setPackage(String pckg){
		new  LabeledCombo("Package").setText(pckg);
	}
	
	public void useDefaultWorkspace(boolean useDefault){
		new CheckBox("Use default Workspace location").toggle(useDefault);
	}
	
	public void setWorkspaceLocation(String location){
		new  LabeledCombo("Location:").setText(location);
	}
	
	
}
