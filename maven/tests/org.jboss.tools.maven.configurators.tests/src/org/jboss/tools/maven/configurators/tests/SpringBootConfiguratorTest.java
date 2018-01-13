/*************************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.ui.Activator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class SpringBootConfiguratorTest extends AbstractMavenConfiguratorTest {

	protected static final IProjectFacet JAVA_FACET = JavaFacet.FACET;
	protected static final IProjectFacet UTILITY_FACET = ProjectFacetsManager.getProjectFacet("jst.utility"); //$NON-NLS-1$

	@Before
	@After
	public void activateCDIConfiguration() {
		setGlobalSpringBootConfigurationActivation(true);
	}

	@Test
	public void testThatJarProjectIsUtility() throws Exception {
		IProject project = loadSpringBootProject("jar");
		assertIsUtilityJarProject(project);
	}

	@Test
	public void testThatWarProjectIsNotUtility() throws Exception {
		IProject project = loadSpringBootProject("war");
		assertIsNotUtilityJarProject(project);
	}

	protected IProject loadSpringBootProject(String projectName) throws Exception {
		String projectLocation = "projects/springboot/" + projectName;
		IProject springBootProject = importProject(projectLocation + "/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(springBootProject);
		return springBootProject;
	}

	protected void assertIsUtilityJarProject(IProject project) throws Exception {
		assertNoErrors(project);
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		if (facetedProject != null) {
			assertTrue("Utility JAR Facet is missing", facetedProject.hasProjectFacet(UTILITY_FACET));
			assertTrue("Java Facet is missing", facetedProject.hasProjectFacet(JAVA_FACET));
		}
	}

	protected void assertIsNotUtilityJarProject(IProject project) throws Exception {
		assertNoErrors(project);

		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		if (facetedProject != null) {
			assertFalse("Utility JAR Facet is set", facetedProject.hasProjectFacet(UTILITY_FACET));
		}
	}

	protected void setGlobalSpringBootConfigurationActivation(boolean active) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(Activator.CONFIGURE_SPRING_BOOT, active);
	}

}
