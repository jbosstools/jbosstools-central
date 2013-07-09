/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.configurators.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;


@SuppressWarnings("restriction")
public abstract class JpaConfiguratorTest extends AbstractMavenConfiguratorTest {

	static final IProjectFacet JPA_FACET = ProjectFacetsManager.getProjectFacet("jpt.jpa");

	static final IProjectFacetVersion JPA_FACET_VERSION_1_0 = JPA_FACET.getVersion("1.0");

	static final IProjectFacetVersion JPA_FACET_VERSION_2_0 = JPA_FACET.getVersion("2.0");

	/*
	 Commented until the new m2e-wtp features are available  

	@Test
	public void testSimpleJavaProjects() throws Exception {
		IProject project = importProject( "projects/jpa/simple-2.0/pom.xml");
		waitForJobsToComplete();
		
		assertIsJpaProject(project, JpaFacet.VERSION_2_0);
		assertNoErrors(project);
		String pid = JptJpaCorePlugin.getJpaPlatformId(project); 
		assertTrue(pid + " is not the expected platform", pid.startsWith("eclipselink") || pid.startsWith("generic"));
		
		project = importProject( "projects/jpa/simple-1.0/pom.xml");
		waitForJobsToComplete();
		assertIsJpaProject(project, JpaFacet.VERSION_1_0);
		assertNoErrors(project);
		
		pid = JptJpaCorePlugin.getJpaPlatformId(project); 
		assertTrue(pid + " is not the expected hibernate platform", pid.startsWith("hibernate") || pid.startsWith("generic"));
	}	
   */

	protected void assertIsJpaProject(IProject project, IProjectFacetVersion expectedJpaVersion) throws Exception {
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		assertNotNull(project.getName() + " is not a faceted project", facetedProject);
		assertEquals("Unexpected JPA Version", expectedJpaVersion, facetedProject.getInstalledVersion(JPA_FACET));
		assertTrue("Java Facet is missing",	facetedProject.hasProjectFacet(JavaFacet.FACET));
	}

	
	/*
	@Test
	public void testMultiModule()  throws Exception {
		IProject[] projects = importProjects("projects/jpa/multi", 
				new String[]{ "pom.xml",
							  "multi-ear/pom.xml",
							  "multi-ejb/pom.xml",
							  "multi-web/pom.xml"}, 
				new ResolverConfiguration());
		waitForJobsToComplete();
		
		IProject pom = projects[0];
		IProject ear = projects[1];
		IProject ejb = projects[2];
		IProject web = projects[3];

		assertNoErrors(pom);
		assertNoErrors(ejb);
		assertNoErrors(ear);
		assertNoErrors(web);
		
		assertIsJpaProject(ejb, JpaFacet.VERSION_2_0);
	}
	
	*/
}
