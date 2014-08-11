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

import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;
import org.eclipse.mylyn.internal.discovery.core.model.BundleDiscoveryStrategy;

/**
 * A subclass of BundleDiscoveryStrategy to avoid validation fails when property expressions are used in connector description 
 * this class only works when {@link ExpressionBasedDiscoveryConnector} is used when loading the connector descriptor
 * 
 * @see org.eclipse.mylyn.internal.discovery.core.model.BundleDiscoveryStrategy
 * @author snjeza
 */
public class ExpressionBasedBundleDiscoveryStrategy extends BundleDiscoveryStrategy {

	private ExpressionBasedDiscoveryExtensionProcessor processor = new ExpressionBasedDiscoveryExtensionProcessor() {
		@Override
		public AbstractDiscoverySource computeDiscoverySource(
				IContributor contributor) {
			return ExpressionBasedBundleDiscoveryStrategy.this.computeDiscoverySource(contributor);
		}
	};

	@Override
	protected void processExtensions(IProgressMonitor monitor,
			IExtension[] extensions) {
		processor.processExtensions(monitor, extensions, connectors,
				categories, certifications);
	}
}
