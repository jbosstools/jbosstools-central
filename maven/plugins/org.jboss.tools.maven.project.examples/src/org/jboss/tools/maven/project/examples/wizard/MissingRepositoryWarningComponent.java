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
package org.jboss.tools.maven.project.examples.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.jboss.tools.maven.ui.wizard.ConfigureMavenRepositoriesWizard;

public class MissingRepositoryWarningComponent extends Composite {

	private Link warninglink;
	private Label warningImg;
	private Image warningIcon;

	public MissingRepositoryWarningComponent(Composite parent) {
		super(parent, SWT.NORMAL);
		
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).span(3, 1)
		.applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
		
		warningImg = new Label(this, SWT.CENTER | SWT.TOP);
		warningIcon = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(warningImg);
		
		warninglink = new Link(this, SWT.NONE |  SWT.FILL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(warninglink);
		warninglink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ConfigureMavenRepositoriesWizard wizard = new ConfigureMavenRepositoriesWizard(null, "redhat-techpreview-all-repository"); //$NON-NLS-1$
				WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
				wizard.init(null, null);
				dialog.create();
				dialog.open(); 
			}
		});
		GridData gd = (GridData) getLayoutData();
		gd.widthHint = 650;
		
		GC gc = new GC(this);
		gc.setFont(getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		gd = (GridData) getLayoutData();
		gd.heightHint = fontMetrics.getHeight() * 3;
	}
	
	public void setLinkText(String text) {
		if (warninglink != null) {
			warninglink.setText(text);
			if (!text.isEmpty() ) {
				warningImg.setImage(warningIcon);
			} else {
				warningImg.setImage(null);
			}
			getParent().layout(true, true);
		}
	}
}
