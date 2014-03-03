/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.discovery;

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
import org.eclipse.mylyn.internal.discovery.ui.AbstractInstallJob;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.mylyn.internal.discovery.ui.wizards.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;
import org.jboss.tools.foundation.core.FoundationCorePlugin;
import org.jboss.tools.foundation.core.usage.IUsageTracker;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.osgi.framework.ServiceReference;

/**
 * 
 * Based on org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi
 * 
 * @author snjeza
 * @since 1.5.3
 */
public class JBossDiscoveryUi {
	
	private static final String MPC_CORE_PLUGIN_ID = "org.eclipse.epp.mpc.core"; //$NON-NLS-1$

	public static boolean install(List<ConnectorDescriptor> descriptors, IRunnableContext context) {
		try {
			IRunnableWithProgress runner = createInstallJob(descriptors);
			context.run(true, true, runner);

			// update stats
			new DiscoveryFeedbackJob(descriptors).schedule();
			recordInstalled(descriptors);
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID, NLS.bind(
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
	
	public static AbstractInstallJob createInstallJob(List<ConnectorDescriptor> descriptors) {
		return new PrepareInstallProfileJob(descriptors);
	}
	
	private static void recordInstalled(List<ConnectorDescriptor> descriptors) {
		StringBuilder sb = new StringBuilder();
		for (ConnectorDescriptor descriptor : descriptors) {
			if (sb.length() > 0) {
				sb.append(","); //$NON-NLS-1$
			}
			sb.append(descriptor.getId());
			FoundationCorePlugin.getDefault().getUsageTrackerService().sendLiveEvent(IUsageTracker.CATEGORY_CENTRAL, IUsageTracker.ACTION_INSTALLED_SOFTWARE, descriptor.getId());
		}
		ScopedPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), DiscoveryUi.ID_PLUGIN);
		store.putValue(DiscoveryUi.PREF_LAST_INSTALLED, sb.toString());
		try {
			store.save();
		} catch (IOException e) {
			ProjectExamplesActivator.log(e);
		}
		
	}

}
