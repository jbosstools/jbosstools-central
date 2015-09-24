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
package org.jboss.tools.maven.sourcelookup.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.containers.JBossSourceContainer;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.internal.RuntimeDetector;
import org.jboss.tools.runtime.core.model.IRuntimeDetector;
import org.jboss.tools.runtime.core.model.IRuntimeDetectorDelegate;
import org.jboss.tools.runtime.core.model.RuntimeDefinition;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 * @author snjeza
 * 
 */
@SuppressWarnings("restriction")
public class SourceLookupTest {

	private static final String TARGET_REQUIREMENTS = "/target/requirements/";
	private static final String JBOSSTOOLS_TEST_JBOSS_HOME_7_1_1 = "jbosstools.test.jboss.home.7.1.1";
	private static final String JBOSSTOOLS_TEST_JBOSS_HOME_EAP_6_1 = "jbosstools.test.jboss.home.eap.6.1";
	private static final String JBOSSTOOLS_TEST_JBOSS_HOME_WILDFLY_9_0 = "jbosstools.test.jboss.home.wildfly.9.0";
	private static final String DEVICE_C = "C:/";
	private static final String WELD_BOOTSTRAP_JAVA = "org/jboss/weld/bootstrap/WeldBootstrap.java";
	private static final String JBOSS_EAP_61 = "jboss-eap-6.1";
	private static final String JBOSS_AS_7_1_1_FINAL = "jboss-as-7.1.1.Final";
	private static final String WILDFLY_9_0_0_FINAL = "wildfly-9.0.0.Final";
	

	@BeforeClass
	public static void init() throws Exception {
		IEclipsePreferences preferences = SourceLookupActivator.getPreferences();
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				true);
		SourceLookupActivator.getDefault().savePreferences();
		IRuntimeDetectorDelegate delegate = getJBossASHandler();
		List<RuntimeDefinition> runtimeDefinitions = new ArrayList<RuntimeDefinition>();
		RuntimeDefinition runtimeDefinition = getRuntimeDefinition(delegate, JBOSSTOOLS_TEST_JBOSS_HOME_7_1_1, JBOSS_AS_7_1_1_FINAL);
		runtimeDefinitions.add(runtimeDefinition);
		runtimeDefinition = getRuntimeDefinition(delegate, JBOSSTOOLS_TEST_JBOSS_HOME_EAP_6_1, JBOSS_EAP_61);
		runtimeDefinitions.add(runtimeDefinition);
		runtimeDefinition = getRuntimeDefinition(delegate, JBOSSTOOLS_TEST_JBOSS_HOME_WILDFLY_9_0, WILDFLY_9_0_0_FINAL);
		runtimeDefinitions.add(runtimeDefinition);
		delegate.initializeRuntimes(runtimeDefinitions);
	}

	private static IRuntimeDetectorDelegate getJBossASHandler() {
		IRuntimeDetector rtdet = RuntimeCoreActivator.getDefault().findRuntimeDetector("org.jboss.tools.runtime.handlers.JBossASHandler");
		return ((RuntimeDetector)rtdet).getDelegate();
	}

	private static RuntimeDefinition getRuntimeDefinition(
			IRuntimeDetectorDelegate delegate, String property, String directory) throws IOException {
		String home = System.getProperty(property, DEVICE_C + directory);
		File homeFile = new File(home);
		if (!homeFile.exists()) {
			homeFile = new File(FileLocator.getBundleFile(Platform.getBundle(Activator.PLUGIN_ID)), TARGET_REQUIREMENTS + directory);
		}
		return delegate.getRuntimeDefinition(homeFile, new NullProgressMonitor());
	}
	
	@Test
	public void testServerCreated() {
		IServer[] servers = ServerCore.getServers();
		assertTrue("Some servers were not created. Found ["+ getServersAsString(servers)+"]", servers.length > 1);
	}
	
	@Test
	public void testSourceContainerAS711() throws Exception {
		assertServerHasSource("JBoss AS 7.1",WELD_BOOTSTRAP_JAVA);
	}
	
	@Test
	public void testSourceContainerEAP61() throws Exception {
		assertServerHasSource("JBoss EAP 6.1",WELD_BOOTSTRAP_JAVA);
	}

	@Test
	public void testSourceContainerWildFly9() throws Exception {
		assertServerHasSource("WildFly 9.0","io/undertow/server/handlers/PredicateHandler.java");
	}
	
	private void assertServerHasSource(String serverName, String sourceName) throws Exception {
		IServer server = getServerByName(serverName);
		assertNotNull(serverName + " was not found among "+ getServersAsString(), server);
		ILaunchConfiguration configuration = server.getLaunchConfiguration(true, null);
		assertNotNull(configuration);
		String sourcePathComputer = configuration.getAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, (String)null);
		assertEquals("The JBoss Maven Source Container is not added." , SourceLookupActivator.JBOSS_LAUNCH_SOURCE_PATH_COMPUTER_ID, sourcePathComputer);
		ISourceContainer sourceContainer = new JBossSourceContainer(configuration);
		Object[] source = sourceContainer.findSourceElements(sourceName);
		assertEquals("The '" + sourceName + "' entry is not found.", 1, source.length);
	}

	private String getServersAsString(IServer[] servers) {
		StringBuilder sb = new StringBuilder();
		boolean addComma = false;
		for (IServer server:servers) {
			if (addComma) {
				sb.append(", ");
			}
			sb.append(server.getName());
			addComma = true;
		}
		return sb.toString();
	}

	private String getServersAsString() {
		IServer[] servers = ServerCore.getServers();
		return getServersAsString(servers);
	}
	private IServer getServerByName(String name) {
		
		IServer[] servers = ServerCore.getServers();
		for (IServer server:servers) {
			if (name.equals(server.getName()) ) { 
				return server;
			}
		}
		return null;
	}
	
}
