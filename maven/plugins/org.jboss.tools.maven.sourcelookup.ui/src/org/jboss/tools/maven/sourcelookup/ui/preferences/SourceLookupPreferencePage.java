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

	private Button autoAddButton;

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

		autoAddButton = new Button(composite, SWT.CHECK);
		autoAddButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		autoAddButton.setSelection(SourceLookupActivator.getDefault()
				.isAutoAddSourceContainer());
		autoAddButton
				.setText("Automatically add the JBoss Maven source container to all JBoss AS launch configurations");

		return composite;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void performApply() {
		IEclipsePreferences preferences = SourceLookupActivator.getDefault()
				.getPreferences();
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				autoAddButton.getSelection());
		SourceLookupActivator.getDefault().savePreferences();
	}

	@Override
	protected void performDefaults() {
		IEclipsePreferences preferences = SourceLookupActivator.getPreferences();

		autoAddButton
				.setSelection(SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
}
