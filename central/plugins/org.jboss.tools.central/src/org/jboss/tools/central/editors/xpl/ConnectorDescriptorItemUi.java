/*******************************************************************************
 * Copyright (c) 2010, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Mickael Istria (Red Hat Inc.)
 *      - Extracted from {@link DiscoveryViewer} into own file
 *      - Added support for versions/updates
 *      - UI improvements
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.core.model.Overview;
import org.eclipse.mylyn.internal.discovery.ui.wizards.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.discovery.core.internal.connectors.JBossDiscoveryUi;

/**
 * Wraps a {@link DiscoveryConnector} in a UI element and provide additional
 * methods to handle versions and updates
 * @author mistria
 *
 */
public class ConnectorDescriptorItemUi implements PropertyChangeListener, Runnable {

	/**
	 * All jobs using this rule instance will be exclusive so they will use a single thread
	 */
	private static final ISchedulingRule SINGLE_JOB_RULE = new ISchedulingRule() {
		@Override
		public boolean isConflicting(ISchedulingRule arg0) {
			return arg0 == this;
		}
		
		@Override
		public boolean contains(ISchedulingRule arg0) {
			return arg0 == this;
		}
	};

	public static enum ConnectorInstallationStatus { UNKNOWN, UP_TO_DATE, UPDATE_AVAILABLE, MORE_RECENT_VERSION_INSTALLED };
	
	private DiscoveryConnector connector;
	private Map<String, org.eclipse.equinox.p2.metadata.Version> connectorUnits;
	private ConnectorInstallationStatus installationStatus = ConnectorInstallationStatus.UNKNOWN;
	private boolean visible;
	private boolean isRealConnector;

	private DiscoveryViewer discoveryViewer;
	
	private Job connectorUnitJob;

	private final Button checkbox;
	private final Label iconLabel;
	private final Label nameLabel;
	private final Label statusLabel;
	private ToolItem infoButton;
	private final Link providerLabel;
	private final Label description;
	private final Composite checkboxContainer;
	private final Composite connectorContainer;
	private final Display display;
	private Image iconImage;

	private static final String COLOR_DARK_GRAY = "DarkGray"; //$NON-NLS-1$
	private Color colorDisabled;
	private Font titleFont;
	private Image infoImage;

	private Set<Resource> disposables = new HashSet<>();

	public ConnectorDescriptorItemUi(DiscoveryViewer discoveryViewer,
			final DiscoveryConnector connector,
			Composite categoryChildrenContainer, Color background,
			Font titleFont, Image infoImage) {

		if (colorDisabled == null) {
			ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
			if (!colorRegistry.hasValueFor(COLOR_DARK_GRAY)) {
				colorRegistry.put(COLOR_DARK_GRAY, new RGB(0x69, 0x69, 0x69));
			}
			colorDisabled = colorRegistry.get(COLOR_DARK_GRAY);
		}

		this.discoveryViewer = discoveryViewer;
		this.titleFont = titleFont;
		this.connector = connector;
		this.infoImage = infoImage;

		display = categoryChildrenContainer.getDisplay();
		connector.addPropertyChangeListener(this);

		connectorContainer = new Composite(categoryChildrenContainer, SWT.NULL);

		connectorContainer.setBackground(background);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(connectorContainer);
		GridLayout layout = new GridLayout(5, false);
		layout.marginLeft = 7;
		layout.marginTop = 2;
		layout.marginBottom = 2;
		connectorContainer.setLayout(layout);

		checkboxContainer = new Composite(connectorContainer, SWT.NULL);
		checkboxContainer.setBackground(background);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).span(1, 2).applyTo(checkboxContainer);
		GridLayoutFactory.fillDefaults().spacing(1, 1).numColumns(2).applyTo(checkboxContainer);

