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

import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.dialogs.ILayoutConstants;
import org.eclipse.equinox.internal.p2.ui.viewers.IUColumnConfig;
import org.eclipse.equinox.internal.p2.ui.viewers.IUDetailsLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.jboss.tools.central.Messages;

public class CentralInstallationPage extends InstallationPage {

	//private static final int COLUMN_INDEX_ID = 0;
	//private static final int COLUMN_INDEX_LABEL = COLUMN_INDEX_ID + 1;
	//private static final int COLUMN_INDEX_VERSION = COLUMN_INDEX_LABEL + 1;
	
	public CentralInstallationPage() {
	}

	@Override
	public void createControl(Composite parent) {
		Label earlyAccessLabel = new Label(parent, SWT.WRAP);
		earlyAccessLabel.setText(Messages.EarlyAccess_Description);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
		layoutData.widthHint = 700;
		earlyAccessLabel.setLayoutData(layoutData);
		
		FilteredTree iusViewer = new FilteredTree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), false);
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
		iusViewer.getViewer().setContentProvider(new ITreeContentProvider() {
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
		});
		iusViewer.getViewer().setInput(InstallationChecker.getInstance().getEarlyAccessUnits());
		iusViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

}
