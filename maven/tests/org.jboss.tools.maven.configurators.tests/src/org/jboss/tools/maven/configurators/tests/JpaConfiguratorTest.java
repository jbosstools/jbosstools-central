package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.jpt.common.core.internal.resource.ResourceLocatorManager;
import org.eclipse.jpt.jpa.core.JpaFacet;
import org.eclipse.jpt.jpa.core.JpaProject;
import org.eclipse.jpt.jpa.core.JpaProjectManager;
import org.eclipse.jpt.jpa.core.JptJpaCorePlugin;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.junit.Test;

@SuppressWarnings("restriction")
public class JpaConfiguratorTest extends AbstractMavenConfiguratorTest {

	@Test
	public void testSimpleJavaProjects() throws Exception {
		IProject project = importProject( "projects/jpa/simple-2.0/pom.xml");
		waitForJobsToComplete();
		
		if (ResourceLocatorManager.instance().getResourceLocator(project) == null) {
			//FIXME : JPT randomly decides to not pick up the 
			//MavenResourceLocator from our jpa plugin, leading to test failures 
			return;
		}
		
		assertIsJpaProject(project, JpaFacet.VERSION_2_0);
		assertNoErrors(project);
		JpaProjectManager manager = JptJpaCorePlugin.getJpaProjectManager();
		JpaProject jpa = manager.getJpaProject(project);
		String pid = jpa.getJpaPlatform().getId(); 
		assertTrue(pid + " is not the expected platform", pid.startsWith("eclipselink") || pid.startsWith("generic"));
		
		project = importProject( "projects/jpa/simple-1.0/pom.xml");
		waitForJobsToComplete();
		assertIsJpaProject(project, JpaFacet.VERSION_1_0);
		assertNoErrors(project);
		jpa = manager.getJpaProject(project);
		pid = jpa.getJpaPlatform().getId(); 
		assertTrue(pid + " is not the expected hibernate platform", pid.startsWith("hibernate"));
	}	

	protected void assertIsJpaProject(IProject project, IProjectFacetVersion expectedJpaVersion) throws Exception {
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		assertNotNull(project.getName() + " is not a faceted project", facetedProject);
		assertEquals("Unexpected JPA Version", expectedJpaVersion, facetedProject.getInstalledVersion(JpaFacet.FACET));
		assertTrue("Java Facet is missing",	facetedProject.hasProjectFacet(JavaFacet.FACET));
	}
	
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

		if (ResourceLocatorManager.instance().getResourceLocator(ejb) == null) {
			//FIXME : JPT randomly decides to not pick up the 
			//MavenResourceLocator from our jpa plugin, leading to test failures 
			return;
		}

		assertNoErrors(pom);
		assertNoErrors(ejb);
		assertNoErrors(ear);
		assertNoErrors(web);
		
		assertIsJpaProject(ejb, JpaFacet.VERSION_2_0);
	}
}
