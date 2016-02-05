/*************************************************************************************
 * Copyright (c) 2008-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.editors;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.mylyn.commons.core.DelegatingProgressMonitor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.about.InstallationDialog;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.part.PageBook;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.Messages;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;
import org.jboss.tools.central.editors.xpl.filters.HideEarlyAccessWhenRegularExistsFilter;
import org.jboss.tools.central.editors.xpl.filters.InstalledFilter;
import org.jboss.tools.central.installation.CentralInstallationPage;
import org.jboss.tools.discovery.core.internal.DiscoveryActivator;
import org.jboss.tools.discovery.core.internal.connectors.ConnectorDescriptorItemUi;
import org.jboss.tools.discovery.core.internal.connectors.JBossDiscoveryUi;

/**
 * 
 * @author snjeza
 *
 */
public class SoftwarePage extends AbstractJBossCentralPage implements IRunnableContext {

	public static final String ID = ID_PREFIX + "SoftwarePage";

	private static final String ICON_INSTALL = "/icons/repository-submit.gif";

	private Dictionary<Object, Object> environment;
	private ScrolledForm form;
	private IProgressMonitor monitor;
	private PageBook pageBook;
	private Composite loadingComposite;
	private Composite featureComposite;
	private DiscoveryViewer discoveryViewer;
	private HideEarlyAccessWhenRegularExistsFilter preferSupportedFilter = new HideEarlyAccessWhenRegularExistsFilter();
	private RefreshJobChangeListener refreshJobChangeListener;
	private InstallAction installAction = new InstallAction();
	
	private Button installButton;
	private Button uninstallButton;
	private Link selectAllButton;
	private Link deselectAllButton;

	private ToolBarManager toolBarManager;
	
	public SoftwarePage(FormEditor editor) {
		super(editor, ID, "Software/Update");
		monitor = new DelegatingProgressMonitor();
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		
		Composite body = form.getBody();
	    GridLayout gridLayout = new GridLayout(1, true);
	    gridLayout.horizontalSpacing = 7;
	    body.setLayout(gridLayout);
	    toolkit.paintBordersFor(body);
		
		createFeaturesSection(toolkit, body);
	    
	    super.createFormContent(managedForm);
		
	}

	protected void createFeaturesSection(FormToolkit toolkit, Composite parent) {
		final Section features = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR|ExpandableComposite.EXPANDED);
		features.setText("Features Available");
	    features.setLayout(new GridLayout());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    gd.widthHint = 350;
	    //gd.heightHint = 100;
	    features.setLayoutData(gd);
	    
	    createFeaturesToolbar(toolkit, features);
	    
	    featureComposite = toolkit.createComposite(features);
		featureComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		featureComposite.setLayout(new GridLayout());
		
		pageBook = new PageBook(featureComposite, SWT.NONE);
		pageBook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    discoveryViewer = new DiscoveryViewer(pageBook, this);
	    discoveryViewer.addUserFilter(new InstalledFilter(), Messages.DiscoveryViewer_Hide_installed, true);
		discoveryViewer.addSystemFilter(preferSupportedFilter);
		discoveryViewer.addDirectoryUrl(DiscoveryActivator.getDefault().getJBossDiscoveryDirectory());
		discoveryViewer.createControl();
		discoveryViewer.setEnvironment(getEnvironment());
		Control discoveryControl = discoveryViewer.getControl();
		adapt(toolkit, discoveryControl);
		if (discoveryControl instanceof Composite) {
			((Composite) discoveryControl).setLayout(new GridLayout());
		}
		discoveryControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    loadingComposite = createLoadingComposite(toolkit, pageBook);	    
		
	    form.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				GridData gridData = (GridData) featureComposite.getLayoutData();
				Point size = form.getSize();
				gridData.heightHint = size.y - 25;
				gridData.widthHint = size.x - 25;
				gridData.grabExcessVerticalSpace = true;

