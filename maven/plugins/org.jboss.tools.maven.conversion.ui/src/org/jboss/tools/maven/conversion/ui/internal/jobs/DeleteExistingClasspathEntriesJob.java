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
package org.jboss.tools.maven.conversion.ui.internal.jobs;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.jdt.internal.MavenClasspathHelpers;
import org.jboss.tools.maven.conversion.ui.internal.MavenDependencyConversionActivator;

public class DeleteExistingClasspathEntriesJob extends Job {

	private IProject project;

	public DeleteExistingClasspathEntriesJob(IProject project) {
		super("Delete classpath entries"
				+ ((project == null) ? "" : "for " + project.getName()));
		this.project = project;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (!project.hasNature(JavaCore.NATURE_ID)) {
				return Status.OK_STATUS;
			}
		} catch (CoreException ignore) {
			return Status.OK_STATUS;
		}
	    IJavaProject javaProject = JavaCore.create(project);
	    if(javaProject != null) {
	        // remove classpatch containers from JavaProject
	    	try {
		      ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
		      for(IClasspathEntry entry : javaProject.getRawClasspath()) {
		        //Keep Source/ JRE / Maven containers 
		    	if(isKept(entry)) {
		          newEntries.add(entry);
		        }
		      }
			javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), monitor);
	      } catch (JavaModelException e) {
	    	IStatus error = new Status(IStatus.ERROR, MavenDependencyConversionActivator.PLUGIN_ID, "Unable to update classpath", e);
			return error;
	      }
	    }

		return Status.OK_STATUS;
	}


	private boolean isKept(IClasspathEntry cpe) {
	    if(IClasspathEntry.CPE_SOURCE == cpe.getEntryKind() ||
	    	(IClasspathEntry.CPE_CONTAINER == cpe.getEntryKind()
	         && ("org.eclipse.jdt.launching.JRE_CONTAINER".equals(cpe.getPath().segment(0))
	         || MavenClasspathHelpers.isMaven2ClasspathContainer(cpe.getPath())))) {
	     	return true;
	    }
		return false;
	}
}