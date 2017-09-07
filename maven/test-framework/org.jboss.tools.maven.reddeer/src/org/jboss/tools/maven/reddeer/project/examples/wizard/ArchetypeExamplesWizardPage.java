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

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.Table;
import org.eclipse.reddeer.swt.impl.combo.LabeledCombo;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;

public class ArchetypeExamplesWizardPage extends WizardPage{
	
	public ArchetypeExamplesWizardPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}

	public String getGroupID(){
		return new LabeledCombo("Group Id:").getText();
	}
	
	public void setGroupID(String groupId){
		new LabeledCombo("Group Id:").setText(groupId);
	}
	
	public String getArtifactID(){
		return new LabeledCombo("Artifact Id:").getText();
	}
	
	public String getVersion(){
		return new LabeledCombo("Version:").getText();
	}
	
	public String getPackage(){
		return new LabeledCombo("Package:").getText();
	}
	
	public Table getTableSuffix(){
		return new DefaultTable();
	}
	

}
