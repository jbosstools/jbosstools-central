/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.junit.Test;

@SuppressWarnings("restriction")
public class FixClasspathConfiguratorTest extends AbstractMavenConfiguratorTest {
	@Test
	public void testJBIDE14589_duplicateCPE()  throws Exception {
		
	    IWorkspaceDescription description = workspace.getDescription();
	    try {
	    	description.setAutoBuilding(true);
		
			IProject[] projects = importProjects("projects/seam/JBIDE-14589/parent-reactor", 
					new String[]{ "pom.xml",
								  "child-module/pom.xml",
								  "other-module/pom.xml",
								  }, 
					new ResolverConfiguration());
			waitForJobsToComplete();
			
			IProject parent = projects[0];
			IProject child = projects[1];
			IProject other = projects[2];
			assertNoErrors(parent);
			assertNoErrors(child);
			assertNoErrors(other);
			
			updateProject(child);
			assertNoErrors(child);
			
			updateProject(other);
			assertNoErrors(other);
		
	    } finally {
	    	description.setAutoBuilding(false);
	    }
	}
}