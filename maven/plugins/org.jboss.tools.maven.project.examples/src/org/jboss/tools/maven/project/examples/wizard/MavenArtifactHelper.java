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
package org.jboss.tools.maven.project.examples.wizard;

import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;

public class MavenArtifactHelper {

	public static final String ENTERPRISE_JBOSS_SPEC = "org.jboss.spec:jboss-javaee-web-6.0:3.0.0.Beta1-redhat-1"; //$NON-NLS-1$
	
	private static final String ENTERPRISE_JBOSS_SPEC_KEYSTRING = ENTERPRISE_JBOSS_SPEC+"::"; //$NON-NLS-1$
	
	/**
	 * Checks if the EAP repository is available
	 * 
	 * @return true if org.jboss.spec:jboss-javaee-web-6.0:3.0.0.Beta1-redhat-1 can be resolved
	 */
	public static boolean isEnterpriseRepositoryAvailable() {
		boolean isRepoAvailable = isArtifactAvailable(ENTERPRISE_JBOSS_SPEC_KEYSTRING, "pom");		 //$NON-NLS-1$
		return isRepoAvailable;
	}

	/**
	 * Checks if an artifact can be resolved
	 * @param artifactKey
	 * @return true is the artifactKey can be resolved to an artifact.
	 */
	public static boolean isArtifactAvailable(String artifactKey, String type) {
		boolean isRepoAvailable = false;
		try {
			IMaven maven = MavenPlugin.getMaven();
			ArrayList<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
			repos.addAll(maven.getArtifactRepositories());
			IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
			ArtifactKey key = ArtifactKey.fromPortableString(artifactKey);
			Artifact a = MavenPlugin.getMaven().resolve(
					key.getGroupId(), key.getArtifactId(), key.getVersion(),
					type, key.getClassifier(), repos, nullProgressMonitor);
			isRepoAvailable = a != null && a.isResolved();
		} catch (CoreException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return isRepoAvailable;
	}

}
