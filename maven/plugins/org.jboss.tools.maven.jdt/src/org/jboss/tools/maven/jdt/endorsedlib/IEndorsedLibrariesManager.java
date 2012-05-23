package org.jboss.tools.maven.jdt.endorsedlib;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.m2e.jdt.IClasspathDescriptor;

public interface IEndorsedLibrariesManager {

	/**
	 * Adds the Endorsed Libraries classpath library to a project's classpath. It's added beore any other classpath build path.
	 * @param javaProject
	 * @param classpath
	 * @param endorsedDirs
	 * @param monitor
	 * @throws CoreException
	 */
	public abstract void configureEndorsedLibs(IJavaProject javaProject,
			IClasspathDescriptor classpath, File[] endorsedDirs,
			IProgressMonitor monitor) throws CoreException;

	public abstract IClasspathContainer getSavedContainer(IProject project)
			throws CoreException;

}