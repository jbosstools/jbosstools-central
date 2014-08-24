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
package org.jboss.tools.project.examples.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.test.util.JobUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * 
 * @author snjeza
 *
 */
public class ProjectTest {

	@BeforeClass
	public static void initRuntimes() throws Exception {
		ProjectExamplesUtil.initRuntimes();
	}

	@AfterClass
	public static void removeProjects() throws Exception {
		ProjectExamplesUtil.removeProjects();
	}
	
	@Test
	public void testRelativePath() throws Exception {
		Bundle bundle = Platform.getBundle("org.jboss.tools.project.examples.test");
		URL entry = bundle.getEntry("/resources/testRelativePath.zip");
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		File rootLocation = root.getLocation().toFile();
		File destination = new File(rootLocation,"testRelativePath");
		destination.mkdirs();
		File file = new File(destination, "testRelativePath.zip");
		InputStream is = null;
		OutputStream os = null;
		try {
			is = entry.openStream();
			os = new FileOutputStream(file);
			copy(is,os);
			ProjectExamplesActivator.extractZipFile(file, destination, new NullProgressMonitor());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// ignore
				}
				is = null;
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		IProjectExampleSite site = new ProjectExampleSite();
		site.setExperimental(false);
		site.setName("Test Relative Path");
		File projectExamplesFile = new File(destination, "test-examples.xml");
		URL url = projectExamplesFile.toURI().toURL();
		site.setUrl(url);
		Set<IProjectExampleSite> sites = new HashSet<IProjectExampleSite>();
		sites.add(site);
		List<ProjectExampleCategory> categories = ProjectExampleUtil.getProjects(sites , new NullProgressMonitor());
		ProjectExampleCategory category = categories.get(0);
		assertTrue("Test".equals(category.getName()));
		ProjectExample project = category.getProjects().get(0);
		String urlString = project.getUrl();
		assertTrue(urlString.startsWith("file:"));
		url = new URL(urlString);
		is = url.openStream();
		assertTrue(is != null);
		is.close();
	}
	
	@Test
	public void importNumberguess() throws Exception {
		WorkspaceJob workspaceJob = new WorkspaceJob(Messages.NewProjectExamplesWizard_Downloading) {

			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				try {
					importProject(monitor);
				} catch (Exception e) {
					// ignore
				} 
				return Status.OK_STATUS;
			}
			
		};
		workspaceJob.setUser(true);
		workspaceJob.schedule();
		JobUtils.waitForIdle(ProjectExamplesUtil.IDLE_TIME);
	}

	private void importProject(IProgressMonitor monitor) throws MalformedURLException, Exception {
		List<ProjectExampleCategory> projects = ProjectExampleUtil.getProjects(monitor);
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
				projectExample = project.createWorkingCopy();
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
	
	@Test
	public void testImportedProjects() {
		projectExists("numberguess");
		projectExists("numberguess-ejb");
		projectExists("numberguess-ear");
	}
	
	private void projectExists(String projectName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
		.getProject(projectName);
		assertTrue(project != null && project.isOpen());
	}
	
	@Test
	public void testWarErrors() throws Exception {
		checkErrors("numberguess");
	}
	
	@Test
	public void testEjbErrors() throws Exception {
		checkErrors("numberguess-ejb");
	}
	
	@Test
	public void testEarErrors() throws Exception {
		checkErrors("numberguess-ear");
	}
	
	private void checkErrors(String projectName) throws CoreException {
		List<IMarker> markers = new ArrayList<IMarker>();
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		IMarker[] projectMarkers = project.findMarkers(IMarker.PROBLEM, true,
				IResource.DEPTH_INFINITE);
		for (int i = 0; i < projectMarkers.length; i++) {
			if (projectMarkers[i].getAttribute(IMarker.SEVERITY,
					IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
				// ignore XHTML errors
				if ("org.jboss.tools.jsf.xhtmlsyntaxproblem".equals(projectMarkers[i].getType())) {
					continue;
				}
				String message = projectMarkers[i].getAttribute(IMarker.MESSAGE, null);
				String location = projectMarkers[i].getAttribute(IMarker.LOCATION, null);
				String lineNumber = projectMarkers[i].getAttribute(IMarker.LINE_NUMBER, null);
				String type = projectMarkers[i].getType();
				System.out.println("projectName=" + projectName);
				System.out.println("marker: type=" + type + ",message=" + message + ",location=" + location + ",line=" + lineNumber);
				markers.add(projectMarkers[i]);
			}
		}
		assertTrue("The '" + projectName + "' contains " + markers.size() + " error(s).", markers.size() == 0);
	}
	
	
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[16 * 1024];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
	}
}
