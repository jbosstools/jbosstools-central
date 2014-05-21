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
package org.jboss.tools.central;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.central.messages"; //$NON-NLS-1$

	public static String DiscoveryViewer_Certification_Label0;
	public static String DiscoveryViewer_X_installed;
	public static String DiscoveryViewer_Hide_installed;
	public static String DiscoveryViewer_Hide_EarlyAccess;
	public static String DiscoveryViewer_Show_only_most_recent;
	public static String DiscoveryViewer_filtersLink;
	public static String DiscoveryViewer_noFeatureToShow;
	public static String DiscoveryViewer_clearFilterText;
	public static String DiscoveryViewer_disableFilters;

	public static String DiscoveryViewer_FilterSelectionDialog_label;
	public static String DiscoveryViewer_FilterSelectionDialog_title;
	
	public static String installWithCount;
	public static String uninstallWithCount;
	public static String updateWithCount;
	public static String selectAll;
	public static String deselectAll;
	
	public static String SoftwarePage_earlyAccessSection_Title;
	public static String SoftwarePage_earlyAccessSection_message;
	public static String SoftwarePage_earlyAccessSection_checkbox; 
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
