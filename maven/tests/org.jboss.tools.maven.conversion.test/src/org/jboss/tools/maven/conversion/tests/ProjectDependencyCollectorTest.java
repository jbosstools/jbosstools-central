package org.jboss.tools.maven.conversion.tests;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.MavenPlugin;
import org.jboss.tools.maven.conversion.core.DependencyCollector;
import org.jboss.tools.maven.conversion.core.DependencyCollectorFactory;
import org.jboss.tools.maven.conversion.core.ProjectDependency;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ProjectDependencyCollectorTest extends AbstractMavenConversionTest {

	@Test
	public void testCollectEarDependencies() throws Exception {
		IProject web = createExisting("web", "projects/conversion/web");
		IProject ear = createExisting("ear", "projects/conversion/ear");
		waitForJobsToComplete();
		
		DependencyCollector collector = DependencyCollectorFactory.INSTANCE.getDependencyCollector(ear);
		assertNotNull(collector);
		List<ProjectDependency> deps = collector.collectDependencies(ear);
		assertEquals(5, deps.size());
		//archive reference
		assertEquals("/ear/otherlib/commons-logging-1.1.1.jar", deps.get(0).getPath().toPortableString());
		//project reference
		assertEquals(web.getFullPath() , deps.get(1).getPath());
		
		//library reference
		File localRepo = MavenPlugin.getRepositoryRegistry().getLocalRepository().getBasedir();
		String junitLocation = localRepo.getAbsolutePath().replace('\\', '/')+"/junit/junit/4.10/junit-4.10.jar";
		assertEquals(junitLocation, deps.get(2).getPath().toPortableString());
		
		//jar not in lib
		assertEquals("/ear/EarContent/ant-1.8.4.jar"          , deps.get(3).getPath().toPortableString());
		//jar in lib
		assertEquals("/ear/EarContent/lib/commons-cli-1.2.jar", deps.get(4).getPath().toPortableString());
		
	}


	@Test
	public void testCollectWarDependencies() throws Exception {
		IProject util = createExisting("util", "projects/conversion/util");
		IProject war = createExisting("war", "projects/conversion/war");
		waitForJobsToComplete();
		
		DependencyCollector collector = DependencyCollectorFactory.INSTANCE.getDependencyCollector(war);
		assertNotNull(collector);
		List<ProjectDependency> deps = collector.collectDependencies(war);
		assertEquals(4, deps.size());
		
		String baseDir = war.getLocation().toPortableString();

		assertEquals(baseDir+"/WebContent/WEB-INF/lib/ant-1.8.4.jar", deps.get(0).getPath().toPortableString());
		assertEquals(baseDir+"/WebContent/WEB-INF/lib/ant-antlr-1.8.4.jar", deps.get(1).getPath().toPortableString());
		assertEquals(baseDir+"/WebContent/WEB-INF/lib/ant-junit-1.8.4.jar", deps.get(2).getPath().toPortableString());
		assertEquals(util.getFullPath(), deps.get(3).getPath());
	}

}
