/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.ui.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;

/**
 * 
 * @author snjeza
 * 
 */
public class SourceLookupPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button addContainerButton;
	
	private RadioGroupFieldEditor addSourceAttachmentEditor;

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {

		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(layout);

		addContainerButton = new Button(composite, SWT.CHECK);
		addContainerButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		addContainerButton.setSelection(SourceLookupActivator.getDefault()
				.isAutoAddSourceContainer());
		addContainerButton
				.setText("Automatically add the JBoss Maven source container to all JBoss AS launch configurations");

		createSourceAttachementControls(composite);
		return composite;
	}

	private void createSourceAttachementControls(Composite parent) {
		String name = SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT;
		String label = "Automatically configure the Java Source Attachment";
        String[][] namesAndValues = {
                { "Always", SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_ALWAYS },
                { "Never", SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_NEVER },
                { "Prompt", SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_PROMPT }, };
		addSourceAttachmentEditor = new RadioGroupFieldEditor(name, label, 3, namesAndValues, parent, true);
		addSourceAttachmentEditor.setPreferenceStore(SourceLookupActivator.getDefault().getPreferenceStore());
		addSourceAttachmentEditor.setPage(this);
		addSourceAttachmentEditor.load();
	}

	@Override
	protected void performApply() {
		IEclipsePreferences preferences = SourceLookupActivator.getPreferences();
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				addContainerButton.getSelection());
		addSourceAttachmentEditor.store();
		SourceLookupActivator.getDefault().savePreferences();
	}

	@Override
	protected void performDefaults() {
		IEclipsePreferences preferences = SourceLookupActivator
				.getPreferences();

		addContainerButton
				.setSelection(SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);

		addSourceAttachmentEditor.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
}
