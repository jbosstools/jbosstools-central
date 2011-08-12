package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.jbosspackaging.configurators.SarProjectConfigurator;
import org.junit.Test;

@SuppressWarnings("restriction")
public class JBossSarConfiguratorTest extends AbstractMavenConfiguratorTest {

	@Test
	public void testBasicSarSupport() throws Exception {
		IProject project = importProject("projects/jboss-sar/jboss-sar-1/pom.xml");
		waitForJobsToComplete(new NullProgressMonitor());
		assertIsSarProject(project);

		IVirtualComponent sarComponent = ComponentCore.createComponent(project);
		assertNotNull(sarComponent);
		IVirtualReference[] references = sarComponent.getReferences();
		assertEquals(1, references.length);
		assertEquals("commons-lang-2.5.jar", references[0].getArchiveName());
		assertEquals("/lib", references[0].getRuntimePath().toPortableString());
		
		IResource[] underlyingResources = getUnderlyingResources(project);
		assertEquals(1, underlyingResources.length);
		assertEquals(project.getFolder("/src/main/resources"), underlyingResources[0]);
	}

	@Test
	public void testExcludeAllDependencies() throws Exception {
		IProject project = importProject("projects/jboss-sar/jboss-sar-2/pom.xml");
		waitForJobsToComplete(new NullProgressMonitor());
		assertIsSarProject(project);

		IVirtualComponent sarComponent = ComponentCore.createComponent(project);
		assertNotNull(sarComponent);
		IVirtualReference[] references = sarComponent.getReferences();
		assertEquals(0, references.length);
	}	
	
	@Test
	public void testExcludeSomeDependencies() throws Exception {
		IProject project = importProject("projects/jboss-sar/jboss-sar-3/pom.xml");
		waitForJobsToComplete(new NullProgressMonitor());
		assertIsSarProject(project);

		IVirtualComponent sarComponent = ComponentCore.createComponent(project);
		assertNotNull(sarComponent);
		IVirtualReference[] references = sarComponent.getReferences();
		assertEquals(1, references.length);
		assertEquals("commons-lang-2.5.jar", references[0].getArchiveName());
	}	
	
	@Test
	public void testSarInEar() throws Exception {
		IProject[] projects = importProjects("projects/jboss-sar/parent", 
				new String[] {"pom.xml", "ear/pom.xml", "sar/pom.xml"}, 
				new ResolverConfiguration());
		waitForJobsToComplete(new NullProgressMonitor());

		IProject sar = projects[2];
		assertIsSarProject(sar);

		IProject ear = projects[1];
		assertNoErrors(ear);

		IVirtualComponent earComponent = ComponentCore.createComponent(ear);
		assertNotNull(earComponent);
		IVirtualReference[] references = earComponent.getReferences();
		assertEquals(2, references.length);
		assertEquals("sar-0.0.1-SNAPSHOT.sar", references[0].getArchiveName());
		assertEquals("/", references[0].getRuntimePath().toPortableString());
		assertEquals("commons-lang-2.5.jar", references[1].getArchiveName());
		assertEquals("/lib", references[1].getRuntimePath().toPortableString());
	}	
	
	// @Test
	// public void testJBIDE9290_errorMarkers() throws Exception {
	// String projectLocation = "projects/jaxrs/jaxrs-error";
	// IProject jaxRsProject = importProject(projectLocation+"/pom.xml");
	// waitForJobsToComplete(new NullProgressMonitor());
	// IFacetedProject facetedProject =
	// ProjectFacetsManager.create(jaxRsProject);
	// assertNotNull(jaxRsProject.getName() + " is not a faceted project",
	// facetedProject);
	// assertFalse("JAX-RS Facet should be missing",
	// facetedProject.hasProjectFacet(JaxrsProjectConfigurator.JAX_RS_FACET));
	// assertHasJaxRsConfigurationError(jaxRsProject,
	// "JAX-RS (REST Web Services) 1.1 can not be installed : One or more constraints have not been satisfied.");
	// assertHasJaxRsConfigurationError(jaxRsProject,
	// "JAX-RS (REST Web Services) 1.1 requires Dynamic Web Module 2.3 or newer.");
	//
	// //Check markers are removed upon configuration update
	// copyContent(jaxRsProject, "src/main/webapp/WEB-INF/good-web.xml",
	// "src/main/webapp/WEB-INF/web.xml", true);
	// updateProject(jaxRsProject);
	// assertNoErrors(jaxRsProject);
	// assertIsJaxRsProject(jaxRsProject,
	// JaxrsProjectConfigurator.JAX_RS_FACET_1_1);
	// }

	// private void assertHasJaxRsConfigurationError(IProject project, String
	// message) throws Exception {
	// WorkspaceHelpers.assertErrorMarker(MavenJaxRsConstants.JAXRS_CONFIGURATION_ERROR_MARKER_ID,
	// message, 1, "", project);
	// }

	private void assertIsSarProject(IProject project) throws Exception {
		IProjectFacetVersion expectedJaxRsVersion = SarProjectConfigurator.JBOSS_SAR_FACET_VERSION_1_0;
		assertNoErrors(project);
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		assertNotNull(project.getName() + " is not a faceted project", facetedProject);
		assertEquals("Unexpected JBoss SAR Version", expectedJaxRsVersion,
				facetedProject.getInstalledVersion(SarProjectConfigurator.JBOSS_SAR_FACET));
		assertTrue("Java Facet is missing", facetedProject.hasProjectFacet(JavaFacet.FACET));
		assertTrue("Manifest is missing", project.getFile("target/classes/META-INF/MANIFEST.MF").exists());
	}

	protected static IResource[] getUnderlyingResources(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		IVirtualFolder root = component.getRootFolder();
		IResource[] underlyingResources = root.getUnderlyingResources();
		return underlyingResources;
	}
}
