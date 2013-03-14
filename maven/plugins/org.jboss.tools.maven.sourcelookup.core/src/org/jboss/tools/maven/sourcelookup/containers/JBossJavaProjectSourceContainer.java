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
package org.jboss.tools.maven.sourcelookup.containers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;
import org.jboss.tools.maven.core.identification.IdentificationUtil;
import org.jboss.tools.maven.core.internal.identification.FileIdentificationManager;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;

/**
 * 
 * @author snjeza
 *
 */
public class JBossJavaProjectSourceContainer extends ProjectSourceContainer {

	public static final String TYPE_ID = "org.jboss.tools.sourcecontainer.containerType"; //$NON-NLS-1$
	
	private static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

	private static File resolvedFile;
	
	private List<ISourceContainer> sourceContainers = new ArrayList<ISourceContainer>();

	private FileIdentificationManager fileIdentificationManager;
	
	public JBossJavaProjectSourceContainer(IProject project, boolean referenced) {
		super(project, referenced);
		this.fileIdentificationManager = new FileIdentificationManager();
	}

	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		Object[] objects = super.findSourceElements(name);
		if (objects != null && objects.length > 0) {
			return objects;
		}
		
		for (ISourceContainer container:sourceContainers) {
			objects = container.findSourceElements(name);
			if (objects != null && objects.length > 0) {
				return objects;
			}
		}
		if (objects == null) {
			objects = new Object[0];
		}
		IJavaProject javaProject = JavaCore.create(getProject());
		if (javaProject != null && !javaProject.isOpen()) {
			return objects;
		}
		String typeName = name.replace(".java", ""); //$NON-NLS-1$ //$NON-NLS-2$
		typeName = typeName.replace("\\", PATH_SEPARATOR); //$NON-NLS-1$
		typeName = typeName.replace(PATH_SEPARATOR, "."); //$NON-NLS-1$
		if (typeName.startsWith("java.")) { //$NON-NLS-1$
			return objects;
		}
		try {
			IType type = javaProject.findType(typeName);
			if (type != null) {
				IClassFile classFile = type.getClassFile();
				if (classFile != null) {
					IPath path = classFile.getPath();
					if (path != null && path.toString().endsWith(".jar")) { //$NON-NLS-1$
						String osPath = path.toOSString();
						if (osPath != null) {
							File file = new File(osPath);
							if (file.isFile()) {
								return findSourceElements(file, name);
							}
						}
					}
				}
			}
			
		} catch (JavaModelException e) {
			// ignore
		}
		return objects;
	}

	private Object[] findSourceElements(File file, String name) throws CoreException {
		Object[] objects = new Object[0];
		ArtifactKey artifact = getArtifact(file);
		if (artifact != null) {
			IPath sourcePath = getSourcePath(artifact);
			if (sourcePath == null) {
				Job job = downloadArtifact(file, artifact);
				try {
					job.join();
				} catch (InterruptedException e) {
					return objects;
				}
				if (resolvedFile != null) {
					ISourceContainer container = new ExternalArchiveSourceContainer(
							resolvedFile.getAbsolutePath(), true);
					objects = container.findSourceElements(name);
					if (objects != null && objects.length > 0) {
						sourceContainers.add(container);
						
					}
				}
			} else {
				ISourceContainer container = new ExternalArchiveSourceContainer(
						sourcePath.toOSString(), true);
				objects = container.findSourceElements(name);
				if (objects != null && objects.length > 0) {
					sourceContainers.add(container);
					return objects;
				}
			}
			//break;
		}
		return objects;
	}

	public ArtifactKey getArtifact(File file) throws CoreException {
		return fileIdentificationManager.identify(file, new NullProgressMonitor());
	}

	public static Job downloadArtifact(File file, ArtifactKey artifact) {
		final ArtifactKey sourcesArtifact = new ArtifactKey(
				artifact.getGroupId(), artifact.getArtifactId(),
				artifact.getVersion(),
				IdentificationUtil.getSourcesClassifier(artifact.getClassifier()));
		resolvedFile = null;
		Job job = new Job("Downloading sources for " + file.getName()) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					resolvedFile = download(sourcesArtifact, monitor);
				} catch (CoreException e) {
					SourceLookupActivator.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return job;
	}

	private static File download(ArtifactKey artifact, IProgressMonitor monitor)
			throws CoreException {
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.beginTask("Downloading sources...", 2);
		monitor.worked(1);
		Artifact resolved = resolveArtifact(artifact, monitor);
		return resolved.getFile();
	}

	protected static Artifact resolveArtifact(ArtifactKey artifact,
			IProgressMonitor monitor) throws CoreException {
		IMaven maven = MavenPlugin.getMaven();
		Artifact resolved = maven.resolve(artifact.getGroupId(), //
				artifact.getArtifactId(), //
				artifact.getVersion(), //
				"jar" /* type */, // //$NON-NLS-1$
				artifact.getClassifier(), //
				maven.getArtifactRepositories(), //
				monitor);
		monitor.done();
		return resolved;
	}

	public static IPath getSourcePath(ArtifactKey a) {
		File file = getAttachedArtifactFile(a,
				IdentificationUtil.getSourcesClassifier(a.getClassifier()));

		if (file != null) {
			return Path.fromOSString(file.getAbsolutePath());
		}

		return null;
	}

	private static File getAttachedArtifactFile(ArtifactKey a, String classifier) {
		try {
			IMaven maven = MavenPlugin.getMaven();
			ArtifactRepository localRepository = maven.getLocalRepository();
			String relPath = maven.getArtifactPath(localRepository,
					a.getGroupId(), a.getArtifactId(), a.getVersion(),
					"jar", classifier); //$NON-NLS-1$
			File file = new File(localRepository.getBasedir(), relPath)
					.getCanonicalFile();
			if (file.canRead()) {
				return file;
			}
		} catch (CoreException ex) {
			// fall through
		} catch (IOException ex) {
			// fall through
		}
		return null;
	}

}
