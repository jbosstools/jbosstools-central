/*************************************************************************************
 * Copyright (c) 2013-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.discovery.core.internal.connectors;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryFeedbackJob;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.mylyn.internal.discovery.ui.InstalledItem;
import org.eclipse.mylyn.internal.discovery.ui.UninstallRequest;
import org.eclipse.mylyn.internal.discovery.ui.wizards.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;

/**
 * 
 * Based on org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi
 * 
 * @author snjeza
 * @since 1.5.3
 */
public class JBossDiscoveryUi {
	
	//private static final String MPC_CORE_PLUGIN_ID = "org.eclipse.epp.mpc.core"; //$NON-NLS-1$
	
	public static final class PreferenceKeys {
		/**
		 * If it's only for reading, use {@link JBossDiscoveryUi#isEarlyAccessEnabled()} instead
		 */
		public static final String ENABLE_EARLY_ACCESS = "enableEarlyAccess";
		private static final boolean ENABLE_EARLY_ACCESS_DEFAULT_VALUE = false;
	}

	public static boolean isEarlyAccessEnabled() {
		return DiscoveryActivator.getDefault().getPreferences().getBoolean(JBossDiscoveryUi.PreferenceKeys.ENABLE_EARLY_ACCESS, JBossDiscoveryUi.PreferenceKeys.ENABLE_EARLY_ACCESS_DEFAULT_VALUE);
	}
	
