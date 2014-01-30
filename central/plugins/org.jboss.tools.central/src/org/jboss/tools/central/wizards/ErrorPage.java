/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.wizards;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ErrorPage extends WizardPage {

	public ErrorPage(String title, String errorMessage) {
		super("Error");
		setTitle(title);
		setErrorMessage(errorMessage);
	}
	
	@Override
	public void createControl(Composite parent) {
		   Composite composite = new Composite(parent, SWT.NULL);
		   composite.setLayout(new GridLayout(3, false));
		   Link link = getLink(composite);
		   
		   link.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					getShell().close();
					PreferenceDialog preferenceDialog = PreferencesUtil
							.createPreferenceDialogOn(getShell(), "org.eclipse.ui.net.NetPreferences", null, null);
					preferenceDialog.open();
				}

			});
		   new Label(composite, SWT.NONE);
           setControl(composite);
	}

	public static Link getLink(Composite composite) {
		Link link = new Link(composite, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		link.setLayoutData(gd);
		link.setText("Please check your Internet connection, <a>Proxy Settings</a> and try again!");
		return link;
	}

}
