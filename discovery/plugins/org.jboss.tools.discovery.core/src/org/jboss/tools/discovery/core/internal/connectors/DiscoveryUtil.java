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
package org.jboss.tools.discovery.core.internal.connectors;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.equinox.internal.p2.discovery.Catalog;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;
import org.jboss.tools.discovery.core.internal.connectors.ChainedDiscoveryStrategy.DiscoveryConnectorCollector;

/**
 * Discovery utility class.
 * 
 * @author Fred Bricon
 *
 */
@SuppressWarnings("restriction")
public class DiscoveryUtil {

	private DiscoveryUtil() {
	}
	
	/**
	 * Creates a new {@link Catalog} which looks for remote discovery sites first and falls back on locally defined connectors.
	 * This will contain ALL discovery content, without filtering, so Early-Access is visible here when although it may not be enabled
	 */
	public static Catalog createCatalog() {
		String directoryUrl = DiscoveryActivator.getDefault().getJBossDiscoveryDirectory();
		return createCatalog(directoryUrl);
	}

	/**
	 * Creates a new {@link Catalog} which looks for a remote discovery site first and falls back on locally defined connectors.
	 */
	public static Catalog createCatalog(String directoryUrl) {
		Catalog catalog = new Catalog();
		ChainedDiscoveryStrategy chainedDiscoveryStrategy = new ChainedDiscoveryStrategy(new DiscoveryConnectorCollector());

		// look for remote descriptor first
		if (directoryUrl  != null) {
			ExpressionBasedRemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new ExpressionBasedRemoteBundleDiscoveryStrategy();
			remoteDiscoveryStrategy.setDirectoryUrl(directoryUrl);
			chainedDiscoveryStrategy.addStrategy(remoteDiscoveryStrategy);
		}

		// look for descriptors from installed bundles
		chainedDiscoveryStrategy.addStrategy(new ExpressionBasedBundleDiscoveryStrategy());

		catalog.getDiscoveryStrategies().add(chainedDiscoveryStrategy);
		catalog.setVerifyUpdateSiteAvailability(true);
		catalog.setEnvironment(getEnvironment());
		return catalog;
	}
	
	private static Dictionary<Object, Object> getEnvironment() {
		Dictionary<Object, Object> environment = new Hashtable<Object, Object>(
				System.getProperties());
		return environment;
	}

}