		checkbox = new Button(checkboxContainer, SWT.CHECK);
		checkbox.setText(" "); //$NON-NLS-1$
		// help UI tests
		checkbox.setData("connectorId", connector.getId()); //$NON-NLS-1$
		checkbox.setVisible(connector.isInstallable());
		checkbox.setSelection(connector.isSelected());
		checkbox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				ConnectorDescriptorItemUi.this.discoveryViewer.showConnectorControl(ConnectorDescriptorItemUi.this);
			}
		});
		this.isRealConnector = JBossDiscoveryUi.isInstallableConnector(connector);
		checkbox.setVisible(this.isRealConnector);
		checkbox.setEnabled(this.isRealConnector);

		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.applyTo(checkbox);

		iconLabel = new Label(checkboxContainer, SWT.NULL);
		iconLabel.setBackground(background);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(iconLabel);

		if (connector.getIcon() != null) {
			iconImage = DiscoveryViewer.computeIconImage(connector.getSource(), connector.getIcon(), 32, false);
			this.disposables.add(iconImage);
			if (iconImage != null) {
				iconLabel.setImage(iconImage);
			}
		}

		nameLabel = new Label(connectorContainer, SWT.NULL);
		nameLabel.setBackground(background);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(nameLabel);
		nameLabel.setFont(this.titleFont);
		nameLabel.setText(connector.getName());

		this.statusLabel = new Label(connectorContainer, SWT.NULL);
		this.statusLabel.setBackground(background);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(this.statusLabel);
		setUpToDateStatus();
		// As resolution of version is a long operation, we create a job for that
		if (this.connector.isInstalled()) {
			createAndScheduleConnectorUnitJob();
		}

		providerLabel = new Link(connectorContainer, SWT.RIGHT);
		providerLabel.setBackground(background);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(providerLabel);
		if (connector.getCertification() != null) {
			providerLabel.setText(NLS.bind(org.jboss.tools.central.Messages.DiscoveryViewer_Certification_Label0,
					new String[] {
						connector.getProvider(),
						connector.getLicense(),
						connector.getCertification().getName() }));
			if (connector.getCertification().getUrl() != null) {
				providerLabel.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						BrowserUtil.openUrl(connector.getCertification().getUrl(), IWorkbenchBrowserSupport.AS_EXTERNAL);
					}
				});
			}
			Overview overview = new Overview();
			overview.setSummary(connector.getCertification().getDescription());
			overview.setUrl(connector.getCertification().getUrl());
			Image image = DiscoveryViewer.computeIconImage(connector.getSource(), connector.getCertification().getIcon(), 48, true);
			DiscoveryViewer.hookTooltip(providerLabel, providerLabel,
					connectorContainer, providerLabel, connector.getSource(),
					overview, image);
			disposables.add(image);
		} else {
			providerLabel.setText(NLS.bind(Messages.ConnectorDiscoveryWizardMainPage_provider_and_license, connector.getProvider(), connector.getLicense()));
		}

		if (hasTooltip(connector)) {
			ToolBar toolBar = new ToolBar(connectorContainer, SWT.FLAT);
			toolBar.setBackground(background);

			infoButton = new ToolItem(toolBar, SWT.PUSH);
			infoButton.setImage(this.infoImage);
			infoButton.setToolTipText(Messages.ConnectorDiscoveryWizardMainPage_tooltip_showOverview);
			DiscoveryViewer.hookTooltip(toolBar, infoButton,
					connectorContainer, nameLabel, connector.getSource(),
					connector.getOverview(), null);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(toolBar);
		} else {
			Label label = new Label(connectorContainer, SWT.NULL);
			label.setText(" "); //$NON-NLS-1$
			label.setBackground(background);
		}

		description = new Label(connectorContainer, SWT.NULL | SWT.WRAP);
		description.setBackground(background);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).hint(100, SWT.DEFAULT).applyTo(description);
		String descriptionText = connector.getDescription();
		int maxDescriptionLength = 162;
		if (descriptionText.length() > maxDescriptionLength) {
			descriptionText = descriptionText.substring(0, maxDescriptionLength);
		}
		description.setText(descriptionText.replaceAll("(\\r\\n)|\\n|\\r", " ")); //$NON-NLS-1$ //$NON-NLS-2$

		// always disabled color to make it less prominent
		providerLabel.setForeground(this.colorDisabled);

		if (this.isRealConnector) {
			checkbox.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean selected = checkbox.getSelection();
					maybeModifySelection(selected);
				}
			});
			MouseListener connectorItemMouseListener = new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					boolean selected = !checkbox.getSelection();
					if (maybeModifySelection(selected)) {
						checkbox.setSelection(selected);
					}
				}
			};
			checkboxContainer.addMouseListener(connectorItemMouseListener);
			connectorContainer.addMouseListener(connectorItemMouseListener);
			iconLabel.addMouseListener(connectorItemMouseListener);
			nameLabel.addMouseListener(connectorItemMouseListener);
			// the provider has clickable links
			// providerLabel.addMouseListener(connectorItemMouseListener);
			description.addMouseListener(connectorItemMouseListener);
		}
	}

	/**
	 * Need asynchronous as it's long-running
	 */
	private void createAndScheduleConnectorUnitJob() {
		this.connectorUnitJob = new Job("Computing connector status") {
			private boolean cancelled;
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				ConnectorDescriptorItemUi.this.connectorUnits = resolveConnectorUnits(connector);
				if (connector.isInstalled() && ConnectorDescriptorItemUi.this.connectorUnits != null) {
					Map<String, org.eclipse.equinox.p2.metadata.Version> profileUnits = resolveProfileUnits(connector.getInstallableUnits());
					if (profileUnits != null && !profileUnits.isEmpty()) {
						for (String unitId : connector.getInstallableUnits()) {
							Version version = profileUnits.get(unitId);
							Version connectorUnitVersion = ConnectorDescriptorItemUi.this.connectorUnits.get(unitId);
							if (connectorUnitVersion == null) {
								JBossCentralActivator.log("Could not resolve remote IU '" + unitId + "' in repository '" + connector.getSiteUrl() + "'");
							} else if (version != null) {
								int compare = version.compareTo(connectorUnitVersion); 
								if (compare == 0) {
									ConnectorDescriptorItemUi.this.installationStatus = ConnectorInstallationStatus.UP_TO_DATE;
									discoveryViewer.modifySelection(ConnectorDescriptorItemUi.this, ConnectorDescriptorItemUi.this.connector.isSelected());
									break;
								} else if (compare <= 0) {
									ConnectorDescriptorItemUi.this.installationStatus = ConnectorInstallationStatus.UPDATE_AVAILABLE;
									// default enables update, so no need to change it
									break;
								} else if (compare >= 0) {
									ConnectorDescriptorItemUi.this.installationStatus = ConnectorInstallationStatus.MORE_RECENT_VERSION_INSTALLED;
									discoveryViewer.modifySelection(ConnectorDescriptorItemUi.this, ConnectorDescriptorItemUi.this.connector.isSelected());
									break;
								}
							}
						}
					}
					if (this.cancelled) {
						return Status.CANCEL_STATUS;
					}
					if (!ConnectorDescriptorItemUi.this.checkboxContainer.isDisposed()) {
						ConnectorDescriptorItemUi.this.checkboxContainer.getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {
								setUpToDateStatus();							
							}
						});
					}
				}
				return Status.CANCEL_STATUS;
			}
			
			@Override
			protected void canceling() {
				this.cancelled = true;
			}
		};
		this.connectorUnitJob.setSystem(true);
		// As the output of this job is more interesting for installed threads,
		// we put lower (==more important) priority on installed jobs
		// In case of join in UIThread, priority must be raised to limit freeze duration.
		if (this.connector.isInstalled()) {
			this.connectorUnitJob.setPriority(Job.SHORT);
		} else {
			this.connectorUnitJob.setPriority(Job.LONG);
		}
		// All jobs use the same thread
		this.connectorUnitJob.setRule(SINGLE_JOB_RULE);
		this.connectorUnitJob.belongsTo(this.discoveryViewer);
		this.connectorUnitJob.schedule();
	}

	/**
	 * @param connector
	 */
	private void setUpToDateStatus() {
		if (!this.connector.isInstalled() || this.statusLabel.isDisposed() || getControl().isDisposed()) {
			return;
		}
		String text = "(INSTALLED";
		if (this.installationStatus == ConnectorInstallationStatus.UP_TO_DATE) {
			text += " - UP TO DATE";
			this.statusLabel.setForeground(getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		} else if (this.installationStatus == ConnectorInstallationStatus.UPDATE_AVAILABLE) {
			text += " - UPDATE AVAILABLE";
			this.statusLabel.setForeground(getControl().getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
		} else if (this.installationStatus == ConnectorInstallationStatus.MORE_RECENT_VERSION_INSTALLED) {
			text += " - MORE RECENT VERSION INSTALLED";
			this.statusLabel.setForeground(getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		}
		text += ")";
		this.statusLabel.setText(text);
		this.statusLabel.getParent().layout(new Control[] { this.statusLabel });
	}

	private static Map<String, org.eclipse.equinox.p2.metadata.Version> resolveProfileUnits(List<String> installableUnits) {
		IProvisioningAgentProvider provider = (IProvisioningAgentProvider) PlatformUI.getWorkbench().getService(IProvisioningAgentProvider.class);
		if (provider == null) {
			// This is not expected to happen, but it actually does when shutting down IDE: JBIDE-18368
			// so we just catch it there to avoid logging a NPE.
			return null;
		}
		try {
			IProvisioningAgent agent = provider.createAgent(null); // null = location for running system
			if (agent == null)
				throw new RuntimeException("Location was not provisioned by p2");
			IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
			if (profileRegistry == null) {
				throw new RuntimeException("Unable to acquire the profile registry service.");
			}
			IProfile profile = profileRegistry.getProfile(IProfileRegistry.SELF);
			Map<String, org.eclipse.equinox.p2.metadata.Version> res = new HashMap<>();
			for (String installableUnit : installableUnits) {
				IQueryResult<IInstallableUnit> queryResult = profile.query(QueryUtil.createIUQuery(installableUnit), new NullProgressMonitor());
				for (IInstallableUnit unit : queryResult) {
					org.eclipse.equinox.p2.metadata.Version previousVersion = res.get(installableUnit);
					if (previousVersion == null 	|| previousVersion.compareTo(unit.getVersion()) < 0) {
						res.put(installableUnit, unit.getVersion());
					}
				}
			}
			return res;
		} catch (ProvisionException ex) {
			JBossCentralActivator.getDefault().getLog().log(new Status(
					IStatus.ERROR,
					JBossCentralActivator.PLUGIN_ID,
					ex.getMessage(),
					ex));
			return null;
		}
	}

	/**
	 * This is a long-running operation! Don't call it in UI Thread.
	 * @param connector
	 * @return the map of id/version for installation units or null if connector repo can't be resolved.
	 */
	private static Map<String, org.eclipse.equinox.p2.metadata.Version> resolveConnectorUnits(	ConnectorDescriptor connector) {
		IMetadataRepository repo = P2CachedRepoUtil.getRepoForConnector(connector);
		if (repo == null) {
			return null;
		}
		Map<String, org.eclipse.equinox.p2.metadata.Version> res = new HashMap<>();
		for (String unitId : connector.getInstallableUnits()) {
			IQueryResult<IInstallableUnit> queryResult = repo.query(QueryUtil.createIUQuery(unitId), new NullProgressMonitor());
			for (IInstallableUnit unit : queryResult) {
				org.eclipse.equinox.p2.metadata.Version previousVersion = res.get(unitId);
				if (previousVersion == null || previousVersion.compareTo(unit.getVersion()) < 0) {
					res.put(unitId, unit.getVersion());
				}
			}
		}
		return res;
	}

	/**
	 * 
	 * @return whether the connector is up-to-date. Ie whether a new version for
	 *         the included IUs exist on the associated site.
	 */
	public boolean isUpToDate() {
		return this.installationStatus == ConnectorInstallationStatus.MORE_RECENT_VERSION_INSTALLED
				|| this.installationStatus == ConnectorInstallationStatus.UP_TO_DATE;
	}

	protected boolean maybeModifySelection(boolean selected) {
		if (!this.isRealConnector || !checkbox.isEnabled()) {
			return false;
		}

		if (selected) {
			if (!connector.isInstalled() && !connector.isInstallable()) {
				if (connector.getInstallMessage() != null) {
					MessageDialog.openInformation(this.checkbox.getShell(),
							Messages.DiscoveryViewer_Install_Connector_Title,
							connector.getInstallMessage());
				}
				return false;
			}
			if (connector.getAvailable() != null && !connector.getAvailable()) {
				MessageDialog.openWarning(this.checkbox.getShell(),
								Messages.ConnectorDiscoveryWizardMainPage_warningTitleConnectorUnavailable,
								NLS.bind(Messages.ConnectorDiscoveryWizardMainPage_warningMessageConnectorUnavailable, connector.getName()));
				return false;
			}
		}
		this.discoveryViewer.modifySelection(this, selected);
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		display.asyncExec(this);
	}

	@Override
	public void run() {
		if (!connectorContainer.isDisposed()) {
			updateAvailability();
		}
	}

	public void updateAvailability() {
		boolean enabled = !connector.isInstalled()	&& (connector.getAvailable() == null || connector.getAvailable());

		checkbox.setEnabled(this.isRealConnector);
		nameLabel.setEnabled(enabled);
		providerLabel.setEnabled(enabled);
		description.setEnabled(enabled);
		Color foreground;
		if (enabled) {
			foreground = connectorContainer.getForeground();
		} else {
			foreground = this.colorDisabled;
		}
		nameLabel.setForeground(foreground);
		description.setForeground(foreground);

		if (iconImage != null) {
			iconLabel.setImage(iconImage);
		}
	}

	void select(boolean select) {
		if (!checkbox.isDisposed() && checkbox.isVisible()	&& checkbox.getSelection() != select) {
			checkbox.setSelection(select);
			this.connector.setSelected(select);
			maybeModifySelection(select);
		}
	}

	boolean hasTooltip(final DiscoveryConnector connector) {
		return connector.getOverview() != null
				&& connector.getOverview().getSummary() != null
				&& connector.getOverview().getSummary().length() > 0;
	}

	public void dispose() {
		if (this.connectorUnitJob != null) {
			this.connectorUnitJob.cancel();
		}
		for (Resource resource : this.disposables) {
			resource.dispose();
		}
	}

	public Control getControl() {
		return this.connectorContainer;
	}

	public DiscoveryConnector getConnector() {
		return this.connector;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		((GridData) this.connectorContainer.getLayoutData()).exclude = !visible;
		this.connectorContainer.setVisible(visible);
	}

	public boolean isVisible() {
		// Use a field there to avoid called to UI elements and UI thread
		return this.visible;
	}
	
	/**
	 * This is a synchronous, potentially long-running operation!
	 * @return the map of connector units
	 */
	public Map<String, Version> getConnectorUnits() {
		if (this.connectorUnitJob == null) {
			createAndScheduleConnectorUnitJob();
		}
		if (isComputingUnits()) {
			try {
				// on request, raise priority to maximal
				this.connectorUnitJob.setPriority(Job.INTERACTIVE);
				this.connectorUnitJob.join();
			} catch (InterruptedException ex) {
				JBossCentralActivator.getDefault().getLog().log(new Status(IStatus.ERROR,
					JBossCentralActivator.PLUGIN_ID,
					ex.getMessage(),
					ex));
			}
		}
		return this.connectorUnits;
	}
	
	public boolean isComputingUnits() {
		return this.connectorUnitJob != null && this.connectorUnitJob.getState() != Job.NONE;
	}
}
