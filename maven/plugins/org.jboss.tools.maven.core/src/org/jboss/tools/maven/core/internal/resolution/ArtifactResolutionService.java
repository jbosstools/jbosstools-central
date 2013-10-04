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
package org.jboss.tools.maven.core.internal.resolution;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.jboss.tools.maven.core.IArtifactResolutionService;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.MavenUtil;

/**
 * Resolves artifacts and retrieves available versions
 * 
 * @since 1.5.2
 * @author Fred Bricon
 */
public class ArtifactResolutionService implements IArtifactResolutionService {

	@Override
	public boolean isResolved(String groupId, String artifactId, String type,
			String version, String classifier,
			List<ArtifactRepository> repositories, IProgressMonitor monitor)
			throws CoreException {
		Artifact artifact = MavenUtil.createArtifact(groupId, artifactId, version, type, classifier);
		return isResolved(artifact, repositories, monitor);
	}

	@Override
	public boolean isResolved(String artifactCoordinates,
			List<ArtifactRepository> repositories, IProgressMonitor monitor)
			throws CoreException {
		Artifact artifact = MavenUtil.createArtifact(artifactCoordinates);
		return isResolved(artifact, repositories, monitor);
	}

	//Not public API - yet
	boolean isResolved(Artifact artifact, List<ArtifactRepository> repositories, IProgressMonitor monitor)
			throws CoreException {
		try {
			RepositorySystem mavenRepositorySystem = MavenUtil.getRepositorySystem();
			IMaven maven = MavenPlugin.getMaven();
			ArtifactResolutionRequest request = new ArtifactResolutionRequest();
			request.setArtifact(artifact);
			request.setLocalRepository(maven.getLocalRepository());
			request.setOffline(MavenPlugin.getMavenConfiguration().isOffline());
			request.setRemoteRepositories(repositories);
			request.setResolveTransitively(false);
			ArtifactResolutionResult resolution = mavenRepositorySystem.resolve(request );
			return resolution.isSuccess();
		} catch (Exception e) {
			throw toCoreException(e);
		}
	}

	
	@Override
	public List<String> getAvailableReleasedVersions(String groupId,
			String artifactId, String type, String versionRange,
			String classifier, List<ArtifactRepository> repositories,
			IProgressMonitor monitor) throws CoreException {
		Artifact artifact = MavenUtil.createArtifact(groupId, artifactId, versionRange, type, classifier);
		return getAvailableReleasedVersions(artifact, repositories, monitor);
	}
	
	@Override
	public List<String> getAvailableReleasedVersions(
			String artifactCoordinates, List<ArtifactRepository> repositories,
			IProgressMonitor monitor) throws CoreException {
		Artifact artifact = MavenUtil.createArtifact(artifactCoordinates);
		return getAvailableReleasedVersions(artifact, repositories, monitor);
	}

	List<String> getAvailableReleasedVersions(
			Artifact artifact, List<ArtifactRepository> repositories,
			IProgressMonitor monitor) throws CoreException {

		ArtifactMetadataSource source = getArtifactMetadataSource();

		IMaven maven = MavenPlugin.getMaven();
		ArtifactRepository localRepository = maven.getLocalRepository();
		try {
			String versionRangeSpec = artifact.getVersion() == null? "[0,)": artifact.getVersion(); //$NON-NLS-1$
			VersionRange versionRange = VersionRange.createFromVersionSpec(versionRangeSpec); 
			artifact.setVersionRange(versionRange);
			List<ArtifactVersion> fullVersions = source.retrieveAvailableVersions(artifact, localRepository, repositories);
			List<String> versions = new ArrayList<String>(fullVersions.size());
			for (ArtifactVersion aVersion : fullVersions) {
				String version = aVersion.toString();
				if (version.endsWith("-SNAPSHOT")) { //$NON-NLS-1$
					continue;
				}
				if (versionRange.containsVersion(aVersion)) {
					versions.add(version);
				}
			}
			return versions;
		} catch (Exception e) {
			throw toCoreException(e);
		}
	}
	
	private static CoreException toCoreException(Exception e) {
		if (e instanceof CoreException) {
			return (CoreException)e;
		}
		IStatus status = new Status(IStatus.ERROR,
				MavenCoreActivator.PLUGIN_ID, e.getMessage(), e);
		return new CoreException(status);
	}

	
	@Override
	public String getLatestReleasedVersion(String groupId, String artifactId,
			String type, String versionRange, String classifier,
			List<ArtifactRepository> repositories, IProgressMonitor monitor)
					throws CoreException {
		Artifact artifact = MavenUtil.createArtifact(groupId, artifactId, versionRange, type, classifier);
		return getLatestReleasedVersion(artifact, repositories, monitor);	
	}
	

	@Override
	public String getLatestReleasedVersion(String artifactCoordinates,
			List<ArtifactRepository> repositories, IProgressMonitor monitor)
			throws CoreException {
		Artifact artifact = MavenUtil.createArtifact(artifactCoordinates);
		return getLatestReleasedVersion(artifact, repositories, monitor);
	}


	String getLatestReleasedVersion(Artifact artifact,
			List<ArtifactRepository> repositories, IProgressMonitor monitor)
			throws CoreException {
		List<String> allVersions = getAvailableReleasedVersions(artifact, repositories, monitor);
		return allVersions.isEmpty()?null:allVersions.get(allVersions.size()-1);
	}

	private static ArtifactMetadataSource getArtifactMetadataSource() {
		ArtifactMetadataSource artifactMetadataSource;
		try {
			artifactMetadataSource = MavenPluginActivator.getDefault().getPlexusContainer()
					.lookup(ArtifactMetadataSource.class, 
							org.apache.maven.artifact.metadata.ArtifactMetadataSource.class.getName(), 
							"maven");
		} catch (ComponentLookupException toughLuck) {
			throw new RuntimeException(toughLuck);
		}
		return artifactMetadataSource;
	}
			  
}
