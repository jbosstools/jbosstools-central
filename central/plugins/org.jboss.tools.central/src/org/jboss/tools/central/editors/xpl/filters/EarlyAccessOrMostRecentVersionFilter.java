/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl.filters;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jboss.tools.central.editors.xpl.ConnectorDescriptorItemUi;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;
import org.jboss.tools.discovery.core.internal.connectors.JBossDiscoveryUi;

/**
 * Hides all visible {@link ConnectorDescriptor} that have another visible
 * {@link ConnectorDescriptor} which either:
 * <ul>
 * <li>is Early Access, or</li>
 * <li>contains higher versions of p2 units.<li>
 * </ul>
 * @author mistria
 *
 */
@SuppressWarnings("restriction")
public class EarlyAccessOrMostRecentVersionFilter extends ViewerFilter {

	private DiscoveryViewer discoveryViewer;
	private Map<String, SortedSet<ConnectorDescriptorItemUi>> sortedConnectorsById;
	private EarlyAccessThenBiggestVersionComparator comparator;
	
	public EarlyAccessOrMostRecentVersionFilter() {
	}
	
	@Override
	public boolean select(Viewer viewer, Object parent, Object item) {
		if (! (viewer instanceof DiscoveryViewer)) {
			throw new IllegalArgumentException("This filter only applies on DiscoveryViewer");
		}
		if (! (item instanceof CatalogItem)) {
			return true;
		}

		CatalogItem desc = (CatalogItem)item;
		if (this.discoveryViewer == null || stateOutdated()) {
			initializeDiscoveryViewer((DiscoveryViewer)viewer);
		}
		// search for first (top-version) connector and returns whether it is the current item
		for (ConnectorDescriptorItemUi other : this.sortedConnectorsById.get(desc.getId())) {
			if (!other.isVisible()) {
				continue;
			}
			return other.getConnector() == desc;
		}
		return true;
	}

	/**
	 * Since this filter is stateful, it can become outdated, for example after
	 * an "updateDiscovery". This methods checks whether the state needs to be reset.
	 * 
	 * @return
	 */
	private boolean stateOutdated() {
		for (Collection<ConnectorDescriptorItemUi> knownItem : this.sortedConnectorsById.values()) {
			if (this.discoveryViewer.getAllConnectorsItemsUi().containsAll(knownItem)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sorts the initially visible connector
	 * @param viewer
	 */
	private void initializeDiscoveryViewer(final DiscoveryViewer viewer) {
		this.discoveryViewer = viewer;
		this.comparator = new EarlyAccessThenBiggestVersionComparator();
		this.sortedConnectorsById = new HashMap<>();
		final Set<ConnectorDescriptorItemUi> invisibleOnes = new HashSet<>();
		for (ConnectorDescriptorItemUi item : viewer.getAllConnectorsItemsUi()) {
			if (this.sortedConnectorsById.get(item.getConnector().getId()) == null) {
				this.sortedConnectorsById.put(item.getConnector().getId(), new TreeSet<>(this.comparator));
			}
			// Only process visible connectors synchronously. Insertion can be a long operation as it requires versions to be resolved
			if (item.isVisible()) {
				this.sortedConnectorsById.get(item.getConnector().getId()).add(item);
			} else {
				invisibleOnes.add(item);
			}
		}
		// Not visible connectors are process asynchronously to not block UI thread
		Job insertNotVisibleConnectorsJob = new Job("Insert not visible connectors") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				for (ConnectorDescriptorItemUi item : invisibleOnes) {
					sortedConnectorsById.get(item.getConnector().getId()).add(item);
				}
				return Status.OK_STATUS;
			}
		};
		insertNotVisibleConnectorsJob.setSystem(true);
		insertNotVisibleConnectorsJob.schedule();
	}
	
	
	
	private class EarlyAccessThenBiggestVersionComparator implements Comparator<ConnectorDescriptorItemUi>, Serializable {
		
		private static final long serialVersionUID = -8050934311301624177L;

		@Override
		public int compare(ConnectorDescriptorItemUi item1, ConnectorDescriptorItemUi item2) {
			if (item1 == item2) {
				return 0;
			}
			if (JBossDiscoveryUi.isEarlyAccess(item1.getConnector()) && !JBossDiscoveryUi.isEarlyAccess(item2.getConnector())) {
				return -1;
			}
			if (JBossDiscoveryUi.isEarlyAccess(item2.getConnector()) && !JBossDiscoveryUi.isEarlyAccess(item1.getConnector())) {
				return 1;
			}
			
			for (Entry<String, Version> entry : item1.getConnectorUnits().entrySet()) {
				Version otherVersion = item2.getConnectorUnits().get(entry.getKey());
				if (otherVersion == null) {
					continue;
				}
				int diffVersion = otherVersion.compareTo(entry.getValue());
				if (diffVersion != 0) {
					return diffVersion;
				}
			}
			return 0;
		}
	}

	
}
