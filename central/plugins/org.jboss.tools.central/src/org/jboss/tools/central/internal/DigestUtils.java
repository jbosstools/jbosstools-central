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
package org.jboss.tools.central.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//TODO move to foundation
//TODO use in Maven source lookup
public class DigestUtils {

	private DigestUtils(){}
	
	public static String sha1(File file) throws IOException {
		return sha1(file.toPath());
	}
	
	public static String sha1(Path pathToFile) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (InputStream inputStream = Files.newInputStream(pathToFile)){
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] bytes = new byte[16 * 1024];
			int count = 0;
			while ((count = inputStream.read(bytes)) != -1) {
				md.update(bytes, 0, count);
			}
			byte[] digestBytes = md.digest();
			for (int i = 0; i < digestBytes.length; i++) {
				sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException cantHappen) {
		}
		return sb.toString();
	}
}
