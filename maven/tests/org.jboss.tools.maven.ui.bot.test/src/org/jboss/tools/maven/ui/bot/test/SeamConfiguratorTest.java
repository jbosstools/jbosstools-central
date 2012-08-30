package org.jboss.tools.maven.ui.bot.test;

import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

public class SeamConfiguratorTest extends AbstractConfiguratorsTest{

	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@Test
	public void testSeamConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_SEAM+"_noRuntime", false);
		addDependencies(PROJECT_NAME_SEAM+"_noRuntime", "org.jboss.seam", "jboss-seam", "2.3.0.Beta2",null);
		updateConf(botUtil,PROJECT_NAME_SEAM+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_SEAM+"_noRuntime"+" with jboss-seam dependency doesn't have "+SEAM_NATURE+" nature.",hasNature(PROJECT_NAME_SEAM+"_noRuntime", SEAM_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_SEAM+"_noRuntime", false);
		addDependencies(PROJECT_NAME_SEAM+"_noRuntime", "org.jboss.seam", "jboss-seam-ui", "2.3.0.ALPHA",null);
		updateConf(botUtil,PROJECT_NAME_SEAM+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_SEAM+"_noRuntime"+" with jboss-seam-ui dependency doesn't have "+SEAM_NATURE+" nature.",hasNature(PROJECT_NAME_SEAM+"_noRuntime", SEAM_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_SEAM+"_noRuntime", false);
		addDependencies(PROJECT_NAME_SEAM+"_noRuntime", "org.jboss.seam", "jboss-seam-pdf", "2.3.0.ALPHA",null);
		updateConf(botUtil,PROJECT_NAME_SEAM+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_SEAM+"_noRuntime"+" with jboss-seam-pdf dependency doesn't have "+SEAM_NATURE+" nature.",hasNature(PROJECT_NAME_SEAM+"_noRuntime", SEAM_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_SEAM+"_noRuntime", false);
		addDependencies(PROJECT_NAME_SEAM+"_noRuntime", "org.jboss.seam", "jboss-seam-remoting", "2.3.0.ALPHA",null);
		updateConf(botUtil,PROJECT_NAME_SEAM+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_SEAM+"_noRuntime"+" with jboss-seam-remoting dependency doesn't have "+SEAM_NATURE+" nature.",hasNature(PROJECT_NAME_SEAM+"_noRuntime", SEAM_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_SEAM+"_noRuntime", false);
		addDependencies(PROJECT_NAME_SEAM+"_noRuntime", "org.jboss.seam", "jboss-seam-ioc", "2.3.0.ALPHA",null);
		updateConf(botUtil,PROJECT_NAME_SEAM+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_SEAM+"_noRuntime"+" with jboss-seam-ioc dependency doesn't have "+SEAM_NATURE+" nature.",hasNature(PROJECT_NAME_SEAM+"_noRuntime", SEAM_NATURE));
		clean();
	}
}
