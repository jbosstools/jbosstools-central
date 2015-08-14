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
package org.jboss.tools.project.examples.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Arrays;
import java.util.HashSet;

import org.jboss.tools.project.examples.fixes.ProjectFixManager;
import org.jboss.tools.project.examples.internal.fixes.PluginFix;
import org.jboss.tools.project.examples.internal.fixes.UnsupportedFixProvider.NoopFix;
import org.jboss.tools.project.examples.internal.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.ArchetypeModel;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.jboss.tools.project.examples.seam.internal.fixes.SeamRuntimeFix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProjectExampleManagerTest {

	private ProjectExampleManager projectExampleManager;

	@Before
	public void setUp() throws Exception {
		projectExampleManager = new ProjectExampleManager(new ProjectFixManager());
	}

	@After
	public void tearDown() throws Exception {
		projectExampleManager = null;
	}

	@Test
	public void testCreateWorkingCopy() throws Exception {
		ProjectExampleWorkingCopy example = new ProjectExampleWorkingCopy();
		ArchetypeModel archetypeModel = new ArchetypeModel();
		archetypeModel.setArchetypeArtifactId("archetypeArtifactId");
		archetypeModel.setArchetypeGroupId("archetypeGroupId");
		archetypeModel.setArchetypeRepository("archetypeRepository");
		archetypeModel.setArchetypeVersion("archetypeVersion");
		archetypeModel.setArtifactId("artifactId");
		archetypeModel.setGroupId("groupId");
		archetypeModel.setJavaPackage("javaPackage");
		archetypeModel.setVersion("version");
		example.setArchetypeModel(archetypeModel );
		example.setEssentialEnterpriseDependencyGavs(new HashSet<>(Arrays.asList("foo", "bar")));
		example.setHeadLine("headline");
		example.setImportType("importType");
		example.setIncludedProjects(Arrays.asList("papa","mama"));
		example.setName("name");
		example.setType("type");
		example.setUrl("url");
		example.setWelcome(true);
		example.setWelcomeFixRequired(true);
		example.setWelcomeURL("welcomeURL");
		example.setVersion("6.6.6");
		
		example.getRequirements().add(new RequirementModel("plugin"));
		example.getRequirements().add(new RequirementModel("unknown"));
		example.getRequirements().add(new RequirementModel("wtpruntime"));
		example.getRequirements().add(new RequirementModel("seam"));
		
		
		ProjectExampleWorkingCopy workingCopy = projectExampleManager.createWorkingCopy(example);
		//assertSame(workingCopy, example);
		// TODO fix example builder
		assertNotNull(workingCopy);
		assertNotNull(workingCopy.getFixes());
		assertEquals(workingCopy.getRequirements().size(), workingCopy.getFixes().size());
		assertEquals(PluginFix.class, workingCopy.getFixes().get(0).getClass());
		assertEquals(NoopFix.class, workingCopy.getFixes().get(1).getClass());
		assertEquals(WTPRuntimeFix.class, workingCopy.getFixes().get(2).getClass());
		assertEquals(SeamRuntimeFix.class, workingCopy.getFixes().get(3).getClass());
		
		ArchetypeModel newModel = workingCopy.getArchetypeModel();
		assertNotSame(archetypeModel, newModel);
		assertEquals(archetypeModel, newModel);
		
		assertEquals(example.getId(), workingCopy.getId());
		assertEquals(example.getEssentialEnterpriseDependencyGavs(), workingCopy.getEssentialEnterpriseDependencyGavs());
		assertEquals(example.getName(), workingCopy.getName());
		assertEquals(example.getHeadLine(), workingCopy.getHeadLine());
		assertEquals(example.getImportType(), workingCopy.getImportType());
		assertEquals(example.getIncludedProjects(), workingCopy.getIncludedProjects());
		assertEquals(example.getName(), workingCopy.getName());
		assertEquals(example.getType(), workingCopy.getType());
		assertEquals(example.getUrl(), workingCopy.getUrl());
		assertEquals(example.getWelcomeURL(), workingCopy.getWelcomeURL());
		assertEquals(example.isWelcome(), workingCopy.isWelcome());
		assertEquals(example.isURLRequired(), workingCopy.isURLRequired());
		assertEquals(example.isWelcomeFixRequired(), workingCopy.isWelcomeFixRequired());
		assertEquals(example.getTrackingId(), workingCopy.getTrackingId());
		assertEquals("name:6.6.6", workingCopy.getTrackingId());
	}

}
