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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
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

	public static boolean install(List<ConnectorDescriptor> descriptors, IRunnableContext context) {
		try {
			IRunnableWithProgress runner = createInstallJob(descriptors);
			context.run(true, true, runner);

			// update stats
			new DiscoveryFeedbackJob(descriptors).schedule();
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
	
	
	public static PrepareInstallProfileJob createInstallJob(List<ConnectorDescriptor> descriptors) {
		return new PrepareInstallProfileJob(descriptors);
	}
	
	private static void recordInstalled(List<ConnectorDescriptor> descriptors) {
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

}
