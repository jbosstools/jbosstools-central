/*************************************************************************************
 * Copyright (c) 2008-2016 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/

package org.jboss.tools.central.internal.browser;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;

/**
 * 
 * @author Konstantin Marmalyukov
 * @author Ilya Buziuk (ibuziuk)
 */
public class VersionedBrowser extends Browser {
	private static final String UNKNOWN_BROWSER = "Unknown Browser"; //$NON-NLS-1$
	private String name;
	private String version;
	
	private static boolean usageReported = false;
	
	public VersionedBrowser(Composite parent, int style) {
		super(parent, style);
		//TODO Check Project Spartan / Edge
		execute("window.detectNameVersionOfBrowser = function() {" +  //$NON-NLS-$1
				"var ua = navigator.userAgent,tem,M=ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\\/))\\/?\\s*(\\d+)/i) || [];" +  //$NON-NLS-1$
				   "if(/trident/i.test(M[1])){" + //$NON-NLS-1$
				   		"tem=/\\brv[ :]+(\\d+)/g.exec(ua) || [];" +  //$NON-NLS-1$
				   		"return 'IE '+ '_' + (tem[1]||'');" + //$NON-NLS-1$
				   	"}" + //$NON-NLS-1$
				   	//get Mozilla(XulRunner) version
				   	"var rv = ua.match(/rv:(\\d{1,2}[.]\\d{1,2})/i);" + //$NON-NLS-1$
				   	"if(rv != null) {" + //$NON-NLS-1$
				   		"return 'Mozilla' + '_' + rv[1];" + //$NON-NLS-1$
				   	"}" + //$NON-NLS-1$
				   	"M=M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];" + //$NON-NLS-1$
				   	"if((tem=ua.match(/version\\/(\\d+)/i))!=null) {" + //$NON-NLS-1$
				   		"M.splice(1,1,tem[1]);" + //$NON-NLS-1$
				   	"}" + //$NON-NLS-1$
					"return M[0] + '_' + M[1];" + //$NON-NLS-1$
				"}");
		String result = (String) evaluate("return detectNameVersionOfBrowser()"); //$NON-NLS-$1	

		if (result != null) {
			name = result.substring(0, result.indexOf("_")); //$NON-NLS-1$
			version = result.substring(result.indexOf("_") + 1); //$NON-NLS-1$
		} else {
			// script failed to detect browser name / version
			result = UNKNOWN_BROWSER;
		}
		
		if (!usageReported) {
			UsageEventType eventType = JBossCentralActivator.getDefault().getUsedBrowserEventType();
			UsageReporter.getInstance().trackEvent(eventType.event(result));
			usageReported = true;
		}
	}

	public String getBrowserName() {
		return name;
	}
	
	public String getBrowserVersion() {
		return version;
	}
	
	/**
	 * @return false if detected browser does not support HTML5 content 
	 * (Mozilla / IE version 9 and below), true otherwise
	 */
	public boolean isHTML5supported() {
		if (name != null && version != null) {
			double browserVersion = Double.parseDouble(version);
			if (("Mozilla".equals(name) && browserVersion < 10) //$NON-NLS-1$
					|| ("IE".equals(name) && browserVersion < 10)) { //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void checkSubclass() {
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + name + " version " + version; //$NON-NLS-1$//$NON-NLS-2$
	}
}
