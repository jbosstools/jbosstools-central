package org.jboss.tools.project.examples.wizard;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.internal.ui.typehierarchy.ShowQualifiedTypeNamesAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;

public class NewProjectExamplesReadyPage extends WizardPage {
	
	private static final String SHOW_THE_QUICK_FIX_DIALOG = "Show the Quick Fix dialog";
	private static final String SHOW_README_FILE_FOR_FURTHER_INSTRUCTIONS = "Show readme file for further instructions";
	private Button showReadme;
	private List<ProjectExample> projectExamples;
	private Button showQuickFix;

	public NewProjectExamplesReadyPage(List<ProjectExample> projectExamples) {
		super("org.jboss.tools.project.examples.ready"); //$NON-NLS-1$
        setImageDescriptor( ProjectExamplesActivator.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID, "icons/new_wiz.gif")); //$NON-NLS-1$
        this.projectExamples = projectExamples;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		composite.setLayoutData(gd);
		Dialog.applyDialogFont(composite);
		setControl(composite);

		showQuickFix = new Button(composite, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		showQuickFix.setLayoutData(gd);
		showQuickFix.setText(SHOW_THE_QUICK_FIX_DIALOG);
		showQuickFix.setSelection(false);
		showQuickFix.setEnabled(false);
		
		showReadme = new Button(composite, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		showReadme.setLayoutData(gd);
		showReadme.setText(SHOW_README_FILE_FOR_FURTHER_INSTRUCTIONS);
		showReadme.setSelection(false);
		showReadme.setEnabled(false);
		
		if (projectExamples != null && projectExamples.size() > 0) {
			configure(projectExamples);
		}
		setPageComplete(true);
	}
	
	public void configure(List<ProjectExample> projectExamples) {
		ProjectExample projectExample = projectExamples.get(0);
		if (projectExample != null) {
			setTitle(projectExample.getShortDescription());
			setDescription("'" + projectExample.getShortDescription() + "' Project is now ready");
			if (showReadme != null) {
				if (projectExample.isWelcome()) {
					showReadme.setEnabled(true);
					showReadme.setSelection(true);
					showReadme.setText("Show '" + projectExample.getWelcomeURL() + "' for further instructions");
				} else {
					showReadme.setEnabled(false);
					showReadme.setSelection(false);
					showReadme.setText(SHOW_README_FILE_FOR_FURTHER_INSTRUCTIONS);
				}
			}
			List<IMarker> markers = ProjectExamplesActivator
					.getMarkers(projectExamples);
			if (markers != null && markers.size() > 0) {
				showQuickFix.setEnabled(true);
				showQuickFix.setSelection(true);
			}
		}
	}

	@Override
	public IWizardPage getPreviousPage() {
		IWizard wizard = getWizard();
		if (wizard instanceof NewProjectExamplesWizard2) {
			ProjectExample projectExample = ((NewProjectExamplesWizard2) wizard)
					.getSelectedProjectExample();
			if (projectExample != null
					&& projectExample.getImportType() != null) {
				List<IProjectExamplesWizardPage> pages = ((NewProjectExamplesWizard2) wizard)
						.getContributedPages();
				IProjectExamplesWizardPage previousPage = null;
				for (IProjectExamplesWizardPage page : pages) {
					if (projectExample.getImportType().equals(
							page.getProjectExampleType())) {
						previousPage = page;
					}
				}
				if (previousPage != null) {
					return previousPage;
				}
			} 
			return ((NewProjectExamplesWizard2) wizard).getLocationsPage();
		}
		return super.getPreviousPage();
	}

	public Button getShowReadme() {
		return showReadme;
	}

	public Button getShowQuickFix() {
		return showQuickFix;
	}

}
