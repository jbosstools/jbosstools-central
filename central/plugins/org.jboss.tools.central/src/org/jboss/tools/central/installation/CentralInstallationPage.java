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
import org.eclipse.equinox.internal.p2.ui.dialogs.ILayoutConstants;
import org.eclipse.equinox.internal.p2.ui.viewers.IUColumnConfig;
import org.eclipse.equinox.internal.p2.ui.viewers.IUDetailsLabelProvider;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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
			Composite header = new Composite(parent, SWT.NONE);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
			layoutData.widthHint = 700;
			header.setLayoutData(layoutData);
			GridLayout headerLayout = new GridLayout(2, false);
			header.setLayout(headerLayout);
			Label warningIcon = new Label(header, SWT.NONE);
			warningIcon.setImage(parent.getDisplay().getSystemImage(SWT.ICON_WARNING));
			Label earlyAccessLabel = new Label(header, SWT.WRAP);
			earlyAccessLabel.setText(Messages.EarlyAccess_Description);
			earlyAccessLabel.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			
			
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
		
		final Button disableEarlyAccessButton = new Button(parent, SWT.PUSH);
		disableEarlyAccessButton.setText(Messages.disableEarlyAccess_title);
		disableEarlyAccessButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
		disableEarlyAccessButton.setToolTipText(Messages.disableEarlyAccess_description);
		disableEarlyAccessButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		disableEarlyAccess(disableEarlyAccessButton);
	    	}
	    });
		disableEarlyAccessButton.setEnabled(JBossCentralActivator.getDefault().getPreferences().getBoolean(PreferenceKeys.ENABLE_EARLY_ACCESS, false));
		
	}
	
	private void disableEarlyAccess(final Button disableEarlyAccessButton) {
		// remaining early-access connectors
		if (MessageDialog.openConfirm(disableEarlyAccessButton.getShell(), Messages.disableEarlyAccess_title, Messages.disableEarlyAccess_description)) {
			// remove early-access sites
			ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(disableEarlyAccessButton.getShell());
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
			JBossCentralActivator.getDefault().getPreferences().putBoolean(PreferenceKeys.ENABLE_EARLY_ACCESS, false);
			disableEarlyAccessButton.setEnabled(false);
		}
	}
}
