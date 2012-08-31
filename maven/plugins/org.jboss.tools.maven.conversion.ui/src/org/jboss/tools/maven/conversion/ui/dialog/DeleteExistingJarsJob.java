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
package org.jboss.tools.maven.conversion.ui.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.jboss.tools.maven.conversion.ui.dialog.xpl.ConversionUtils;
import org.jboss.tools.maven.core.MavenCoreActivator;

public class DeleteExistingJarsJob extends Job {

	private Set<IClasspathEntry> entries;

	private IJavaProject javaProject;

	public DeleteExistingJarsJob(IProject project, Set<IClasspathEntry> entries) {
		super("Delete classpath entries"
				+ ((project == null) ? "" : "for " + project.getName()));
		this.javaProject = JavaCore.create(project);
		this.entries = entries;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Set<IPath> pathsToRemove = new HashSet<IPath>();
		Set<IFile> filesToRemove = new HashSet<IFile>();
		try {

			for (IClasspathEntry e : entries) {
				collectDeletableEntries(e, pathsToRemove, filesToRemove);
			}

			updateClasspath(pathsToRemove, monitor);
			/* Need to use the refactoring API
			deleteFiles(filesToRemove, monitor);
			*/

		} catch (CoreException e) {
			return new Status(IStatus.ERROR, MavenCoreActivator.PLUGIN_ID, "Unable to delete classpath entries", e);
		}

		return Status.OK_STATUS;
	}

	private void deleteFiles(Set<IFile> filesToRemove, IProgressMonitor monitor)
			throws CoreException {
		List<IStatus> exceptions = new ArrayList<IStatus>();
		for (IFile f : filesToRemove) {
			if (f.exists()) {
				try {
					f.delete(true, monitor);
				} catch (CoreException e) {
					exceptions.add(new Status(Status.ERROR, MavenCoreActivator.PLUGIN_ID, 0, "Unable to delete "+f.getFullPath(), e));
				}
			}
		}
		
		if (!exceptions.isEmpty()) {
			IStatus[] statuses = new IStatus[exceptions.size()];
			exceptions.toArray(statuses);
			IStatus status = new MultiStatus(MavenCoreActivator.PLUGIN_ID, Status.ERROR, statuses  ,"Unable to delete files", null);
			throw new CoreException(status );
		}
	}

	private void updateClasspath(Set<IPath> pathsToRemove,	IProgressMonitor monitor) throws CoreException {
		if (pathsToRemove.isEmpty()) {
			return;
		}
		ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
		for (IClasspathEntry entry : javaProject.getRawClasspath()) {
			if (!pathsToRemove.contains(entry.getPath())) {
				newEntries.add(entry);
			}
		}
		javaProject.setRawClasspath(
				newEntries.toArray(new IClasspathEntry[newEntries.size()]),
				null);
	}

	private void collectDeletableEntries(IClasspathEntry entry,
			Set<IPath> pathsToRemove, Set<IFile> filesToRemove)
			throws CoreException {
		if ((entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY 
			 && entry.getPath() != null)) {
			
			if (pathsToRemove != null) {
				pathsToRemove.add(entry.getPath());
			}
			
			if (filesToRemove != null) {
				IFile f = ConversionUtils.getIFile(entry);
				if (f != null && f.exists()
						&& javaProject.getProject().equals(f.getProject())) {
					filesToRemove.add(f);
				}
			}
			return;
		}

		if (pathsToRemove == null) {
			return;
		}
		if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
			IPath path = entry.getPath();
			pathsToRemove.add(path);
			IClasspathContainer container = JavaCore.getClasspathContainer(	path, javaProject);
			if (container != null) {
				IClasspathEntry[] cpes = container.getClasspathEntries();
				if (cpes != null && cpes.length > 0) {
					collectDeletableEntries(entry, null, filesToRemove);
				}
			}
		}
		// TODO handle other entry kinds
	}

}