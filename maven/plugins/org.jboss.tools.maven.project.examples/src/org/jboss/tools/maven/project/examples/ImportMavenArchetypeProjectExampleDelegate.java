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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;

/**
 * @author snjeza
 * 
 */
public class ImportMavenArchetypeProjectExampleDelegate extends AbstractImportMavenProjectDelegate {

	@Override
	public boolean importProject(final ProjectExample projectDescription, File file, 
			Map<String, Object> propertiesMap, final IProgressMonitor monitor) throws Exception {
		List<ProjectExample> projects = new ArrayList<ProjectExample>();
		projects.add(projectDescription);
		List<String> includedProjects = projectDescription.getIncludedProjects();
		if (includedProjects == null) {
			includedProjects = new ArrayList<String>();
			projectDescription.setIncludedProjects(includedProjects);
		}
		projectDescription.getIncludedProjects().clear();
		String projectName = (String) propertiesMap.get(ProjectExamplesActivator.PROPERTY_PROJECT_NAME);
		includedProjects.add(projectName);
		String artifactId = (String) propertiesMap.get(ProjectExamplesActivator.PROPERTY_ARTIFACT_ID);
		IPath location = (IPath) propertiesMap.get(ProjectExamplesActivator.PROPERTY_LOCATION_PATH);
		String projectFolder = location.append(artifactId).toFile().getAbsolutePath();
		MavenModelManager mavenModelManager = MavenPlugin.getMavenModelManager();
		
		LocalProjectScanner scanner = new LocalProjectScanner(location.toFile(),
						projectFolder, true, mavenModelManager);
		try {
			scanner.run(monitor);
		} catch (InterruptedException e1) {
			return false;
		}

		Set<MavenProjectInfo> projectSet = collectProjects(scanner.getProjects());
		ProjectImportConfiguration importConfiguration = new ProjectImportConfiguration();
				
		for (MavenProjectInfo info : projectSet) {
			try {
				projectName = MavenProjectExamplesActivator.getProjectName(info, importConfiguration);
				if (!includedProjects.contains(projectName)) {
					includedProjects.add(projectName);
				}
			} catch (CoreException e) {
				MavenProjectExamplesActivator.log(e);
				return false;
			}
		}
		MavenProjectExamplesActivator.updateMavenConfiguration(projectName, includedProjects, monitor);
		return true;
	}

	public Set<MavenProjectInfo> collectProjects(
			Collection<MavenProjectInfo> projects) {
		return new LinkedHashSet<MavenProjectInfo>() {
			private static final long serialVersionUID = 1L;

			public Set<MavenProjectInfo> collectProjects(
					Collection<MavenProjectInfo> projects) {
				for (MavenProjectInfo projectInfo : projects) {
					add(projectInfo);
					collectProjects(projectInfo.getProjects());
				}
				return this;
			}
		}.collectProjects(projects);
	}

	private static Shell getActiveShell() {
		return Display.getDefault().getActiveShell();
	}

}
