package org.jboss.tools.maven.configurators.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.jboss.tools.maven.jsf.configurators.JSFProjectConfigurator;
import org.junit.Test;

@SuppressWarnings("restriction")
public class SeamConfiguratorTest extends AbstractMavenConfiguratorTest {

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

}
