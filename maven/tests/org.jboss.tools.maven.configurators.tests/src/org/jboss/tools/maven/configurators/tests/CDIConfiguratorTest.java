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
package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.cdi.core.CDICoreNature;
import org.junit.Test;

@SuppressWarnings("restriction")
public class CDIConfiguratorTest extends AbstractMavenConfiguratorTest {

	protected static final IProjectFacet MAVEN_FACET = ProjectFacetsManager.getProjectFacet("jboss.m2");
	protected static final IProjectFacet CDI_FACET = ProjectFacetsManager.getProjectFacet("jst.cdi"); //$NON-NLS-1$
	protected static final IProjectFacetVersion DEFAULT_CDI_VERSION = CDI_FACET.getVersion("1.0"); //$NON-NLS-1$
	
	@Test
	public void testJBIDE11741_deltaSpikeDependency() throws Exception {
		String projectLocation = "projects/cdi/deltaspike";
		IProject cdiProject = importProject(projectLocation+"/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(cdiProject);
		assertIsCDIProject(cdiProject, DEFAULT_CDI_VERSION);
	}

	private void assertIsCDIProject(IProject project, IProjectFacetVersion expectedCdiVersion) throws Exception {
		assertNoErrors(project);
		assertTrue("CDI nature is missing", project.hasNature(CDICoreNature.NATURE_ID));

		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		if (facetedProject != null) {
			IProjectFacetVersion cdiVersion = facetedProject.getInstalledVersion(CDI_FACET);
			assertEquals("Unexpected CDI Version",  expectedCdiVersion, cdiVersion);
			assertTrue("Maven Facet is missing",	facetedProject.hasProjectFacet(MAVEN_FACET));
		}
	}
}
