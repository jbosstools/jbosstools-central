/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.jee.AbstractClasspathContainer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.internal.search.JBossServerSearchInitializer;

/**
 * 
 * @author snjeza
 *
 */
public class SourceLookupUtil {
	
	public static final String SERVER_SEPARATOR = "<server_separator>\n"; //$NON-NLS-1$
	public static final String ID_SEPARATOR = "<id_separator>"; //$NON-NLS-1$
	public static final String SEARCH_PROJECT_NAME = ".JBoss Servers"; //$NON-NLS-1$
	

	public static IProject getProject(IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(SEARCH_PROJECT_NAME);
		if (project.exists()) {
			if (!project.isOpen()) {
				project.open(monitor);
			}
		}
		return project;

		

	}

	public static IProject createProject(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject(monitor);
		if (!project.exists()) {
			monitor.beginTask("Creating the '" + SEARCH_PROJECT_NAME + "' project", 5);
			project.create(new SubProgressMonitor(monitor, 1));
			project.open(new SubProgressMonitor(monitor, 1));
			addNatureToProject(project, JavaCore.NATURE_ID, new SubProgressMonitor(monitor, 1));
			IJavaProject javaProject = JavaCore.create(project);
			javaProject.setOutputLocation(project.getFullPath(), new SubProgressMonitor(monitor, 1));
			computeClasspath(javaProject, new SubProgressMonitor(monitor, 1));
			return project;
		}
		return project;
	}

	private static void computeClasspath(IJavaProject project, IProgressMonitor monitor) {
		IClasspathEntry[] classpath = new IClasspathEntry[2];
		classpath[0] = JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath());
		classpath[1] = JavaCore.newContainerEntry(new Path(JBossServerSearchInitializer.ID));
		try {
			project.setRawClasspath(classpath, monitor);
		} catch (JavaModelException e) {
		}
	}
	
	public static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	public static void deleteProject(IProject project,
			IProgressMonitor monitor) throws CoreException {
		project.delete(true, true, monitor);
	}

	public static void updateClasspath() throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		IProject project = getProject(monitor);
		if (project.exists()) {
			IJavaProject javaProject = JavaCore.create(project);
			final IClasspathContainer container = JavaCore.getClasspathContainer(
					new Path(JBossServerSearchInitializer.ID), javaProject);
			if (container instanceof AbstractClasspathContainer) {
				((AbstractClasspathContainer) container).refresh();
			}
		}
	}
	
	public static String getServerId(IServer server) {
		if (server == null || server.getId() == null
				|| server.getServerType() == null) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder builder = new StringBuilder();
		String id = server.getId();
		String type = server.getServerType().getId();
		builder.append(id);
		builder.append(ID_SEPARATOR);
		builder.append(type);
		return builder.toString();
	}
	
	public static String getServerIds(IServer[] servers) {
		if (servers == null) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < servers.length; i++) {
			builder.append(getServerId(servers[i]));
			if (i < servers.length-1) {
				builder.append(SERVER_SEPARATOR);
			}
		}
		return builder.toString();
	}
	
	public static List<IServer> getServers() {
		List<IServer> servers = new ArrayList<IServer>();
		String serverIds = SourceLookupActivator.getDefault().getSearchServers();
		if (serverIds == null || serverIds.isEmpty()) {
			return servers;
		}
		String[] ids = serverIds.split(SERVER_SEPARATOR);
		if (ids != null && ids.length > 0) {
			for (String idType:ids) {
				String[] types = idType.split(ID_SEPARATOR);
				if (types != null && types.length == 2) {
					String id = types[0];
					String type = types[1];
					IServer server = getServer(type,id);
					if (server!= null) {
						servers.add(server);
					}
				}
			}
		}
		return servers;
	}

	private static IServer getServer(String type, String id) {
		if (type == null || id == null) {
			return null;
		}
		IServer[] servers = ServerCore.getServers();
		if (servers != null) {
			for (IServer server : servers) {
				if (server != null && server.getId() != null
						&& server.getServerType() != null) {
					if (id.equals(server.getId())
							&& type.equals(server.getServerType().getId())) {
						IJBossServer jbossServer = null;
						try {
							jbossServer = ServerConverter
									.checkedGetJBossServer(server);
						} catch (CoreException e) {
							// ignore
						}
						if (jbossServer != null) {
							return server;
						}
					}
				}
			}
		}
		return null;
	}

}
