/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.markers;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.ui.IMarkerResolution;
import org.jboss.tools.maven.ui.internal.markers.ConfigureMavenRepositoriesMarkerResolution;
import org.jboss.tools.maven.ui.internal.markers.ConfigureRedHatRepositoryMarkerResolution;
import org.jboss.tools.maven.ui.internal.markers.MavenDependencyMarkerResolutionGenerator;
import org.jboss.tools.maven.ui.internal.markers.OpenPageInBrowserMarkerResolution;

public class MarkerResolutionTest extends AbstractMavenProjectTestCase {

	MavenDependencyMarkerResolutionGenerator markerGenerator = new MavenDependencyMarkerResolutionGenerator();
	
	public void testDefaultMarkerResolution() throws Exception {
		testMarkerResolution("p1", ConfigureMavenRepositoriesMarkerResolution.class);
	}

	public void testJBossMarkerResolution() throws Exception {
		testMarkerResolution("p2", ConfigureMavenRepositoriesMarkerResolution.class,
								   OpenPageInBrowserMarkerResolution.class);
	}

	public void testRedHatMarkerResolution() throws Exception {
		testMarkerResolution("p3", ConfigureRedHatRepositoryMarkerResolution.class,
								   OpenPageInBrowserMarkerResolution.class);
	}

	public void testMarkerResolution(String projectName, Class ... markerClasses) throws Exception {
		IProject project = importProject("projects/markers/"+projectName+"/pom.xml");
		assertNotNull(project);
		
		List<IMarker> markers = findErrorMarkers(project);
		assertEquals("Unexpected marker size : " + toString(markers),  2, markers.size());
		assertTrue(markers.get(0).getAttribute(IMarker.MESSAGE).toString().startsWith("The container 'Maven Dependencies' references non existing library")); 
		IMarkerResolution[] resolutions = markerGenerator.getResolutions(markers.get(1));
		assertEquals("Unexpected resolution number", markerClasses.length, resolutions.length);
		int i = 0;
		for (Class<?> mc : markerClasses) {
			assertEquals(mc, resolutions[i++].getClass());
		}
	}

}
