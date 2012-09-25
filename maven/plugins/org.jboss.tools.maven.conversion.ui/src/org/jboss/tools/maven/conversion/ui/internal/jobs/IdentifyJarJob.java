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

import java.io.File;

import org.apache.maven.model.Dependency;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;

public class IdentifyJarJob extends IdentificationJob {
	
	private File file;
	
	private IFileIdentificationManager fileIdentificationManager;

	public IdentifyJarJob(String name, IFileIdentificationManager fileIdentificationManager, File file) {
		super(name);
		this.fileIdentificationManager = fileIdentificationManager;
		this.file = file;
		setRequestedProcess(Task.ALL);
	}

	@Override
	protected void identifyDependency(IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Identifying "+ file);
		ArtifactKey artifactKey = fileIdentificationManager.identify(file, monitor);
		if (artifactKey != null) {
			dependency = new Dependency();
			dependency.setArtifactId(artifactKey.getArtifactId());
			dependency.setGroupId(artifactKey.getGroupId());
			dependency.setVersion(artifactKey.getVersion());
			dependency.setClassifier(artifactKey.getClassifier());
		}
	}
}
