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
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.cdi.core.CDICoreNature;
import org.jboss.tools.cdi.core.CDIUtil;
import org.jboss.tools.maven.ui.Activator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class CDIConfiguratorTest extends AbstractMavenConfiguratorTest {

	protected static final IProjectFacet MAVEN_FACET = ProjectFacetsManager.getProjectFacet("jboss.m2");
	protected static final IProjectFacet CDI_FACET = ProjectFacetsManager.getProjectFacet("jst.cdi"); //$NON-NLS-1$
	protected static final IProjectFacetVersion CDI_VERSION_1_0 = CDI_FACET.getVersion("1.0"); //$NON-NLS-1$
	protected static final IProjectFacetVersion CDI_VERSION_1_1 = CDI_FACET.getVersion("1.1"); //$NON-NLS-1$
	protected static final IProjectFacetVersion CDI_VERSION_DEFAULT = CDI_FACET.getDefaultVersion(); //$NON-NLS-1$

	@Before
	@After
	public void activateCDIConfiguration() {
	  setGlobalCdiConfigurationActivation(true);
	}
	
	@Test
	public void testJBIDE11741_deltaSpikeDependency() throws Exception {
		testCdiProject("deltaspike", CDI_VERSION_1_0);
	}

	@Test
	public void testJBIDE12558_unsupportedCdiVersion() throws Exception {
		testCdiProject("unsupported-cdi-version", CDI_VERSION_DEFAULT);
	}

	@Test
	public void testJBIDE13128_warHasBeansXml() throws Exception {
		testCdiProject("war-has-beans-xml", CDI_VERSION_1_0);
	}

	@Test
	public void testJBIDE13128_warHasMetaInfBeansXml() throws Exception {
		testCdiProject("war-has-metainfbeans-xml", CDI_VERSION_1_0);
	}

	@Test
	public void testJBIDE13128_jarHasBeansXml() throws Exception {
		testCdiProject("jar-has-beans-xml", CDI_VERSION_1_0);
	}

	@Test
	public void testJBIDE13128_warNoCdi() throws Exception {
		String projectLocation = "projects/cdi/war-no-cdi";
		IProject notCdiProject = importProject(projectLocation+"/pom.xml");
		waitForJobsToComplete();
    assertIsNotCDIProject(notCdiProject);
	}

	@Test
	public void testJBIDE13739_DisableCdi() throws Exception {
		
		String projectLocation = "projects/cdi/cdi";
		IProject cdiProject = importProject(projectLocation+"/pom.xml");
		waitForJobsToComplete();
		assertIsCDIProject(cdiProject, CDI_VERSION_1_0);
		
		IWorkspaceDescription description = workspace.getDescription();
		description.setAutoBuilding(true);
		workspace.setDescription(description);
		
		CDIUtil.disableCDI(cdiProject);
		waitForJobsToComplete();
		assertFalse("CDI nature should be missing after disabling CDI", cdiProject.hasNature(CDICoreNature.NATURE_ID));

		updateProject(cdiProject);
		waitForJobsToComplete();
		assertFalse("CDI nature should be missing after updating the maven project", cdiProject.hasNature(CDICoreNature.NATURE_ID));
		
		description.setAutoBuilding(false);
		workspace.setDescription(description);
	}

	@Test
	public void testJBIDE15628_Cdi11() throws Exception {
		testCdiProject("cdi-1.1", CDI_VERSION_1_1);
	}

	@Test
	public void testJBIDE15628_Cdi11BeansXml() throws Exception {
		testCdiProject("cdi-beans-1.1", CDI_VERSION_1_1);
	}

 @Test
  public void testJBIDE17885_disableCdiViaProperty() throws Exception {
    String projectLocation = "projects/cdi/deactivated-cdi";
    IProject notCdiProject = importProject(projectLocation+"/pom.xml");
    waitForJobsToComplete();
    assertIsNotCDIProject(notCdiProject);
  }

  @Test
  public void testJBIDE17885_forceCdiViaProperty() throws Exception {
    setGlobalCdiConfigurationActivation(false);
    String projectLocation = "projects/cdi/force-cdi";
    IProject notCdiProject = importProject(projectLocation+"/pom.xml");
    waitForJobsToComplete();
    assertIsCDIProject(notCdiProject, CDI_VERSION_1_1);
  }

	
  protected void testCdiProject(String projectName, IProjectFacetVersion expectedCdiVersion) throws Exception {
		String projectLocation = "projects/cdi/"+projectName;
		IProject cdiProject = importProject(projectLocation+"/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(cdiProject);
		assertIsCDIProject(cdiProject, expectedCdiVersion);
	}

	protected void assertIsCDIProject(IProject project, IProjectFacetVersion expectedCdiVersion) throws Exception {
		assertNoErrors(project);
		assertTrue("CDI nature is missing", project.hasNature(CDICoreNature.NATURE_ID));

		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		if (facetedProject != null) {
			IProjectFacetVersion cdiVersion = facetedProject.getInstalledVersion(CDI_FACET);
			assertEquals("Unexpected CDI Version",  expectedCdiVersion, cdiVersion);
			assertTrue("Maven Facet is missing",	facetedProject.hasProjectFacet(MAVEN_FACET));
		}
	}
	
	protected void assertIsNotCDIProject(IProject project) throws Exception {
	    assertNoErrors(project);
	    assertFalse("CDI nature should be missing", project.hasNature(CDICoreNature.NATURE_ID));

	    IFacetedProject facetedProject = ProjectFacetsManager.create(project);
	    if (facetedProject != null) {
	    	assertNull("CDI Facet should be missing ",facetedProject.getInstalledVersion(CDI_FACET));
	    }
	}
	
	protected void setGlobalCdiConfigurationActivation(boolean active) {
	  IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	  System.err.println(((active)?"restoring":"disabling")+ " CDI config");
	  store.setValue(Activator.CONFIGURE_CDI, active);
	}
	
}
