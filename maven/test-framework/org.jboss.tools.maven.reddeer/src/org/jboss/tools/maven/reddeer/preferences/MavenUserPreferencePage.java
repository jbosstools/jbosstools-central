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

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.api.Text;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.link.AnchorLink;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

public class MavenUserPreferencePage extends PreferencePage{
	
	private static final Logger log = Logger.getLogger(MavenUserPreferencePage.class);
	
	public MavenUserPreferencePage(ReferencedComposite referencedComposite){
		super(referencedComposite, "Maven","User Settings");
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
	        } catch(CoreLayerException ex){
	            log.debug("'Update project required' shell not found.");
	        } finally {
	            new WaitUntil(new JobIsRunning(),TimePeriod.DEFAULT,false);
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
	
	public PreferencePage apply(){
		new PushButton("Apply").click();
		try{
		    new DefaultShell("Update project required");
		    new PushButton("Yes").click();
		    new DefaultShell("Preferences");
		    new WaitWhile(new JobIsRunning(),TimePeriod.VERY_LONG);
		} catch(CoreLayerException ex){
			log.info("Update project required shell was not found.");
		}
		return this;
	}
	
	public void openUserSettings(){
		new AnchorLink("open file").click();
	}
}
