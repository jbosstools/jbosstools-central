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
package org.jboss.tools.maven.reddeer.maven.ui.preferences;

import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.jboss.tools.maven.reddeer.wizards.Repository;

public class RemoteRepositoriesPreferencePage extends PreferencePage{
	
	public RemoteRepositoriesPreferencePage(ReferencedComposite referencedComposite){
		super(referencedComposite, "JBoss Tools","Remote Repositories");
	}
	
	public void addRepository(Repository repository){
		new PushButton("Add...").click();
		new DefaultShell("New Repository");
		new LabeledText("Name:").setText(repository.getName());
		new LabeledText("URL:").setText(repository.getUrl());
		new CheckBox("Enabled").toggle(repository.isEnabled());
		new PushButton("OK").click();
		new DefaultShell("Preferences");
	}
	
	public void modifyRepository(Repository oldRepo, Repository newRepo){
		new DefaultTable().select(oldRepo.getName());
		new PushButton("Edit...").click();
		new DefaultShell("Edit Repository");
		new LabeledText("Name:").setText(newRepo.getName());
		new LabeledText("URL:").setText(newRepo.getUrl());
		new CheckBox("Enabled").toggle(newRepo.isEnabled());
		new PushButton("OK").click();
		new DefaultShell("Preferences");
	}
	
	public void deleteRepository(Repository repository){
		new DefaultTable().select(repository.getName());
		new PushButton("Remove").click();
	}
	
	public Repository getRepository(String name){
		try{
			new DefaultTable().select(name);
		}	catch(CoreLayerException ex){
			return null;
		}
		new PushButton("Edit...").click();
		new DefaultShell("Edit Repository");
		String repoName = new LabeledText("Name:").getText();
		String URL = new LabeledText("URL:").getText();
		boolean enabled = new CheckBox("Enabled").isChecked();
		new PushButton("OK").click();
		new DefaultShell("Preferences");
		Repository r = new Repository(repoName,URL,enabled);
		return r;
	}
}