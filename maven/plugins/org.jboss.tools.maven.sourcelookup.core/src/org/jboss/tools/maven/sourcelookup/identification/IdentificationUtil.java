/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.identification;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IdentificationUtil {

	private static final String CLASSIFIER_SOURCES = "sources"; //$NON-NLS-1$
	private static final String CLASSIFIER_TESTS = "tests"; //$NON-NLS-1$
	private static final String CLASSIFIER_TESTSOURCES = "test-sources"; //$NON-NLS-1$

	private IdentificationUtil() {}
	
	public static String getSHA1(File file) throws IOException,
			NoSuchAlgorithmException {
		
		InputStream inputStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			inputStream = new FileInputStream(file);
			byte[] bytes = new byte[16 * 1024];
			int count = 0;
			while ((count = inputStream.read(bytes)) != -1) {
				md.update(bytes, 0, count);
			}
			byte[] digestBytes = md.digest();
			for (int i = 0; i < digestBytes.length; i++) {
				sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e){
				  //ignore
				}
			}
		}
		return sb.toString();
	}
	
	public static String getSourcesClassifier(String baseClassifier) {
		return CLASSIFIER_TESTS.equals(baseClassifier) ? CLASSIFIER_TESTSOURCES
				: CLASSIFIER_SOURCES;
	}
}
