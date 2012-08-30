package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

public class JSFConfiguratorTest extends AbstractConfiguratorsTest{
	
	

	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@Test
	public void testJSFConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_noRuntime", false);
		addDependencies(PROJECT_NAME_JSF+"_noRuntime", "com.sun.faces", "mojarra-jsf-api", "2.0.0-b04",null);
		updateConf(botUtil,PROJECT_NAME_JSF+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+" with mojarra dependency doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_noRuntime", JSF_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_noRuntime", false);
		addFacesConf(PROJECT_NAME_JSF+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+" with faces config doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_noRuntime", JSF_NATURE));
		clean();
		
		//https://issues.jboss.org/browse/JBIDE-10831
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_noRuntime", false);
		addServlet(PROJECT_NAME_JSF+"_noRuntime","Faces Servlet","javax.faces.webapp.FacesServlet","1");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+"with servlet in web.xml doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_noRuntime", JSF_NATURE));
		IProject facade = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME_JSF+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JSF+"_noRuntime"+" doesn't have faces-config.xml file",facade.getProject().getFile("faces-config.xml") != null);
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF, true);
		assertTrue("Project "+PROJECT_NAME_JSF+" doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF, JSF_NATURE));
		clean();
		
		//https://issues.jboss.org/browse/JBIDE-8755
		createMavenizedDynamicWebProject(PROJECT_NAME_JSF+"_seam", false);
		addDependencies(PROJECT_NAME_JSF+"_seam", "org.jboss.seam.faces", "seam-faces", "3.0.0.Alpha3",null);
		updateConf(botUtil,PROJECT_NAME_JSF+"_seam");
		assertTrue("Project "+PROJECT_NAME_JSF+"_seam"+" with seam-faces3 dependency doesn't have "+JSF_NATURE+" nature",hasNature(PROJECT_NAME_JSF+"_seam", JSF_NATURE));
		
		
		
	}
}
