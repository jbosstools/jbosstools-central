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
import org.jboss.tools.maven.seam.Messages;

public class SeamConfiguratorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	
	private Button configureSeamButton;
	private Button configureSeamRuntimeButton;
	private Button configureSeamArtifactsButton;
	private Button configureJSFButton;
	private Button configurePortletButton;
	private Button configureJSFPortletButton;
	private Button configureSeamPortletButton;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		
		configureSeamButton = new Button(composite,SWT.CHECK);
		configureSeamButton.setText(Messages.SeamConfiguratorPreferencePage_Configure_Seam_when_importing_Maven_projects);
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeam = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM);
		configureSeamButton.setSelection(configureSeam);
		
		configureSeamRuntimeButton = new Button(composite,SWT.CHECK);
		configureSeamRuntimeButton.setText(Messages.SeamConfiguratorPreferencePage_Configure_Seam_Runtime);
		boolean configureSeamRuntime = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME);
		configureSeamRuntimeButton.setSelection(configureSeamRuntime);
		configureSeamRuntimeButton.setEnabled(configureSeam);
		
		configureSeamArtifactsButton = new Button(composite,SWT.CHECK);
		configureSeamArtifactsButton.setText(Messages.SeamConfiguratorPreferencePage_Configure_Seam_Artifacts);
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
		
		configureJSFButton = new Button(composite,SWT.CHECK);
		configureJSFButton.setText(Messages.SeamConfiguratorPreferencePage_Configure_JSF_facet);
		boolean configureJSF = store.getBoolean(MavenSeamActivator.CONFIGURE_JSF);
		configureJSFButton.setSelection(configureJSF);
		
		configurePortletButton = new Button(composite,SWT.CHECK);
		configurePortletButton.setText(Messages.SeamConfiguratorPreferencePage_Configure_JBoss_Portlet_Core_facet);
		boolean configurePortlet = store.getBoolean(MavenSeamActivator.CONFIGURE_PORTLET);
		configurePortletButton.setSelection(configurePortlet);
		
		configureJSFPortletButton = new Button(composite,SWT.CHECK);
		configureJSFPortletButton.setText(Messages.SeamConfiguratorPreferencePage_Configure_JBoss_JSF_Portlet_facet);
		boolean configureJSFPortlet = store.getBoolean(MavenSeamActivator.CONFIGURE_JSFPORTLET);
		configureJSFPortletButton.setSelection(configureJSFPortlet);
		
		configureSeamPortletButton = new Button(composite,SWT.CHECK);
		configureSeamPortletButton.setText(Messages.SeamConfiguratorPreferencePage_Configure_JBoss_Seam_Portlet_facet);
		boolean configureSeamPortlet = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAMPORTLET);
		configureJSFPortletButton.setSelection(configureSeamPortlet);
		
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		configureJSFButton.setSelection(MavenSeamActivator.CONFIGURE_JSF_VALUE);
		configureSeamButton.setSelection(MavenSeamActivator.CONFIGURE_SEAM_VALUE);
		configureSeamRuntimeButton.setSelection(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME_VALUE);
		configureSeamArtifactsButton.setSelection(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS_VALUE);
		configurePortletButton.setSelection(MavenSeamActivator.CONFIGURE_PORTLET_VALUE);
		configureJSFPortletButton.setSelection(MavenSeamActivator.CONFIGURE_JSFPORTLET_VALUE);
		configureSeamPortletButton.setSelection(MavenSeamActivator.CONFIGURE_SEAMPORTLET_VALUE);
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM, MavenSeamActivator.CONFIGURE_SEAM_VALUE);
		store.setValue(MavenSeamActivator.CONFIGURE_JSF, MavenSeamActivator.CONFIGURE_JSF_VALUE);
		store.setValue(MavenSeamActivator.CONFIGURE_JSFPORTLET, MavenSeamActivator.CONFIGURE_JSFPORTLET_VALUE);
		store.setValue(MavenSeamActivator.CONFIGURE_SEAMPORTLET, MavenSeamActivator.CONFIGURE_SEAMPORTLET_VALUE);
		store.setValue(MavenSeamActivator.CONFIGURE_PORTLET, MavenSeamActivator.CONFIGURE_PORTLET_VALUE);
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
		store.setValue(MavenSeamActivator.CONFIGURE_PORTLET, configurePortletButton.getSelection());
		store.setValue(MavenSeamActivator.CONFIGURE_JSF, configureJSFButton.getSelection());
		store.setValue(MavenSeamActivator.CONFIGURE_JSFPORTLET, configureJSFPortletButton.getSelection());
		store.setValue(MavenSeamActivator.CONFIGURE_SEAMPORTLET, configureSeamPortletButton.getSelection());
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME, configureSeamRuntimeButton.getSelection());
		store.setValue(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS, configureSeamArtifactsButton.getSelection());
		return super.performOk();
	}

		
}
