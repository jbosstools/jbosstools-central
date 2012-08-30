package org.jboss.tools.maven.ui.bot.test;

import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

public class CDIConfiguratorTest extends AbstractConfiguratorsTest{
	
	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@Test
	public void testCDIConfigurator() throws Exception{
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime", "javax.enterprise", "cdi-api","1.1.EDR1.2",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime"+" with cdi dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime", "javax.enterprise", "cdi-api","1.1.EDR1.2",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime"+" with cdi dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime", CDI_NATURE));
		clean();

		createMavenizedDynamicWebProject(PROJECT_NAME_CDI, true);
		assertFalse("Project "+PROJECT_NAME_CDI+" has "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI, CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB, true);
		assertFalse("Project "+PROJECT_NAME_CDI_EJB+" has "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB, CDI_NATURE));
		clean();
		
		//https://issues.jboss.org/browse/JBIDE-8755
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_seam", "org.jboss.seam.faces", "seam-faces", "3.0.0.Alpha3",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_seam"+" with seam-faces3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_seam", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", "org.jboss.seam.faces", "seam-faces", "3.0.0.Alpha3",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_seam"+" with seam-faces3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", CDI_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_seam", "org.jboss.seam.international", "seam-international", "3.0.0.Alpha1",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_seam"+" with seam3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_seam", CDI_NATURE));
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", "org.jboss.seam.international", "seam-international", "3.0.0.Alpha1",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_seam");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_seam"+" with seam3 dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_seam", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api", "org.apache.deltaspike.core", "deltaspike-core-api", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api"+" with deltaspike-api dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-api", CDI_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_deltaspike-api", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_deltaspike-api", "org.apache.deltaspike.core", "deltaspike-core-api", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_deltaspike-api");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_deltaspike-api"+" with deltaspike-api dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_deltaspike-api", CDI_NATURE));
		clean();
		
		createMavenizedEJBProject(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl", false);
		addDependencies(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl", "org.apache.deltaspike.core", "deltaspike-core-impl", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl");
		assertTrue("Project "+PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl"+" with deltaspike-impl dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI_EJB+"_noRuntime_deltaspike-impl", CDI_NATURE));
		clean();
		
		createMavenizedDynamicWebProject(PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl", false);
		addDependencies(PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl", "org.apache.deltaspike.core", "deltaspike-core-impl", "incubating-0.1-SNAPSHOT",null);
		updateConf(botUtil,PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl");
		assertTrue("Project "+PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl"+" with deltaspike-impl dependency doesn't have "+CDI_NATURE+" nature.",hasNature(PROJECT_NAME_CDI+"_noRuntime_deltaspike-impl", CDI_NATURE));
		
	}

}
