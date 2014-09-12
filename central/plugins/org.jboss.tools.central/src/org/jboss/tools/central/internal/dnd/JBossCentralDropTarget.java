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
package org.jboss.tools.central.internal.dnd;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.project.examples.internal.discovery.DiscoveryUtil;
import org.jboss.tools.project.examples.internal.discovery.JBossDiscoveryUi;

/**
 * 
 * @author snjeza
 *
 */
public class JBossCentralDropTarget {

	public static final String JBOSS_DROP_TARGET = "jbossDropTarget"; //$NON-NLS-1$
	public static final String JBOSS_DROP_TARGET_ID = "jdt"; //$NON-NLS-1$
	private static final String DOWNLOAD_JBOSS_ORG_JBOSSTOOLS_CENTRAL_INSTALL_CONNECTORS = "http://download.jboss.org/jbosstools/central/install?connectors="; //$NON-NLS-1$
	private static final String LEGACY_DEVSTUDIO_JBOSS_COM_CENTRAL_INSTALL_CONNECTORS = "https://devstudio.jboss.com/central/install?connectors="; //$NON-NLS-1$
	private static final String DEVSTUDIO_REDHAT_COM_CENTRAL_INSTALL_CONNECTORS = "https://devstudio.redhat.com/central/install?connectors="; //$NON-NLS-1$
	
