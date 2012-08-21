/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.profiles.tests;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jboss.tools.maven.profiles.core.profiles.ProfileState;
import org.jboss.tools.maven.profiles.core.profiles.ProfileStatus;
import org.junit.Test;
import static org.junit.Assert.*;

@SuppressWarnings("restriction")
public class MavenProfileManagerTest extends AbstractMavenProfileTest {

	@Test
	public void testJBIDE12442_parentFromRemote() throws Exception {
		String pomPath = "projects/12442/pom.xml";
		IProject project = importProject(pomPath);
		waitForJobsToComplete();
		assertNotNull(pomPath+" could not be imported", project);
		
		IMavenProjectFacade facade = getFacade(project);
		List<ProfileStatus> profiles = profileManager.getProfilesStatuses(facade, monitor);
		assertEquals(profiles.toString(), 5, profiles.size());
		for (ProfileStatus p : profiles) {
			String pid = p.getId();
			if ("other-parent-profile".equals(pid)) {
				assertFalse(p.isAutoActive());//parent profile activation is not inherited
				assertEquals(ProfileState.Inactive, p.getActivationState());
			} else if ("inactive-settings-profile".equals(pid)) {
				assertFalse(p.isAutoActive());
				assertEquals(ProfileState.Inactive, p.getActivationState());
			} else if ("active-settings-profile".equals(pid)) {
				assertTrue(p.isAutoActive());
				assertEquals(ProfileState.Active, p.getActivationState());
			} else if ("activebydefault-settings-profile".equals(pid)) {
				assertTrue(p.isAutoActive());
				assertEquals(ProfileState.Active, p.getActivationState());
			} else if ("parent-profile".equals(pid)) {
				assertFalse(p.isAutoActive());
				assertEquals(ProfileState.Inactive, p.getActivationState());
			} else {
				fail("Unexpected profile "+pid);
			}
		}
		
	}

}
