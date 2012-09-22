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
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.wizard.Wizard;

public class ConvertToMavenDependencyWizard extends Wizard {

	private IProject project;
	
	private Set<IClasspathEntry> entries;


	IdentifyMavenDependencyPage identificationPage;
	private List<Dependency> dependencies;
	
	public ConvertToMavenDependencyWizard(IProject project, Set<IClasspathEntry> entries) {
		this.project = project;
		this.entries = entries;
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
		//DependencyConversionPreviewPage page2 = new DependencyConversionPreviewPage("Foo");
		//addPage(page2);
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
				/* actually only delete classpath entries */
				DeleteExistingJarsJob deleteJob = new DeleteExistingJarsJob(project, entries);
				deleteJob.schedule();
				try {
					deleteJob.join();//wait for job to finish to prevent bad concurrency issues 
				} catch (InterruptedException e) {
					e.printStackTrace();
					//ignore
				}
			}
		}
		
		return true;
	}


	public List<Dependency> getDependencies() {
		return dependencies;
	}

	
}
