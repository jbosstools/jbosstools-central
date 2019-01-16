/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.common.launcher.reddeer.wizards;

import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.text.LabeledText;


/**
 * 
 * @author zcervink
 *
 */
public class NewLauncherProjectWizardPage extends WizardPage {

	public NewLauncherProjectWizardPage(ReferencedComposite referencedComposite) {
		super(referencedComposite);
	}

	protected final static Logger log = Logger
			.getLogger(NewLauncherProjectWizardPage.class);
	
	/**
	 * Sets target mission
	 * @param targetMissionName
	 */
	public void setTargetMission(String targetMissionName) {
		new DefaultCombo(0).setSelection(targetMissionName);
	}

	/**
	 * Sets nth target mission
	 * @param index
	 */
	public void setTargetMission(int index) {
		new DefaultCombo(0).setSelection(index);
	}
	
	/**
	 * Sets target runtime
	 * @param targetRuntimeName
	 */
	public void setTargetRuntime(String targetRuntimeName) {
		new DefaultCombo(1).setSelection(targetRuntimeName);
	}
	
	/**
	 * Sets nth target runtime
	 * @param index
	 */
	public void setTargetRuntime(int index) {
		new DefaultCombo(1).setSelection(index);
	}

	/**
	 * Toggles project path checkbox
	 * @param newCheckboxState
	 */
	public void toggleUseDefaultLocationCheckBox(boolean newCheckboxState) {
		CheckBox useDefaultLocationCheckBox = new CheckBox("Use default location");
		useDefaultLocationCheckBox.toggle(newCheckboxState);
	}
	
	/**
	 * Sets the "Location" value
	 * @param newProjectLocation
	 */
	public void setCustomProjectLocation(String newProjectLocation) {
		LabeledText projectLocationLabeledText = new LabeledText("Location:");
		projectLocationLabeledText.setText(newProjectLocation);
	}
	
	/**
	 * Returns the current project location path
	 * @return
	 */
	public String getProjectLocation() {
		LabeledText projectLocationLabeledText = new LabeledText("Location:");
		return (projectLocationLabeledText.getText());
	}
	
	/**
	 * Sets the "Project name" value
	 * @param newProjectName
	 */
	public void setProjectName(String newProjectName) {
		LabeledText projectNameLabeledText = new LabeledText("Project name:");
		projectNameLabeledText.setText(newProjectName);
	}
	
	/**
	 * Sets the "Artifact id" value
	 * @param newArtifactId
	 */
	public void setArtifactId(String newArtifactId) {
		LabeledText artifactIdLabeledText = new LabeledText("Artifact id:");
		artifactIdLabeledText.setText(newArtifactId);
	}
	
	/**
	 * Returns the "Artifact id" value
	 * @return
	 */
	public String getArtifactId() {
		LabeledText artifactIdLabeledText = new LabeledText("Artifact id:");
		return (artifactIdLabeledText.getText());
	}
	
	/**
	 * Sets the "Group id" value
	 * @param newGroupId
	 */
	public void setGroupId(String newGroupId) {
		LabeledText groupIdLabeledText = new LabeledText("Group id:");
		groupIdLabeledText.setText(newGroupId);
	}
	
	/**
	 * Returns the "Group id" value
	 * @return
	 */
	public String getGroupId() {
		LabeledText groupIdLabeledText = new LabeledText("Group id:");
		return (groupIdLabeledText.getText());
	}

	/**
	 * Sets the version.
	 * @param newVersion
	 */
	public void setVersion(String newVersion) {
		LabeledText versionLabeledText = new LabeledText("Version:");
		versionLabeledText.setText(newVersion);
	}
	
	/**
	 * Returns the version.
	 * @return
	 */
	public String getVersion() {
		LabeledText versionLabeledText = new LabeledText("Version:");
		return (versionLabeledText.getText());
	}
}
