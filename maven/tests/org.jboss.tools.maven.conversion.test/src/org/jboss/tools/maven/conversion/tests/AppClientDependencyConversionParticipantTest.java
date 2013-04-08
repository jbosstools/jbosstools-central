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
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jboss.tools.maven.conversion.ui.internal.AbstractReferenceConversionParticipant;
import org.junit.Test;

public class AppClientDependencyConversionParticipantTest extends
		AbstractMavenConversionTest {

	@Test
	public void testAddMavenAcrPlugin() throws Exception {
		IProject appClient = importProject("projects/conversion/JBIDE-13781/earClient/pom.xml");
	
		 //Import existing regular Eclipse project
	    IProject ear = createExisting("JBIDE-13781-ear", "projects/conversion/JBIDE-13781/JBIDE-13781-ear/");
	    assertTrue("JBIDE-13781-ear was not created!", ear.exists());
	    assertNoErrors(ear);
	    IMavenProjectFacade facadeEar; 
	    try {
	    	System.setProperty(AbstractReferenceConversionParticipant.REFERENCE_CONVERSION_SKIP_KEY, "true");
	    	facadeEar = convert(ear);
	    } finally {
	    	System.clearProperty(AbstractReferenceConversionParticipant.REFERENCE_CONVERSION_SKIP_KEY);
	    }
	    List<Dependency> deps = facadeEar.getMavenProject().getDependencies();
	    assertEquals(1, deps.size());
	    assertEquals("app-client", deps.get(0).getType());
	    
	    Build build = facadeEar.getMavenProject().getModel().getBuild();
	    Plugin acrPlugin = build.getPluginsAsMap().get("org.apache.maven.plugins:maven-acr-plugin");
	    assertNotNull("maven-acr-plugin is missing", acrPlugin);
	    assertTrue("maven-acr-plugin not set as extension", acrPlugin.isExtensions());
	}
}
