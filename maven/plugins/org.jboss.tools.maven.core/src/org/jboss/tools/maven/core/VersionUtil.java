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
package org.jboss.tools.maven.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version related utiliy class
 * 
 * @author Fred Bricon
 *
 */
public class VersionUtil {

	private static final Pattern MAJOR_MINOR_PATTERN = Pattern.compile("([0-9]+.[0-9]+)"); //$NON-NLS-1$
	
	/**
	 * Finds the Major.Minor version in a version string. Ex.: 
	 * <pre>
	 * VersionUtils.getMajorMinorVersion("1.0-SP4") returns "1.0"
	 * </pre>
	 */
	public static String getMajorMinorVersion(String versionString) {
		if (versionString != null) {
			Matcher m = MAJOR_MINOR_PATTERN.matcher(versionString);
			if (m.find()) {
				versionString = m.group();
			}
		}
		return versionString;	
	}
}
