package org.jboss.tools.maven.seam.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.maven.seam.MavenSeamActivator;

public class SeamConfiguratorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	
	private Button button;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		
		button = new Button(composite,SWT.CHECK);
		button.setText("Configure Seam when importing Maven projects");
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeam = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM);
		button.setSelection(configureSeam);
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		button.setSelection(MavenSeamActivator.CONFIGURE_SEAM_VALUE);
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM, MavenSeamActivator.CONFIGURE_SEAM_VALUE);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM, button.getSelection());
		return super.performOk();
	}

		
}
