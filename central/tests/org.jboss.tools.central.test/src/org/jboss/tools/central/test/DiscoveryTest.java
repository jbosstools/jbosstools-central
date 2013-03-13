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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.editors.JBossCentralEditor;
import org.jboss.tools.central.editors.SoftwarePage;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;
import org.jboss.tools.central.internal.discovery.DiscoveryConnector;
import org.junit.BeforeClass;
import org.junit.Test;

public class DiscoveryTest {

	private static DiscoveryViewer discoveryViewer;
	private static final String TEST_URL = "http://download.jboss.org/jbosstools/updates/development/kepler/central/test/";
	private static final String DEFAULT_URL = "http://download.jboss.org/jbosstools/updates/development/kepler/central/core/";
	private static final String TEST_ID = "test.feature";
	private static final String DEFAULT_ID = "test.default.feature";
	
	private static final String INVALID1_ID = "test.feature.invalid1";
	private static final String INVALID2_ID = "test.feature.invalid2";
	
	@BeforeClass
	public static void init() throws Exception {
		System.setProperty("central.URL", TEST_URL);
		final WorkbenchWindow window = (WorkbenchWindow) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		IViewPart welcomeView = page.findView(CentralTest.ORG_ECLIPSE_UI_INTERNAL_INTROVIEW);
		if (welcomeView != null) {
			page.hideView(welcomeView);
		}
		JBossCentralEditor editor = JBossCentralActivator.getJBossCentralEditor(true);
		CentralTest.waitForJobs();
		activateSoftwarePage(editor);
		CentralTest.waitForJobs();
		SoftwarePage softwarePage = editor.getSoftwarePage();
		discoveryViewer = (DiscoveryViewer) getValue(softwarePage, "discoveryViewer");
	}
	
	private static void activateSoftwarePage(JBossCentralEditor editor) throws Exception {
		Method method = getSetActiveMethod(editor.getClass());
		method.setAccessible(true);
		method.invoke(editor, new Object[] {new Integer(1)});
	}

	private static Method getSetActiveMethod(Class<?> clazz) {
		try {
			Method method = clazz.getDeclaredMethod("setActivePage", new Class[] {int.class});
			return method;
		} catch (SecurityException e) {
			throw e;
		} catch (NoSuchMethodException e) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				return getSetActiveMethod(superclass);
			}
		}
		return null;
	}

	@Test
	public void testConnectors() throws Exception {
		List<DiscoveryConnector> connectors = getConnectors();
		assertNotNull(connectors);
	}
	
	@Test
	public void testDefaultConnector() throws Exception {
		DiscoveryConnector connector = getConnector(DEFAULT_ID);
		assertNotNull(connector);
		String siteUrl = (String) getValue(connector, "siteUrl");
		assertEquals(siteUrl, DEFAULT_URL);
	}
	
	@Test
	public void testCentralConnector() throws Exception {
		DiscoveryConnector connector = getConnector(TEST_ID);
		assertNotNull(connector);
		String siteUrl = (String) getValue(connector, "siteUrl");
		assertEquals(siteUrl, TEST_URL);
	}
	
	@Test
	public void testInvalidConnectors() throws Exception {
		testInvalidUrl(INVALID1_ID);
		testInvalidUrl(INVALID2_ID);
	}

	public void testInvalidUrl(String id) throws Exception {
		DiscoveryConnector connector = getConnector(id);
		assertNotNull(connector);
		String siteUrl = (String) getValue(connector, "siteUrl");
		try {
			new URL(siteUrl);
			fail("The '" + id + "' connector is valid.");
		} catch (Exception e) {
			//
		}
	}
	
	private DiscoveryConnector getConnector(String id) throws Exception {
		List<DiscoveryConnector> connectors = getConnectors();
		for (DiscoveryConnector connector:connectors) {
			if (connector == null) {
				continue;
			}
			String connectorId = (String) getValue(connector, "id" );
			if (id.equals(connectorId)) {
				return connector;
			}
		}
		return null;
	}

	private List<DiscoveryConnector> getConnectors() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		 Class<?> clazz = discoveryViewer.getClass();
		 Field field = clazz.getDeclaredField("allConnectors");
		 field.setAccessible(true);
		 List<DiscoveryConnector> connectors = (List<DiscoveryConnector>) field.get(discoveryViewer);
		 return connectors;
	}
	
	private static Object getValue(Object object, String property)
			throws Exception {
		Class<?> clazz = object.getClass();
		Field field = getField(clazz, property);
		field.setAccessible(true);
		return field.get(object);

	}

	public static Field getField(Class<?> clazz, String property) {
		try {
			Field field = clazz.getDeclaredField(property);
			return field;
		} catch (SecurityException e) {
			throw e;
		} catch (NoSuchFieldException e) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				return getField(superclass, property);
			}
		}
		return null;
	}
}
