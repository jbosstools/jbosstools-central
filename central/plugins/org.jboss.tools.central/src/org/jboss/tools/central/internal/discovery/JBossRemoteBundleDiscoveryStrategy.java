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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.discovery.core.DiscoveryCore;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscoveryExtensionReader;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryCategory;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryCertification;
import org.eclipse.mylyn.internal.discovery.core.model.RemoteBundleDiscoveryStrategy;
import org.eclipse.mylyn.internal.discovery.core.model.ValidationException;
import org.eclipse.osgi.util.NLS;

/**
 * 
 * (non-Javadoc)
 * @see org.eclipse.mylyn.internal.discovery.core.model.RemoteBundleDiscoveryStrategy
 * 
 * @author snjeza
 * 
 */
public class JBossRemoteBundleDiscoveryStrategy extends
		RemoteBundleDiscoveryStrategy {

	@Override
	protected void processExtensions(IProgressMonitor monitor, IExtension[] extensions) {
		monitor.beginTask("Processing extensions", extensions.length == 0
				? 1
				: extensions.length);
		try {
			ConnectorDiscoveryExtensionReader extensionReader = new ConnectorDiscoveryExtensionReader();

			for (IExtension extension : extensions) {
				AbstractDiscoverySource discoverySource = computeDiscoverySource(extension.getContributor());
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (monitor.isCanceled()) {
						return;
					}
					try {
						if (ConnectorDiscoveryExtensionReader.CONNECTOR_DESCRIPTOR.equals(element.getName())) {
							DiscoveryConnector descriptor = extensionReader.readConnectorDescriptor(element,
									DiscoveryConnector.class);
							descriptor.setSource(discoverySource);
							connectors.add(descriptor);
						} else if (ConnectorDiscoveryExtensionReader.CONNECTOR_CATEGORY.equals(element.getName())) {
							DiscoveryCategory category = extensionReader.readConnectorCategory(element,
									DiscoveryCategory.class);
							category.setSource(discoverySource);
							if (!discoverySource.getPolicy().isPermitCategories()) {
								StatusHandler.log(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN, NLS.bind(
										"Cannot create category '{0}' with id '{1}' from {2}: disallowed",
										new Object[] { category.getName(), category.getId(),
												element.getContributor().getName() }), null));
							} else {
								categories.add(category);
							}
						} else if (ConnectorDiscoveryExtensionReader.CERTIFICATION.equals(element.getName())) {
							DiscoveryCertification certification = extensionReader.readCertification(element,
									DiscoveryCertification.class);
							certification.setSource(discoverySource);
							certifications.add(certification);
						} else {
							throw new ValidationException(NLS.bind("Unexpected element {0}",
									element.getName()));
						}
					} catch (ValidationException e) {
						StatusHandler.log(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN,
								NLS.bind("{0}: {1}", element.getContributor().getName(),
										e.getMessage()), e));
					}
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}

}
