/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jdt.internal.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.jdt.MavenJdtPlugin;

public class ExecutePhaseJob extends WorkspaceJob {

	private final IMavenProjectFacade mavenProjectFacade;
	private final String phase;

	public ExecutePhaseJob(String name, IMavenProjectFacade mavenProjectFacade, String phase) {
		super(name);
		this.mavenProjectFacade = mavenProjectFacade;
		this.phase = phase;
		setRule(MavenPlugin.getProjectConfigurationManager().getRule());
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

		MavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setPom(mavenProjectFacade.getPomFile());
		request.setGoals(Arrays.asList(phase));
		String localRepositoryPath = RepositorySystem.defaultUserLocalRepository.getAbsolutePath();
		ArtifactRepository localRepository;
		try {
			localRepository = mavenProjectFacade.getComponentLookup().lookup(RepositorySystem.class)
					.createLocalRepository(new File(localRepositoryPath));
		} catch (InvalidRepositoryException | CoreException e) {
			return toStatus(e);
		}
		request.setLocalRepository(localRepository);
		request.setLocalRepositoryPath(localRepositoryPath);

		MavenExecutionResult result = MavenPlugin.getMaven().createExecutionContext().execute(request);

		if (result.hasExceptions()) {
			IStatus errorStatus;
			if (result.getExceptions().size() > 1) {
				ArrayList<IStatus> errors = new ArrayList<>();
				for (Throwable t : result.getExceptions()) {
					errors.add(toStatus(t));
				}
				errorStatus = new MultiStatus(MavenJdtPlugin.PLUGIN_ID, -1, errors.toArray(new IStatus[errors.size()]),
						"Unable to execute mvn " + phase, null);
			} else {
				errorStatus = toStatus(result.getExceptions().get(0));
			}
			return errorStatus;
		}

		UpdateMavenProjectJob updateProjectJob = new UpdateMavenProjectJob(
				new IProject[] { mavenProjectFacade.getProject() });
		updateProjectJob.schedule();
		return Status.OK_STATUS;
	}

	private Status toStatus(Throwable t) {
		return new Status(IStatus.ERROR, MavenJdtPlugin.PLUGIN_ID, t.getLocalizedMessage());
	}

}