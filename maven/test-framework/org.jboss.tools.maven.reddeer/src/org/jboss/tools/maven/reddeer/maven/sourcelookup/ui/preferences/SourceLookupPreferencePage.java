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
package org.jboss.tools.maven.reddeer.maven.sourcelookup.ui.preferences;

import org.jboss.reddeer.jface.preference.PreferencePage;
import org.jboss.reddeer.swt.api.Group;
import org.jboss.reddeer.swt.impl.button.CheckBox;
import org.jboss.reddeer.swt.impl.button.RadioButton;
import org.jboss.reddeer.swt.impl.group.DefaultGroup;

public class SourceLookupPreferencePage extends PreferencePage{
	
	private static final String ADD_JBOSS="Automatically add the JBoss Maven source container to all JBoss AS launch configurations";
	
	public SourceLookupPreferencePage(){
		super("JBoss Tools","Source Lookup");
	}
	
	public void toggleAutomaticallyAddJBossMavenContainer(boolean toggle){
		new CheckBox(ADD_JBOSS).toggle(toggle);;
	}
	
	public boolean getAutomaticallyAddJBossMavenContainer(){
		return new CheckBox(ADD_JBOSS).isChecked();
	}
	
	public void toggleAutomaticallyConfigureSourceAttachement(SourceAttachment attachment, boolean toggle){
		Group g = new DefaultGroup("Automatically configure the Java Source Attachment");
		new RadioButton(g,attachment.getAttachment()).toggle(toggle);
	}
	
	public enum SourceAttachment{
		ALWAYS ("Always"),
		NEVER ("Never"),
		PROMPT ("Prompt");
		
		private String attachment;
		
		SourceAttachment(String attachment){
			this.attachment = attachment;
		}
		
		public String getAttachment(){
			return attachment;
		}
	}
	
	

}
