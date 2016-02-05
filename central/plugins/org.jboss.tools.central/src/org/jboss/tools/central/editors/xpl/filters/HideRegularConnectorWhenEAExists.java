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

public class HideRegularConnectorWhenEAExists extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object item) {
		Assert.isTrue(viewer instanceof DiscoveryViewer, "This filter only applies on DiscoveryViewer");
		DiscoveryViewer discoveryViewer = (DiscoveryViewer)viewer;
		if (! (item instanceof ConnectorDescriptor)) { // category headers
			return true;
		}
		ConnectorDescriptor desc = (ConnectorDescriptor)item;
		
		if (!JBossDiscoveryUi.isEarlyAccess(desc)) {
			for (ConnectorDescriptorItemUi otherConnector : discoveryViewer.getAllConnectorsItemsUi()) {
				if (JBossDiscoveryUi.isEarlyAccess(otherConnector.getConnector()) && otherConnector.isVisible() && otherConnector.getConnector().getId().equals(desc.getId())) {
					return false;
				}
			}
		}
		return true;
	}

}
