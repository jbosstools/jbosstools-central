/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl.filters;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;
import org.jboss.tools.discovery.core.internal.connectors.ConnectorDescriptorItemUi;
import org.jboss.tools.discovery.core.internal.connectors.JBossDiscoveryUi;

/**
 * When a connector exists in both Early-Access and Regular version,
 * show only regular.
 * @author mistria
 *
 */
public class HideEarlyAccessWhenRegularExistsFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parent, Object item) {
		Assert.isTrue(viewer instanceof DiscoveryViewer, "This filter only applies on DiscoveryViewer");
		DiscoveryViewer discoveryViewer = (DiscoveryViewer)viewer;
		if (! (item instanceof ConnectorDescriptor)) { // category headers
			return true;
		}
		ConnectorDescriptor desc = (ConnectorDescriptor)item;
		
		if (JBossDiscoveryUi.isEarlyAccess(desc)) {
			Set<ConnectorDescriptor> visibleConnectors = new HashSet<>();
			for (ConnectorDescriptorItemUi otherConnector : discoveryViewer.getAllConnectorsItemsUi()) {
				if (otherConnector.getConnector() != desc && otherConnector.isVisible() && otherConnector.getConnector().getId().equals(desc.getId())) {
					return false;
				}
			}
		}
		return true;
	}

}
