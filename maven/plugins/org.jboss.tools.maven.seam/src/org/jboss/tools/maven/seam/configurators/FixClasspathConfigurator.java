/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.seam.configurators;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.jboss.tools.maven.seam.MavenSeamActivator;

/**
 * A workaround for https://issues.sonatype.org/browse/MNGECLIPSE-2433
 * 
 * @author snjeza
 */
public class FixClasspathConfigurator extends AbstractProjectConfigurator {

	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureInternal(mavenProject,project, monitor);
	}

	private void configureInternal(MavenProject mavenProject, IProject project,
			IProgressMonitor monitor) throws CoreException {
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			return;
		}
		List<Resource> resources = mavenProject.getResources();
		for (Resource resource:resources) {
			
			File directory = new File(resource.getDirectory());
			String absolutePath = directory.getAbsolutePath();
			try {
				absolutePath = directory.getCanonicalPath();
			} catch(IOException e) {
				MavenSeamActivator.log(e);
			}
			if (! new File(absolutePath).exists()) {
				continue;
			}
			IPath relativePath = getProjectRelativePath(project, absolutePath);
			IResource r = project.findMember(relativePath);
			if (r != null) {
				continue;
			}
			String path = getWorkspaceRelativePath(absolutePath);
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			boolean exists = false;
			for (IClasspathEntry entry:entries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IPath entryPath = entry.getPath();
					if (entryPath != null && path.equals(entryPath.toString())) {
						exists = true;
						break;
					}
				}
			}
			if (!exists) {
				IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
				for (int i = 0; i < entries.length; i++) {
					newEntries[i] = entries[i];
				}
				IClasspathEntry pathEntry = JavaCore.newLibraryEntry(new Path(path), null, null);
				newEntries[entries.length] = pathEntry;
				javaProject.setRawClasspath(newEntries, monitor);
			}
		}
	}

	private String getWorkspaceRelativePath(String absolutePath) {
		File basedir = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toFile();
		String relative;
		if (absolutePath.equals(basedir.getAbsolutePath())) {
			relative = "."; //$NON-NLS-1$
		} else if (absolutePath.startsWith(basedir.getAbsolutePath())) {
			relative = absolutePath.substring(basedir.getAbsolutePath()
					.length() + 1);
		} else {
			return absolutePath;
		}
		relative = relative.replace("\\", "/").trim(); //$NON-NLS-1$ //$NON-NLS-2$
		if (!relative.startsWith("/")) { //$NON-NLS-1$
			relative = "/" + relative; //$NON-NLS-1$
		}
		return relative;
	}
	
	private IPath getProjectRelativePath(IProject project, String absolutePath) {
		File basedir = project.getLocation().toFile();
		String relative;
		if (absolutePath.equals(basedir.getAbsolutePath())) {
			relative = "."; //$NON-NLS-1$
		} else if (absolutePath.startsWith(basedir.getAbsolutePath())) {
			relative = absolutePath.substring(basedir.getAbsolutePath().length() + 1);
		} else {
			relative = absolutePath;
		}
		return new Path(relative.replace('\\', '/'));
	}
	
	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event,
			IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
		if (facade != null) {
			IProject project = facade.getProject();
			MavenProject mavenProject = facade.getMavenProject(monitor);
			configureInternal(mavenProject, project, monitor);
		}
		super.mavenProjectChanged(event, monitor);
	}
}
