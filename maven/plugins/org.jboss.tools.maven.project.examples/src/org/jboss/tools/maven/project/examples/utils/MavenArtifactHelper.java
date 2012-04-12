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

import java.util.ArrayList;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.maven.project.examples.Messages;
import org.jboss.tools.maven.project.examples.xpl.DependencyKey;
import org.jboss.tools.project.examples.model.ProjectExample;

public class MavenArtifactHelper {

	private static final DependencyKey ENTERPRISE_JBOSS_SPEC = DependencyKey.fromPortableString("org.jboss.spec:jboss-javaee-web-6.0:pom:3.0.0.Beta1-redhat-1::"); //$NON-NLS-1$
	
	/**
	 * Checks if the EAP repository is available
	 * 
	 * @return true if org.jboss.spec:jboss-javaee-web-6.0:pom:3.0.0.Beta1-redhat-1 can be resolved
	 */
	public static boolean isEnterpriseRepositoryAvailable() {
		boolean isRepoAvailable = isArtifactAvailable(ENTERPRISE_JBOSS_SPEC);		 //$NON-NLS-1$
		return isRepoAvailable;
	}

	/**
	 * Checks if an artifact can be resolved
	 * @param dependencyKey
	 * @return true is the dependencyKey can be resolved to an artifact.
	 */
	public static boolean isArtifactAvailable(String dependencyKey) {
		DependencyKey key = DependencyKey.fromPortableString(dependencyKey);
		return isArtifactAvailable(key);
	}

	public static boolean isArtifactAvailable(DependencyKey key) {
		boolean isRepoAvailable = false;
		try {
			IMaven maven = MavenPlugin.getMaven();
			ArrayList<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
			repos.addAll(maven.getArtifactRepositories());
			IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
			Artifact a = MavenPlugin.getMaven().resolve(
					key.getGroupId(), key.getArtifactId(), key.getVersion(),
					key.getType(), key.getClassifier(), repos, nullProgressMonitor);
			isRepoAvailable = a != null && a.isResolved();
		} catch (CoreException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return isRepoAvailable;
	}

	
	public static IStatus checkEnterpriseRequirementsAvailable(ProjectExample project) {
		if (!isEnterpriseRepositoryAvailable()) {
			return new Status(IStatus.ERROR, 
					   MavenProjectExamplesActivator.PLUGIN_ID, 
					   NLS.bind(Messages.ArchetypeExamplesWizardFirstPage_Unresolved_Enterprise_Repo, ENTERPRISE_JBOSS_SPEC));
		}
		if (project != null) {
			Set<String> requirements = project.getEssentialEnterpriseDependencyGavs();
			if (requirements != null) {
				for (String gav : requirements) {
					DependencyKey key = DependencyKey.fromPortableString(gav);
					if (!isArtifactAvailable(key)) {
						return new Status(IStatus.ERROR, 
								   MavenProjectExamplesActivator.PLUGIN_ID, 
								   NLS.bind(Messages.ArchetypeExamplesWizardFirstPage_Unresolved_WFK_Repo, key));
					}
				}
			}
		}
		return Status.OK_STATUS;
	}
}
