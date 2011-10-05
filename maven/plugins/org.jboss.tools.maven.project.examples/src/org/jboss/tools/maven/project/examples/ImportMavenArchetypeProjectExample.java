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

package org.jboss.tools.maven.project.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.maven.project.examples.wizard.ArchetypeExamplesWizard;
import org.jboss.tools.project.examples.model.AbstractImportProjectExample;
import org.jboss.tools.project.examples.model.Project;

/**
 * @author snjeza
 * 
 */
public class ImportMavenArchetypeProjectExample extends
		AbstractImportProjectExample {

	private static final String UNNAMED_PROJECTS = "UnnamedProjects"; //$NON-NLS-1$

	private static final String JBOSS_TOOLS_MAVEN_PROJECTS = "/.JBossToolsMavenProjects"; //$NON-NLS-1$

	@Override
	public List<Project> importProject(final Project projectDescription, File file,
			IProgressMonitor monitor) throws Exception {
		List<Project> projects = new ArrayList<Project>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootPath = workspaceRoot.getLocation();
		IPath mavenProjectsRoot = rootPath.append(JBOSS_TOOLS_MAVEN_PROJECTS);
		String projectName = projectDescription.getName();
		if (projectName == null || projectName.isEmpty()) {
			projectName = UNNAMED_PROJECTS;
		}
		IPath path = mavenProjectsRoot.append(projectName);
		final File destination = new File(path.toOSString());

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				ArchetypeExamplesWizard wizard = new ArchetypeExamplesWizard(destination, projectDescription);
				WizardDialog wizardDialog = new WizardDialog(getActiveShell(), wizard);
				wizardDialog.open();
			}
			
		});
		return projects;
	}

	
	private static Shell getActiveShell() {
		return Display.getDefault().getActiveShell();
	}
}
