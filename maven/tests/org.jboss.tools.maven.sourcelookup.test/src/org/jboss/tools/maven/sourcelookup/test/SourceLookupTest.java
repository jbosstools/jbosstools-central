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
package org.jboss.tools.maven.sourcelookup.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.runtime.JBossASHandler;
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
public class SourceLookupTest {

	private static final String JBOSS_EAP_61 = "jboss-eap-6.1";
	private static final String JBOSS_AS_7_1_1_FINAL = "jboss-as-7.1.1.Final";

	@BeforeClass
	public static void init() throws Exception {
		IEclipsePreferences preferences = SourceLookupActivator.getPreferences();
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				true);
		SourceLookupActivator.getDefault().savePreferences();
		IRuntimeDetectorDelegate delegate = getJBossASHandler();
		List<RuntimeDefinition> runtimeDefinitions = new ArrayList<RuntimeDefinition>();
		RuntimeDefinition runtimeDefinition = getRuntimDefinition(delegate, "jbosstools.test.jboss.home.7.1.1", JBOSS_AS_7_1_1_FINAL);
		runtimeDefinitions.add(runtimeDefinition);
		runtimeDefinition = getRuntimDefinition(delegate, "jbosstools.test.jboss.home.eap.6.1", JBOSS_EAP_61);
		runtimeDefinitions.add(runtimeDefinition);
		delegate.initializeRuntimes(runtimeDefinitions);
	}

	private static IRuntimeDetectorDelegate getJBossASHandler() {
		Set<IRuntimeDetector> detectors = RuntimeCoreActivator.getDefault().getRuntimeDetectors();
		for (IRuntimeDetector detector:detectors) {
			IRuntimeDetectorDelegate delegate = ((RuntimeDetector)detector).getDelegate();
			if (delegate instanceof JBossASHandler) {
				return delegate;
			}
		}
		return null;
	}

	private static RuntimeDefinition getRuntimDefinition(
			IRuntimeDetectorDelegate delegate, String property, String directory) throws IOException {
		String home = System.getProperty(property, "C:/" + directory);
		File homeFile = new File(home);
		if (!homeFile.exists()) {
			homeFile = new File(FileLocator.getBundleFile(Platform.getBundle(Activator.PLUGIN_ID)), "/target/requirements/" + directory);
		}
		return delegate.getRuntimeDefinition(homeFile, new NullProgressMonitor());
	}
	
	@Test
	public void testServerCreated() {
		IServer[] servers = ServerCore.getServers();
		assertTrue("No one JBoss server is created.", servers.length > 0);
	}
	
	@Test
	public void testSourceContainerAS711() throws Exception {
		IServer server = getServerByName(JBOSS_AS_7_1_1_FINAL);
		assertNotNull(server);
		ILaunchConfiguration configuration = server.getLaunchConfiguration(true, null);
		assertNotNull(configuration);
		String sourcePathComputer = configuration.getAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, (String)null);
		assertEquals("The JBoss Maven Source Container is not added." , SourceLookupActivator.JBOSS_LAUNCH_SOURCE_PATH_COMPUTER_ID, sourcePathComputer);
		ISourceContainer sourceContainer = new JBossSourceContainer(configuration);
		String sourceName = "org/jboss/weld/bootstrap/WeldBootstrap.java";
		Object source = sourceContainer.findSourceElements(sourceName);
		assertNotNull("The '" + sourceName + "' entry is not found.", source);
	}
	
	@Test
	public void testSourceContainerEAP61() throws Exception {
		IServer server = getServerByName(JBOSS_EAP_61);
		assertNotNull(server);
		ILaunchConfiguration configuration = server.getLaunchConfiguration(true, null);
		assertNotNull(configuration);
		String sourcePathComputer = configuration.getAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, (String)null);
		assertEquals("The JBoss Maven Source Container is not added." , SourceLookupActivator.JBOSS_LAUNCH_SOURCE_PATH_COMPUTER_ID, sourcePathComputer);
		ISourceContainer sourceContainer = new JBossSourceContainer(configuration);
		String sourceName = "org/jboss/weld/bootstrap/WeldBootstrap.java";
		Object source = sourceContainer.findSourceElements(sourceName);
		assertNotNull("The '" + sourceName + "' entry is not found.", source);
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
