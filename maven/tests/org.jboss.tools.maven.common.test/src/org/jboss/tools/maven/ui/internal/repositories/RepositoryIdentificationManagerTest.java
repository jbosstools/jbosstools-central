/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.maven.settings.Repository;
import org.jboss.tools.maven.ui.wizard.RepositoryWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RepositoryIdentificationManagerTest {

	RepositoryIdentificationManager identifier;
	
	@Before
	public void setUp() {
		identifier = new RepositoryIdentificationManager();
	}
	
	@Test
	public void folder_with_marker_should_be_identified() {
		File folder = getRepo("foo");
		RepositoryWrapper repoWrapper = identifier.identifyRepository(folder, null);
		assertNotNull(repoWrapper);
		assertEquals("Awesome-sauce", repoWrapper.getProfileId());
		Repository repo = repoWrapper.getRepository();
		assertEquals("Yeeehaaa", repo.getId());
		assertEquals("Look my Repo Dude!", repo.getName());
	}

	@Test
	public void minimal_marker_should_be_identified() {
		File folder = getRepo("minimal");
		RepositoryWrapper repoWrapper = identifier.identifyRepository(folder, null);
		assertNotNull(repoWrapper);
		assertEquals("Minimal", repoWrapper.getProfileId());
		Repository repo = repoWrapper.getRepository();
		assertEquals(repoWrapper.getProfileId(), repo.getId());
		assertEquals(repoWrapper.getProfileId(), repo.getName());
	}
	
	
	@Test
	public void marker_identification_should_take_precedence_over_everything_else() {
		File folder = getRepo("mixed");
		RepositoryWrapper repoWrapper = identifier.identifyRepository(folder, null);
		assertNotNull(repoWrapper);
		assertEquals("Mixed", repoWrapper.getProfileId());
		Repository repo = repoWrapper.getRepository();
		assertEquals(repoWrapper.getProfileId(), repo.getId());
		assertEquals("Marker wins over the rest", repo.getName());
	}
	
	@Test
	public void eap_identification_should_take_precedence_over_wfk() {
		File folder = getRepo("eapwins");
		RepositoryWrapper repoWrapper = identifier.identifyRepository(folder, null);
		assertNotNull(repoWrapper);
		assertEquals(RepositoryIdentificationManager.EAPRepoIdentifier.JBOSS_EAP_MAVEN_REPOSITORY_ID, repoWrapper.getProfileId());
		Repository repo = repoWrapper.getRepository();
		assertEquals(RepositoryIdentificationManager.EAPRepoIdentifier.JBOSS_EAP_MAVEN_REPOSITORY_ID, repo.getId());
		assertEquals(RepositoryIdentificationManager.EAPRepoIdentifier.JBOSS_EAP_MAVEN_REPOSITORY, repo.getName());
	}

	@Test
	public void wfk_repo_should_be_identified() {
		File folder = getRepo("wfk");
		RepositoryWrapper repoWrapper = identifier.identifyRepository(folder, null);
		assertNotNull(repoWrapper);
		assertEquals(RepositoryIdentificationManager.WFKRepoIdentifier.JBOSS_WFK_MAVEN_REPOSITORY_ID, repoWrapper.getProfileId());
		Repository repo = repoWrapper.getRepository();
		assertEquals(RepositoryIdentificationManager.WFKRepoIdentifier.JBOSS_WFK_MAVEN_REPOSITORY_ID, repo.getId());
		assertEquals(RepositoryIdentificationManager.WFKRepoIdentifier.JBOSS_WFK_MAVEN_REPOSITORY, repo.getName());
	}
	
	@After
	public void tearDown() {
		identifier = null;
	}
	
	private File getRepo(String repoName) {
		return new File("resources"+File.separator+"repositories", repoName);
	}

}
