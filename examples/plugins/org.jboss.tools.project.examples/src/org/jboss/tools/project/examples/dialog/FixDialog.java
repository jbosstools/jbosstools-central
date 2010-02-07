package org.jboss.tools.project.examples.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;

public class FixDialog extends Dialog {

	private TableViewer tableViewer;
	private List<ProjectFix> fixes;
	
	public FixDialog(Shell parentShell, IStructuredSelection selection) {
		super(parentShell);
		Iterator iterator = selection.iterator();
		fixes = new ArrayList<ProjectFix>();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof Project) {
				Project project = (Project) object;
				fixes.addAll(project.getUnsatisfiedFixes());
			}
			
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 300;
		contents.setLayoutData(gd);
		contents.setLayout(new GridLayout());
		getShell().setText("Fixes");
		applyDialogFont(contents);
		initializeDialogUnits(area);

		Label fixesLabel = new Label(contents, SWT.NULL);
		fixesLabel.setText("Fixes:");
		tableViewer = new TableViewer(contents, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER | SWT.SINGLE);
		Table table = tableViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnNames = new String[] { "Type", "Short description" };
		int[] columnWidths = new int[] { 200, 350 };

		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setText(columnNames[i]);
			tc.setWidth(columnWidths[i]);
		}

		tableViewer.setLabelProvider(new FixLabelProvider());
		tableViewer.setContentProvider(new FixContentProvider(fixes));
		tableViewer.setInput(fixes);

		Label descriptionLabel = new Label(contents, SWT.NONE);
		descriptionLabel.setText("Description:");
		final Text description = new Text(contents, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.heightHint=50;
		description.setLayoutData(gd);
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						description.setText(""); //$NON-NLS-1$
						ISelection selection = event.getSelection();
						if (selection instanceof IStructuredSelection) {
							Object fix = ((IStructuredSelection) selection).getFirstElement();
							if (fix instanceof ProjectFix) {
								description.setText(((ProjectFix) fix).getProperties().get(ProjectFix.DESCRIPTION));
							}
						}
					}

				});
		
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}
	
	private class FixLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof ProjectFix) {
				ProjectFix fix = (ProjectFix) element;
				if (columnIndex == 0) {
					return fix.getType();
				}
				if (columnIndex == 1) {
					return fix.getProperties().get(ProjectFix.SHORT_DESCRIPTION);
				}
			}
			return null;
		}
	}
	
	private class FixContentProvider implements IStructuredContentProvider {

		private List<ProjectFix> fixes;

		public FixContentProvider(List<ProjectFix> fixes) {
			this.fixes = fixes;
		}
		
		public Object[] getElements(Object inputElement) {
			return fixes.toArray();
		}

		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}
		
	}

}
