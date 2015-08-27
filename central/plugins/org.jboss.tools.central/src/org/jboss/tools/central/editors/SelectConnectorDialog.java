/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.editors;

import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.jboss.tools.central.Messages;
import org.jboss.tools.central.editors.xpl.ConnectorDescriptorItemUi;

/**
 * This dialog gives the opportunity to user to choose a single connector amongst a giveb list
 * @author mistria
 *
 */
public class SelectConnectorDialog extends Dialog {

	private Set<DiscoveryConnector> alternatives;
	private ConnectorDescriptorItemUi selectedConnector;

	protected SelectConnectorDialog(IShellProvider shellProvider, Set<DiscoveryConnector> alternatives) {
		super(shellProvider);
		this.alternatives = alternatives;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.DiscoveryViewer_selectConnectorFlavor_title);
		Composite res = new Composite(parent, SWT.NONE);
		res.setLayout(new GridLayout(1, false));
		
		Label selectConnectorFlavorMessage = new Label(res, SWT.NONE);
		selectConnectorFlavorMessage.setText(Messages.DiscoveryViewer_selectConnectorFlavor_description);
		
		boolean firstItem = true;
		for (DiscoveryConnector connector : alternatives) {
			// Would be good to reuse same UI as in Viewer there
			final ConnectorDescriptorItemUi ui = new ConnectorDescriptorItemUi(null, connector, parent, parent.getBackground(), null, null, SWT.RADIO);
			ui.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.widget).getSelection()) {
						if (selectedConnector != null && e.data != selectedConnector) {
							selectedConnector.setSelection(false);
						}
						if (ui.getSelection()) {
							SelectConnectorDialog.this.selectedConnector = ui;
						}
					}
				}
			});
			if (firstItem) {
				firstItem = false;
				ui.setSelection(true);
				this.selectedConnector = ui;
			}
		}
		return res;
	}
	
	public DiscoveryConnector getSelectedConnector() {
		return this.selectedConnector.getConnector();
	}

}
