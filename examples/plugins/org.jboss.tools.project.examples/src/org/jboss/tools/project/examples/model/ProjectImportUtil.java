/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Utility class to import a selection of projects from a directory
 * 
 * @author Fred Bricon
 * @since 2.0.0
 */
public class ProjectImportUtil {

	private static final String PROJECT_DESCRIPTOR = ".project"; //$NON-NLS-1$


	public Collection<IProject> importProjects(IPath baseDir, Collection<String> projectNames, IProgressMonitor monitor) throws CoreException {
		if (projectNames == null || projectNames.isEmpty()) {
			return Collections.emptyList();
		}
		File directory = baseDir.makeAbsolute().toFile();
		Set<String> projectsToLookFor = new HashSet<String>(projectNames);
		Map<String, IPath> projectPaths = new HashMap<String, IPath>(); 
		collectProjects(projectPaths, projectsToLookFor, directory, monitor);
		List<IProject> projects = importProjects(projectPaths, monitor);
		return projects;
	}
	
	
	private List<IProject> importProjects(Map<String, IPath> projectPaths,
			IProgressMonitor monitor) throws CoreException {
		List<IProject> projects = new ArrayList<IProject>(projectPaths.size());
		for (Entry<String, IPath> e : projectPaths.entrySet()) {
			if (monitor.isCanceled()) {
				break;
			}
			IProject p = createProject(e.getKey(),  e.getValue(), monitor);
			if (p != null) {
				projects.add(p);
			}
		}
		return projects;
	}


	private void collectProjects(Map<String, IPath> projectPaths, final Set<String> projectsToLookFor, File directory,  IProgressMonitor monitor) {
		if (!projectsToLookFor.isEmpty() && directory.isDirectory()) {
			File projectFile = new File(directory, PROJECT_DESCRIPTOR);
			boolean fileExists = projectFile.exists() && projectFile.isFile() && projectFile.canRead();
			if (projectsToLookFor.contains(directory.getName()) && fileExists) {
				projectsToLookFor.remove(directory.getName());
				projectPaths.put(directory.getName(), new Path(directory.getAbsolutePath()));
				return;
			}
			
			File[] dirs = directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File dir) {
					return dir.isDirectory();
				}
			});
			
			for (File d : dirs) {
				if (monitor.isCanceled()) {
					return;
				}
				projectFile = new File(d, PROJECT_DESCRIPTOR);
				if (projectsToLookFor.contains(d.getName()) && projectFile.exists() && projectFile.isFile() && projectFile.canRead()) {
					projectsToLookFor.remove(d.getName());
					projectPaths.put(d.getName(), new Path(d.getAbsolutePath()));
				} else {
					collectProjects(projectPaths, projectsToLookFor, d, monitor);
				}
			}
		}
		
	}


	private IProject createProject(String projectName, IPath location, IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		if (project.exists()) {
			return null;
		}
		if (!Platform.getLocation().equals(location)) {
			IProjectDescription desc = project.getWorkspace().newProjectDescription(projectName);
			desc.setLocation(location);
			project.create(desc, monitor);
		} else {
			project.create(monitor);
		}
		project.open(IResource.NONE, monitor);
		return project;
	}

}
