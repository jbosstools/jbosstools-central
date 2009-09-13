package org.jboss.tools.maven.seam.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.maven.seam.MavenSeamActivator;

public class SeamConfiguratorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	
	private Button configureSeamButton;
	private Button configureSeamRuntimeButton;
	private Button configureSeamArtifactsButton;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		
		configureSeamButton = new Button(composite,SWT.CHECK);
		configureSeamButton.setText("Configure Seam when importing Maven projects");
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeam = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM);
		configureSeamButton.setSelection(configureSeam);
		
		configureSeamRuntimeButton = new Button(composite,SWT.CHECK);
		configureSeamRuntimeButton.setText("Configure Seam Runtime");
		boolean configureSeamRuntime = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME);
		configureSeamRuntimeButton.setSelection(configureSeamRuntime);
		configureSeamRuntimeButton.setEnabled(configureSeam);
		
		configureSeamArtifactsButton = new Button(composite,SWT.CHECK);
		configureSeamArtifactsButton.setText("Configure Seam Artifacts (view folder, model source folder, package ...)");
		boolean configureSeamArtifacts = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS);
		configureSeamArtifactsButton.setSelection(configureSeamArtifacts);
		configureSeamArtifactsButton.setEnabled(configureSeam);
		
		configureSeamButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				configureSeamRuntimeButton.setEnabled(configureSeamButton.getSelection());
				configureSeamArtifactsButton.setEnabled(configureSeamButton.getSelection());
			}
		
		});
		
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		configureSeamButton.setSelection(MavenSeamActivator.CONFIGURE_SEAM_VALUE);
		configureSeamRuntimeButton.setSelection(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME_VALUE);
		configureSeamArtifactsButton.setSelection(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS_VALUE);
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM, MavenSeamActivator.CONFIGURE_SEAM_VALUE);
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME, MavenSeamActivator.CONFIGURE_SEAM_RUNTIME_VALUE);
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS, MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS_VALUE);
		configureSeamRuntimeButton.setEnabled(configureSeamButton.getSelection());
		configureSeamArtifactsButton.setEnabled(configureSeamButton.getSelection());
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM, configureSeamButton.getSelection());
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME, configureSeamRuntimeButton.getSelection());
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS, configureSeamArtifactsButton.getSelection());
		return super.performOk();
	}

		
}
