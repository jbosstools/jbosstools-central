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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.discovery.AbstractCatalogSource;
import org.jboss.tools.discovery.core.internal.connectors.xpl.BundleDiscoveryStrategy;

/**
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class InstalledProxyWizardDiscoveryStrategy extends BundleDiscoveryStrategy implements ProxyWizardDiscoveryStrategy {

	private List<ProxyWizard> proxyWizards;

	@Override
	protected String getExtensionPointId() {
		return PROXY_WIZARD_EXTENSION_POINT;
	}
	
	@Override
	protected void processExtensions(IProgressMonitor monitor, IExtension[] extensions) {
		ProxyWizardExtensionReader extensionReader = new ProxyWizardExtensionReader();
		proxyWizards = new ArrayList<>();
		for (IExtension extension : extensions) {
			IContributor contributor = extension.getContributor();
			AbstractCatalogSource discoverySource = computeDiscoverySource(contributor);
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement e : elements) {
				if ("proxyWizard".equals(e.getName())) {
					ProxyWizard proxyWizard = extensionReader.readProxyWizardElement(e, discoverySource);
					if (!containsWizard(proxyWizard.getId(), proxyWizards)) {
						proxyWizards.add(proxyWizard);
					}
				}
			}
		}
		
		Collections.sort(proxyWizards);
	}
	
	private boolean containsWizard(String id, List<ProxyWizard> proxyWizards) {
		for (ProxyWizard p : proxyWizards) {
			if (p.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public List<ProxyWizard> getProxyWizards() {
		return proxyWizards;
	}

}