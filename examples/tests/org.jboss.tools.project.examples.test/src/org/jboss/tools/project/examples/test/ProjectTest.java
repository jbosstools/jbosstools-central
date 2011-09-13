/*************************************************************************************
 * Copyright (c) 2011 Red Hat, Inc. and others.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Category;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectUtil;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard;
import org.jboss.tools.test.util.JobUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
		List<Category> projects = ProjectUtil.getProjects(monitor);
		Category seamCategory = null;
		for (Category category: projects) {
			if ("Seam".equals(category.getName())) {
				seamCategory = category;
				break;
			}
		}
		assertNotNull(seamCategory);
		Project projectExample = null;
		for (Project project: seamCategory.getProjects()) {
			if ("numberguess".equals(project.getName())) {
				projectExample = project;
				break;
			}
		}
		assertNotNull(projectExample);
		String urlString = projectExample.getUrl();
		String name = projectExample.getName();
		URL url = null;
		url = new URL(urlString);
		File file = ProjectUtil.getProjectExamplesFile(
				url, name, ".zip", monitor); //$NON-NLS-1$
		assertNotNull(file);
		IImportProjectExample importProjectExample = ProjectExamplesActivator.getDefault().getImportProjectExample(projectExample.getImportType());
		importProjectExample.importProject(projectExample, file, monitor);
		importProjectExample.fix(projectExample, monitor);
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
				markers.add(projectMarkers[i]);
			}
		}
		assertTrue("The '" + projectName + "' contains " + markers.size() + " error(s).", markers.size() == 0);
	}
}
