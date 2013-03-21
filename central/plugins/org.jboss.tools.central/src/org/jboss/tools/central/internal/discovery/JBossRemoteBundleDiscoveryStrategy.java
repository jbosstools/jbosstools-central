/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery;

import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;
import org.eclipse.mylyn.internal.discovery.core.model.RemoteBundleDiscoveryStrategy;

/**
 * (non-Javadoc)
 * @see org.eclipse.mylyn.internal.discovery.core.model.RemoteBundleDiscoveryStrategy
 * 
 * @author snjeza
 */
public class JBossRemoteBundleDiscoveryStrategy extends
		RemoteBundleDiscoveryStrategy {

	private JBossDiscoveryExtensionProcessor processor = new JBossDiscoveryExtensionProcessor() {
		@Override
		public AbstractDiscoverySource computeDiscoverySource(
				IContributor contributor) {
			return JBossRemoteBundleDiscoveryStrategy.this.computeDiscoverySource(contributor);
		}
	};

	@Override
	protected void processExtensions(IProgressMonitor monitor,
			IExtension[] extensions) {
		processor.processExtensions(monitor, extensions, connectors,
				categories, certifications);
	}
}
