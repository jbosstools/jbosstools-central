/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.tests;

import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jboss.tools.maven.conversion.ui.internal.AbstractReferenceConversionParticipant;
import org.junit.Test;

public class AppClientDependencyConversionParticipantTest extends
		AbstractMavenConversionTest {

	@Test
	public void testAddMavenAcrPluginToEar() throws Exception {
		checkJBIDE13781_MavenAcrPlugin("JBIDE-13781-ear");
	}

	@Test
	public void testAddMavenAcrPluginToWar() throws Exception {
		checkJBIDE13781_MavenAcrPlugin("JBIDE-13781-web");
	}

	@Test
	public void testAddMavenAcrPluginToPlainJava() throws Exception {
		checkJBIDE13781_MavenAcrPlugin("JBIDE-13781-java");
	}
		
	private void checkJBIDE13781_MavenAcrPlugin(String projectName) throws Exception {
		IProject appClient = importProject("projects/conversion/JBIDE-13781/earClient/pom.xml");
	
		 //Import existing regular Eclipse project
	    IProject project = createExisting(projectName, "projects/conversion/JBIDE-13781/"+projectName);
	    assertTrue(projectName+ " was not created!", project.exists());
	    assertNoErrors(project);
	    IMavenProjectFacade mavenFacade; 
	    try {
	    	System.setProperty(AbstractReferenceConversionParticipant.REFERENCE_CONVERSION_SKIP_KEY, "true");
	    	mavenFacade = convert(project);
	    } finally {
	    	System.clearProperty(AbstractReferenceConversionParticipant.REFERENCE_CONVERSION_SKIP_KEY);
	    }
	    List<Dependency> deps = mavenFacade.getMavenProject(monitor).getDependencies();
	    assertEquals(1, deps.size());
	    assertEquals("app-client", deps.get(0).getType());
	    
	    Build build = mavenFacade.getMavenProject(monitor).getModel().getBuild();
	    Plugin acrPlugin = build.getPluginsAsMap().get("org.apache.maven.plugins:maven-acr-plugin");
	    assertNotNull("maven-acr-plugin is missing", acrPlugin);
	    assertTrue("maven-acr-plugin not set as extension", acrPlugin.isExtensions());
	}
}
