package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.jaxrs.configurators.JaxrsProjectConfigurator;
import org.jboss.tools.maven.jaxrs.configurators.MavenJaxRsConstants;
import org.junit.Test;

@SuppressWarnings("restriction")
public class JaxRsConfiguratorTest extends AbstractMavenConfiguratorTest {

	/*@Test
	public void testJBIDE9290_supportMultipleJaxRsDependencies() throws Exception {
		IProject[] projects = importProjects("projects/jaxrs/", 
											new String[]{ 
												"jaxrs-jersey/pom.xml",
												"jaxrs-resteasy/pom.xml"
											}, 
											new ResolverConfiguration());
		waitForJobsToComplete(new NullProgressMonitor());
		IProject jersey = projects[0];
		assertNoErrors(jersey);
		assertIsJaxRsProject(jersey, JaxrsProjectConfigurator.JAX_RS_FACET_1_1);
		
		IProject resteasy = projects[1];
		assertNoErrors(resteasy);
		assertIsJaxRsProject(resteasy, JaxrsProjectConfigurator.JAX_RS_FACET_1_1);
		
	}
	*/

	
	@Test
	public void testJBIDE9290_errorMarkers() throws Exception {
		String projectLocation = "projects/jaxrs/jaxrs-error";
		IProject jaxRsProject = importProject(projectLocation+"/pom.xml");
		waitForJobsToComplete(new NullProgressMonitor());
		IFacetedProject facetedProject = ProjectFacetsManager.create(jaxRsProject);
		assertNotNull(jaxRsProject.getName() + " is not a faceted project", facetedProject);
		assertFalse("JAX-RS Facet should be missing", facetedProject.hasProjectFacet(JaxrsProjectConfigurator.JAX_RS_FACET));
		assertHasJaxRsConfigurationError(jaxRsProject, "JAX-RS (REST Web Services) 1.1 can not be installed : One or more constraints have not been satisfied.");
		assertHasJaxRsConfigurationError(jaxRsProject, "JAX-RS (REST Web Services) 1.1 requires Dynamic Web Module 2.3 or newer.");
		
		//Check markers are removed upon configuration update
		copyContent(jaxRsProject, "src/main/webapp/WEB-INF/good-web.xml", "src/main/webapp/WEB-INF/web.xml", true);
		updateProject(jaxRsProject);
		assertNoErrors(jaxRsProject);
		assertIsJaxRsProject(jaxRsProject, JaxrsProjectConfigurator.JAX_RS_FACET_1_1);
	}

	private void assertHasJaxRsConfigurationError(IProject project, String message) throws Exception {
		WorkspaceHelpers.assertErrorMarker(MavenJaxRsConstants.JAXRS_CONFIGURATION_ERROR_MARKER_ID, message, 1, "", project);
	}
	
	private void assertIsJaxRsProject(IProject project,
			IProjectFacetVersion expectedJaxRsVersion) throws Exception {
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		assertNotNull(project.getName() + " is not a faceted project", facetedProject);
		assertEquals("Unexpected JAX-RS Version", expectedJaxRsVersion, facetedProject.getInstalledVersion(JaxrsProjectConfigurator.JAX_RS_FACET));
		assertTrue("Java Facet is missing",	facetedProject.hasProjectFacet(JavaFacet.FACET));
	}
}
