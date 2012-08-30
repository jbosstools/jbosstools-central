package org.jboss.tools.maven.ui.bot.test;

import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

public class JAXRSConfiguratorTest extends AbstractConfiguratorsTest{
	
	private SWTUtilExt botUtil= new SWTUtilExt(bot);

	@Test
	public void testJAXRSConfigurator() throws Exception {
		createMavenizedDynamicWebProject(PROJECT_NAME_JAXRS+"_noRuntime", false);
		addDependencies(PROJECT_NAME_JAXRS+"_noRuntime", "com.cedarsoft.rest", "jersey", "1.0.0",null);
		updateConf(botUtil,PROJECT_NAME_JAXRS+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JAXRS+"_noRuntime"+" with jersey dependency doesn't have "+JAXRS_NATURE+" nature.",hasNature(PROJECT_NAME_JAXRS+"_noRuntime", JAXRS_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JAXRS+"_noRuntime", false);
		addDependencies(PROJECT_NAME_JAXRS+"_noRuntime", "org.jboss.jbossas", "jboss-as-resteasy", "6.1.0.Final",null);
		updateConf(botUtil,PROJECT_NAME_JAXRS+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JAXRS+"_noRuntime"+" with resteasy dependency doesn't have "+JAXRS_NATURE+" nature.",hasNature(PROJECT_NAME_JAXRS+"_noRuntime", JAXRS_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_JAXRS, true);
		assertTrue("Project "+PROJECT_NAME_JAXRS+" doesn't have "+JAXRS_NATURE+" nature.",hasNature(PROJECT_NAME_JAXRS, JAXRS_NATURE));
	}

}
