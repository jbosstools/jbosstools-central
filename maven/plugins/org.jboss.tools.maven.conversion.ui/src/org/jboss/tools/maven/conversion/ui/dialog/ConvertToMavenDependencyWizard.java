/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.ui.dialog;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.DeleteResourcesWizard;
import org.jboss.tools.maven.conversion.core.ProjectDependency;

/**
 * Convert project dependencies to Maven dependencies wizard
 * 
 * @author Fred Bricon
 *
 */
public class ConvertToMavenDependencyWizard extends Wizard {

	private IProject project;
	
	private List<ProjectDependency> entries;

	private IdentifyMavenDependencyPage identificationPage;
	
	private List<Dependency> dependencies;
	
	public ConvertToMavenDependencyWizard(IProject project, List<ProjectDependency> projectDependencies) {
		System.err.println("New wizard");
		this.project = project;
		this.entries = projectDependencies;
		String title = "Convert to Maven ";
		if (entries.size() > 1) {
			title += "Dependencies";
		} else {
			title += "Dependency";
		}
		setWindowTitle(title);
	}


	@Override
	public void addPages() {
		identificationPage = new IdentifyMavenDependencyPage(project, entries);
		addPage(identificationPage);
	}
	
	@Override
	public boolean performCancel() {
		identificationPage.cancel();
		return super.performCancel();
	}
	
	
	@Override
	public boolean performFinish() {
		if (identificationPage != null) {
			dependencies = identificationPage.getDependencies();
			
			if (identificationPage.isDeleteJars()) {
				//Only delete jars that are directly under a project's hierarchy
				IResource[] resourcesToDelete = identificationPage.getResourcesToDelete();
				if (resourcesToDelete != null && resourcesToDelete.length > 0) {
					DeleteResourcesWizard wizard = new DeleteResourcesWizard(resourcesToDelete);
					try {
						RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
						op.run(getShell(), "Delete project relative jars");
					} catch(InterruptedException e) {
						// ignored
					}
				}
			}
		}
		
		return true;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}
	
}
