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

import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;

public class MissingRepositoryWarningComponent extends Composite {

	private Link warninglink;

	public MissingRepositoryWarningComponent(Composite parent, boolean visibleInitially) {
		super(parent, SWT.NORMAL);
		
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).span(3, 1)
		.applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
		
		Label warningImg = new Label(this, SWT.CENTER | SWT.TOP);
		Image warningIcon = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(warningImg);
		warningImg.setImage(warningIcon);

		warninglink = new Link(this, SWT.NONE |  SWT.FILL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(warninglink);
		warninglink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Open default external browser
					PlatformUI.getWorkbench().getBrowserSupport()
							.getExternalBrowser().openURL(new URL(e.text));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		setVisible(visibleInitially);
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	public void setLinkText(String text) {
		if (warninglink != null) {
			warninglink.setText(text);
			getParent().layout(true, true);
		}
	}
}
