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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryCategory;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class RemoteProxyWizardDiscoveryStrategyTest extends AbstractProxyWizardDiscoveryTest {

	private File downloadArea;
	
	private RemoteProxyWizardDiscoveryStrategy strategy;
	
	@Before
	public void setUp() throws IOException {
		downloadArea = createDownloadArea();
		strategy = new RemoteProxyWizardDiscoveryStrategy();
		strategy.setStorageFolder(downloadArea);
		strategy.setConnectors(new ArrayList<DiscoveryConnector>());
		strategy.setCategories(new ArrayList<DiscoveryCategory>());
	}
	
	@Test
	public void testDiscovery() throws Exception {
		
		int port = startServer();
		
		createRemoteResources(port);
		strategy.setDirectoryUrl("http://localhost:"+port+"/directory.xml");
		strategy.performDiscovery(null);
		List<ProxyWizard> proxyWizards = strategy.getProxyWizards();
		assertNotNull("no wizards were discovered", proxyWizards);
		assertEquals(6, proxyWizards.size());
		assertEquals("HTML5 Project", proxyWizards.get(0).getLabel());
		assertEquals("OpenShift Application", proxyWizards.get(1).getLabel());
		assertEquals("Richfaces Project", proxyWizards.get(2).getLabel());
		assertEquals("Java EE Web Project", proxyWizards.get(3).getLabel());
		assertEquals("Maven Project", proxyWizards.get(4).getLabel());
		assertEquals("Hybrid Mobile Project", proxyWizards.get(5).getLabel());
	}
	
	@After
	public void tearDown() throws Exception {
		if (downloadArea != null) {
			FileUtils.deleteDirectory(downloadArea);
		}
	}
	
	private static File createDownloadArea() throws IOException {
		File dir = new File("target", "downloadArea"); //$NON-NLS-1$
		FileUtils.deleteDirectory(dir);
		if (!dir.mkdirs()) {
			throw new IOException("Can't create temp folder");
		}
		return dir;
	}

	
}
