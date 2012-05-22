/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.dialog.MarkerDialog;
import org.jboss.tools.project.examples.model.ProjectExample;

/**
 * 
 * @author snjeza
 * 
 */
public class ProjectReadyWizard extends Wizard {

	private List<ProjectExample> projectExamples;
	private NewProjectExamplesReadyPage readyPage;

	public ProjectReadyWizard(List<ProjectExample> projectExamples) {
		this.projectExamples = projectExamples;
		setDefaultPageImageDescriptor(IDEInternalWorkbenchImages
				.getImageDescriptor(IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG));
		setWindowTitle(Messages.NewProjectExamplesWizard_New_Project_Example);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		readyPage = new NewProjectExamplesReadyPage(projectExamples);
		addPage(readyPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean openWelcome = readyPage.getShowReadme().isEnabled() && readyPage.getShowReadme().getSelection();
		if (openWelcome) {
			ProjectExamplesActivator.openWelcome(projectExamples);
		}
		boolean showQuickFix = readyPage.getShowQuickFix().isEnabled() && readyPage.getShowQuickFix().getSelection();
		if (showQuickFix) {
			Display.getCurrent().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					Dialog dialog = new MarkerDialog(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), projectExamples);
					dialog.open();
				}
			});
			
		}
		return true;
	}

}

