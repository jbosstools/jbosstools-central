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
package org.jboss.tools.maven.conversion.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Fred Bricon
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.conversion.ui.messages"; //$NON-NLS-1$

	public static String Convert_Maven_Dependency;

	public static String Maven_Configuration_Warning;

	public static String Maven_Configuration_Dialog_Warning;

	public static String Jre_Warning;

	public static String Jre_Dialog_Warning;

	public static String Gradle_Configuration_Warning;

	public static String Gradle_Configuration_Dialog_Warning;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
