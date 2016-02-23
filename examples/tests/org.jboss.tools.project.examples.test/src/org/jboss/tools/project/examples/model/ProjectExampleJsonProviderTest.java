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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.project.examples.internal.ProjectExampleJsonProvider;
import org.junit.Before;
import org.junit.Test;

public class ProjectExampleJsonProviderTest {

	private ProjectExampleJsonProvider provider;
	
	private IProgressMonitor monitor;
	
	@Before
	public void setUp() {
		provider = new ProjectExampleJsonProvider();
		monitor = new NullProgressMonitor();
	}
	
	@Test
	public void testEap7Examples() throws CoreException {
		File searchJson = new File("resources", "search.json");
		List<ProjectExample> examples = new ArrayList<>(provider.getExamples(searchJson, monitor));
		assertEquals(239, examples.size());
		{
			ProjectExample exampleEAP7 = examples.get(0);
			assertEquals("cdi-veto", exampleEAP7.getShortDescription());
			assertEquals("Creating a basic CDI extension to demonstrate vetoing beans.", exampleEAP7.getDescription());
			List<RequirementModel> reqs = exampleEAP7.getRequirements();
			assertEquals(2, reqs.size());
			RequirementModel serverReq = reqs.get(0);
			assertEquals("wtpruntime", serverReq.getType());
			assertEquals("jbosseap700runtime", serverReq.getProperties().get(RequirementModel.DOWNLOAD_ID));
			assertEquals("org.jboss.ide.eclipse.as.runtime.eap.70,org.jboss.ide.eclipse.as.runtime.wildfly.80,org.jboss.ide.eclipse.as.runtime.wildfly.90,org.jboss.ide.eclipse.as.runtime.wildfly.100", serverReq.getProperties().get(RequirementModel.ALLOWED_TYPES));
			
			RequirementModel pluginReq = reqs.get(1);
			assertEquals("plugin", pluginReq.getType());
			assertEquals("org.jboss.tools.maven.cdi", pluginReq.getProperties().get(RequirementModel.ID));
		}
		{
			ProjectExample exampleEAP6 = examples.get(238);
			assertEquals("helloworld-rs", exampleEAP6.getShortDescription());
			assertEquals("The `helloworld-rs` quickstart demonstrates a simple Hello World application, bundled and deployed as a WAR, that uses *JAX-RS* to say Hello.", exampleEAP6.getDescription());
			List<RequirementModel> reqs = exampleEAP6.getRequirements();
			assertEquals(2, reqs.size());
			RequirementModel serverReq = reqs.get(0);
			assertEquals("wtpruntime", serverReq.getType());
			assertEquals("jbosseap640runtime", serverReq.getProperties().get(RequirementModel.DOWNLOAD_ID));
			assertEquals("org.jboss.ide.eclipse.as.runtime.eap.61,org.jboss.ide.eclipse.as.runtime.eap.70,org.jboss.ide.eclipse.as.runtime.wildfly.80,org.jboss.ide.eclipse.as.runtime.wildfly.90,org.jboss.ide.eclipse.as.runtime.wildfly.100", serverReq.getProperties().get(RequirementModel.ALLOWED_TYPES));
			
			RequirementModel pluginReq = reqs.get(1);
			assertEquals("plugin", pluginReq.getType());
			assertEquals("org.jboss.tools.maven.cdi", pluginReq.getProperties().get(RequirementModel.ID));
		}
	}
}
