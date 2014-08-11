/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
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
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractProxyWizardDiscoveryTest {
	
	private static final String directoryTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<directory xmlns=\"http://www.eclipse.org/mylyn/discovery/directory/\">\r\n" + 
			"	<entry url=\"http://localhost:${port}/${jarName}\" permitCategories=\"true\"/>\r\n" + 
			"</directory>";

	protected Server server; 
	
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("org.jboss.tools.central.donotshow", Boolean.FALSE.toString());
		System.setProperty("org.eclipse.ui.testsDisableWorkbenchAutoSave", Boolean.TRUE.toString());
	}
	
	@AfterClass
	public static void afterClass() {
		System.clearProperty("org.jboss.tools.central.donotshow");		
		System.clearProperty("org.eclipse.ui.testsDisableWorkbenchAutoSave");		
	}


	protected void createRemoteResources(int port) throws IOException {
		createRemoteResources(port, "test-resources/remote/org.jboss.tools.central.discovery-4.2.0-SNAPSHOT.jar");
	}
	
	protected void createRemoteResources(int port, String sourceDiscoveryJar) throws IOException {
		File jettyResources = new File("target/jetty-resources");
		FileUtils.deleteDirectory(jettyResources);
		
		File discoveryFile = new File(jettyResources, "directory.xml");
		if (discoveryFile.exists()) {
			discoveryFile.delete();
		}
		File originalJar = new File(sourceDiscoveryJar); 
		String jarName = originalJar.getName();
		FileUtils.copyFile(originalJar, new File(jettyResources, jarName));
		String directoryContent = directoryTemplate.replace("${port}", ""+port)
				                                   .replace("${jarName}", jarName);
		FileUtils.write(discoveryFile, directoryContent, "UTF-8");
	}
	
	protected int startServer() throws Exception {
		server = new Server(0);
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setResourceBase(new File("target/jetty-resources").getAbsolutePath());
		server.setHandler(resourceHandler);
	    server.start();
	    return server.getConnectors()[0].getLocalPort();
	}
	
	@After
	public void stopServer() throws Exception {
		if (server != null && !server.isStopped()) {
			server.stop();
		}
	}
	
}
