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

import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.discovery.AbstractCatalogSource;
import org.eclipse.equinox.internal.p2.discovery.compatibility.BundleDiscoveryStrategy;

/**
 * A subclass of BundleDiscoveryStrategy to avoid validation fails when property expressions are used in connector description 
 * this class only works when {@link ExpressionBasedDiscoveryConnector} is used when loading the connector descriptor
 * 
 * @see org.eclipse.mylyn.internal.discovery.core.model.BundleDiscoveryStrategy
 * @author snjeza
 */
@SuppressWarnings("restriction")
public class ExpressionBasedBundleDiscoveryStrategy extends BundleDiscoveryStrategy {

	private ExpressionBasedDiscoveryExtensionProcessor processor = new ExpressionBasedDiscoveryExtensionProcessor() {
		@Override
		public AbstractCatalogSource computeDiscoverySource(
				IContributor contributor) {
			return ExpressionBasedBundleDiscoveryStrategy.this.computeDiscoverySource(contributor);
		}
	};

	@Override
	protected void processExtensions(IProgressMonitor monitor,
			IExtension[] extensions) {
		processor.processExtensions(monitor, extensions, items,
				categories, certifications);
	}
}
