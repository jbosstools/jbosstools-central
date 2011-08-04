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
package org.jboss.tools.maven.configurators.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.common.util.FileUtil;
import org.jboss.tools.maven.jsf.configurators.JSFProjectConfigurator;

public abstract class AbstractMavenConfiguratorTest extends
		AbstractMavenProjectTestCase {

	protected String toString(File file) {
		return FileUtil.readFile(file);
	}

	protected String toString(IFile file) {
		return FileUtil.getContentFromEditorOrFile(file);
	}

	protected void assertIsJSFProject(IProject project,
			IProjectFacetVersion expectedJSFVersion) throws Exception {
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		assertNotNull(project.getName() + " is not a faceted project", facetedProject);
		assertEquals("Unexpected JSF Version", expectedJSFVersion, facetedProject.getInstalledVersion(JSFProjectConfigurator.JSF_FACET));
		assertTrue("Java Facet is missing",	facetedProject.hasProjectFacet(JavaFacet.FACET));
		assertTrue("faces-config.xml is missing", project.getFile("src/main/webapp/WEB-INF/faces-config.xml").exists());
	}

	/**
	 * Replace the project pom.xml with a new one, triggers new build
	 * 
	 * @param project
	 * @param newPomName
	 * @throws Exception
	 */
	protected void updateProject(IProject project, String newPomName) throws Exception {
		updateProject(project, newPomName, -1);
	}

	/**
	 * Replace the project pom.xml with a new one, triggers new build, wait for
	 * waitTime milliseconds.
	 * 
	 * @param project
	 * @param newPomName
	 * @param waitTime
	 * @throws Exception
	 */
	protected void updateProject(IProject project, String newPomName, int waitTime) throws Exception {

		if (newPomName != null) {
			copyContent(project, newPomName, "pom.xml");
		}
		IProgressMonitor mon = new NullProgressMonitor();
		IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
		ResolverConfiguration configuration = new ResolverConfiguration();
		configurationManager.enableMavenNature(project, configuration, mon);
		configurationManager.updateProjectConfiguration(project, mon);
		waitForJobsToComplete(mon);

		project.build(IncrementalProjectBuilder.FULL_BUILD, mon);
		if (waitTime > 0) {
			Thread.sleep(waitTime);
		}
		waitForJobsToComplete(mon);
	}

	protected void updateProject(IProject project) throws Exception {
		updateProject(project, null, -1);
	}
}
