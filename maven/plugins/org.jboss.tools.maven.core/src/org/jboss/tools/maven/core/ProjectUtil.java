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
package org.jboss.tools.maven.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.Messages;
import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

/**
 * A utility class for Eclipse Projects.
 * 
 * @author Xavier Coulon
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class ProjectUtil {
	
	/**
	 * Refreshes the projects hierarchy. For example, if the project on
	 * which a facet should be installed is 'Parent1/Parent2/Child',
	 * then both Parent1, Parent2 and Child are refreshed.
	 * 
	 * @param basedir : the base directory (absolute file system path) of the (child) project to refresh.
	 * @param refreshDepth: the refresh depth
	 * @param monitor : the progress monitor
	 * @return the number of projects that were refreshed
	 * @throws CoreException
	 *             in case of problem during refresh
	 * @see org.eclipse.core.resources.IResource for depth values.
	 */
	public static int refreshHierarchy(File basedir, int refreshDepth, IProgressMonitor monitor) throws CoreException {
		try {
			int count = 0;
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final List<IProject> projects = new ArrayList<>(Arrays.asList(root.getProjects()));
			
			final IPath rootLocation = root.getLocation();
			IPath basedirPath = new Path(basedir.getAbsolutePath());
			while(!rootLocation.equals(basedirPath) && rootLocation.isPrefixOf(basedirPath)) {
				Iterator<IProject> ite = projects.iterator();
				 
				// In case of maven module projects, root.findContainersForLocationURI(...) would return an IFolder
				// instead of an IProject. So we manually loop through all projects and test their path against the 
				// current basedirPath. Refreshed projects will be removed from the list for subsequent checks
				while(ite.hasNext()) {
					IProject project = ite.next();
					final IPath projectLocation = project.getLocation();
					if(projectLocation != null && projectLocation.equals(basedirPath) && project.isAccessible()) {
						project.refreshLocal(refreshDepth, monitor);
						count++;
						ite.remove();
						break;
					}
				}
				basedirPath = basedirPath.removeLastSegments(1);
			}
			
			return count;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the underlying file for a given path 
	 * @param project
	 * @param path, ex. WEB-INF/web.xml
	 * @return the underlying file corresponding to path, or null if no file exists.
	 */
	public static IFile getWebResourceFile(IProject project, String path) {
	    IVirtualComponent component = ComponentCore.createComponent(project);
	    if (component == null) {
		   return null;
	    }
	    IPath filePath = new Path(path);
	    IContainer[] underlyingFolders = component.getRootFolder().getUnderlyingFolders();
	    for (IContainer underlyingFolder : underlyingFolders) {
		  IPath p = underlyingFolder.getProjectRelativePath().append(filePath);
		  IFile f = project.getFile(p);
		  if (f.exists()) {
			 return f;
		  }
	    }
	    return null;
	}
	
	/**
	 * Returns the first underlying IFile for a given path, not under the folder tagged as defaultRoot 
	 * @param project
	 * @param path, ex. WEB-INF/web.xml
	 * @return the underlying file corresponding to path, or null if no file exists.
	 */
	public static IFile getGeneratedWebResourceFile(IProject project, String path) {
	    IVirtualComponent component = ComponentCore.createComponent(project);
	    if (component == null) {
		   return null;
	    }
	    IPath filePath = new Path(path);
	    IVirtualFolder root = component.getRootFolder();
	    IContainer[] underlyingFolders = root.getUnderlyingFolders();
	    IPath defaultDDFolderPath = J2EEModuleVirtualComponent.getDefaultDeploymentDescriptorFolder(root);
	    for (IContainer underlyingFolder : underlyingFolders) {
	      if (defaultDDFolderPath ==null || !defaultDDFolderPath.equals(underlyingFolder.getProjectRelativePath())) {
	    	  IPath p = underlyingFolder.getProjectRelativePath().append(filePath);
	    	  IFile f = project.getFile(p);
	    	  return f;
	      }
	    }
	    return null;
	}
	
	public static String getRelativePath(IProject project, String absolutePath) {
		File basedir = project.getLocation().toFile();
		String relative;
		if (absolutePath.equals(basedir.getAbsolutePath())) {
			relative = ".";//$NON-NLS-1$ 
		} else if (absolutePath.startsWith(basedir.getAbsolutePath())) {
			relative = absolutePath.substring(basedir.getAbsolutePath().length() + 1);
		} else {
			relative = absolutePath;
		}
		return relative.replace('\\', '/'); 
	}
	
	public static void removeWTPContainers(IDataModel m2FacetModel, IProject project) throws CoreException {
		if (m2FacetModel != null && project != null && project.hasNature(JavaCore.NATURE_ID) 
		 && m2FacetModel.getBooleanProperty(IJBossMavenConstants.REMOVE_WTP_CLASSPATH_CONTAINERS) ) {
		    IJavaProject javaProject = JavaCore.create(project);
		    if(javaProject != null) {
		      // remove classpatch container from JavaProject
		      ArrayList<IClasspathEntry> newEntries = new ArrayList<>();
		      for(IClasspathEntry entry : javaProject.getRawClasspath()) {
		    	String path = entry.getPath().toString();
		        if(path != null && !path.startsWith("org.eclipse.jst.j2ee.internal.")) { //$NON-NLS-1$
			          newEntries.add(entry);
		        }
		      }
		      javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), null);
		    }
		}
	}
	
	/**
	 * Refresh a list of {@link IProject}s 
	 * @param projects
	 * @param monitor
	 */
	public static void refreshProjects(List<IProject> projects, int depth,
			final IProgressMonitor monitor) {
		if (projects == null || projects.isEmpty()) {
			return;
		}
		for (IProject project : projects) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}
			if (project != null && project.isAccessible()) {
				try {
					project.refreshLocal(depth, monitor);
				} catch (CoreException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Update Maven projects whose configuration is flagged has "out-of-date"
	 */
	public static void updateOutOfDateMavenProjects(List<IProject> projects,
			final IProgressMonitor monitor) throws CoreException {
		if (projects == null || projects.isEmpty()) {
			return;
		}
		List<IProject> projectsTorUpdate = new ArrayList<>(
				projects.size());

		for (IProject project : projects) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}
			if (hasOutOfDateMavenMarker(project)) {
				projectsTorUpdate.add(project);
			}
		}
		if (projectsTorUpdate.isEmpty()) {
			return;
		}
		refreshProjects(projectsTorUpdate, IResource.DEPTH_INFINITE, monitor);
		IProject[] selectedProjects = new IProject[projectsTorUpdate.size()];
		Job updateJob = new UpdateMavenProjectJob(selectedProjects);
		updateJob.schedule();
	}

	/**
	 * Checks if a Maven project configuration is flagged as out-of-date.
	 */
	public static boolean hasOutOfDateMavenMarker(IProject project)
			throws CoreException {
		if (project == null || !project.isAccessible() || !project.hasNature(IMavenConstants.NATURE_ID)) {
			return false;
		}
		IMarker[] markers = project.findMarkers(IMavenConstants.MARKER_CONFIGURATION_ID, true, IResource.DEPTH_ZERO);
		for (IMarker marker : markers) {
			String message = (String) marker.getAttribute(IMarker.MESSAGE);
			if (Messages.ProjectConfigurationUpdateRequired.equals(message)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts a list of project names into a list of {@link IProject}s
	 */
	public static List<IProject> toIProjects(List<String> projectNames) {
		if (projectNames == null) {
			return null;
		}
		List<IProject> projects = new ArrayList<>(projectNames.size());
		for (String name : projectNames) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (project != null) {
				projects.add(project);
			}
		}
		return projects;
	}	
}
