/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.central.Messages;

/**
 * Shows all available {@link UserFilterEntry} and let user selects the one to
 * enable/disable
 * @author mistria
 *
 */
public class FiltersSelectionDialog extends Dialog {

	private Collection<UserFilterEntry> filters;
	private Set<UserFilterEntry> toggledFilters;

	public FiltersSelectionDialog(Shell parentShell, Collection<UserFilterEntry> filters) {
		super(parentShell);
		this.filters = filters;
		this.toggledFilters = new HashSet<>();
	}
	
	public Control createDialogArea(Composite parent) {
		getShell().setText(Messages.DiscoveryViewer_FilterSelectionDialog_title);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.DiscoveryViewer_FilterSelectionDialog_label);
		for (final UserFilterEntry filter : filters) {
			final Button checkbox = new Button(composite, SWT.CHECK);
			checkbox.setText(filter.getLabel());
			checkbox.setSelection(filter.isEnabled());
			checkbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (checkbox.getSelection() == filter.isEnabled()) {
						FiltersSelectionDialog.this.toggledFilters.remove(filter);
					} else {
						FiltersSelectionDialog.this.toggledFilters.add(filter);
					}
				}
			});
			GridData layoutData = new GridData();
			layoutData.horizontalIndent = 15;
			checkbox.setLayoutData(layoutData);
		}
		return composite;
	}
	
	public Set<UserFilterEntry> getToggledFilters() {
		return this.toggledFilters;
	}

}
