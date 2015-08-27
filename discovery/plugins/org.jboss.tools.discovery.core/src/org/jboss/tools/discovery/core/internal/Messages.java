/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.discovery.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.discovery.core.internal.messages"; //$NON-NLS-1$

	public static String preparingUninstall;
	public static String UsageEventTypeInstallLabelDescription;
	
	public static String SoftwarePage_earlyAccessSection_Title;
	public static String SoftwarePage_earlyAccessSection_message;
	public static String SoftwarePage_nothingToInstall_title;
	public static String SoftwarePage_nothingToInstall_description;
	public static String DiscoveryViewer_selectConnectorFlavor_description;
	public static String DiscoveryViewer_selectConnectorFlavor_title;
	public static String DiscoveryViewer_Certification_Label0;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
