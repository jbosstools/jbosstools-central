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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil;
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
	
	@Test
	public void testGetDefaultExamplesDirectory() throws CoreException {
		ProjectExamplesTestUtil.removeProjects();
		try {
		  IPath exampleDir = ProjectExampleUtil.getDefaultExamplesDirectory();
		  assertEquals("examples", exampleDir.lastSegment());
		  
		  int nbProjects = 5;
		  createExampleProjects("examples", nbProjects);
		  exampleDir = ProjectExampleUtil.getDefaultExamplesDirectory();
		  assertEquals("examples_"+(nbProjects+1), exampleDir.lastSegment());
		} finally {
		  ProjectExamplesTestUtil.removeProjects();
		}
		
	}
	
	private void createExampleProjects(String baseName, int nbProjects) throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		for (int i = 1; i<= nbProjects; i++) {
		  String projectName = baseName;
		  if (i > 1) {
			  projectName += "_"+i; 
		  }
		  IProject project = root.getProject(projectName);
		  if (!project.exists()) {
			IProjectDescription projectDescription = workspace.newProjectDescription(projectName);
			project.create(projectDescription, monitor);    
			project.open(IResource.NONE, monitor);
		  }
		}
	}
	
}
