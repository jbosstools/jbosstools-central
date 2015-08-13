/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArtifactResolutionHelperTest {

	private static IMavenConfiguration mavenConfiguration;
	
	private static String oldUserSettingsFile;

	private IArtifactResolutionService resolutionService;
	
	private List<ArtifactRepository> repositories = new ArrayList<ArtifactRepository>();
	
	private IMaven maven;
	
	private IProgressMonitor monitor; 
	
	@Before
	public void setup() throws Exception {
		maven = MavenPlugin.getMaven();
	    mavenConfiguration = MavenPlugin.getMavenConfiguration();
	    oldUserSettingsFile = mavenConfiguration.getUserSettingsFile();
	    File settings = new File("settings.xml").getCanonicalFile();
	    if(settings.canRead()) {
	      String userSettingsFile = settings.getAbsolutePath();
	      mavenConfiguration.setUserSettingsFile(userSettingsFile);
	    }
	    cleanDirsInLocalRepo("org", "com");
	    resolutionService = MavenCoreActivator.getDefault().getArtifactResolutionService();
	    repositories.addAll(MavenPlugin.getMaven().getArtifactRepositories());
	    monitor = new NullProgressMonitor();
	}
	
	
	@Test
	public void testResolveArtifact() throws Exception {
		String javaee_web = "org.jboss.spec:jboss-javaee-web-6.0:pom:3.0.0.Final-redhat-1";
		
		assertNotResolved(javaee_web);

		ArtifactRepository repo = maven.createArtifactRepository("redhat-ga-repository", "http://maven.repository.redhat.com/ga");

		assertResolved(javaee_web, Collections.singletonList(repo));
		
		//Resolve ejb packaging
		assertTrue("org.codehaus.cargo:simple-ejb:ejb:1.4.4 should be resolved", resolutionService.isResolved("org.codehaus.cargo","simple-ejb","ejb","1.4.4", null, repositories, monitor));
		
		assertTrue("org.agorava:agorava-twitter-cdi:jar:0.6.0:javadoc should be resolved", resolutionService.isResolved("org.agorava","agorava-twitter-cdi", null,"0.6.0", "javadoc", repositories, monitor));

		assertNotResolved("unknown.artifact:"+System.currentTimeMillis()+":foo:[0,1)");

	}

//	private void assertResolved(String coordinates) throws Exception {
//		assertResolved(coordinates, repositories);
//	}

	private void assertResolved(String coordinates, List<ArtifactRepository> repos) throws Exception {
		assertTrue(coordinates + " should be resolved", resolutionService.isResolved(coordinates, repos, monitor));
	}


	private void assertNotResolved(String coordinates) throws Exception {
		assertNotResolved(coordinates, null);
	}

	private void assertNotResolved(String coordinates, List<ArtifactRepository> repositories) throws Exception {
		assertFalse(coordinates + " should not be resolved", resolutionService.isResolved(coordinates, repositories, monitor));
	}

	@Test
	public void testGetReleasedVersions() throws Exception {
		String coords = "org.jboss.seam:jboss-seam:ejb:[0,)";
		ArtifactRepository jbossrepo = maven.createArtifactRepository("jboss-repo", "https://repository.jboss.org/nexus/content/groups/public-jboss/");
		jbossrepo.getSnapshots().setEnabled(true);
		List<ArtifactRepository> repos = Collections.singletonList(jbossrepo);
		List<String> versions = resolutionService.getAvailableReleasedVersions(coords, repos, monitor);
		
		assertNotNull("Versions shouldn't be null", versions);
		assertFalse("Versions shouldn't be empty", versions.isEmpty());
		
		assertTrue(versions.toString(), versions.size() > 2);
		
		for (String v : versions) {
			if (v.endsWith("-SNAPSHOT")) {
				fail("Unexpected SNAPSHOT version found : "+v);
			}
		}
		
		versions = resolutionService.getAvailableReleasedVersions("org.jboss.seam","jboss-seam","ejb", "[0,1)", null, repos, monitor);
		assertEquals(0, versions.size());
		
		versions = resolutionService.getAvailableReleasedVersions("foo",System.currentTimeMillis()+"","bar", "[0,1)", null, repos, monitor);
		assertEquals(0, versions.size());
	}

	@Test
	public void testLatestReleasedVersion() throws Exception {
		String coords = "org.jboss.seam:jboss-seam:ejb:[0,)";
		ArtifactRepository jbossrepo = maven.createArtifactRepository("jboss-repo", "https://repository.jboss.org/nexus/content/groups/public-jboss/");
		jbossrepo.getSnapshots().setEnabled(true);
		List<ArtifactRepository> repos = Collections.singletonList(jbossrepo);
		List<String> versions = resolutionService.getAvailableReleasedVersions(coords, repos, monitor);
		
		assertNotNull("Versions shouldn't be null", versions);
		assertFalse("Versions shouldn't be empty", versions.isEmpty());
		assertTrue(versions.toString(), versions.size() > 2);
		
		String latestVersion= versions.get(versions.size()-1);
		String highestVersion = resolutionService.getLatestReleasedVersion(coords, repos, monitor);
		
		assertEquals(latestVersion, highestVersion);
		
		highestVersion = resolutionService.getLatestReleasedVersion("org.jboss.seam","jboss-seam","ejb", "[2.0.1.GA,2.3.0.Final)", null, repos, monitor);
		assertEquals("2.3.0.CR1",highestVersion);
		
		highestVersion = resolutionService.getLatestReleasedVersion("org.jboss.seam","jboss-seam","ejb", "[100,)", null, repos, monitor);
		assertNull(highestVersion);

	}

	private void cleanDirsInLocalRepo(String ... dirs) throws Exception {
		ArtifactRepository localRepository = MavenPlugin.getMaven().getLocalRepository();
	    if(localRepository == null) {
	      fail("Cannot determine local repository path");
	    }		
	    File localrepo = new File(localRepository.getBasedir());
	    for (String d : dirs) {
	    	FileUtils.deleteDirectory(new File(localrepo, d));
	    }
	}

	@After
	public void tearDown() throws Exception {
	    mavenConfiguration.setUserSettingsFile(oldUserSettingsFile);
	    resolutionService = null;
	    repositories.clear();
	}
	
}