	private DropTargetListener listener = new DropTargetAdapter() {
		@Override
		public void dragEnter(DropTargetEvent e) {
			if (e.detail == DND.DROP_NONE) {
				e.detail = DND.DROP_LINK;
			}
		}

		@Override
		public void dragOperationChanged(DropTargetEvent e) {
			if (e.detail == DND.DROP_NONE) {
				e.detail = DND.DROP_LINK;
			}
		}

		@Override
		public void drop(DropTargetEvent event) {
			if (event.data == null) {
				event.detail = DND.DROP_NONE;
				return;
			}
			String url = getUrlFromEvent(event);
			if (url != null) {
				url = url.trim();
				final String[] connectorIds = new String[1];
				if (url.startsWith(DOWNLOAD_JBOSS_ORG_JBOSSTOOLS_CENTRAL_INSTALL_CONNECTORS)) {
					connectorIds[0] = url.replace(DOWNLOAD_JBOSS_ORG_JBOSSTOOLS_CENTRAL_INSTALL_CONNECTORS, "");
				} else if (url.startsWith(DEVSTUDIO_REDHAT_COM_CENTRAL_INSTALL_CONNECTORS)) {
					connectorIds[0] = url.replace(DEVSTUDIO_REDHAT_COM_CENTRAL_INSTALL_CONNECTORS, "");
				} else if (url.startsWith(LEGACY_DEVSTUDIO_JBOSS_COM_CENTRAL_INSTALL_CONNECTORS)) {
					connectorIds[0] = url.replace(LEGACY_DEVSTUDIO_JBOSS_COM_CENTRAL_INSTALL_CONNECTORS, "");
				}
				
				if (connectorIds[0] != null && !connectorIds[0].trim().isEmpty()) {
					Display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							dropConnectors(connectorIds[0]);
						}
					});
					
				} else {
					if (event.data instanceof String) {
						String[] urls = ((String)event.data).split(System.getProperty("line.separator")); //$NON-NLS-1$
						for (String fn:urls) {
							String file;
							try {
								URL u = new URL(fn);
								file = u.getFile();
							} catch (MalformedURLException e1) {
								file = fn;
							}
							if (new File(file).exists()) {
								final IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(file));
								Display.getCurrent().asyncExec(new Runnable() {
									@Override
									public void run() {
										IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
										try {
											IDE.openEditorOnFileStore(page, fileStore);
										} catch (PartInitException e) {
											// silently ignore problems opening the editor
										}
									}
								});
							}
						}
					}
				}
			}
		}

		private String getUrlFromEvent(DropTargetEvent event) {
			String eventData = (String) event.data;
			String[] dataLines = eventData.split(System.getProperty("line.separator")); //$NON-NLS-1$
			String url = dataLines[0];
			return url;
		}
	};
	
	public JBossCentralDropTarget(final Control control) {
		final DropTarget target = new DropTarget(control, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		Transfer[] transfers;
		if (LinuxURLTransfer.isLinuxGTK()) {
			transfers = new Transfer[] { URLTransfer.getInstance(), LinuxURLTransfer.getInstance() };
		} else {
			transfers = new Transfer[] { URLTransfer.getInstance() };
		}
		target.setTransfer(transfers);
		addListener(target);
	}
	
	public JBossCentralDropTarget(DropTarget target) {
		Assert.isNotNull(target);
		Object object = ((DropTarget)target).getData(JBossCentralDropTarget.JBOSS_DROP_TARGET_ID);
		if (JBossCentralDropTarget.JBOSS_DROP_TARGET.equals(object)) {
			return;
		}
		boolean hasUrlTransfer = false;
		Transfer[] transfers = target.getTransfer();
		for (Transfer transfer : transfers) {
			if (transfer instanceof URLTransfer) {
				hasUrlTransfer = true;
				break;
			}
		}
		Transfer[] newTransfers = null;
		if (!hasUrlTransfer) {
			if (LinuxURLTransfer.isLinuxGTK()) {
				newTransfers = new Transfer[transfers.length + 2];
				System.arraycopy(transfers, 0, newTransfers, 2, transfers.length);
				newTransfers[0] = URLTransfer.getInstance();
				newTransfers[1] = LinuxURLTransfer.getInstance();
			} else {
				newTransfers = new Transfer[transfers.length + 1];
				System.arraycopy(transfers, 0, newTransfers, 1, transfers.length);
				newTransfers[0] = URLTransfer.getInstance();
			}
		} else {
			if (LinuxURLTransfer.isLinuxGTK()) {
				newTransfers = new Transfer[transfers.length + 1];
				System.arraycopy(transfers, 0, newTransfers, 1, transfers.length);
				newTransfers[1] = LinuxURLTransfer.getInstance();
			}
		}
		if (newTransfers != null) {
			target.setTransfer(newTransfers);
		}
		addListener(target);
	}
	
	public void addListener(DropTarget target) {
		target.setData(JBOSS_DROP_TARGET_ID, JBOSS_DROP_TARGET);
		target.addDropListener(listener);
	}

	private void install(final Set<String> connectorIds) throws InvocationTargetException, InterruptedException {
		if (connectorIds == null || connectorIds.isEmpty()) {
			JBossCentralActivator.log("No connectors selected for installation");
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
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		dialog.run(true, true, runnable);
		if (results[0] == null) {
			return;
		}
		if (results[0].isOK()) {
			List<DiscoveryConnector> connectors = connectorDiscoveries[0].getConnectors();
			List<ConnectorDescriptor> installableConnectors = new ArrayList<ConnectorDescriptor>();
			List<String> notFoundConnectors = new ArrayList<String>(connectorIds);
			for (DiscoveryConnector connector:connectors) {
				if (connectorIds.contains(connector.getId())) {
					installableConnectors.add(connector);
					notFoundConnectors.remove(connector.getId());
				} 
			}
			Set<String> installedFeatures = JBossDiscoveryUi.createInstallJob(installableConnectors).getInstalledFeatures(new NullProgressMonitor());
			Set<ConnectorDescriptor> installedConnectors = new HashSet<ConnectorDescriptor>();
			Iterator<ConnectorDescriptor> iter = installableConnectors.iterator();
			while (iter.hasNext()) {
				ConnectorDescriptor connector = iter.next();
				connector.setInstalled(installedFeatures != null
						&& installedFeatures.containsAll(connector.getInstallableUnits()));
				if (connector.isInstalled()) {
					installedConnectors.add(connector);
					iter.remove();
				} 
			}
			
			StringBuilder buffer = new StringBuilder();
			if (!notFoundConnectors.isEmpty()) {
				buffer.append( "The following connectors can not be found:\n");
				for (String id:notFoundConnectors) {
					buffer.append(" - ");
					buffer.append(id);
					buffer.append("\n");
				}
				buffer.append("\n");
			}
			if (!installedConnectors.isEmpty()) {
				buffer.append( "The following connectors are already installed:\n");
				formatConnectors(installedConnectors, buffer);
				buffer.append("\n");
			}
			boolean continueInstallation = !installableConnectors.isEmpty();
			if (!buffer.toString().isEmpty()) {
				if (continueInstallation) {
					buffer.append("Would you like to proceed with the installation of:\n");
					formatConnectors(installableConnectors, buffer);
					continueInstallation = MessageDialog.openQuestion(getShell(), "Install New Software", buffer.toString());
				} else {
					MessageDialog.openInformation(getShell(), "Install New Software", buffer.toString());
				}
			}
			if (continueInstallation) {
				JBossDiscoveryUi.install(installableConnectors, dialog);
			}
		} else {
			String message = results[0].toString();
			switch (results[0].getSeverity()) {
			case IStatus.ERROR:	
				MessageDialog.openError(getShell(), "Error", message);
				break;
			case IStatus.WARNING:
				MessageDialog.openWarning(getShell(), "Warning", message);
				break;
			case IStatus.INFO:
				MessageDialog.openInformation(getShell(), "Information", message);
				break;
			}
		}
	}

	private void formatConnectors(Collection<ConnectorDescriptor> installedConnectors,
			StringBuilder buffer) {
		for (ConnectorDescriptor cd:installedConnectors) {
			buffer.append(" - ");
			buffer.append(cd.getId());
			buffer.append(" : ");
			buffer.append(cd.getName());
			buffer.append("\n");
		}
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}

	private void dropConnectors(String connectorIds) {
		String[] ids = connectorIds.trim().split(",");
		if (ids != null && ids.length > 0) {
			Set<String> idSet = new HashSet<String>();
			for (String id : ids) {
				id = id.trim();
				if (!id.isEmpty()) {
					idSet.add(id);
				}
			}
			if (!idSet.isEmpty()) {
				try {
					install(idSet);
				} catch (InvocationTargetException e) {
					JBossCentralActivator.log(e);
				} catch (InterruptedException e) {
					JBossCentralActivator.log(e);
				}
			}
		}
	}

	}
