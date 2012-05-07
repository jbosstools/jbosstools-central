/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.markers;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.jboss.tools.maven.ui.Activator;

public class OpenPageInBrowserMarkerResolution implements IMarkerResolution,
		IMarkerResolution2 {

	
	private final String label;
	private final String url;

	public OpenPageInBrowserMarkerResolution(String label, String url) {
		this.label = label;
		this.url = url;
	}
	
	public String getDescription() {
		return getLabel();
	}

	public Image getImage() {					
		return null;
	}

	public String getLabel() {
		return label;
	}

	public void run(IMarker arg0) {

		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			IWebBrowser browser;
			if (browserSupport.isInternalWebBrowserAvailable()) {
				browser = browserSupport.createBrowser(null);
			} else {
				browser = browserSupport.getExternalBrowser(); 
			}
			browser.openURL(new URL(url));
		} catch (PartInitException e) {
			Activator.log(e);
		} catch (MalformedURLException e) {
			Activator.log(e);
		}
	}
	
}
