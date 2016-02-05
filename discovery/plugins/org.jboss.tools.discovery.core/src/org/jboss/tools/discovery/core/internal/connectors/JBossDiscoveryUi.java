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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryFeedbackJob;
import org.eclipse.mylyn.internal.discovery.core.model.Icon;
import org.eclipse.mylyn.internal.discovery.core.model.Overview;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.mylyn.internal.discovery.ui.InstalledItem;
import org.eclipse.mylyn.internal.discovery.ui.UninstallRequest;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;
import org.jboss.tools.discovery.core.internal.Messages;
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
					org.eclipse.mylyn.internal.discovery.ui.wizards.Messages.ConnectorDiscoveryWizard_installProblems, new Object[] { e.getCause().getMessage() }),
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
					org.eclipse.mylyn.internal.discovery.ui.wizards.Messages.ConnectorDiscoveryWizard_installProblems, new Object[] { e.getCause().getMessage() }),
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
	 * @param interactive Whether user is asked for choice. If false, will automatically select best one according to EA state
	 * @param context
	 * @return the set of resolved connector. In case a connector wasn't resolved, it's simply absent in the set, so you
	 *         should check size or the output for completeness. 
	 */
	public static Collection<ConnectorDescriptor> resolveToConnectors(Collection<String> connectorsId, boolean interactive, IRunnableContext context) {
		final ConnectorDiscovery catalog = DiscoveryUtil.createConnectorDiscovery();
		IShellProvider shellProvider = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
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
		boolean hasEarlyAccessContent = false;
		Set<ConnectorDescriptor> res = new HashSet<>();
		Set<String> notResolved = new HashSet<>();
		for (String connectorId : connectorsId) {
			ConnectorDescriptor toInstall = resolveSingleConnector(catalog, connectorId, interactive, shellProvider);
			if (toInstall != null) {
				hasEarlyAccessContent |= JBossDiscoveryUi.isEarlyAccess(toInstall);
				res.add(toInstall);
			} else {
				notResolved.add(connectorId);
			}
		}

		if (hasEarlyAccessContent) {
			if (MessageDialog.openConfirm(shellProvider.getShell(), Messages.SoftwarePage_earlyAccessSection_Title, Messages.SoftwarePage_earlyAccessSection_message)) {
				DiscoveryActivator.getDefault().getPreferences().putBoolean(JBossDiscoveryUi.PreferenceKeys.ENABLE_EARLY_ACCESS, true);
			} else {
				return null;
			}
		}
		
		if (!notResolved.isEmpty()) {
			DiscoveryActivator.getDefault().getLog().log(new Status(
				IStatus.ERROR,
				DiscoveryActivator.getDefault().getBundle().getSymbolicName(),
				"Could not resolve the following connectorIds to catalog entries: " + notResolved.toString())); //$NON-NLS-1$
		}
		
		return res;
	}

	private static ConnectorDescriptor resolveSingleConnector(final ConnectorDiscovery catalog, String connectorId,
			boolean interactive, IShellProvider shellProvider) {
		Set<DiscoveryConnector> alternatives = new HashSet<>();
		for (DiscoveryConnector otherConnector : catalog.getConnectors()) {
			if (otherConnector.getId().equals(connectorId)) {
				alternatives.add(otherConnector);
			}
		}
		ConnectorDescriptor toInstall = null;
		if (alternatives.size() == 1) {
			toInstall = alternatives.iterator().next();
		} else if (alternatives.size() > 1) {
			if (interactive) {
				SelectConnectorDialog selectConnectorDialog = new SelectConnectorDialog(shellProvider, alternatives);
				if (selectConnectorDialog.open() == Dialog.OK) {
					toInstall = selectConnectorDialog.getSelectedConnector();
				} else {
					return null;
				}
			} else {
				for (ConnectorDescriptor alternative : alternatives) {
					if (alternative.getInstallableUnits() != null &&
						!alternative.getInstallableUnits().isEmpty() &&
						isInstallableConnector(alternative)) {
						if (isEarlyAccess(alternative)) {
							if (isEarlyAccessEnabled()) {
								// always prefer EA connector when EA available
								toInstall = alternative;
							}
							// but never use EA connector when EA not enabled
						} else if (toInstall == null) /*no better connector so far*/ {
							toInstall = alternative;
						}
					}
				}
			}
		}
		return toInstall;
	}
	

	/**
	 * Installed the specified connectors by id. The visibility/availability of the connectors
	 * takes into account the state of discovery (EA enabled, not installable connectors...)
	 * @param connectorIds
	 * @param interactive whether user is asked to choose when multiple flavours of same connector exist
	 * @param context
	 */
	public static boolean installByIds(Collection<String> connectorIds, boolean interactive, IRunnableContext context) {
		Collection<ConnectorDescriptor> connectorsToInstall = resolveToConnectors(connectorIds, interactive, context);
		if (connectorsToInstall == null || connectorsToInstall.size() != connectorIds.size()) {
			return false;
		}
		return install(connectorsToInstall, context);
	}
	
	public static Image computeIconImage(AbstractDiscoverySource discoverySource, Icon icon, int dimension, boolean fallback) {
		String imagePath;
		switch (dimension) {
		case 64:
			imagePath = icon.getImage64();
			if (imagePath != null || !fallback) {
				break;
			}
		case 48:
			imagePath = icon.getImage48();
			if (imagePath != null || !fallback) {
				break;
			}
		case 32:
			imagePath = icon.getImage32();
			break;
		default:
			throw new IllegalArgumentException();
		}
		if (imagePath != null && imagePath.length() > 0) {
			URL resource = discoverySource.getResource(imagePath);
			if (resource != null) {
				ImageDescriptor descriptor = ImageDescriptor.createFromURL(resource);
				Image image = descriptor.createImage();
				if (image != null) {
					return image;
				}
			}
		}
		return null;
	}

	public static void hookTooltip(final Control parent, final Widget tipActivator, final Control exitControl,
			final Control titleControl, AbstractDiscoverySource source, Overview overview, Image image) {
		final OverviewToolTip toolTip = new OverviewToolTip(parent, source, overview, image);
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseHover:
					toolTip.show(titleControl);
					break;
				case SWT.Dispose:
				case SWT.MouseWheel:
					toolTip.hide();
					break;
				}
	
			}
		};
		tipActivator.addListener(SWT.Dispose, listener);
		tipActivator.addListener(SWT.MouseWheel, listener);
		if (image != null) {
			tipActivator.addListener(SWT.MouseHover, listener);
		}
		Listener selectionListener = new Listener() {
			public void handleEvent(Event event) {
				toolTip.show(titleControl);
			}
		};
		tipActivator.addListener(SWT.Selection, selectionListener);
		Listener exitListener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseWheel:
					toolTip.hide();
					break;
				case SWT.MouseExit:
					/*
					 * Check if the mouse exit happened because we move over the tooltip
					 */
					Rectangle containerBounds = exitControl.getBounds();
					Point displayLocation = exitControl.getParent().toDisplay(containerBounds.x, containerBounds.y);
					containerBounds.x = displayLocation.x;
					containerBounds.y = displayLocation.y;
					if (containerBounds.contains(Display.getCurrent().getCursorLocation())) {
						break;
					}
					toolTip.hide();
					break;
				}
			}
		};
		hookRecursively(exitControl, exitListener);
	}
	
	private static void hookRecursively(Control control, Listener listener) {
		control.addListener(SWT.Dispose, listener);
		control.addListener(SWT.MouseHover, listener);
		control.addListener(SWT.MouseMove, listener);
		control.addListener(SWT.MouseExit, listener);
		control.addListener(SWT.MouseDown, listener);
		control.addListener(SWT.MouseWheel, listener);
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				hookRecursively(child, listener);
			}
		}
	}

}
