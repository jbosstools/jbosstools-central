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
package org.jboss.tools.maven.reddeer.profiles;

import java.util.ArrayList;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.jface.wizard.WizardDialog;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

public class SelectProfilesDialog extends WizardDialog{
	
	private String projectName;
	
	public SelectProfilesDialog(String projectName){
		this.projectName=projectName;
	}
	
	public void open(){
		ProjectExplorer pe = new ProjectExplorer();
		pe.open();
		pe.getProject(projectName).select();
		new ContextMenuItem("Maven","Select Maven Profiles...").select();
		new DefaultShell("Select Maven profiles");		
	}
	
	public void activateAllProfiles(){
		new PushButton("Select All").click();
	}
	
	public void deselectAllProfiles(){
		new PushButton("Deselect all").click();
	}
	
	public void activateProfile(String profileName){
		try{
			new DefaultTable().getItem(profileName).setChecked(true);
		} catch(IllegalArgumentException ex){
			new DefaultTable().getItem(profileName+" (auto activated)").setChecked(true);
		}
	}
	
	public void deactivateProfile(String profileName){
		try{
			new DefaultTable().getItem(profileName).select();
		} catch(IllegalArgumentException ex){
			new DefaultTable().getItem(profileName+" (auto activated)").select();
		}
		new PushButton("Deactivate").click();
	}
	
	public java.util.List<String> getAllProfiles(){
		java.util.List<String> profiles = new ArrayList<String>();
		for(int i=0; i<new DefaultTable().rowCount();i++){
			profiles.add(new DefaultTable().getItem(i).getText(0));
		}
		return profiles;
	}
	
	public void ok(){
		new PushButton("OK").click();
		new WaitWhile(new JobIsRunning(),TimePeriod.LONG);
	}
	
	public String getActiveProfilesText(){
		return new DefaultText(0).getText();//("Active profiles for "+projectName+":").getText();
	}
	
	public void cancel(){
		new PushButton("Cancel").click();
		new WaitWhile(new JobIsRunning(),TimePeriod.LONG);
	}

}