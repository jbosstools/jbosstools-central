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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.jboss.tools.project.examples.seam.internal.fixes.SeamProjectFixProvider;
import org.jboss.tools.project.examples.seam.internal.fixes.SeamRuntimeFix;
import org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.core.project.facet.SeamVersion;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SeamFixTest {
	
	private SeamProjectFixProvider provider;
	
	@BeforeClass
	public static void initialize() {
		ProjectExamplesTestUtil.createSeamRuntime(ProjectExamplesTestUtil.SEAM_RUNTIME_NAME, ProjectExamplesTestUtil.SEAM_HOME_PROPERTY, SeamVersion.SEAM_2_0);
		assertEquals("No Seam runtime has been initialized, make sure a maven build has run before", 1, SeamRuntimeManager.getInstance().getRuntimes().length);
	}
	
	@Before
	public void setup() {
		provider = new SeamProjectFixProvider();
	}
	
	
	@Test
	public void seam20_requirement_is_satisfied() {
		RequirementModel requirement = createRequirement("2.0", null);
		SeamRuntimeFix fix = provider.create(null, requirement);
		assertTrue(fix.isSatisfied());
	}

	@Test
	public void any_seam_requirement_is_satisfied() {
		RequirementModel requirement = createRequirement("any", null);
		SeamRuntimeFix fix = provider.create(null, requirement);
		assertTrue(fix.isSatisfied());
	}
	
	@Test
	public void seam231_requirement_is_not_satisfied() {
		RequirementModel requirement = createRequirement("2.3.1", "seam231runtime");
		SeamRuntimeFix fix = provider.create(null, requirement);
		assertFalse(fix.isSatisfied());
	}
	
	@Test
	public void seam231_requirement_is_downloadable() {
		RequirementModel requirement = createRequirement("2.3.1", "seam231runtime");
		SeamRuntimeFix fix = provider.create(null, requirement);
		Collection<DownloadRuntime> runtimes = fix.getDownloadRuntimes(new NullProgressMonitor());
		assertNotNull(runtimes);
		assertEquals(1, runtimes.size());
		assertEquals("JBoss Seam 2.3.1", runtimes.iterator().next().getName());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void unsupported_type_throws_exception() {
		RequirementModel requirement = new RequirementModel("foo");
		provider.create(null, requirement);
	}
	
	protected RequirementModel createRequirement(String version, String downloadId) {
		RequirementModel requirement = new RequirementModel("seam");
		requirement.getProperties().put(RequirementModel.ALLOWED_VERSIONS, version);
		requirement.getProperties().put(RequirementModel.DOWNLOAD_ID, downloadId);
		return requirement;
	}
}
