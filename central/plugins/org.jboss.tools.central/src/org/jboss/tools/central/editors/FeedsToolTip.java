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

package org.jboss.tools.central.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.forms.widgets.FormText;
import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza
 *
 */
public class FeedsToolTip extends ToolTip {

	private String toolText;
	
	public FeedsToolTip(FormText formText, String toolText) {
		super(formText);
		this.toolText = "<html>" +
				"<head>" +
				"<meta charset=\"UTF-8\" />" +
				"<title>JBoss</title>" +
				"<style>" +
				"html, body { font-size: 12px;font-family: Arial, Helvetica, sans-serif; }" +
				"h1, h2, h3, h4, h5, h6 { font-size: 14px;font-weight:bold;font-family: Arial, Helvetica, sans-serif; }" +
				"</style>" +
				"</head>" +
				"<body>" +
				toolText +
				"</body>" +
				"</html>";
		setShift(new Point(0, 0));
		setPopupDelay(400);
		setHideOnMouseDown(false);
	}

	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {	
		
		parent.setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayoutData(gd);
		
		final Browser[] browser = new Browser[1];
		try {
			browser[0] = new Browser(parent, SWT.NONE);
		} catch (Exception e1) {
			browser[0] = new Browser(parent, SWT.WEBKIT);
		}
		browser[0].setJavascriptEnabled(false);
		browser[0].addLocationListener(new LocationAdapter() {
			
			@Override
			public void changed(LocationEvent event) {
				String location = event.location;
				if (location != null && !location.startsWith("about:")) {
					JBossCentralActivator.openUrl(location, Display.getCurrent().getActiveShell());
					hide();
				}
			}
		});
		browser[0].addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				event.required= true;
				event.browser = browser[0];
			}
		});
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 150;
		gd.widthHint = 400;
		browser[0].setLayoutData(gd);
		browser[0].setText(toolText);
        
		return parent;
	}

}
