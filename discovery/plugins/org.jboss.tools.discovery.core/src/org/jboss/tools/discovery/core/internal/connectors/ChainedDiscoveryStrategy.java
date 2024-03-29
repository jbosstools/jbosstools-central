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
package org.jboss.tools.discovery.core.internal.connectors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.discovery.AbstractDiscoveryStrategy;
import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
import org.eclipse.equinox.internal.p2.discovery.model.Certification;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;


/**
 * 
 * @author Fred Bricon
 *
 */
@SuppressWarnings("restriction")
public class ChainedDiscoveryStrategy extends AbstractDiscoveryStrategy  {

	private List<AbstractDiscoveryStrategy> strategies;
	private DataCollector dataCollector;
	
	public ChainedDiscoveryStrategy(DataCollector dataCollector){
		this.dataCollector = dataCollector;
		strategies = new ArrayList<AbstractDiscoveryStrategy>();
	}
	
	public ChainedDiscoveryStrategy addStrategy(AbstractDiscoveryStrategy strategy) {
		strategies.add(strategy);
		return this;
	}
	
	@Override
	public void performDiscovery(IProgressMonitor monitor) throws CoreException {
		if (strategies.isEmpty()) {
			throw new IllegalStateException("At least one AbstractDiscoveryStrategy must be added");
		}
		
		MultiStatus status = new MultiStatus(DiscoveryActivator.PLUGIN_ID, 0,
				"All attempts to discover connectors have failed", null);
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		for (AbstractDiscoveryStrategy ds : strategies) {
			try {
				if (monitor.isCanceled()) {
					break;
				}
				ds.performDiscovery(monitor);
				dataCollector.collectData(ds);
				if (dataCollector.isComplete()) {
					break;
				}
			} catch (Exception e) {
				status.add(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN, NLS.bind(
						"Failed to get connectors from {0}", ds.getClass()
								.getSimpleName()), e));		
			}
		}
		if (status.getChildren().length == strategies.size()) {
			throw new CoreException(status);
		}
		if (status.getChildren().length > 0) {
			DiscoveryActivator.getDefault().getLog().log(status);
		}
	}
	
	@Override
	public void setCategories(List<CatalogCategory> categories) {
		for (AbstractDiscoveryStrategy ds : strategies) 
		  ds.setCategories(categories);
	}
	
	@Override
	public void setItems(List<CatalogItem> connectors) {
		for (AbstractDiscoveryStrategy ds : strategies) 
		  ds.setItems(connectors);
	}

	@Override
	public void setCertifications(List<Certification> certifications) {
		for (AbstractDiscoveryStrategy ds : strategies) 
  		  ds.setCertifications(certifications);
	}
	
	/**
	 * Collects data while {@link ChainedDiscoveryStrategy} iterates over {@link AbstractDiscoveryStrategy}s
	 *  
	 * @author Fred Bricon
	 *
	 */
	public interface DataCollector {
		
		
		void collectData(AbstractDiscoveryStrategy ds);
		
		/**
		 * Indicates if collection is complete
		 * 
		 * @return
		 */
		boolean isComplete();
		
	}
	
	public final static class DiscoveryConnectorCollector implements DataCollector {
		
		/**
		 * Use for testing purposes only
		 */
		public static String ALLOW_DUPLICATE_DISCOVERY_CONNECTORS_KEY = "org.jboss.tools.discovery.allow.duplicate.connectors";//$NON-NLS-1$
		
		private boolean isComplete;
		
		private boolean allowDuplicates;

		/**
		 * Creates a new DiscoveryConnectorCollector instance which allows duplicate connectors if the <code>org.jboss.tools.discovery.allow.duplicate.connectors</code> system property exists and is set to true 
		 */
		public DiscoveryConnectorCollector() {
			this(Boolean.getBoolean(ALLOW_DUPLICATE_DISCOVERY_CONNECTORS_KEY));
		}		
		
		/**
		 * Creates a new DiscoveryConnectorCollector instance. If <code>allowDuplicates</code> is true, duplicate connectors can be collected. 
		 */
		public DiscoveryConnectorCollector(boolean allowDuplicates) {
			this.allowDuplicates = allowDuplicates;
		}
		
		@Override
		public boolean isComplete() {
			return isComplete && !allowDuplicates;
		}

		@Override
		public void collectData(AbstractDiscoveryStrategy ds) {
			List<CatalogItem> collected = ds.getItems();
			isComplete = (collected != null && !collected.isEmpty());
		}		
	}
	
}
