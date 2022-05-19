/******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     JBoss by Red Hat - resolution for EL ${sysPropName:defaultValue} added
 ******************************************************************************/
package org.jboss.tools.discovery.core.internal.connectors;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.discovery.AbstractCatalogSource;
import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
import org.eclipse.equinox.internal.p2.discovery.compatibility.ConnectorDiscoveryExtensionReader;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
import org.eclipse.equinox.internal.p2.discovery.model.Certification;
import org.eclipse.equinox.internal.p2.discovery.model.ValidationException;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;

/**
 * @author eskimo
 *
 */
@SuppressWarnings("restriction")
public abstract class ExpressionBasedDiscoveryExtensionProcessor {
	protected void processExtensions(IProgressMonitor monitor, 
			IExtension[] extensions,
			List<CatalogItem> connectors,
			List<CatalogCategory> categories,
			List<Certification> certifications
			) {
		monitor.beginTask("Processing extensions", extensions.length == 0
				? 1
				: extensions.length);
		try {
			ConnectorDiscoveryExtensionReader extensionReader = new ConnectorDiscoveryExtensionReader();

			for (IExtension extension : extensions) {
				AbstractCatalogSource discoverySource = computeDiscoverySource(extension.getContributor());
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (monitor.isCanceled()) {
						return;
					}
					try {
						if (ConnectorDiscoveryExtensionReader.CONNECTOR_DESCRIPTOR.equals(element.getName())) {
							ExpressionBasedDiscoveryConnector descriptor = extensionReader.readConnectorDescriptor(element,
									ExpressionBasedDiscoveryConnector.class);
							descriptor.setSource(discoverySource);
							connectors.add(descriptor);
						} else if (ConnectorDiscoveryExtensionReader.CONNECTOR_CATEGORY.equals(element.getName())) {
							CatalogCategory category = extensionReader.readConnectorCategory(element,
									CatalogCategory.class);
							category.setSource(discoverySource);
							if (!discoverySource.getPolicy().isPermitCategories()) {
								DiscoveryActivator.getDefault().getLog().log(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN, NLS.bind(
										"Cannot create category '{0}' with id '{1}' from {2}: disallowed",
										new Object[] { category.getName(), category.getId(),
												element.getContributor().getName() }), null));
							} else {
								categories.add(category);
							}
						} else if (ConnectorDiscoveryExtensionReader.CERTIFICATION.equals(element.getName())) {
							Certification certification = extensionReader.readCertification(element,
									Certification.class);
							certification.setSource(discoverySource);
							certifications.add(certification);
						} else {
							throw new ValidationException(NLS.bind("Unexpected element {0}",
									element.getName()));
						}
					} catch (ValidationException e) {
						DiscoveryActivator.getDefault().getLog().log(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN,
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

	public abstract AbstractCatalogSource computeDiscoverySource(IContributor contributor);
}
