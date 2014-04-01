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
package org.jboss.tools.central.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.editors.JBossCentralEditor;
import org.jboss.tools.central.editors.SoftwarePage;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;
import org.jboss.tools.project.examples.internal.discovery.ChainedDiscoveryStrategy.DiscoveryConnectorCollector;
import org.jboss.tools.project.examples.internal.discovery.ExpressionBasedDiscoveryConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DiscoveryTest {

	private static DiscoveryViewer discoveryViewer;
	private static final String TEST_URL = "http://unknown.org/test";
	private static final String DEFAULT_URL = "http://unknown.org/core/";
	private static final String TEST_ID = "test.feature";
	private static final String DEFAULT_ID = "test.default.feature";
	
	private static final String INVALID1_ID = "test.feature.invalid1";
	private static final String INVALID2_ID = "test.feature.invalid2";

	@BeforeClass
	public static void init() throws Exception {
		System.setProperty("central.URL", TEST_URL);
		//Need to allow bundle discovery AND remote discovery hence :
		System.setProperty(DiscoveryConnectorCollector.ALLOW_DUPLICATE_DISCOVERY_CONNECTORS_KEY, Boolean.TRUE.toString());
		final WorkbenchWindow window = (WorkbenchWindow) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		IViewPart welcomeView = page.findView(CentralTest.ORG_ECLIPSE_UI_INTERNAL_INTROVIEW);
		if (welcomeView != null) {
			page.hideView(welcomeView);
		}
		JBossCentralEditor editor = JBossCentralActivator.getJBossCentralEditor(true);
		CentralTest.waitForJobs();
		editor.setActivePage(SoftwarePage.ID);
		CentralTest.waitForJobs();
		SoftwarePage softwarePage = editor.getSoftwarePage();
		discoveryViewer = softwarePage.getDiscoveryViewer();
	}
	
	@AfterClass
	public static void shutDown() {
		System.clearProperty(DiscoveryConnectorCollector.ALLOW_DUPLICATE_DISCOVERY_CONNECTORS_KEY);
		System.clearProperty("central.URL");
	}

	@Test
	public void testConnectors() throws Exception {
		List<ExpressionBasedDiscoveryConnector> connectors = getConnectors();
		assertNotNull(connectors);
		assertNotEquals(0, connectors.size());
	}

	@Test
	public void testDefaultConnector() throws Exception {
		ExpressionBasedDiscoveryConnector connector = getConnector(DEFAULT_ID);
		assertNotNull("Connector "+DEFAULT_ID+ " not found", connector);
		String siteUrl = connector.getSiteUrl();
		assertEquals(siteUrl, DEFAULT_URL);
	}

	@Test
	public void testCentralConnector() throws Exception {
		ExpressionBasedDiscoveryConnector connector = getConnector(TEST_ID);
		assertNotNull("Connector "+TEST_ID+ " not found", connector);
		String siteUrl = connector.getSiteUrl();
		assertEquals(siteUrl, TEST_URL);
	}

	@Test
	public void testInvalidConnectors() throws Exception {
		testInvalidUrl(INVALID1_ID);
		testInvalidUrl(INVALID2_ID);
	}

	public void testInvalidUrl(String id) throws Exception {
		ExpressionBasedDiscoveryConnector connector = getConnector(id);
		assertNull(connector);
	}

	private ExpressionBasedDiscoveryConnector getConnector(String id) throws Exception {
		List<ExpressionBasedDiscoveryConnector> connectors = getConnectors();
		for (ExpressionBasedDiscoveryConnector connector:connectors) {
			if (connector != null) { 
				String connectorId = connector.getId();
				if (id.equals(connectorId)) {
					return connector;
				}
			}
		}
		return null;
	}

	private List<ExpressionBasedDiscoveryConnector> getConnectors() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		 Class<?> clazz = discoveryViewer.getClass();
		 Field field = clazz.getDeclaredField("allConnectors");
		 field.setAccessible(true);
		 List<ExpressionBasedDiscoveryConnector> connectors = (List<ExpressionBasedDiscoveryConnector>) field.get(discoveryViewer);
		 return connectors;
	}
	
	public void testLocalDiscovery() throws Exception {
		Set<String> urls = discoveryViewer.getDirectoryUrls();
		try  {
			discoveryViewer.resetDirectoryUrls();
			discoveryViewer.addDirectoryUrl("file:/"+new File("test-resources", "directory.xml").getAbsolutePath().replace('\\', '/'));
			discoveryViewer.updateDiscovery();
			assertNotNull("Couldn't find connector from local discovery", getConnector("test.local.discovery"));
			
		} finally {
			discoveryViewer.resetDirectoryUrls();
			discoveryViewer.addDirectoryUrls(urls);
		}
		
	}
	
}
