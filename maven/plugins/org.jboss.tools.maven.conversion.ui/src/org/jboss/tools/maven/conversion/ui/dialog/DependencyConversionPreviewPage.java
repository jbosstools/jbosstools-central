package org.jboss.tools.maven.conversion.ui.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class DependencyConversionPreviewPage extends WizardPage {

	private Button deleteJarsBtn;

	private boolean deleteJars;
	
	protected DependencyConversionPreviewPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setEnabled(true);
		setControl(container);
		
		deleteJarsBtn = addCheckButton(container, "Delete local project jars after conversion", deleteJars);

	}

	private Button addCheckButton(Composite container, String label,
			boolean selected) {
		Button checkBtn = new Button(container, SWT.CHECK);
		checkBtn.setText(label);
		checkBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		checkBtn.setSelection(selected);
		return checkBtn;
	}
	
	@Override
	public boolean isPageComplete() {
		return false;
	}
	
	public boolean isDeleteJars() {
		return deleteJars;
	}

}
