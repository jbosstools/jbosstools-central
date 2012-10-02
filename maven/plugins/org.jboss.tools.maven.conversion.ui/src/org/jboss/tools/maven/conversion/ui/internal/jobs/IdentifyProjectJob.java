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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class IdentifyProjectJob extends IdentificationJob {

	private IPath projectPath;

	private IMavenProjectFacade facade; 
	
	public IdentifyProjectJob(String name, IPath projectPath) {
		super(name);
		this.projectPath = projectPath;
	}

	@Override
	protected void identifyDependency(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject(projectPath);
		if (project != null) {
			facade = MavenPlugin.getMavenProjectRegistry().getProject(project );
			if (facade != null &&
				facade.getMavenProject() != null &&
				facade.getMavenProject().getArtifact() != null) {
				Artifact a = facade.getMavenProject().getArtifact();
				dependency = new Dependency();
				dependency.setArtifactId(a.getArtifactId());
				dependency.setGroupId(a.getGroupId());
				dependency.setVersion(a.getVersion());
				if (a.getArtifactHandler() != null &&
					a.getArtifactHandler().getPackaging() != null) {
					String type  = a.getArtifactHandler().getPackaging();
					dependency.setType(type);
				}
			}
		}
	}
	
	@Override
	protected void checkResolution(IProgressMonitor monitor) throws CoreException {
		resolvable = facade != null &&
					 facade.getMavenProject() != null &&
				     facade.getMavenProject().getArtifact() != null && 
				     facade.getProject() != null &&
				     facade.getProject().isAccessible();
	}
	
	private IProject getProject(IPath projectPath) {
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath.lastSegment());
		return p;
	}
}

