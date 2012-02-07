package org.jboss.tools.maven.configurators.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.jst.web.kb.IKbProject;
import org.jboss.tools.maven.jsf.configurators.JSFProjectConfigurator;
import org.jboss.tools.seam.core.ISeamProject;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.junit.Test;

@SuppressWarnings("restriction")
public class SeamConfiguratorTest extends AbstractMavenConfiguratorTest {

	private static final IProjectFacet SEAM_FACET = ProjectFacetsManager.getProjectFacet("jst.seam");
	
	@Test
	public void testJBIDE9454_webXml_overwrite() throws Exception {
		IProject project = importAndCheckSeamProject("seam-webxml");
		assertIsJSFProject(project, JSFProjectConfigurator.JSF_FACET_VERSION_1_2);
	}	
	
	@Test
	public void testJBIDE6228_webXml_changed_richfaces() throws Exception {
		IProject project = importAndCheckSeamProject("seamIntegration");
		assertIsJSFProject(project, JSFProjectConfigurator.JSF_FACET_VERSION_1_2);
	}
	
	protected IProject importAndCheckSeamProject(String projectName) throws Exception {
		String projectLocation = "projects/seam/"+projectName;
		String webxmlRelPath = "src/main/webapp/WEB-INF/web.xml";
		
		IProject seamProject = importProject(projectLocation+"/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(seamProject);
		
		IFile webXml = seamProject.getFile(webxmlRelPath);
		assertTrue(webXml.exists());
		File originalWebXml = new File(projectLocation, webxmlRelPath);
		assertEquals("web.xml content changed ", toString(originalWebXml), toString(webXml));
		return seamProject;
	}	

	@Test
	public void testJBIDE10764_builderOrder() throws Exception {
		IProject ejb = importProject("projects/seam/JBIDE-10764/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(ejb);
		assertTrue("Seam nature is missing", ejb.hasNature(ISeamProject.NATURE_ID));
		assertTrue("KB nature is missing", ejb.hasNature(IKbProject.NATURE_ID));
	}
	
	@Test
	public void testJBIDE10790_earConfiguration()  throws Exception {
		IProject[] projects = importProjects("projects/seam/multi", 
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

		assertTrue("Seam nature is missing", ejb.hasNature(ISeamProject.NATURE_ID));
		assertTrue("KB nature is missing", ejb.hasNature(IKbProject.NATURE_ID));

		IFacetedProject fpWeb = ProjectFacetsManager.create(web);
		assertEquals(SEAM_FACET.getVersion("2.2"), fpWeb.getProjectFacetVersion(SEAM_FACET));
		IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(web);
		assertEquals(ISeamFacetDataModelProperties.DEPLOY_AS_EAR, prefs.get(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS, null));
	}
}
