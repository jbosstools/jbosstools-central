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
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.ui.Activator;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class SpringBootConfiguratorTest extends AbstractMavenConfiguratorTest {

	protected static final String SPRINGBOOT_PROJECTS = "projects/springboot/";

	protected static final String JAR_SPRINGBOOT_PROJECT = "jar";
	protected static final String JAR_SPRINGBOOT_PROJECT_DEPENDENCY = "dependency";
	protected static final String WAR_SPRINGBOOT_PROJECT = "war";
	protected static final IProjectFacet JAVA_FACET = JavaFacet.FACET;
	protected static final IProjectFacet UTILITY_FACET = 
			ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_UTILITY_MODULE); //$NON-NLS-1$

	@Before
	public void activateCDIConfiguration() {
		setGlobalSpringBootConfigurationActivation(true);
	}

	@Test
	public void shouldAddUtilityFacetToJaredSpringbootProject() throws Exception {
		loadProject(JAR_SPRINGBOOT_PROJECT_DEPENDENCY);
		IProject project = loadProject(JAR_SPRINGBOOT_PROJECT);
		assertHasUtilityFacet(project);
	}

	@Test
	public void shouildAddUtilityFacetToDependencyOfSpringBootProject() throws Exception {
		IProject dependency = loadProject(JAR_SPRINGBOOT_PROJECT_DEPENDENCY);
		loadProject(JAR_SPRINGBOOT_PROJECT);
		assertHasUtilityFacet(dependency);
	}

	@Test
	public void shouldNotAddUtilityFacetToDependencyOnly() throws Exception {
		IProject dependency = loadProject(JAR_SPRINGBOOT_PROJECT_DEPENDENCY);
		assertHasNotUtilityFacet(dependency);
	}

	@Test
	public void shouldNotAddUtilityFacetToWaredSpringBootProject() throws Exception {
		IProject project = loadProject(WAR_SPRINGBOOT_PROJECT);
		assertHasNotUtilityFacet(project);
	}

	protected IProject loadProject(String projectName) throws Exception {
		String projectLocation = SPRINGBOOT_PROJECTS + projectName;
		IProject springBootProject = importProject(projectLocation + "/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(springBootProject);
		return springBootProject;
	}

	protected void assertHasUtilityFacet(IProject project) throws Exception {
		assertNoErrors(project);
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		assertTrue(NLS.bind("Project {0} is not a faceted project", project.getName()), facetedProject != null);
		assertTrue(NLS.bind("Utility JAR Facet is missing for project {0}", project.getName()),
				facetedProject.hasProjectFacet(UTILITY_FACET));
		assertTrue(NLS.bind("Java Facet is missing for project {0}", project.getName()),
				facetedProject.hasProjectFacet(JAVA_FACET));
	}

	protected void assertHasNotUtilityFacet(IProject project) throws Exception {
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
