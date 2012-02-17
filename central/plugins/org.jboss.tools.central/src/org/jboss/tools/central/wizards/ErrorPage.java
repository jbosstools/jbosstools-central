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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

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
           setControl(composite);
	}

}
