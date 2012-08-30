package org.jboss.tools.maven.ui.bot.test;

import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

public class JPAConfiguratorTest extends AbstractConfiguratorsTest{

	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@Test
	public void testJPAConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_JPA+"_noRuntime", false);
		addPersistence(PROJECT_NAME_JPA+"_noRuntime");
		updateConf(botUtil,PROJECT_NAME_JPA+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_JPA+"_noRuntime"+" with persistence.xml file doesn't have "+JPA_NATURE+" nature.",hasNature(PROJECT_NAME_JPA+"_noRuntime", JPA_NATURE));
		clean();
	}
}
