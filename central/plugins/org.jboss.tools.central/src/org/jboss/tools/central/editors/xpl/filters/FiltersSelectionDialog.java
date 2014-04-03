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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.central.Messages;

public class FiltersSelectionDialog extends Dialog {

	private Collection<FilterEntry> filters;
	private Set<FilterEntry> toggledFilters;

	public FiltersSelectionDialog(Shell parentShell, Collection<FilterEntry> filters) {
		super(parentShell);
		this.filters = filters;
		this.toggledFilters = new HashSet<FilterEntry>();
	}
	
	public Control createDialogArea(Composite parent) {
		getShell().setText(Messages.DiscoveryViewer_FilterSelectionDialog_title);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.DiscoveryViewer_FilterSelectionDialog_label);
		for (final FilterEntry filter : filters) {
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
	
	public Set<FilterEntry> getToggledFilters() {
		return this.toggledFilters;
	}

}
