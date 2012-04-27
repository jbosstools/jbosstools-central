/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.containers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;

/**
 * 
 * @author snjeza
 * 
 */
public class JBossASSourcePathComputer implements ISourcePathComputer {

	@Override
	public ISourceContainer[] computeSourceContainers(
			ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		IRuntimeClasspathEntry[] unresolvedEntries = JavaRuntime
				.computeUnresolvedSourceLookupPath(configuration);
		List<ISourceContainer> sourcefolderList = new ArrayList<ISourceContainer>();

		IServer server = ServerUtil.getServer(configuration);
		IModule[] modules = server.getModules();

		List<IJavaProject> javaProjectList = new ArrayList<IJavaProject>();

		processModules(sourcefolderList, modules, javaProjectList, server,
				monitor);

		IRuntimeClasspathEntry[] projectEntries = new IRuntimeClasspathEntry[javaProjectList
				.size()];
		for (int i = 0; i < javaProjectList.size(); i++) {
			projectEntries[i] = JavaRuntime
					.newDefaultProjectClasspathEntry(javaProjectList.get(i));
		}
		IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[projectEntries.length
				+ unresolvedEntries.length];
		System.arraycopy(unresolvedEntries, 0, entries, 0,
				unresolvedEntries.length);
		System.arraycopy(projectEntries, 0, entries, unresolvedEntries.length,
				projectEntries.length);

		IRuntimeClasspathEntry[] resolved = JavaRuntime
				.resolveSourceLookupPath(entries, configuration);
		ISourceContainer[] javaSourceContainers = JavaRuntime
				.getSourceContainers(resolved);

		if (!sourcefolderList.isEmpty()) {
			ISourceContainer[] combinedSourceContainers = new ISourceContainer[javaSourceContainers.length
					+ sourcefolderList.size()];
			sourcefolderList.toArray(combinedSourceContainers);
			System.arraycopy(javaSourceContainers, 0, combinedSourceContainers,
					sourcefolderList.size(), javaSourceContainers.length);
			javaSourceContainers = combinedSourceContainers;
		}

		ISourceContainer jbossContainer = new JBossSourceContainer(
				configuration);
		ISourceContainer[] sourceContainers = new ISourceContainer[javaSourceContainers.length + 1];
		System.arraycopy(javaSourceContainers, 0, sourceContainers, 0,
				javaSourceContainers.length);
		sourceContainers[javaSourceContainers.length] = jbossContainer;
		return sourceContainers;

	}

	private void processModules(List<ISourceContainer> sourcefolderList,
			IModule[] modules, List<IJavaProject> javaProjectList,
			IServer server, IProgressMonitor monitor) {
		for (int i = 0; i < modules.length; i++) {
			IProject project = modules[i].getProject();
			IModule[] pModule = new IModule[1];
			pModule[0] = modules[i];
			IModule[] cModule = server.getChildModules(pModule, monitor);
			if (cModule != null && cModule.length > 0) {
				processModules(sourcefolderList, cModule, javaProjectList,
						server, monitor);
			}
			if (project != null) {
				IFolder moduleFolder = project.getFolder(modules[i].getName());
				if (moduleFolder.exists()) {
					sourcefolderList.add(new FolderSourceContainer(
							moduleFolder, true));
				} else {
					try {
						if (project.hasNature(JavaCore.NATURE_ID)) {
							IJavaProject javaProject = (IJavaProject) project
									.getNature(JavaCore.NATURE_ID);
							if (!javaProjectList.contains(javaProject)) {
								javaProjectList.add(javaProject);
							}
						}
					} catch (Exception e) {
						// ignore
					}
				}
			}
		}
	}

	@Override
	public String getId() {
		return SourceLookupActivator.JBOSS_LAUNCH_SOURCE_PATH_COMPUTER_ID;
	}

}
