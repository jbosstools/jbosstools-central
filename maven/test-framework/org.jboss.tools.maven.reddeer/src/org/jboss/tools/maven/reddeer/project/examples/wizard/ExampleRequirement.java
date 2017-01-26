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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.reddeer.swt.api.Group;
import org.jboss.reddeer.swt.api.Table;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.group.DefaultGroup;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.tools.runtime.reddeer.wizard.DownloadRuntimesTaskWizard;
import org.jboss.tools.runtime.ui.RuntimeSharedImages;
import org.jboss.tools.runtime.ui.RuntimeUIActivator;

public class ExampleRequirement {
	
	private String type;
	private String name;
	private boolean met;
	private Table table;
	private int requirementIndex;
	
	public ExampleRequirement(Table table, int requirementIndex){
		this.table=table;
		this.requirementIndex = requirementIndex;
		this.type = table.getItem(requirementIndex).getText();
		this.name = table.getItem(requirementIndex).getText(1);
//		this.met = isRequirementMet(table.getItem(requirementIndex).getImage(2));
		this.met = true; // NASTY HACK!!!
	}
	
	public String getType(){
		return type;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isMet(){
		return met;
	}
	
	public DownloadRuntimesTaskWizard downloadAndInstall(){
		table.select(requirementIndex);
		Group reqGroup = new DefaultGroup("Requirements");
		new PushButton(reqGroup, "Download and Install...").click();
		new DefaultShell("Download Runtimes");
		return new DownloadRuntimesTaskWizard();
	}
	
	public void install(){
		table.select(requirementIndex);
		Group reqGroup =  new DefaultGroup("Requirements");
		new PushButton(reqGroup, "Install...").click();
		new DefaultShell("Preferences");
	}
	
	private boolean isRequirementMet(Image i){
 		AbstractUIPlugin plugin = (AbstractUIPlugin)Platform.getPlugin("org.jboss.tools.central");
 		ImageDescriptor imageDescriptorFromPlugin = plugin.imageDescriptorFromPlugin("org.jboss.tools.project.examples", "icons/ok.png");
 		Image image = plugin.getImageRegistry().get("org.jboss.tools.central/icons/ok.png");
//		URL entry = Platform.getBundle("org.jboss.project.examples").getEntry("icons/ok.png");
 		String imageId = "org.jboss.tools.central/"+imageDescriptorFromPlugin.hashCode();
 		Image image2 = plugin.getImageRegistry().get(imageId);
		return i.equals(RuntimeUIActivator.sharedImages().image(
				RuntimeSharedImages.CHECKBOX_ON_KEY));
	}

}
