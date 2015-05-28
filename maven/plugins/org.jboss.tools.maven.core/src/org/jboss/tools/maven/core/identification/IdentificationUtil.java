/*************************************************************************************
 * Copyright (c) 2008-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.identification;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.jboss.tools.foundation.core.digest.DigestUtils;

public class IdentificationUtil {

	private static final String CLASSIFIER_SOURCES = "sources"; //$NON-NLS-1$
	private static final String CLASSIFIER_TESTS = "tests"; //$NON-NLS-1$
	private static final String CLASSIFIER_TESTSOURCES = "test-sources"; //$NON-NLS-1$

	private IdentificationUtil() {}
	
	/**
	 * @deprecated use {@link DigestUtils#sha1(File)} instead
	 */
	@Deprecated
	public static String getSHA1(File file) throws IOException,
			NoSuchAlgorithmException {
		return DigestUtils.sha1(file);
	}
	
	public static String getSourcesClassifier(String baseClassifier) {
		return CLASSIFIER_TESTS.equals(baseClassifier) ? CLASSIFIER_TESTSOURCES
				: CLASSIFIER_SOURCES;
	}
}
