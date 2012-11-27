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
package org.jboss.tools.maven.seam.utils;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * Seam utility class
 * 
 * @author Fred Bricon
 *
 */
public class SeamUtils {

	 private static VersionRange SUPPORTED_SEAM_VERSION_RANGE;
	 
	 static {
		try {
			SUPPORTED_SEAM_VERSION_RANGE = VersionRange.createFromVersionSpec("[2.0, 2.3)");
		} catch (InvalidVersionSpecificationException e) {
			//Impossible
		}
	 }
	
	private SeamUtils() {
		// private constructor for utility class
	}
	
	/**
	 * Checks if the Seam conversion to Maven is supported.
	 * 
	 * @param seamVersion the Seam Version
	 * 
	 * @return true if Seam version is between 2.0 and 2.3
	 */
	public static boolean isSeamConversionSupported(String seamVersion) {
		if (seamVersion == null) {
			return false;
		}
		//Seam naming scheme doesn't play well with the Maven Version range stuff
		//So we rebuild a simpler version
		String[] versionBits = seamVersion.split("\\."); //$NON-NLS-1$
		if (versionBits.length > 0) {
			StringBuilder shortVersion = new StringBuilder(versionBits[0]);
			if (versionBits.length > 1) {
				shortVersion.append(".").append(versionBits[1]); //$NON-NLS-1$
			}
			return SUPPORTED_SEAM_VERSION_RANGE.containsVersion(new DefaultArtifactVersion(shortVersion.toString()));
		}
		return false;
	}
	
}
