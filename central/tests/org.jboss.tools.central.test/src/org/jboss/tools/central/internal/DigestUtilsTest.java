/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class DigestUtilsTest {
	
	private static final String expectedSha1 = "528FEA2AEA2AF98F4DB189399B6465923C7943EB".toLowerCase();

	@Test
	public void testSha1Path() throws IOException{
		Path zip = Paths.get("test-resources", "sha1", "some.zip");
		String sha1 = DigestUtils.sha1(zip);
		assertEquals(expectedSha1, sha1);
	}
	
	@Test
	public void testSha1File() throws IOException{
		File zip = new File("test-resources", "sha1" + File.separator + "some.zip");
		String sha1 = DigestUtils.sha1(zip);
		assertEquals(expectedSha1, sha1);
	}
}
