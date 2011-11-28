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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;

public class MissingRepositoryWarningComponent extends Composite {

	private Composite warningLink;

	public MissingRepositoryWarningComponent(Composite parent) {
		super(parent, SWT.NORMAL);
		
		warningLink = new Composite(parent, SWT.NONE);
		
		Display display = Display.getCurrent();
		Color color = display.getSystemColor(SWT.COLOR_BLUE);
		
		warningLink.setBackground(color);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).span(3, 1)
				.applyTo(warningLink);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(warningLink);

		Label warningImg = new Label(warningLink, SWT.CENTER | SWT.TOP);
		warningImg.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));

		Link link = new Link(warningLink, SWT.NONE);
		String message = "Artifacts needed from JBoss Enterprise Maven repository do not seem to be available.\nThis might cause build problems. "
				+ "Follow this <a href=\"http://community.jboss.org/wiki/SettingUpTheJBossEnterpriseRepositories\">link</a> for more details.";
		link.setText(message);
		link.addSelectionListener(new SelectionAdapter() {
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
		link.setBackground(display.getSystemColor(SWT.COLOR_RED));
	}
	
	@Override
	public void setVisible(boolean visible) {
		warningLink.setVisible(visible);
		super.setVisible(visible);
	}
	
	@Override
	public void dispose() {
		warningLink.dispose();
		super.dispose();
	}
	
}
