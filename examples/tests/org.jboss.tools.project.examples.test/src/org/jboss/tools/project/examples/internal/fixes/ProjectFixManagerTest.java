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
package org.jboss.tools.project.examples.internal.fixes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jboss.tools.project.examples.fixes.ProjectFixManager;
import org.jboss.tools.project.examples.internal.fixes.UnsupportedFixProvider.NoopFix;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.jboss.tools.project.examples.seam.internal.fixes.SeamRuntimeFix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProjectFixManagerTest {
	
	private ProjectFixManager manager;
	
	@Before
	public void setup() {
		manager = new ProjectFixManager();
	}
	
	@After
	public void tearDown() {
		manager = null;
	}

	@Test
	public void testLoadFixesWithEmpty() {
		ProjectExampleWorkingCopy workingCopy = null;
		manager.loadFixes(workingCopy);
		ProjectExample example = new ProjectExample();
	    workingCopy = new ProjectExampleWorkingCopy(example);
	    manager.loadFixes(workingCopy);
	}

	
	@Test
	public void testLoadFixes() {
		ProjectExample example = new ProjectExample();
		example.getRequirements().add(new RequirementModel("plugin"));
		example.getRequirements().add(new RequirementModel("unknown"));
		example.getRequirements().add(new RequirementModel("wtpruntime"));
		example.getRequirements().add(new RequirementModel("seam"));
		ProjectExampleWorkingCopy workingCopy =new ProjectExampleWorkingCopy(example);
		assertNull(workingCopy.getFixes());
		
		manager.loadFixes(workingCopy);

		assertNotNull(workingCopy.getFixes());
		assertEquals(workingCopy.getRequirements().size(), workingCopy.getFixes().size());
		assertEquals(PluginFix.class, workingCopy.getFixes().get(0).getClass());
		assertEquals(NoopFix.class, workingCopy.getFixes().get(1).getClass());
		assertEquals(WTPRuntimeFix.class, workingCopy.getFixes().get(2).getClass());
		assertEquals(SeamRuntimeFix.class, workingCopy.getFixes().get(3).getClass());
	}
}
