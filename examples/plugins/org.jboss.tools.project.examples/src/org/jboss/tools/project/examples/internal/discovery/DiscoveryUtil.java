/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.discovery;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.internal.discovery.ChainedDiscoveryStrategy.DiscoveryConnectorCollector;

/**
 * Discovery utility class.
 * 
 * @author Fred Bricon
 *
 */
public class DiscoveryUtil {

	private DiscoveryUtil() {
	}
	
	/**
	 * Creates a new {@link ConnectorDiscovery} which looks for remote discovery sites first and falls back on locally defined connectors.
	 */
	public static ConnectorDiscovery createConnectorDiscovery() {
		String directoryUrl = ProjectExamplesActivator.getDefault().getConfigurator().getJBossDiscoveryDirectory();
		return createConnectorDiscovery(directoryUrl);
	}

	/**
	 * Creates a new {@link ConnectorDiscovery} which looks for a remote discovery site first and falls back on locally defined connectors.
	 */
	public static ConnectorDiscovery createConnectorDiscovery(String directoryUrl) {
		ConnectorDiscovery connectorDiscovery = new ConnectorDiscovery();
		ChainedDiscoveryStrategy chainedDiscoveryStrategy = new ChainedDiscoveryStrategy(new DiscoveryConnectorCollector());

		// look for remote descriptor first
		if (directoryUrl  != null) {
			ExpressionBasedRemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new ExpressionBasedRemoteBundleDiscoveryStrategy();
			remoteDiscoveryStrategy.setDirectoryUrl(directoryUrl);
			chainedDiscoveryStrategy.addStrategy(remoteDiscoveryStrategy);
		}

		// look for descriptors from installed bundles
		chainedDiscoveryStrategy.addStrategy(new ExpressionBasedBundleDiscoveryStrategy());

		connectorDiscovery.getDiscoveryStrategies().add(chainedDiscoveryStrategy);
		connectorDiscovery.setVerifyUpdateSiteAvailability(true);
		connectorDiscovery.setEnvironment(getEnvironment());
		return connectorDiscovery;
	}
	
	private static Dictionary<Object, Object> getEnvironment() {
		Dictionary<Object, Object> environment = new Hashtable<Object, Object>(
				System.getProperties());
		return environment;
	}

}
