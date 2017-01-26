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

import org.jboss.reddeer.swt.api.Group;
import org.jboss.reddeer.swt.impl.button.CheckBox;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.combo.LabeledCombo;
import org.jboss.reddeer.swt.impl.group.DefaultGroup;
import org.jboss.reddeer.swt.impl.text.LabeledText;

public class AddRepositoryDialog {
	
	public void chooseRepositoryFromList(String repo){
		Group profileGroup = new DefaultGroup("Profile");
		new LabeledCombo(profileGroup, "Profile ID:").setSelection(repo);
	}
	
	public String getProfileID(){
		Group profileGroup = new DefaultGroup("Profile");
		return new LabeledCombo(profileGroup, "Profile ID:").getText();
	}
	
	public String getRepositoryID(){
		Group repoGroup = new DefaultGroup("Repository");
		return new LabeledText(repoGroup, "ID:").getText();
	}
	
	public String getRepositoryName(){
		Group repoGroup = new DefaultGroup("Repository");
		return new LabeledText(repoGroup, "Name:").getText();
	}
	
	public String getRepositoryURL(){
		Group repoGroup = new DefaultGroup("Repository");
		return new LabeledText(repoGroup, "URL:").getText();
	}
	
	public boolean isActiveByDefault(){
		Group profileGroup = new DefaultGroup("Profile");
		return new CheckBox(profileGroup, "Active by default").isChecked();
	}
	
	public void setProfileID(String profileID){
		Group profileGroup = new DefaultGroup("Profile");
		new LabeledCombo(profileGroup, "Profile ID:").setText(profileID);
	}
	
	public void setRepositoryID(String repoID){
		Group repoGroup = new DefaultGroup("Repository");
		new LabeledText(repoGroup, "ID:").setText(repoID);
	}
	
	public void setRepositoryName(String repoName){
		Group repoGroup = new DefaultGroup("Repository");
		new LabeledText(repoGroup, "Name:").setText(repoName);
	}
	
	public void setRepositoryURL(String repoURL){
		Group repoGroup = new DefaultGroup("Repository");
		new LabeledText(repoGroup, "URL:").setText(repoURL);
	}
	
	public void setActiveByDefault(boolean activeByDefault){
		Group profileGroup = new DefaultGroup("Profile");
		new CheckBox(profileGroup, "Active by default").toggle(activeByDefault);
	}
	
	public String getNameWithURL(){
		Group repoGroup = new DefaultGroup("Repository");
		String a =new LabeledText(repoGroup, "Name:").getText();
		String b = new LabeledText(repoGroup, "URL:").getText();
		String nameWithUrl = a+"-"+b;
		return nameWithUrl;
	}
	
	public void ok(){
		new PushButton("OK").click();
	}
	

}
