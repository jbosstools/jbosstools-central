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
package org.jboss.tools.maven.conversion.ui.handlers;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;
import org.jboss.tools.maven.conversion.ui.internal.MavenDependencyConversionActivator;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;

public class IdentifyJarJob extends Job {

	public enum Task  {
		ALL, IDENTIFICATION_ONLY, RESOLUTION_ONLY
	}
	
	private File file;
	
	private IFileIdentificationManager fileIdentificationManager;

	private Dependency dependency;
	
	private Boolean resolvable;

	private Task task;
	
	public IdentifyJarJob(String name, IFileIdentificationManager fileIdentificationManager, File file) {
		super(name);
		this.fileIdentificationManager = fileIdentificationManager;
		this.file = file;
		setRequestedProcess(Task.ALL);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		if (monitor.isCanceled()) {
			return Status.OK_STATUS;
		}
		
		if (Task.ALL.equals(task) 
			|| Task.IDENTIFICATION_ONLY.equals(task)) {

			monitor.subTask("Identifying "+ file);
			ArtifactKey artifactKey;
			try {
				artifactKey = fileIdentificationManager.identify(file, monitor);
			} catch (CoreException e) {
				monitor.worked(1);
				return new Status(IStatus.ERROR, MavenDependencyConversionActivator.PLUGIN_ID, e.getMessage(), e);
			}
			if (artifactKey != null) {
				dependency = new Dependency();
				dependency.setArtifactId(artifactKey.getArtifactId());
				dependency.setGroupId(artifactKey.getGroupId());
				dependency.setVersion(artifactKey.getVersion());
				dependency.setClassifier(artifactKey.getClassifier());
			}
		}
		

		if (dependency != null && (Task.ALL.equals(task) 
			|| Task.RESOLUTION_ONLY.equals(task))) {
			resolvable = checkResolution(dependency, monitor);
		}
		monitor.worked(1);
		return Status.OK_STATUS;
	}

	private static boolean checkResolution(Dependency d, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return false;
		}
		String groupId = d.getGroupId();
		String artifactId = d.getArtifactId();
		String version = d.getVersion();
		String type = d.getType();
		String classifier = d.getClassifier();
		IMaven maven = MavenPlugin.getMaven();
		Artifact a =null;
		try {
			List<ArtifactRepository> artifactRepositories = maven.getArtifactRepositories();
			a = maven.resolve(groupId , artifactId , version , type , classifier , artifactRepositories , monitor);
		} catch(CoreException e) {
			//ignore
		}
		boolean resolved = a != null && a.isResolved();
		return resolved;
	}

	public Dependency getDependency() {
		return dependency;
	}

	public void setDependency(Dependency dependency) {
		this.dependency = dependency;
	}

	public Boolean isResolvable() {
		return resolvable;
	}

	public void setRequestedProcess(Task requestedProcess) {
		this.task = requestedProcess;
	}
	
}