				gridData = (GridData) features.getLayoutData();
				gridData.heightHint = size.y - 20;
				gridData.widthHint = size.x - 20;
				gridData.grabExcessVerticalSpace = false;
				form.reflow(true);
				form.redraw();
			}
	    });

	    Composite selectionButtonsComposite = toolkit.createComposite(featureComposite);
	    selectionButtonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
	    selectionButtonsComposite.setLayout(new GridLayout(3, false));

	    // selectAll/Deselect All button would better be part of DiscoveryViewer
	    selectAllButton = new Link(selectionButtonsComposite, SWT.NONE);
	    selectAllButton.setText("<A>" + Messages.selectAll + "</A>");
	    selectAllButton.setEnabled(true);
	    selectAllButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (discoveryViewer != null && !discoveryViewer.getControl().isDisposed()) {
					discoveryViewer.selectAllVisible();
				}
			}
			
		});
	    deselectAllButton = new Link(selectionButtonsComposite, SWT.NONE);
	    deselectAllButton.setText("<A>" + Messages.deselectAll + "</A>");
	    deselectAllButton.setEnabled(true);
	    deselectAllButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (discoveryViewer != null && !discoveryViewer.getControl().isDisposed()) {
					discoveryViewer.deselectAll();
				}
			}
		});
	    discoveryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						updateInstallButton();						
					}
				});
			}
		});
	    
	    Composite installationButtonsComposite = toolkit.createComposite(featureComposite);
	    installationButtonsComposite.setLayout(new GridLayout(3, false));
	    installButton = toolkit.createButton(installationButtonsComposite, NLS.bind(Messages.installWithCount, "0"), SWT.PUSH);
	    installButton.setEnabled(false);
	    installButton.setImage(JBossCentralActivator.getDefault().getImage(ICON_INSTALL));
	    this.installButton.setText(NLS.bind(Messages.installWithCount, 99));
	    // Allow until 99 connectors since at this point, we can't already know how many connectors are actually there
	    int installWidthHint = this.installButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	    GridDataFactory.swtDefaults().hint(installWidthHint, SWT.DEFAULT).applyTo(this.installButton);
	    this.installButton.setText(NLS.bind(Messages.installWithCount, 0));
	    installButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				installAction.run();
			}
			
		});
	    
	    this.uninstallButton = toolkit.createButton(installationButtonsComposite, NLS.bind(Messages.uninstallWithCount, "0"), SWT.PUSH);
	    this.uninstallButton.setEnabled(false);
	    this.uninstallButton.setText(NLS.bind(Messages.uninstallWithCount, 99)); // Allow until 99 connectors
	    this.uninstallButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
	    int uninstallWidthHint = this.uninstallButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	    GridDataFactory.swtDefaults().hint(uninstallWidthHint, SWT.DEFAULT).applyTo(this.uninstallButton);
	    this.uninstallButton.setText(NLS.bind(Messages.uninstallWithCount, 0));
	    this.discoveryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						updateUninstallButton();						
					}
				});
			}
		});
	    this.uninstallButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JBossDiscoveryUi.uninstall(new ArrayList<ConnectorDescriptor>(discoveryViewer.getInstalledConnectors()), SoftwarePage.this, false);
			}
		});
	    final Link earlyAccessLink = new Link(installationButtonsComposite, SWT.DEFAULT);
	    earlyAccessLink.setText("<A>" + Messages.manageEarlyAccess + "</A>");
	    earlyAccessLink.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		InstallationDialog dialog = new InstallationDialog(earlyAccessLink.getShell(), getSite().getWorkbenchWindow());
	    		try {
	    			Field lastTabField = dialog.getClass().getDeclaredField("lastSelectedTabId");
	    			lastTabField.setAccessible(true);
					lastTabField.set(null, CentralInstallationPage.PAGE_ID);
    			} catch (Exception ex) {
    				JBossCentralActivator.log(ex);
    			}
	    		dialog.open();
	    	}
	    });

	    features.setClient(featureComposite);
		showLoading();
		pageBook.pack(true);
		
		RefreshDiscoveryJob refreshDiscoveryJob = RefreshDiscoveryJob.INSTANCE;
		refreshJobChangeListener = new RefreshJobChangeListener();
		refreshDiscoveryJob.addJobChangeListener(refreshJobChangeListener);
		refreshDiscoveryJob.schedule();
				
	}

	private Dictionary<Object, Object> getEnvironment() {
		if (environment == null) {
			environment = JBossCentralActivator.getEnvironment();
		}
		return environment;
	}

	private void createFeaturesToolbar(FormToolkit toolkit, Section section) {
		Composite headerComposite = toolkit.createComposite(section, SWT.NONE);
	    RowLayout rowLayout = new RowLayout();
	    rowLayout.marginTop = 0;
	    rowLayout.marginBottom = 0;
	    headerComposite.setLayout(rowLayout);
	    headerComposite.setBackground(null);
	    
	    toolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		toolBarManager.createControl(headerComposite);
		toolBarManager.add(new CheckForUpdatesAction());
		
		CommandContributionItem item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.refreshDiscovery");
		toolBarManager.add(item);

	    toolBarManager.update(true);
	    
		section.setTextClient(headerComposite);
	}
	
	private void adapt(FormToolkit toolkit, Control control) {
		toolkit.adapt(control, true, true);
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (Control c:children) {
				adapt(toolkit, c);
			}
		}
	}

	@Override
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		ModalContext.run(runnable, fork, monitor, getDisplay());
	}
	
	public boolean showLoading() {
		if (pageBook.isDisposed()) {
			return false;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				pageBook.showPage(loadingComposite);
				setBusyIndicator(loadingComposite, true);
				form.reflow(true);
				form.redraw();
			}
		});
		return true;
	}

	public boolean refresh() {
		if (pageBook == null || pageBook.isDisposed() || discoveryViewer == null || discoveryViewer.getControl() == null) {
			return false;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				pageBook.showPage(discoveryViewer.getControl());
				form.reflow(true);
				form.redraw();
				updateInstallButton();
				updateUninstallButton();
				setEnabled(deselectAllButton, true);
				setEnabled(selectAllButton, true);
			}
		});
		
		return true;
	}
	
	@Override
	public void dispose() {
		if (refreshJobChangeListener != null) {
			RefreshDiscoveryJob.INSTANCE.removeJobChangeListener(refreshJobChangeListener);
			refreshJobChangeListener = null;
		}
		if (toolBarManager != null) {
			toolBarManager.dispose();
			toolBarManager = null;
		}
		super.dispose();
	}

	public DiscoveryViewer getDiscoveryViewer() {
		return discoveryViewer;
	}

	private class RefreshJobChangeListener extends JobChangeAdapter {

		@Override
		public void done(IJobChangeEvent event) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					setBusyIndicator(loadingComposite, false);
					refresh();
				}
			});
			
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			SoftwarePage.setEnabled(installButton, false);
			SoftwarePage.setEnabled(uninstallButton, false);
			SoftwarePage.setEnabled(selectAllButton, false);
			SoftwarePage.setEnabled(deselectAllButton, false);
			showLoading();
		}

	}

	private class InstallAction extends Action {

		public InstallAction() {
			super("Install", JBossCentralActivator.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, ICON_INSTALL));
		}

		@Override
		public void run() {
			final Shell shell = getSite().getShell();
			final ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			try {
				setEnabled(false);
				SoftwarePage.setEnabled(installButton, false);
				dialog.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.setTaskName(Messages.DiscoveryViewer_waitingForDiscoveryCompletion);
						for (ConnectorDescriptorItemUi item : discoveryViewer.getAllConnectorsItemsUi()) {
							// Calling this methods waits synchronously for jobs to finish and avoid conflict
							// Cf JBIDE-17496, JBIDE-17504, Eclipse #436378
							// When we use a p2 version with bug #436378 fixed, we should remove that to save much time
							if (item.isComputingUnits()) {
								item.getConnectorUnits();
							}
						}
					}						
				});
				Set<DiscoveryConnector> toInstall = new HashSet<DiscoveryConnector>(discoveryViewer.getInstallableConnectors());
				toInstall.addAll(discoveryViewer.getUpdatableConnectors());
				Set<String> ids = new HashSet<String>();
				for (DiscoveryConnector connector : toInstall) {
					ids.add(connector.getId());
				}
				JBossDiscoveryUi.installByIds(ids, true, dialog);
			} catch (InterruptedException ex) {
                JBossCentralActivator.getDefault().getLog().log(new Status(IStatus.ERROR, JBossCentralActivator.ID, ex.getMessage(), ex));
			} catch (InvocationTargetException ite) {
                JBossCentralActivator.getDefault().getLog().log(new Status(IStatus.ERROR, JBossCentralActivator.ID, ite.getMessage(), ite));
			} finally {
				setEnabled(true);
				updateInstallButton();
			}
		}
		
	}

	private class CheckForUpdatesAction extends Action {

		public CheckForUpdatesAction() {
			super("Check for Updates", JBossCentralActivator.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, "/icons/update.gif"));
		}

		@Override
		public void run() {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
	        try {
	        	setEnabled(false);
	        	handlerService.executeCommand("org.eclipse.equinox.p2.ui.sdk.update", new Event());
	        }
	        catch (CommandException e) {
	        	JBossCentralActivator.log(e);
	        } finally {
	        	setEnabled(true);
	        }
		}
		
	}

	private void updateInstallButton() {
		if (installButton != null && !installButton.isDisposed()) {
			int installableConnectors = discoveryViewer.getInstallableConnectors().size() + discoveryViewer.getUpdatableConnectors().size();
			installButton.setEnabled(installableConnectors > 0);
			installButton.setText(NLS.bind(Messages.installWithCount, installableConnectors));
		}
	}

	private void updateUninstallButton() {
		if (uninstallButton != null && !uninstallButton.isDisposed()) {
			int installedConnectors = discoveryViewer.getInstalledConnectors().size();
			uninstallButton.setEnabled(installedConnectors > 0);
			uninstallButton.setText(NLS.bind(Messages.uninstallWithCount, installedConnectors));
		}
	}

	private static void setEnabled(Control control, boolean enabled) {
		if (control != null && !control.isDisposed()) {
			control.setEnabled(enabled);
		}
	}
}
