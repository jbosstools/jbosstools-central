/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.preferences;

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
import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza
 *
 */
public class JBossCentralPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button showOnStartup;

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
				
		showOnStartup = new Button(composite, SWT.CHECK);
		showOnStartup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		showOnStartup.setSelection(JBossCentralActivator.getDefault().showJBossCentralOnStartup());
		showOnStartup.setText("Show JBoss Central On Startup");
		
		return composite;
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
	@Override
	protected void performApply() {
		IEclipsePreferences preferences = JBossCentralActivator.getDefault().getPreferences();
		preferences.putBoolean(JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP, showOnStartup.getSelection());
		JBossCentralActivator.getDefault().savePreferences();
	}

	@Override
	protected void performDefaults() {
		IEclipsePreferences preferences = JBossCentralActivator.getDefault().getPreferences();
		
		showOnStartup.setSelection(JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE);
		preferences.putBoolean(JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP, JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE);
		JBossCentralActivator.getDefault().savePreferences();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}

}
