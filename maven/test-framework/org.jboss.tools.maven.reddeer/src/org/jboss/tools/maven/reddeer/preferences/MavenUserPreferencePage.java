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
package org.jboss.tools.maven.reddeer.preferences;

import org.jboss.reddeer.common.logging.Logger;
import org.jboss.reddeer.swt.api.Button;
import org.jboss.reddeer.swt.api.Text;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.jface.preference.PreferencePage;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.link.AnchorLink;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.text.DefaultText;
import org.jboss.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;

public class MavenUserPreferencePage extends PreferencePage{
	
	private static final Logger log = Logger.getLogger(MavenUserPreferencePage.class);
	
	public MavenUserPreferencePage(){
		super("Maven","User Settings");
	}
	
	public void setUserSettings(String pathToSettings){
		Text text = null;
		for(int i=0;i<10;i++){
			text = new DefaultText(i);
			if(text.getMessage().contains("settings") && text.getMessage().contains(".xml")){
				break;
			}
		}
		if(!text.getText().equals(pathToSettings)){
			text.setText(pathToSettings);
			Button button = new PushButton("Update Settings");
			button.click();
			try{
	            new DefaultShell("Update project required");
	            new PushButton("Yes").click();
	            new DefaultShell("Preferences");
	        } catch(SWTLayerException ex){
	            log.debug("'Update project required' shell not found.");
	        } finally {
	            new WaitUntil(new JobIsRunning(),TimePeriod.NORMAL,false);
	            new WaitWhile(new JobIsRunning(),TimePeriod.VERY_LONG);
	        }
		}
	}
	
	public String getUserSettings(){
		Text text = null;
		for(int i=0;i<10;i++){
			text = new DefaultText(i);
			if((text.getMessage().contains("settings") && text.getMessage().contains(".xml")) ||
					(text.getText().contains("settings") && text.getText().contains(".xml"))){
				break;
			}
		}
		if(!text.getText().isEmpty()){
			return text.getText();
		}
		return text.getMessage();
	}
	
	public void apply(){
		new PushButton("Apply").click();
		try{
		    new DefaultShell("Update project required");
		    new PushButton("Yes").click();
		    new DefaultShell("Preferences");
		    new WaitWhile(new JobIsRunning(),TimePeriod.VERY_LONG);
		} catch(SWTLayerException ex){
			log.info("Update project required shell was not found.");
		}
	}
	
	public void openUserSettings(){
		new AnchorLink("open file").click();
	}
}
