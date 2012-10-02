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
package org.jboss.tools.maven.conversion.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.jdt.internal.MavenClasspathHelpers;
import org.jboss.tools.maven.conversion.core.DependencyCollector;
import org.jboss.tools.maven.conversion.core.ProjectDependency;

/**
 * Collects {@link ProjectDependency}'s from a java project's classpath entries
 * 
 * @author Fred Bricon
 *
 */
@SuppressWarnings("restriction")
public class JavaDependencyCollector extends DependencyCollector {

	@Override
	public List<ProjectDependency> collectDependencies(IProject project) throws CoreException {
		if (!appliesTo(project)) {
			return null;
		}
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return null;
		}
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		List<ProjectDependency> classpathDependencies = new ArrayList<ProjectDependency>(classpath.length);
		extractDependencies(javaProject, classpath, classpathDependencies);
		return classpathDependencies;
	}

	private void extractDependencies(IJavaProject javaProject, IClasspathEntry[] classpath,
			List<ProjectDependency> classpathDependencies) throws JavaModelException {
		for (IClasspathEntry cpe : classpath) {
			if (!isValid(cpe)) {
				continue;
			}
			if (cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IClasspathContainer container = JavaCore.getClasspathContainer(cpe.getPath(), javaProject );
				if (container != null) {
					IClasspathEntry[] cpes = container.getClasspathEntries();
					if (cpes != null && cpes.length > 0) {
						extractDependencies(javaProject, cpes, classpathDependencies);
					}
				}
			} else {
				classpathDependencies.add(new JavaDependency(cpe));
			}
		}
	}

	@Override
	public boolean appliesTo(IProject project) throws CoreException {
		return project != null && project.hasNature(JavaCore.NATURE_ID);
	}

	private boolean isValid(IClasspathEntry cpe) {
	   
       if(IClasspathEntry.CPE_CONTAINER == cpe.getEntryKind()
            && ("org.eclipse.jdt.launching.JRE_CONTAINER".equals(cpe.getPath().segment(0))
            || MavenClasspathHelpers.isMaven2ClasspathContainer(cpe.getPath()))) {
            	return false;
       }
       if (IClasspathEntry.CPE_SOURCE == cpe.getEntryKind()) {
    	   return false;
       }
       return true;
	}

}
