/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.installation;

import java.net.URI;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.actions.UninstallAction;
import org.eclipse.equinox.internal.p2.ui.dialogs.ILayoutConstants;
import org.eclipse.equinox.internal.p2.ui.viewers.IUColumnConfig;
import org.eclipse.equinox.internal.p2.ui.viewers.IUDetailsLabelProvider;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.Messages;
import org.jboss.tools.central.preferences.PreferenceKeys;

public class CentralInstallationPage extends InstallationPage {

	//private static final int COLUMN_INDEX_ID = 0;
	//private static final int COLUMN_INDEX_LABEL = COLUMN_INDEX_ID + 1;
	//private static final int COLUMN_INDEX_VERSION = COLUMN_INDEX_LABEL + 1;
	public static final String PAGE_ID = "org.jboss.tools.central.installation.centralInstallationPage"; //$NON-NLS-1$
	
	private static final class ArrayTreeContentProvider implements ITreeContentProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return new ArrayContentProvider().getElements(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
		}
	}

	private Set<IInstallableUnit> earlyAccessUnits;
	private Set<String> earlyAccessSites;
	private FilteredTree iusViewer;
	private InstallationChecker installChecker;

	public CentralInstallationPage() {
	}

	@Override
	public void createControl(Composite parent) {
		final Button enableEarlyAccessButton = new Button(parent, SWT.CHECK);
		enableEarlyAccessButton.setText(Messages.DiscoveryViewer_Enable_EarlyAccess);
		enableEarlyAccessButton.setToolTipText(Messages.EarlyAccess_Description);
		enableEarlyAccessButton.setSelection(JBossCentralActivator.getDefault().getPreferences().getBoolean(PreferenceKeys.ENABLE_EARLY_ACCESS, PreferenceKeys.ENABLE_EARLY_ACCESS_DEFAULT_VALUE));
		enableEarlyAccessButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		handleEarlyAccessChanged(enableEarlyAccessButton);
	    	}
	    });
		
		installChecker = null;
		try {
			installChecker = InstallationChecker.getInstance();
		} catch (ProvisionException ex) {
			JBossCentralActivator.getDefault().getLog().log(new Status(IStatus.ERROR,
					JBossCentralActivator.PLUGIN_ID,
					ex.getMessage(),
					ex));
		}

		{
			Label earlyAccessLabel = new Label(parent, SWT.WRAP);
			earlyAccessLabel.setText(Messages.EarlyAccess_Description);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
			layoutData.widthHint = 700;
			earlyAccessLabel.setLayoutData(layoutData);
			
			iusViewer = new FilteredTree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), false);
			IUColumnConfig[] columnsConfig = new IUColumnConfig[] {new IUColumnConfig(ProvUIMessages.ProvUI_NameColumnTitle, IUColumnConfig.COLUMN_NAME, ILayoutConstants.DEFAULT_PRIMARY_COLUMN_WIDTH), new IUColumnConfig(ProvUIMessages.ProvUI_VersionColumnTitle, IUColumnConfig.COLUMN_VERSION, ILayoutConstants.DEFAULT_SMALL_COLUMN_WIDTH), new IUColumnConfig(ProvUIMessages.ProvUI_IdColumnTitle, IUColumnConfig.COLUMN_ID, ILayoutConstants.DEFAULT_COLUMN_WIDTH), new IUColumnConfig(ProvUIMessages.ProvUI_ProviderColumnTitle, IUColumnConfig.COLUMN_PROVIDER, ILayoutConstants.DEFAULT_COLUMN_WIDTH)};
			iusViewer.getViewer().setLabelProvider(new IUDetailsLabelProvider(iusViewer, columnsConfig, parent.getShell()));
			// copied from AvailableIUGroup
			Tree tree = iusViewer.getViewer().getTree();
			tree.setHeaderVisible(true);
			for (IUColumnConfig column : columnsConfig) {
				TreeColumn tc = new TreeColumn(tree, SWT.NONE);
				tc.setResizable(true);
				tc.setText(column.getColumnTitle());
				tc.setWidth(column.getWidthInPixels(tree));
			}
			iusViewer.getViewer().setContentProvider(new ArrayTreeContentProvider());
			earlyAccessUnits = installChecker.getEarlyAccessUnits();
			iusViewer.getViewer().setInput(earlyAccessUnits);
			iusViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		
		{
			Label earlyAccessSitesLabel = new Label(parent, SWT.WRAP);
			earlyAccessSitesLabel.setText(Messages.EarlyAccessSites_Description);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
			layoutData.widthHint = 700;
			earlyAccessSitesLabel.setLayoutData(layoutData);
			FilteredTree sitesViewer = new FilteredTree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), false);
			sitesViewer.getViewer().setContentProvider(new ArrayTreeContentProvider());
			this.earlyAccessSites = installChecker.getActiveEarlyAccessURLs(new NullProgressMonitor());
			sitesViewer.getViewer().setInput(this.earlyAccessSites);
		}
		
	}
	
	/**
	 * @param checkbox
	 */
	private void handleEarlyAccessChanged(final Button checkbox) {
		if (checkbox.getSelection()) {
			if (MessageDialog.openConfirm(checkbox.getShell(), Messages.SoftwarePage_earlyAccessSection_Title, Messages.SoftwarePage_earlyAccessSection_message)) {
				JBossCentralActivator.getDefault().getPreferences().putBoolean(PreferenceKeys.ENABLE_EARLY_ACCESS, true);
			} else {
				checkbox.setSelection(false);
			}
		} else {
			ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(checkbox.getShell());
			// remaining early-access connectors
			if (MessageDialog.openConfirm(checkbox.getShell(), Messages.disableEarlyAccess_title, Messages.disableEarlyAccess_description)) {
				// remove early-access sites
				IProvisioningAgent agent = (IProvisioningAgent)JBossCentralActivator.getDefault().getService(IProvisioningAgent.SERVICE_NAME);
				final IMetadataRepositoryManager metadataRepositoryManager = (IMetadataRepositoryManager)agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
				IArtifactRepositoryManager artifactsitoryManager = (IArtifactRepositoryManager)agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
				for (String site : this.earlyAccessSites) {
					try {
						URI repoUri = new URI(site);
						metadataRepositoryManager.removeRepository(repoUri);
						artifactsitoryManager.removeRepository(repoUri);
					} catch (Exception ex) {
						JBossCentralActivator.getDefault().getLog().log(new Status(IStatus.ERROR,
							JBossCentralActivator.PLUGIN_ID,
							ex.getMessage(),
							ex));
					}
				}
				
				// TODO wrap this in some Progress Monitor
//					iusViewer.getViewer().setSelection(new StructuredSelection(this.earlyAccessUnits.toArray()));
//					UninstallAction uninstallAction = new UninstallAction(ProvisioningUI.getDefaultUI(), this.iusViewer.getViewer(), ProvisioningUI.getDefaultUI().getProfileId()) {
//						@Override
//						public void run() {
//							super.run();
//							if (getReturnCode() == Window.OK)
//								getPageContainer().closeModalContainers();
//						}
//					};
//					uninstallAction.run();
				JBossCentralActivator.getDefault().getPreferences().putBoolean(PreferenceKeys.ENABLE_EARLY_ACCESS, false);
			} else {
				checkbox.setSelection(true);
			}
		}
	}

}
