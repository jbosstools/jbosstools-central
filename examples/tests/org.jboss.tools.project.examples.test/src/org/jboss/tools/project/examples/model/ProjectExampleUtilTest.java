/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import static org.junit.Assert.*;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProjectExampleUtilTest {

	private IPreferenceStore store;
	
	@Before
	public void setUp() {
		store = ProjectExamplesActivator.getDefault().getPreferenceStore();
	}
	

	@After
	public void tearDown() {
		setUserSites(null);
	}


	@Test
	public void testGetUserSites() throws Exception {
		
		String sites = "<sites><site url=\"file:/foo.xml\" name=\"foo\" experimental=\"true\" editable=\"false\"/>"
				      + "<site url=\"file:/bar.xml\" name=\"bar\" experimental=\"false\" editable=\"true\"/></sites>";
		
		setUserSites(sites);

		Set<IProjectExampleSite> userSites = ProjectExampleUtil.getUserSites();
		assertEquals(2, userSites.size());
		for (IProjectExampleSite us : userSites) {
			switch(us.getName()) {
			case "foo":
				assertEquals("file:/foo.xml", us.getUrl().toString());
				assertTrue(us.isExperimental());
				assertFalse(us.isEditable());
				break;
			case "bar":
				assertEquals("file:/bar.xml", us.getUrl().toString());
				assertFalse(us.isExperimental());
				assertTrue(us.isEditable());
				break;
			default:
				fail("Unexpected site found "+us.getName());
			}
		}
	}

	@Test
	public void testGetBadUserSites() throws Exception {
		Set<IProjectExampleSite> userSites = ProjectExampleUtil.getUserSites();
		assertNotNull(userSites);
		assertTrue(userSites.isEmpty());
		
		
		setUserSites("<sites/>");
		userSites = ProjectExampleUtil.getUserSites();
		assertNotNull(userSites);
		assertTrue(userSites.isEmpty());
		
		setUserSites("<foo/>");
		userSites = ProjectExampleUtil.getUserSites();
		assertNotNull(userSites);
		assertTrue(userSites.isEmpty());
	}
	private void setUserSites(String xml) {
		if (xml == null) {
			store.setToDefault(ProjectExamplesActivator.USER_SITES);
		} else {
			store.putValue(ProjectExamplesActivator.USER_SITES, xml);	
		}
	}
}
