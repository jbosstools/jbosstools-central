package org.jboss.tools.maven.ui.wizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.jboss.tools.maven.ui.Activator;

public class ConfigureMavenRepositoriesWizard extends Wizard implements
		INewWizard {

	private ConfigureMavenRepositoriesWizardPage page;

	public ConfigureMavenRepositoriesWizard() {
		super();
		setWindowTitle("Maven Repositories");
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		initializeDefaultPageImageDescriptor();
	}

	private void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						"icons/repo_wiz.gif"); //$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}
	
	@Override
	public boolean performFinish() {
		page.finishPage();
		return true;
	}

	@Override
	public void addPages() {
		page = new ConfigureMavenRepositoriesWizardPage();
		addPage(page);
	}
}
