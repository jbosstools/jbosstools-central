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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.jboss.tools.central.editors.xpl.ConnectorDescriptorItemUi;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;

/**
 * Hides all visible {@link ConnectorDescriptor} that have another visible
 * {@link ConnectorDescriptor} which contains higher versions of p2 units.
 * @author mistria
 *
 */
public class MostRecentVersionFilter extends ViewerFilter {

	private DiscoveryViewer discoveryViewer;
	private Map<String, SortedSet<ConnectorDescriptorItemUi>> sortedConnectorsById;
	private BiggestVersionComparator comparator;
	
	public MostRecentVersionFilter() {
	}
	
	@Override
	public boolean select(Viewer viewer, Object parent, Object item) {
		if (! (viewer instanceof DiscoveryViewer)) {
			throw new IllegalArgumentException("This filter only applies on DiscoveryViewer");
		}
		if (! (item instanceof ConnectorDescriptor)) {
			return true;
		}

		ConnectorDescriptor desc = (ConnectorDescriptor)item;
		if (this.discoveryViewer == null) {
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
	 * Sorts the initially visible connector
	 * @param viewer
	 */
	private void initializeDiscoveryViewer(final DiscoveryViewer viewer) {
		this.discoveryViewer = viewer;
		this.comparator = new BiggestVersionComparator();
		this.sortedConnectorsById = new HashMap<String, SortedSet<ConnectorDescriptorItemUi>>();
		final Set<ConnectorDescriptorItemUi> invisibleOnes = new HashSet<ConnectorDescriptorItemUi>();
		for (ConnectorDescriptorItemUi item : viewer.getAllConnectorsItemsUi()) {
			if (this.sortedConnectorsById.get(item.getConnector().getId()) == null) {
				this.sortedConnectorsById.put(item.getConnector().getId(), new TreeSet<ConnectorDescriptorItemUi>(this.comparator));
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
	
	
	
}
