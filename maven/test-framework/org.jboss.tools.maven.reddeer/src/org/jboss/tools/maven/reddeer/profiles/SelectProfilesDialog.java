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

import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
import org.jboss.reddeer.jface.wizard.WizardDialog;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.table.DefaultTable;
import org.jboss.reddeer.swt.impl.text.DefaultText;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitWhile;

public class SelectProfilesDialog extends WizardDialog{
	
	private String projectName;
	
	public SelectProfilesDialog(String projectName){
		this.projectName=projectName;
	}
	
	public void open(){
		ProjectExplorer pe = new ProjectExplorer();
		pe.open();
		pe.getProject(projectName).select();
		new ContextMenu("Maven","Select Maven Profiles...").select();
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

}