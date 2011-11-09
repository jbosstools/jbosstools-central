package org.jboss.tools.maven.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.jboss.tools.maven.configurators.tests.AbstractMavenConfiguratorTest;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ProjectUtilTest extends AbstractMavenConfiguratorTest {

	@Test
	public void testShouldRefreshProjectHierarchy() throws IOException, CoreException, InterruptedException {
		//String projectLocation = "projects/jaxrs/jaxrs-3layers-root/jaxrs-3layers-middle/jaxrs-3layers-child";
		IProject[] jaxrsProject = importProjects("projects/jaxrs/", 
				new String[]{"jaxrs-3layers-root/pom.xml",
				"jaxrs-3layers-root/jaxrs-3layers-middle/pom.xml",
				"jaxrs-3layers-root/jaxrs-3layers-middle/jaxrs-3layers-child/pom.xml"}, new ResolverConfiguration());
		waitForJobsToComplete(new NullProgressMonitor());
		File basedir = jaxrsProject[2].getLocation().toFile();
		assertEquals(3, ProjectUtil.refreshHierarchy(basedir, IResource.DEPTH_INFINITE, new NullProgressMonitor()));		
	}
}
