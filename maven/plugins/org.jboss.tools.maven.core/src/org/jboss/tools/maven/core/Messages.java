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
package org.jboss.tools.maven.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author snjeza
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.core.messages"; //$NON-NLS-1$
	public static String MavenFacetInstallDelegate_Internal_Error_creating_JBoss_Maven_Facet;
	public static String MavenFacetInstallPage_The_artifactId_field_is_required;
	public static String MavenFacetInstallPage_The_groupId_field_is_required;
	public static String MavenFacetInstallPage_The_packaging_field_is_required;
	public static String MavenFacetInstallPage_The_version_field_is_required;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
