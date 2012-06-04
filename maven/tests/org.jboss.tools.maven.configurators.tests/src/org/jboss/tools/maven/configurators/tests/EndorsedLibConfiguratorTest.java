/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.configurators.tests;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IMarkerResolution;
import org.jboss.tools.maven.jdt.internal.markers.MissingEndorsedLibMarkerResolutionGenerator;
import org.jboss.tools.maven.jdt.utils.ClasspathHelpers;
import org.junit.Test;

@SuppressWarnings("restriction")
public class EndorsedLibConfiguratorTest extends AbstractMavenConfiguratorTest {

	@Test
	public void testJBIDE11738_endorsed_libraries() throws Exception {
		String projectLocation = "projects/endorsed_lib/endorsing";
		IProject endorsing = importProject(projectLocation+"/pom.xml");
		endorsing.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		waitForJobsToComplete();
		
		//When the project is imported, the endorsed dir doesn't exist, so we should see
		//compilation errors and a marker about that missing Endorsed dir
		List<IMarker> errors = findErrorMarkers(endorsing);
		assertEquals(toString(errors), 3, errors.size());
		IMarker marker = errors.get(2);
		String error = getMessage(marker);
		assertTrue("Unexpected error message :"+error, error.startsWith("Endorsed dir"));
		
		//Since the endorsed dir is missing, no Endorsed Libraries classpath library 
		//is added to the project's classpath
		IJavaProject javaProject = JavaCore.create(endorsing);
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		assertFalse("Endorsed Lib should not have been added", 
				     ClasspathHelpers.isEndorsedDirsClasspathContainer(classpath[0].getPath()));
		
		//Now let's fix the project

		//Check quick fix is available
		MissingEndorsedLibMarkerResolutionGenerator generator = new MissingEndorsedLibMarkerResolutionGenerator();
		assertTrue("project should be fixable", generator.hasResolutions(marker));
		IMarkerResolution[] resolutions = generator.getResolutions(marker);
		assertEquals(1, resolutions.length);
		//Execute quick fix
		resolutions[0].run(marker);
		waitForJobsToComplete();
		endorsing.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		waitForJobsToComplete();
		
		//Check it compiles properly now
		assertNoErrors(endorsing);
		
		//And Endorsed Libraries is added first on the classpath
		classpath = javaProject.getRawClasspath();
		assertTrue("Endorsed Lib should have been added first", 
				   ClasspathHelpers.isEndorsedDirsClasspathContainer(classpath[0].getPath()));
	}
	
	
	@Test
	public void testJBIDE11738_quote_hack_support() throws Exception {
		String projectLocation = "projects/endorsed_lib/endorsing_quotehack";
		IProject endorsing = importProject(projectLocation+"/pom.xml");
		endorsing.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		waitForJobsToComplete();
		
		//When the project is imported, the endorsed dir doesn't exist, so we should see
		//compilation errors and a marker about that missing Endorsed dir
		List<IMarker> errors = findErrorMarkers(endorsing);
		assertEquals(toString(errors), 3, errors.size());
		IMarker marker = errors.get(2);
		String error = getMessage(marker);
		assertTrue("Unexpected error message :"+error, error.startsWith("Endorsed dir"));
		
		//Since the endorsed dir is missing, no Endorsed Libraries classpath library 
		//is added to the project's classpath
		IJavaProject javaProject = JavaCore.create(endorsing);
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		assertFalse("Endorsed Lib should not have been added", 
				     ClasspathHelpers.isEndorsedDirsClasspathContainer(classpath[0].getPath()));
		
		//Now let's fix the project

		//Check quick fix is available
		MissingEndorsedLibMarkerResolutionGenerator generator = new MissingEndorsedLibMarkerResolutionGenerator();
		assertTrue("project should be fixable", generator.hasResolutions(marker));
		IMarkerResolution[] resolutions = generator.getResolutions(marker);
		assertEquals(1, resolutions.length);
		//Execute quick fix
		resolutions[0].run(marker);
		waitForJobsToComplete();
		endorsing.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		waitForJobsToComplete();
		
		//Check it compiles properly now
		assertNoErrors(endorsing);
		
		//And Endorsed Libraries is added first on the classpath
		classpath = javaProject.getRawClasspath();
		assertTrue("Endorsed Lib should have been added first", 
				   ClasspathHelpers.isEndorsedDirsClasspathContainer(classpath[0].getPath()));
	}
	
	@Test
	public void testJBIDE11738_non_fixable_endorsed_libraries() throws Exception {
		String projectLocation = "projects/endorsed_lib/broken_endorsing";
		IProject endorsing = importProject(projectLocation+"/pom.xml");
		endorsing.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		waitForJobsToComplete();
		
		//When the project is imported, the endorsed dir doesn't exist, so we should see
		//compilation errors and a marker about that missing Endorsed dir
		List<IMarker> errors = findErrorMarkers(endorsing);
		assertEquals(toString(errors), 3, errors.size());
		IMarker marker = errors.get(2);
		String error = getMessage(marker);
		assertTrue("Unexpected error message :"+error, error.startsWith("Endorsed dir"));
		
		//Since the endorsed dir is missing, no Endorsed Libraries classpath library 
		//is added to the project's classpath
		IJavaProject javaProject = JavaCore.create(endorsing);
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		assertFalse("Endorsed Lib should not have been added", 
			     ClasspathHelpers.isEndorsedDirsClasspathContainer(classpath[0].getPath()));
		
		//Check quick fix is not available
		MissingEndorsedLibMarkerResolutionGenerator generator = new MissingEndorsedLibMarkerResolutionGenerator();
		assertFalse("Project should not be fixable", generator.hasResolutions(marker));
	}	
}
