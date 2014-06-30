/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.project.examples.internal.discovery;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.RemediationOperation;
import org.eclipse.equinox.p2.operations.UninstallOperation;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.ui.UninstallRequest;
import org.eclipse.swt.widgets.Display;

public class PrepareUninstallProfileJob extends PrepareInstallProfileJob {

	public PrepareUninstallProfileJob(List<ConnectorDescriptor> connectors, UninstallRequest request) {
		super(connectors);
	}
	
	@Override
	public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
		if (installableConnectors.isEmpty()) {
			throw null;
		}

		try {
			SubMonitor monitor = SubMonitor.convert(progressMonitor, org.jboss.tools.project.examples.Messages.preparingUninstall,
					100);
			try {
				final IInstallableUnit[] ius = computeInstallableUnits(monitor.newChild(50));

				checkCancelled(monitor);

				final UninstallOperation uninstallOperation = resolveUninstall(monitor.newChild(50), ius,
						repositoryLocations.toArray(new URI[0]));
				
				checkCancelled(monitor);
				
				final RemediationOperation[] rops = new RemediationOperation[1];
				rops[0] = null;
				if (uninstallOperation != null && uninstallOperation.getResolutionResult().getSeverity() == IStatus.ERROR) {
					rops[0] = new RemediationOperation(ProvisioningUI.getDefaultUI()
							.getSession(), uninstallOperation.getProfileChangeRequest());
					rops[0].resolveModal(monitor.newChild(500));
				}

				checkCancelled(monitor);

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						provisioningUI.openUninstallWizard(Arrays.asList(ius), uninstallOperation, null);
					}
				});
			} finally {
				monitor.done();
			}
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}
	

}
