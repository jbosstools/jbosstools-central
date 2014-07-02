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
package org.jboss.tools.project.examples.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.tools.project.examples.model.ProjectImportUtil;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Fred Bricon
 *
 */
public class ProjectImportUtilTest {

	@BeforeClass
	public static void removeProjects() throws Exception {
		ProjectExamplesUtil.removeProjects();
	}
	
	@Test
	public void testImport() throws Exception {
		ProjectImportUtil importer = new ProjectImportUtil();
		IPath root = new Path(new File("resources/projects/multiple-imports").getAbsolutePath());
		Collection<String> projectNames = Arrays.asList("java6",  "java", "idontexistbutidontcare", "nested");
		Collection<IProject> projects = importer.importProjects(root, projectNames, new NullProgressMonitor());
		
		//nested projects are ignored
		assertEquals(2, projects.size());
		
		assertContains(projects, "java");
		assertContains(projects, "java6");

		//Only projects not already created are imported
		projectNames = Arrays.asList("java6",  "java", "ignored");
		projects = importer.importProjects(root, projectNames, new NullProgressMonitor());
		assertEquals(1, projects.size());
		assertContains(projects, "ignored");
	}
	
	
	private void assertContains(Collection<IProject> projects, String projectName) {
		for (IProject p : projects) {
			if (p.getName().equals(projectName)) {
				assertTrue(p.getName() + " is closed", p.isOpen());
				return;
			}
		}
		fail(projectName + " is missing");
	}

}
