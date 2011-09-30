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

/**
 * @author snjeza
 * 
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;

public class NewProjectExamplesWizard extends Wizard implements INewWizard {

	private List<Project> projects = new ArrayList<Project>();
	
	private NewProjectExamplesWizardPage page;

	public NewProjectExamplesWizard() {
		super();
		setWindowTitle(Messages.NewProjectExamplesWizard_New_Project_Example);

	}

	/**
	 * Creates an empty wizard for creating a new resource in the workspace.
	 */

	@Override
	public boolean performFinish() {
		final List<Project> selectedProjects = new ArrayList<Project>();
		if (page.getSelection() == null || page.getSelection().size() <= 0) {
			return false;
		}
		IStructuredSelection selection = page.getSelection();
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof Project) {
				Project project = (Project) object;
				selectedProjects.add(project);
			}
		}
		ProjectExamplesActivator.importProjectExamples(selectedProjects, page.showQuickFix());
		return true;
	}

			
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		initializeDefaultPageImageDescriptor();
	}

	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ProjectExamplesActivator
				.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID,
						"icons/new_wiz.gif"); //$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	@Override
	public void addPages() {
		super.addPages();
		page = new NewProjectExamplesWizardPage();
		addPage(page);
	}
	
}
