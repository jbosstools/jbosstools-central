/*************************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.springboot.configurators;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.osgi.util.NLS;

public class MavenUtils {

	private static final String PACKAGING_JAR = "jar";

	private MavenUtils() {
	}

	/**
	 * Returns {@code true} if the given project has the java nature.
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	public static boolean hasJavaNature(IProject project) throws CoreException {
		return project.getNature(JavaCore.NATURE_ID) != null;
	}

	public static boolean isJarPackaginging(String packaging) {
		return PACKAGING_JAR.equals(packaging);
	}

	public static boolean isDeployableScope(String scope) {
		return Artifact.SCOPE_COMPILE.equals(scope)
				//MNGECLIPSE-1578 Runtime dependencies should be deployed 
				|| Artifact.SCOPE_RUNTIME.equals(scope);
	}

	/**
	 * Returns the maven dependencies that exists in the workspace for the given
	 * maven project facade and project registry.
	 * 
	 * @param mavenProject
	 * @param projectRegistry
	 * @param monitor
	 * @return
	 */
	public static List<IMavenProjectFacade> getWorkspaceDependencies(MavenProject mavenProject, IProject project, 
			IMavenProjectRegistry projectRegistry, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.beginTask(NLS.bind("Looking for workspace dependencies for project {0}", project.getName()), 1);
		try {
			return mavenProject.getArtifacts().stream().filter(artifact -> MavenUtils.isDeployableScope(artifact.getScope()))
					.map(artifact -> {
						IMavenProjectFacade dependency = 
								projectRegistry.getMavenProject(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
						if (dependency != null && !dependency.getProject().equals(project)
								&& dependency.getFullPath(artifact.getFile()) != null) {
							return dependency;
						} else {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.distinct()
					.collect(Collectors.toList());
		} finally {
			subMonitor.done();
		}
	}
}
