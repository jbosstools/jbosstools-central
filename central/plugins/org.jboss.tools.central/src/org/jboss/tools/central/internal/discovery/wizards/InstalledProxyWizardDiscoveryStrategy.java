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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;
import org.jboss.tools.central.internal.discovery.wizards.xpl.BundleDiscoveryStrategy;

/**
 * @author Fred Bricon
 */
public class InstalledProxyWizardDiscoveryStrategy extends BundleDiscoveryStrategy implements ProxyWizardDiscoveryStrategy {

	private List<ProxyWizard> proxyWizards;

	@Override
	protected String getExtensionPointId() {
		return PROXY_WIZARD_EXTENSION_POINT;
	}
	
	@Override
	protected void processExtensions(IProgressMonitor monitor, IExtension[] extensions) {
		ProxyWizardExtensionReader extensionReader = new ProxyWizardExtensionReader();
		proxyWizards = new ArrayList<ProxyWizard>();
		for (IExtension extension : extensions) {
			IContributor contributor = extension.getContributor();
			AbstractDiscoverySource discoverySource = computeDiscoverySource(contributor);
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement e : elements) {
				if ("proxyWizard".equals(e.getName())) {
					ProxyWizard proxyWizard = extensionReader.readProxyWizardElement(e, discoverySource);
					if (!proxyWizards.contains(proxyWizard)) {
						proxyWizards.add(proxyWizard);
					}
				}
			}
		}
		
		Collections.sort(proxyWizards);
	}
	
	public List<ProxyWizard> getProxyWizards() {
		return proxyWizards;
	}

}