/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractProxyWizardDiscoveryTest {
	
	private static final String directoryTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<directory xmlns=\"http://www.eclipse.org/mylyn/discovery/directory/\">\r\n" + 
			"	<entry url=\"file:${discoveryJarPath}\" permitCategories=\"true\"/>\r\n" + 
			"</directory>";

	@BeforeClass
	public static void beforeClass() {
		System.setProperty("org.jboss.tools.central.donotshow", Boolean.FALSE.toString());
	}
	
	@AfterClass
	public static void afterClass() {
		System.clearProperty("org.jboss.tools.central.donotshow");		
	}


	protected File createDirectoryXml() throws IOException {
		return createDirectoryXml("test-resources/remote/org.jboss.tools.central.discovery-1.2.0-SNAPSHOT.jar");
	}
	
	protected File createDirectoryXml(String discoveryJarRelativePath) throws IOException {
		File discoveryFile = new File("target/test-resources", "directory.xml");
		if (discoveryFile.exists()) {
			discoveryFile.delete();
		}
		String baseDir = new File("").getAbsolutePath().replace("\\", "/");
		String discoveryJarPath = baseDir + "/" + discoveryJarRelativePath;
		String directoryContent = directoryTemplate.replace("${discoveryJarPath}", discoveryJarPath);
		FileUtils.write(discoveryFile, directoryContent, "UTF-8");
		return discoveryFile;
	}
}
