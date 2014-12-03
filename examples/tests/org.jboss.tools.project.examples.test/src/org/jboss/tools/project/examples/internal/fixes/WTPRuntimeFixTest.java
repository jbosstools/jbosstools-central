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
package org.jboss.tools.project.examples.internal.fixes;

import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_HOME;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_RUNTIME_ID;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_RUNTIME_NAME;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_SERVER_ID;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_SERVER_NAME;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.createJBossServer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.project.examples.fixes.AbstractRuntimeFix;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Fred Bricon
 * 
 */
public class WTPRuntimeFixTest {

	@Test
	public void parseRuntimeKeys() {

		String[] values = new String[] { "fo.o.o{bar:[a,b)}", "f.o.o{GATEIN:(3.6)}", "f.o.o", "azdea{az:1.2}" };

		StringBuilder input = new StringBuilder();
		int k = 0;
		for (String s : values) {
			input.append(s).append(",");
			if (++k % 2 == 1) {
				input.append(" ");
			}
		}

		List<String> result = AbstractRuntimeFix.parseRuntimeKeys(input.toString());

		assertEquals(values.length, result.size());
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[i], result.get(i));
		}
	}
	
	private RuntimeFixProvider provider;
	
	@BeforeClass
	public static void initialize() throws CoreException {
		createJBossServer(new File(JBOSS_AS_HOME), JBOSS_AS_SERVER_ID, JBOSS_AS_RUNTIME_ID, JBOSS_AS_SERVER_NAME, JBOSS_AS_RUNTIME_NAME);
		assertEquals("No JBoss AS runtime has been initialized, make sure a maven build has run before", 1, ServerCore.getServers().length);
	}
	
	@Before
	public void setup() {
		provider = new RuntimeFixProvider();
	}
	
	
	@Test
	public void jbossas42_requirement_is_satisfied() {
		RequirementModel requirement = createRequirement(JBOSS_AS_RUNTIME_ID, null);
		WTPRuntimeFix fix = provider.create(null, requirement);
		assertTrue(fix.isSatisfied());
	}

	@Test
	public void any_server_requirement_is_satisfied() {
		RequirementModel requirement = createRequirement("any", null);
		WTPRuntimeFix fix = provider.create(null, requirement);
		assertTrue(fix.isSatisfied());
	}
	
	@Test
	public void some_requirement_is_not_satisfied() {
		RequirementModel requirement = createRequirement("foo", null);
		WTPRuntimeFix fix = provider.create(null, requirement);
		assertFalse(fix.isSatisfied());
	}
	
	@Test
	public void wildlfy_requirement_is_downloadable() {
		RequirementModel requirement = createRequirement("org.jboss.ide.eclipse.as.runtime.71, org.jboss.ide.eclipse.as.runtime.wildfly.80", null);
		WTPRuntimeFix fix = provider.create(null, requirement);
		Collection<DownloadRuntime> runtimes = fix.getDownloadRuntimes(new NullProgressMonitor());
		assertTrue(runtimes.size() > 2);//several runtimes of these families are available for d/l
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void unsupported_type_throws_exception() {
		RequirementModel requirement = new RequirementModel("foo");
		provider.create(null, requirement);
	}
	
	protected RequirementModel createRequirement(String allowedTypes, String downloadId) {
		RequirementModel requirement = new RequirementModel("wtpruntime");
		requirement.getProperties().put(RequirementModel.ALLOWED_TYPES, allowedTypes);
		requirement.getProperties().put(RequirementModel.DOWNLOAD_ID, downloadId);
		return requirement;
	}
}
