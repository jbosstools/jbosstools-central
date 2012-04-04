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
package org.jboss.tools.maven.core;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * Utility class for Maven related operations.
 * 
 * @author Fred Bricon
 *
 */
public class MavenUtil {

	/**
	 * Refresh the mavenProject parent, if it exists in the workspace.
	 *  
	 * @param mavenProject
	 * @throws CoreException
	 */
	public static void refreshParent(MavenProject mavenProject) throws CoreException {
		if (mavenProject == null || mavenProject.getModel()== null) {
			return;
		}
		Parent parent = mavenProject.getModel().getParent();
		if (parent != null) {
			IMavenProjectFacade parentFacade = MavenPlugin.getMavenProjectRegistry().getMavenProject(parent.getGroupId(), 
																									 parent.getArtifactId(), 
																									 parent.getVersion());
			if (parentFacade != null) {
				parentFacade.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
		}
	}
	
	/**
	 * Returns the Maven Model of a project, or null if no pom.xml exits
	 */
	public static Model getModel(IProject project) throws CoreException {
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		Model model = null;
		if (pom.exists()) {
			MavenModelManager modelManager = MavenPlugin.getMavenModelManager();
			model = modelManager.readMavenModel(pom);
		}
		return model;
	}
}
