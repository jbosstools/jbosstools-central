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
package org.jboss.tools.maven.conversion.ui.internal;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.m2e.core.project.conversion.AbstractProjectConversionParticipant;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.maven.conversion.core.DependencyCollector;
import org.jboss.tools.maven.conversion.core.DependencyCollectorFactory;
import org.jboss.tools.maven.conversion.core.ProjectDependency;
import org.jboss.tools.maven.conversion.ui.dialog.ConvertToMavenDependencyWizard;

public class ClasspathConversionParticipant extends
		AbstractProjectConversionParticipant {

	@Override
	public boolean accept(IProject project) throws CoreException {
		return getDependencyCollector(project) != null;
	}

	private DependencyCollector getDependencyCollector(IProject project) throws CoreException {
		return DependencyCollectorFactory.INSTANCE.getDependencyCollector(project);
	}

	@Override
	public void convert(final IProject project, final Model model, final IProgressMonitor monitor)
			throws CoreException {

		DependencyCollector dependencyCollector = getDependencyCollector(project);
		if (dependencyCollector != null) {
			List<ProjectDependency> entries = dependencyCollector.collectDependencies(project);
			if (entries == null || entries.isEmpty()) {
				return;
			}
			final ConvertToMavenDependencyWizard conversionWizard = new ConvertToMavenDependencyWizard(project, entries);
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					WizardDialog dialog = new WizardDialog(shell, conversionWizard);
					if (dialog.open() == Window.OK) {
						List<Dependency> dependencies = conversionWizard.getDependencies();
						if (dependencies != null && !dependencies.isEmpty()) {
							model.setDependencies(dependencies);
						}
					}
				}
			});
		}
	}

}
