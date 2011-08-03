package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.jsf.configurators.JSFProjectConfigurator;
import org.junit.Test;

@SuppressWarnings("restriction")
public class JSFConfiguratorTest extends AbstractMavenConfiguratorTest {

	@Test
	public void testJBIDE9242_supportMultipleJSFDependencies() throws Exception {
		IProject[] projects = importProjects("projects/jsf/", 
											new String[]{ "jsf-mojarra/pom.xml",
														  "jsf-jboss/pom.xml",
														  "jsf-jsfapi/pom.xml",
														  "jsf-myfaces/pom.xml",
														  "jsf-jsfapi-12/pom.xml"}, 
											new ResolverConfiguration());
		IProject mojarra = projects[0];
		assertNoErrors(mojarra);
		assertIsJSFProject(mojarra, JSFProjectConfigurator.JSF_FACET_VERSION_2_0);
		
		IProject jboss = projects[1];
		assertNoErrors(jboss);
		assertIsJSFProject(jboss, JSFProjectConfigurator.JSF_FACET_VERSION_2_0);
		
		IProject jsfapi_20 = projects[2];
		assertNoErrors(jsfapi_20);
		assertIsJSFProject(jsfapi_20, JSFProjectConfigurator.JSF_FACET_VERSION_2_0);
		
		IProject myfaces = projects[3];
		assertNoErrors(myfaces);
		assertIsJSFProject(myfaces, JSFProjectConfigurator.JSF_FACET_VERSION_1_1);

		IProject jsfapi_12 = projects[4];
		assertNoErrors(jsfapi_12);
		assertIsJSFProject(jsfapi_12, JSFProjectConfigurator.JSF_FACET_VERSION_1_2);
		

	}

	private void assertIsJSFProject(IProject project, IProjectFacetVersion expectedJSFVersion) throws Exception {
		  IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		    assertNotNull(project.getName() + " is not a faceted project", facetedProject);
		    assertEquals("Unexpected JSF Version", expectedJSFVersion, facetedProject.getInstalledVersion(JSFProjectConfigurator.JSF_FACET));
		    assertTrue("Java Facet is missing", facetedProject.hasProjectFacet(JavaFacet.FACET));
	}
}
