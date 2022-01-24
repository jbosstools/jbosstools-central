/*************************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.project.examples.internal.Messages;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.internal.UnArchiver;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.test.util.JobUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * @author snjeza
 */
public class ProjectTest {

	private static final String CENTRAL_PROP = "org.jboss.tools.central.donotshow";
	
	private static boolean hideCentral;
	
	@BeforeClass
	public static void initRuntimes() throws Exception {
		hideCentral = Boolean.getBoolean(CENTRAL_PROP);
		System.setProperty(CENTRAL_PROP, "true");
		ProjectExamplesTestUtil.initRuntimes();
	}

	@AfterClass
	public static void removeProjects() throws Exception {
		ProjectExamplesTestUtil.removeProjects();
		System.setProperty(CENTRAL_PROP, Boolean.toString(hideCentral));
	}
	
	@Test
	public void testRelativePath() throws Exception {
		Bundle bundle = Platform.getBundle("org.jboss.tools.project.examples.test");
		URL entry = bundle.getEntry("/resources/testRelativePath.zip");
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		File rootLocation = root.getLocation().toFile();
		File destination = new File(rootLocation,"testRelativePath");
		destination.mkdirs();
		try (InputStream is = entry.openStream()){
			File file = new File(destination, "testRelativePath.zip");
			FileUtils.copyInputStreamToFile(is, file);
			UnArchiver unarchiver = UnArchiver.create(file, destination);
			unarchiver.extract(new NullProgressMonitor());
		} 
		IProjectExampleSite site = new ProjectExampleSite();
		site.setExperimental(false);
		site.setName("Test Relative Path");
		File projectExamplesFile = new File(destination, "test-examples.xml");
		URI uri = projectExamplesFile.toURI();
		site.setUrl(uri);
		Set<IProjectExampleSite> sites = new HashSet<>();
		sites.add(site);
		List<ProjectExampleCategory> categories = ProjectExampleUtil.getCategories(sites , new NullProgressMonitor());
		ProjectExampleCategory category = categories.get(categories.size()-1);
		assertTrue("Test".equals(category.getName()));
		ProjectExample project = category.getProjects().get(0);
		String urlString = project.getUrl();
		assertTrue(urlString, urlString.startsWith("file:"));
		URL url = new URL(urlString);
		try (InputStream is = url.openStream()){
			assertTrue(is != null);
		}
	}
	
	@Test
	public void importNumberguess() throws Exception {
		WorkspaceJob workspaceJob = new WorkspaceJob(Messages.NewProjectExamplesWizard_Downloading) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				try {
					importProject(monitor);
				} catch (Exception e) {
					return Status.CANCEL_STATUS;
				} 
				return Status.OK_STATUS;
			}
			
		};
		workspaceJob.setUser(true);
		workspaceJob.schedule();
		JobUtils.waitForIdle();
		testImportedProject("numberguess");
		testImportedProject("numberguess-ejb");
		testImportedProject("numberguess-ear");
	}

	private void importProject(IProgressMonitor monitor) throws MalformedURLException, Exception {
		
		IProjectExampleSite site = ProjectExampleUtil.getSite("https://download.jboss.org/jbosstools/examples/project-examples-community-4.2.Beta2.xml");
		site.setExperimental(false);
		List<ProjectExampleCategory> projects = ProjectExampleUtil.getCategories(Collections.singleton(site), monitor);
		ProjectExampleCategory seamCategory = null;
		for (ProjectExampleCategory category: projects) {
			if ("Seam".equals(category.getName())) {
				seamCategory = category;
				break;
			}
		}
		assertNotNull(seamCategory);
		ProjectExampleWorkingCopy projectExample = null;
		for (ProjectExample project: seamCategory.getProjects()) {
			if ("numberguess".equals(project.getName())) {
				projectExample = ProjectExamplesActivator.getDefault().getProjectExampleManager().createWorkingCopy(project);
				break;
			}
		}
		assertNotNull(projectExample);
		ProjectExamplesActivator.downloadProject(projectExample, new NullProgressMonitor());
		assertNotNull(projectExample.getFile());
		IImportProjectExample importProjectExample = ProjectExamplesActivator.getDefault().getImportProjectExample(projectExample.getImportType());
		if (importProjectExample.importProject(projectExample, projectExample.getFile(), null, monitor)) {
			importProjectExample.fix(projectExample, monitor);
		}
	}
	
	private void testImportedProject(String project) throws CoreException {
		projectExists(project);
		checkErrors(project);
	}
	
	private void projectExists(String projectName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
		.getProject(projectName);
		assertTrue(project != null && project.isOpen());
	}
	
	private void checkErrors(String projectName) throws CoreException {
		List<IMarker> markers = getMarkers(projectName);
		assertEquals("The '" + projectName + "' contains " + markers.size() + " error(s). "+ProjectExamplesTestUtil.toString(markers), 0, markers.size());
	}
	
	private List<IMarker> getMarkers(String projectName) throws CoreException {
		List<IMarker> markers = new ArrayList<>();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IMarker[] projectMarkers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		for (IMarker marker : projectMarkers) {
			if (marker.getAttribute(IMarker.SEVERITY,
					IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
				// ignore XHTML errors
				if ("org.jboss.tools.jsf.xhtmlsyntaxproblem".equals(marker.getType())) {
					continue;
				}
				markers.add(marker);
			}
		}
		return markers;
	}
	
}
