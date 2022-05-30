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

import java.util.Collections;
import java.util.List;

import org.eclipse.equinox.internal.p2.discovery.AbstractDiscoveryStrategy;
import org.jboss.tools.discovery.core.internal.connectors.ChainedDiscoveryStrategy.DataCollector;

/**
 * Collects {@link ProxyWizard}s discovered by {@link ProxyWizardDiscoveryStrategy}s
 * 
 * @author Fred Bricon
 *
 */
public final class ProxyWizardDataCollector implements DataCollector {
	
	private List<ProxyWizard> proxyWizards;
	
	@Override
	public boolean isComplete() {
		return this.proxyWizards != null && !this.proxyWizards.isEmpty();
	}

	@Override
	public void collectData(AbstractDiscoveryStrategy ds) {
		if (ds instanceof ProxyWizardDiscoveryStrategy) {
			this.proxyWizards = ((ProxyWizardDiscoveryStrategy)ds).getProxyWizards();
		}
	}
	
	public List<ProxyWizard> getProxyWizards() {
		return this.proxyWizards == null?Collections.<ProxyWizard>emptyList(): Collections.unmodifiableList(this.proxyWizards);
	}
}