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
package org.jboss.tools.maven.core;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Resolves artifacts and retrieves available versions
 * 
 * @author Fred Bricon
 * @since 1.5.2
 */
public interface IArtifactResolutionService {

	/**
	 * Checks if the given artifact coordinates can be resolved against the provided repositories. 
	 * @param artifactCoordinates the artifact coordinates. Expected format is &lt;groupId&gt;:&lt;artifactId&gt;[:&lt;type&gt;[:&lt;classifier&gt;]]:&lt;version&gt; version must not be a range
	 * @param repositories the repositories to resolve the artifact against
	 * @param monitor a progress monitor
	 * @return true if the artifact can be resolved.
	 * @throws CoreException
	 */
	boolean isResolved(String artifactCoordinates, List<ArtifactRepository> repositories, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Checks if the given artifact coordinates can be resolved against the provided repositories. 
	 * 
	 * @param groupId the artifact group id
	 * @param artifactId the artifact id
	 * @param type the artifact type
	 * @param version the artifact version (must not be a range)
	 * @param classifier the artifact classifier, optional
	 * @param repositories the repositories to resolve the artifact against
	 * @param monitor a progress monitor
	 * @return true if the artifact can be resolved.
	 * @throws CoreException
	 */
	boolean isResolved(String groupId, String artifactId, String type, String version, String classifier, List<ArtifactRepository> repositories, IProgressMonitor monitor) throws CoreException;

	/**
	 * Gets all released versions for a given artifact, resolved against the provided repositories.
	 * 
     * @param artifactCoordinates the artifact coordinates. Expected format is &lt;groupId&gt;:&lt;artifactId&gt;[:&lt;type&gt;[:&lt;classifier&gt;]]:&lt;version&gt;
	 * @param repositories the repositories to resolve the artifact against
	 * @param monitor a progress monitor
	 * @return true if the artifact can be resolved.
	 * @throws CoreException
	 */
	List<String> getAvailableReleasedVersions(String artifactCoordinates, List<ArtifactRepository> repositories, IProgressMonitor monitor) throws CoreException;

	/**
	 * Gets all released versions for a given artifact, resolved against the provided repositories.
	 * 
	 * @param groupId the artifact group id
	 * @param artifactId the artifact id
	 * @param type the artifact type
	 * @param versionRange a version range, optional
	 * @param classifier the artifact classifier, optional
	 * @param repositories the repositories to resolve the artifact against
	 * @param monitor a progress monitor
	 * @return a list of released versions, in ascending order, can not be <code>null</code>.
	 * @throws CoreException
	 */
	List<String> getAvailableReleasedVersions(String groupId, String artifactId, String type, String versionRange, String classifier, List<ArtifactRepository> repositories, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the highest version of an artifact, matching a given version range, resolved against the provided repositories.
	 * 
     * @param artifactCoordinates the artifact coordinates. Expected format is &lt;groupId&gt;:&lt;artifactId&gt;[:&lt;type&gt;[:&lt;classifier&gt;]]:&lt;version&gt;
	 * @param repositories the repositories to resolve the artifact against
	 * @param monitor a progress monitor
	 * @return the highest version of the artifact, in the provided versionRange boundaries, or <code>null</code>, if no version applies.
	 * @throws CoreException
	 */
	String getLatestReleasedVersion(String artifactCoordinates, List<ArtifactRepository> repositories, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the highest version of an artifact, matching a given version range, resolved against the provided repositories.
	 * 
	 * @param groupId the artifact group id
	 * @param artifactId the artifact id
	 * @param type the artifact type
	 * @param versionRange a version range, optional
	 * @param classifier the artifact classifier, optional
	 * @param repositories the repositories to resolve the artifact against
	 * @param monitor a progress monitor
	 * @return the highest version of the artifact, in the provided versionRange boundaries, or <code>null</code>, if no version applies.
	 * @throws CoreException
	 */
	String getLatestReleasedVersion(String groupId, String artifactId, String type, String versionRange, String classifier, List<ArtifactRepository> repositories, IProgressMonitor monitor) throws CoreException;
}
