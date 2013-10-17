/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.utils;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.osgi.util.NLS;
import org.jboss.jdf.stacks.model.ArchetypeVersion;
import org.jboss.tools.maven.core.IArtifactResolutionService;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.maven.project.examples.Messages;
import org.jboss.tools.maven.project.examples.internal.stacks.StacksArchetypeUtil;
import org.jboss.tools.maven.project.examples.xpl.DependencyKey;
import org.jboss.tools.project.examples.model.ProjectExample;

public class MavenArtifactHelper {

	private static final String JBOSS_SPEC = "org.jboss.spec:jboss-javaee-web-6.0"; //$NON-NLS-1$
	
	private static final String COORDS = JBOSS_SPEC+":[0,)"; //$NON-NLS-1$
	/**
	 * Checks if the EAP repository is available
	 * 
	 * @return true if org.jboss.spec:jboss-javaee-web-6.0:pom:*redhat* can be resolved
	 */
	public static boolean isEnterpriseRepositoryAvailable() {
		boolean isRepoAvailable = redHatArtifactExists(COORDS);
		return isRepoAvailable;
	}

	/**
	 * Checks if an artifact can be resolved
	 * 
	 * @deprecated use {@link IArtifactResolutionService#isResolved(String, List, org.eclipse.core.runtime.IProgressMonitor)}
	 * @param dependencyKey
	 * @return true is the dependencyKey can be resolved to an artifact.
	 */
	@Deprecated
	public static boolean isArtifactAvailable(String dependencyKey) {
		try {
			IArtifactResolutionService resolutionService = MavenCoreActivator.getDefault().getArtifactResolutionService();
			List<ArtifactRepository> repos = MavenPlugin.getMaven().getArtifactRepositories();
			return resolutionService.isResolved(dependencyKey, repos, new NullProgressMonitor());
		} catch (CoreException e) {
			MavenProjectExamplesActivator.log(e);
		}
		return false;
	}


	/**
	 * Checks if an artifact can be resolved
	 * 
	 * @deprecated use {@link IArtifactResolutionService#isResolved(String, List, org.eclipse.core.runtime.IProgressMonitor)}
	 * @param dependencyKey
	 * @return true is the dependencyKey can be resolved to an artifact.
	 */
	public static boolean isArtifactAvailable(DependencyKey key) {
		return isArtifactAvailable(key.toPortableString());
	}

	
	public static IStatus checkEnterpriseRequirementsAvailable(ProjectExample project) {
		if (!isEnterpriseRepositoryAvailable()) {
			return new Status(IStatus.ERROR, 
					   MavenProjectExamplesActivator.PLUGIN_ID, 
					   NLS.bind(Messages.ArchetypeExamplesWizardFirstPage_Unresolved_Enterprise_Repo, JBOSS_SPEC));
		}
		if (project != null) {
			Set<String> requirements = project.getEssentialEnterpriseDependencyGavs();
			if (requirements != null && !requirements.isEmpty()) {
				IArtifactResolutionService resolutionService = MavenCoreActivator.getDefault().getArtifactResolutionService();
				List<ArtifactRepository> repos;
				try {
					repos = MavenPlugin.getMaven().getArtifactRepositories();
				} catch (CoreException e1) {
					return new Status(IStatus.ERROR, 
							MavenProjectExamplesActivator.PLUGIN_ID, 
							"Can't load maven repositories");
				}
				for (String gav : requirements) {
					boolean isResolved = false;
					try {
						isResolved = resolutionService.isResolved(gav, repos, new NullProgressMonitor());
					} catch (CoreException e) {
						MavenProjectExamplesActivator.log(e);
					}
					if (!isResolved) {
						return new Status(IStatus.ERROR, 
								MavenProjectExamplesActivator.PLUGIN_ID, 
								NLS.bind(Messages.ArchetypeExamplesWizardFirstPage_Unresolved_WFK_Repo, gav));
					}
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * @since 1.5.3
	 */
	public static IStatus checkRequirementsAvailable(ArchetypeVersion archetypeVersion) {
		if (archetypeVersion == null) {
			return Status.OK_STATUS;
		}
		Set<String> requirements = StacksArchetypeUtil.getRequiredDependencies(archetypeVersion);
		if (requirements == null || requirements.isEmpty()) {
			return Status.OK_STATUS;
		}
		IArtifactResolutionService resolutionService = MavenCoreActivator.getDefault().getArtifactResolutionService();
		List<ArtifactRepository> repos;
		try {
			repos = MavenPlugin.getMaven().getArtifactRepositories();
		} catch (CoreException e1) {
			return new Status(IStatus.ERROR, 
					MavenProjectExamplesActivator.PLUGIN_ID, 
					"Can't load maven repositories");
		}
		for (String gav : requirements) {
			boolean isResolved = false;
			try {
				isResolved = resolutionService.isResolved(gav, repos, new NullProgressMonitor());
			} catch (CoreException e) {
				MavenProjectExamplesActivator.log(e);
			}
			if (!isResolved) {
				return new Status(IStatus.ERROR, 
						MavenProjectExamplesActivator.PLUGIN_ID, 
						NLS.bind(Messages.ArchetypeExamplesWizardFirstPage_Unresolved_Essential_Dependency, gav));
			}
		}
		return Status.OK_STATUS;
	}
	
	private static boolean redHatArtifactExists(String coords) {
		try {
			IArtifactResolutionService resolutionService = MavenCoreActivator.getDefault().getArtifactResolutionService();
			List<ArtifactRepository> repositories = MavenPlugin.getMaven().getArtifactRepositories();
			List<String> availableVersions = resolutionService.getAvailableReleasedVersions(coords, repositories , new NullProgressMonitor());
			for (String version:availableVersions) {
				if (version != null && version.toString().contains("redhat")) { //$NON-NLS-1$
					return true;
				}
			}
		} catch (CoreException e) {
			MavenProjectExamplesActivator.log(e);
		}
		return false;
	}
}
