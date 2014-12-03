/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.fixes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.discovery.core.internal.connectors.DiscoveryUtil;
import org.jboss.tools.discovery.core.internal.connectors.JBossDiscoveryUi;
import org.jboss.tools.project.examples.fixes.AbstractUIHandler;
import org.jboss.tools.project.examples.fixes.IProjectExamplesFix;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;

public class PluginFixUIHandler extends AbstractUIHandler {

	
	@Override
	public void handleDownloadRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix) {
		if (!fix.isSatisfied() && fix instanceof PluginFix) {
			PluginFix pluginFix = (PluginFix) fix;
			Collection<String> connectorIds = pluginFix.getConnectorIDs();
			try {
				discoverAndInstall(shell, context, connectorIds);
			} catch (InvocationTargetException | InterruptedException e) {
				ProjectExamplesActivator.log(e);
			}
		}
	}

	private void discoverAndInstall(Shell shell, IRunnableContext context, Collection<String> connectorIds) throws InvocationTargetException, InterruptedException {
		if (connectorIds== null || connectorIds.isEmpty()){
			return;
		}
		final IStatus[] results = new IStatus[1];
		final ConnectorDiscovery[] connectorDiscoveries = new ConnectorDiscovery[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ConnectorDiscovery connectorDiscovery = DiscoveryUtil.createConnectorDiscovery();
				connectorDiscoveries[0] = connectorDiscovery;
				results[0] = connectorDiscoveries[0].performDiscovery(monitor);
				if (monitor.isCanceled()) {
					results[0] = Status.CANCEL_STATUS;
				}
			}
		};
		context.run(true, true, runnable);
		if (results[0] == null) {
			return;
		}
		if (results[0].isOK()) {
			List<DiscoveryConnector> connectors = connectorDiscoveries[0].getConnectors();
			List<ConnectorDescriptor> installableConnectors = new ArrayList<ConnectorDescriptor>();
			for (DiscoveryConnector connector:connectors) {
				if (connectorIds.contains(connector.getId())) {
					installableConnectors.add(connector);
				}
			}
			JBossDiscoveryUi.install(installableConnectors, context);
		} else {
			String message = results[0].toString();
			switch (results[0].getSeverity()) {
			case IStatus.ERROR:	
				MessageDialog.openError(shell, "Error", message); //$NON-NLS-1$
				break;
			case IStatus.WARNING:
				MessageDialog.openWarning(shell, "Warning", message); //$NON-NLS-1$
				break;
			case IStatus.INFO:
				MessageDialog.openInformation(shell, "Information", message); //$NON-NLS-1$
				break;
			}
		}
	}

	@Override
	public void decorateInstallButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor) {
		if (button != null && !button.isDisposed()) {
			button.setEnabled(false);
			button.setToolTipText(""); //$NON-NLS-1$
		}
	}

	@Override
	public void decorateDownloadButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor) {
		if (button != null && !button.isDisposed()) {
			super.decorateInstallButton(button, fix, monitor);
			button.setToolTipText("Install required feature(s)"); //$NON-NLS-1$
		}
	}

}
