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
import org.eclipse.m2e.core.embedder.IMaven;
import org.jboss.tools.maven.conversion.ui.internal.MavenDependencyConversionActivator;

public abstract class IdentificationJob extends Job {

	public enum Task  {
		ALL, IDENTIFICATION_ONLY, RESOLUTION_ONLY
	}
	
	protected Boolean resolvable;

	protected Task task;

	protected Dependency dependency;
	
	public IdentificationJob(String name) {
		super(name);
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
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		if (monitor.isCanceled()) {
			return Status.OK_STATUS;
		}
		resolvable = null;
		try {
			if (Task.ALL.equals(task) 
				|| Task.IDENTIFICATION_ONLY.equals(task)) {
				dependency = null;
				identifyDependency(monitor);
			}
		
			if (dependency != null && (Task.ALL.equals(task) 
				|| Task.RESOLUTION_ONLY.equals(task))) {
				checkResolution(monitor);
			}
			
		} catch (CoreException e) {
			monitor.worked(1);
			return new Status(IStatus.ERROR, MavenDependencyConversionActivator.PLUGIN_ID, e.getMessage(), e);
		}

		monitor.worked(1);
		return Status.OK_STATUS;
	}

	protected abstract void identifyDependency(IProgressMonitor monitor) throws CoreException;

	protected void checkResolution(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		resolvable = Boolean.FALSE;
		String groupId = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		String version = dependency.getVersion();
		String type = dependency.getType();
		String classifier = dependency.getClassifier();
		IMaven maven = MavenPlugin.getMaven();
		
		List<ArtifactRepository> artifactRepositories = maven.getArtifactRepositories();
		Artifact a = null;
		try {
			a = maven.resolve(groupId , artifactId , version , type , classifier , artifactRepositories , monitor);
		} catch (CoreException e) {
			//Expected 
		}
		resolvable = (a != null && a.isResolved());
	}


}
