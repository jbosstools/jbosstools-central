/*******************************************************************************
 * Copyright (c) 2008-2012 Sonatype, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *      Red Hat, Inc.  - Changed behaviour to support Endorsed Libraries
 *******************************************************************************/

package org.jboss.tools.maven.jdt.configurators;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.jboss.tools.maven.jdt.MavenJdtActivator;
import org.jboss.tools.maven.jdt.configurators.xpl.MavenClasspathContainerSaveHelper;

public class EndorsedLibrariesManager {

	private File stateLocationDir;

	public EndorsedLibrariesManager(File containerLocationDir) {
		stateLocationDir = containerLocationDir;
	}
	
	/**
	 * Adds the Endorsed Libraries classpath library to a project's classpath. It's added beore any other classpath build path.
	 * @param javaProject
	 * @param classpath
	 * @param endorsedDirs
	 * @param monitor
	 * @throws CoreException
	 */
	public void configureEndorsedLibs(IJavaProject javaProject, IClasspathDescriptor classpath, File[] endorsedDirs,
			IProgressMonitor monitor) throws CoreException {
	
	  if(javaProject != null && classpath != null) {
		
		IClasspathEntry[] classpathEntries = getClasspathEntries(javaProject, endorsedDirs, monitor);
		
		ClasspathHelpers.removeEndorsedLibClasspathContainer(classpath);
		
        if (classpathEntries.length > 0) {
        	IClasspathEntry containerEntry = ClasspathHelpers.addEndorsedLibClasspathContainer(classpath);
        	IPath path = containerEntry != null ? containerEntry.getPath() : new Path(ClasspathHelpers.CONTAINER_ID);
        	IClasspathContainer container = new EndorsedLibrariesContainer(path, classpathEntries);
        	JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[] {javaProject},
        			new IClasspathContainer[] {container}, monitor);
        	saveContainerState(javaProject.getProject(), container);
        }
	  }
	}

	/**
	 * Returns the classpath entries found under the endorsed directories
	 */
	private IClasspathEntry[] getClasspathEntries(IJavaProject javaProject, File[] endorsedDirs, IProgressMonitor monitor) {
		List<IClasspathEntry> cpes = new ArrayList<IClasspathEntry>();
		for (File dir : endorsedDirs) {
			if (dir.isDirectory() && dir.canRead()) {
				setEntries(dir, cpes);
			}
		}
		return (IClasspathEntry[]) cpes.toArray(new IClasspathEntry[0]);
	}

	/**
	 * Adds all the jar and zip files found under the lib directory to the list of {@link IClasspathEntry}
	 */
	private void setEntries(File lib, List<IClasspathEntry> cpes) {
		if (!lib.canRead()) {
			return;
		}
		for (File f : lib.listFiles(new JarFilter())) {
			if (f.isFile()) {
				IPath fullPath = new Path(f.getAbsolutePath());
				IClasspathEntry entry =  JavaCore.newLibraryEntry(fullPath, null, null);
				if (!cpes.contains(entry)) {
					cpes.add(entry);
				}
			}
		}
	}	
	
	
	private void saveContainerState(IProject project, IClasspathContainer container) {
		File containerStateFile = getContainerStateFile(project);
		FileOutputStream is = null;
		try {
			is = new FileOutputStream(containerStateFile);
			new MavenClasspathContainerSaveHelper().writeContainer(container, is);
		} catch (IOException ex) {
			MavenJdtActivator.log(
					"Can't save classpath container state for " + project.getName(), ex); //$NON-NLS-1$
		} finally {
			closeQuietly(is, "Can't close output stream for " + containerStateFile.getAbsolutePath());
		}
	}

	public IClasspathContainer getSavedContainer(IProject project)
			throws CoreException {
		File containerStateFile = getContainerStateFile(project);
		if (!containerStateFile.exists()) {
			return null;
		}

		FileInputStream is = null;
		try {
			is = new FileInputStream(containerStateFile);
			return new MavenClasspathContainerSaveHelper().readContainer(is);
		} catch (IOException ex) {
			throw new CoreException(new Status(IStatus.ERROR,
					MavenJdtActivator.PLUGIN_ID, -1, //
					"Can't read classpath container state for "
							+ project.getName(), ex));
		} catch (ClassNotFoundException ex) {
			throw new CoreException(new Status(IStatus.ERROR,
					MavenJdtActivator.PLUGIN_ID, -1, //
					"Can't read classpath container state for "
							+ project.getName(), ex));
		} finally {
			closeQuietly(is, "Can't close output stream for " + containerStateFile.getAbsolutePath());
		}
	}

	private void closeQuietly(Closeable c, String message) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				MavenJdtActivator.log(message, e);	
			}
		}
	}
	
    private File getContainerStateFile(IProject project) {
    	return new File(stateLocationDir, project.getName() + ".container"); //$NON-NLS-1$
    }
}
