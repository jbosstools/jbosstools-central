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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.tools.project.examples.fixes.IProjectExamplesFix;
import org.jboss.tools.project.examples.fixes.ProjectFixManager;
import org.jboss.tools.project.examples.fixes.UIHandler;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.jboss.tools.project.examples.seam.internal.fixes.SeamRuntimeFix;
import org.jboss.tools.project.examples.seam.internal.fixes.SeamRuntimeFixUIHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ProjectFixManagerParameterizedTest {
	
	private ProjectFixManager manager;
	
	@Parameter
	public String type;
	
	@Parameter(value=1)
	public Class<? extends IProjectExamplesFix> expectedFixClazz;

	@Parameter(value=2)
	public Class<? extends UIHandler> expectedUIHandlerClazz;

	private RequirementModel requirement;

	@Parameters(name = "{0}")
	public static Collection<Object[]> loadParameters() {
		List<Object[]> params = new ArrayList<>();
		params.add(new Object[] {"plugin", PluginFix.class, PluginFixUIHandler.class});
		params.add(new Object[] {"seam", SeamRuntimeFix.class, SeamRuntimeFixUIHandler.class});
		params.add(new Object[] {"wtpruntime", WTPRuntimeFix.class, WTPRuntimeFixUIHandler.class});
		params.add(new Object[] {"unknown-fix", UnsupportedFixProvider.NoopFix.class, UnsupportedFixProvider.NoOpUIHandler.class});
		return params;
	}

	@Before
	public void setup() {
		manager = new ProjectFixManager();
		requirement = new RequirementModel(type);
	}
	
	@After
	public void tearDown() {
		manager = null;
	}
	
	@Test
	public void testGetFix() {
		IProjectExamplesFix fix = manager.getFix(null, requirement);
		assertEquals(expectedFixClazz, fix.getClass());
	}

	@Test
	public void testGetUIHandler() {
		IProjectExamplesFix fix = manager.getFix(null, requirement);
		UIHandler handler = manager.getUIHandler(fix);
		assertEquals(expectedUIHandlerClazz, handler.getClass());
	}



}