	/**
	 * Install the specified connectors.
	 * @param descriptors the specified connectors. Those must have actual content to install.
	 * @param context
	 * @return true if installation performed successfully
	 */
	public static boolean install(Collection<ConnectorDescriptor> descriptors, IRunnableContext context) {
		for (ConnectorDescriptor toInstall : descriptors) {
			if (toInstall.getInstallableUnits() == null || toInstall.getInstallableUnits().isEmpty()) {
				return false;
			}
		}
		try {
			IRunnableWithProgress runner = createInstallJob(descriptors);
			if (context != null) {
				context.run(true, true, runner);
			} else {
				runner.run(new NullProgressMonitor());
			}

			// update stats
			new DiscoveryFeedbackJob(descriptors instanceof List ? (List<ConnectorDescriptor>)descriptors : new ArrayList<>(descriptors)).schedule();
			recordInstalled(descriptors);
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, DiscoveryActivator.PLUGIN_ID, NLS.bind(
					Messages.ConnectorDiscoveryWizard_installProblems, new Object[] { e.getCause().getMessage() }),
					e.getCause());
			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
			return false;
		} catch (InterruptedException e) {
			// canceled
			return false;
		}
		return true;
	}
	
	public static boolean uninstall(final List<ConnectorDescriptor> descriptors, IRunnableContext context, boolean fork) {
		try {
			UninstallRequest request = new UninstallRequest() {
				@Override
				public boolean select(InstalledItem item) {
					for (ConnectorDescriptor desc : descriptors) {
						for (String id : desc.getInstallableUnits()) {
							if (id.equals(desc.getId())) {
								return true;
							}
						}
					}
					return false;
				}
			};
			PrepareUninstallProfileJob runner = new PrepareUninstallProfileJob(descriptors, request);
			context.run(fork, true, runner);

			// update stats
			DiscoveryFeedbackJob discoveryFeedbackJob = new DiscoveryFeedbackJob(descriptors);
			discoveryFeedbackJob.schedule();
			if (!fork) {
				discoveryFeedbackJob.join();
				return discoveryFeedbackJob.getResult().isOK();
			}
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, DiscoveryActivator.PLUGIN_ID, NLS.bind(
					Messages.ConnectorDiscoveryWizard_installProblems, new Object[] { e.getCause().getMessage() }),
					e.getCause());
			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
			return false;
		} catch (InterruptedException e) {
			// canceled
			return false;
		}
		return true;
	}
	
	
	public static PrepareInstallProfileJob createInstallJob(Collection<ConnectorDescriptor> descriptors) {
		return new PrepareInstallProfileJob(descriptors);
	}
	
	private static void recordInstalled(Collection<ConnectorDescriptor> descriptors) {
		StringBuilder sb = new StringBuilder();
		for (ConnectorDescriptor descriptor : descriptors) {
			UsageEventType eventType = DiscoveryActivator.getDefault().getInstallSoftwareEventType();
			UsageReporter.getInstance().trackEvent(eventType.event(descriptor.getId()));

			if (sb.length() > 0) {
				sb.append(","); //$NON-NLS-1$
			}
			sb.append(descriptor.getId());
		}
		ScopedPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), DiscoveryUi.ID_PLUGIN);
		store.putValue(DiscoveryUi.PREF_LAST_INSTALLED, sb.toString());
		try {
			store.save();
		} catch (IOException e) {
			// ignore
		}
	}

	public static boolean isEarlyAccess(ConnectorDescriptor connector) {
		String cert = connector.getCertificationId();
		return cert != null && cert.toLowerCase().contains("earlyaccess");
	}
	
	public static boolean isInstallableConnector(final ConnectorDescriptor connector) {
		return connector.getCertificationId() == null || !connector.getCertificationId().toLowerCase().contains("notavailable");
	}


	/**
	 * 
	 * @param connectorsId
	 * @param context
	 * @return the set of resolved connector. In case a connector wasn't resolved, it's simply absent in the set, so you
	 *         should check size or the output for completeness. 
	 */
	public static Collection<ConnectorDescriptor> resolveToConnectors(Collection<String> connectorsId, IRunnableContext context) {
		Map<String, ConnectorDescriptor> res = new HashMap<>();
		final ConnectorDiscovery catalog = DiscoveryUtil.createConnectorDiscovery();
		try {
			context.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					catalog.performDiscovery(monitor);
				}
			});
		} catch (InterruptedException ex) {
			DiscoveryActivator.getDefault().getLog().log(new Status(IStatus.ERROR, DiscoveryActivator.getDefault().getBundle().getSymbolicName(), ex.getMessage(), ex));
		} catch (InvocationTargetException ex) {
			DiscoveryActivator.getDefault().getLog().log(new Status(IStatus.ERROR, DiscoveryActivator.getDefault().getBundle().getSymbolicName(), ex.getMessage(), ex));
		}
		for (ConnectorDescriptor connector : catalog.getConnectors()) {
			if (connectorsId.contains(connector.getId()) &&
				connector.getInstallableUnits() != null &&
				!connector.getInstallableUnits().isEmpty() &&
				isInstallableConnector(connector)) {
				if (isEarlyAccessEnabled() && isEarlyAccess(connector)) {
					res.put(connector.getId(), connector);
				} else if (!res.containsKey(connector.getId()) /*no early access so far*/) {
					res.put(connector.getId(), connector);
				}
			}
		}
		if (res.size() != connectorsId.size()) {
			Set<String> missingConnectoes = new HashSet<>(connectorsId);
			missingConnectoes.removeAll(res.keySet());
			DiscoveryActivator.getDefault().getLog().log(new Status(
				IStatus.ERROR,
				DiscoveryActivator.getDefault().getBundle().getSymbolicName(),
				"Could not resolve the following connectorIds to catalog entries: " + missingConnectoes.toString())); //$NON-NLS-1$
		}
		return res.values();
	}
	

	/**
	 * Installed the specified connectors by id. The visibility/availability of the connectors
	 * takes into account the state of discovery (EA enabled, not installable connectors...)
	 * @param connectorIds
	 * @param context
	 */
	public static boolean installByIds(Collection<String> connectorIds, IRunnableContext context) {
		Collection<ConnectorDescriptor> connectorsToInstall = resolveToConnectors(connectorIds, context);
		if (connectorsToInstall == null || connectorsToInstall.size() != connectorIds.size()) {
			return false;
		}
		return install(connectorsToInstall, context);
	}

}
