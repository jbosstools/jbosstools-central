/*************************************************************************************
 * Copyright (c) 2012-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.embedder.MavenImpl;
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
	
	public static Dependency createDependency(String groupId, String artifactId, String version) {
		return createDependency(groupId, artifactId, version, null);
	}

	public static Dependency createDependency(String groupId, String artifactId, String version, String type) {
		Dependency d = new Dependency();
		d.setGroupId(groupId);
		d.setArtifactId(artifactId);
		d.setVersion(version);
		d.setType(type);
		return d;
	}

	public static String getDependencyVersion(MavenProject mavenProject, String gid, String aid) {
		List<Artifact> artifacts = new ArrayList<Artifact>();
		ArtifactFilter filter = new org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter(
				Artifact.SCOPE_TEST);
		for (Artifact artifact : mavenProject.getArtifacts()) {
			if (filter.include(artifact)) {
				artifacts.add(artifact);
			}
		}
        for (Artifact artifact:artifacts) {
	    	String groupId = artifact.getGroupId();
    		if (groupId != null && (groupId.equals(gid)) ) {
    			String artifactId = artifact.getArtifactId();
    			if (artifactId != null && artifactId.equals(aid)) {
	    			return artifact.getVersion();
	    		} 
	    	}
	    }
	    return null;
	}
	
	@SuppressWarnings("restriction")
	public static String getDependencyVersion(Artifact artifact, List<ArtifactRepository> remoteRepos, String gid, String aid) {
	    IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject(
	    		artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
		//If the artifact is a workspace dependency, the mavenProject is already loaded
	    if (facade != null) {
			return getDependencyVersion(facade.getMavenProject(), gid, aid);
		}
		
	    //look at the artifact pom file and if it exists, load the corresponding MavenProject
		File pom = getPomFile(artifact);
		String version = null;
	    if (pom != null) {
		    MavenProject mavenProject = null;
	    	MavenImpl maven = (MavenImpl)MavenPlugin.getMaven();
		    try {
		    	//Create a custom execution request
		    	IProgressMonitor monitor = new NullProgressMonitor();
		    	MavenExecutionRequest request = maven.createExecutionRequest(monitor);
		    	for (ArtifactRepository repo : remoteRepos) {
		    		request.addRemoteRepository(repo);
		    	}
		    	request.setPom(pom);
		    	request.getProjectBuildingRequest().setResolveDependencies(true);

		    	//Load the MavenProject
		    	MavenExecutionResult result = maven.readProject(request, monitor);
		    	//log errors
		    	if (result.hasExceptions()) {
		    		for (Throwable e : result.getExceptions()) {
		    			MavenCoreActivator.log(e);
		    		}
		    	} else {
		    		mavenProject = result.getProject();
		    		if (mavenProject != null) {
		    			//finally look at the dependency version
						version = getDependencyVersion(mavenProject , gid, aid);
				    	//Detach the mavenProject from the maven session to avoid memory leaks
						maven.detachFromSession(mavenProject);
		    		}		    		
		    	}
			} catch (CoreException e) {
				//Don't crash on failures to read the dependency version
				MavenCoreActivator.log(e);
			} 
	    }
    	return version; 	
	}

	private static File getPomFile(Artifact artifact) {
		File artifactFile = artifact.getFile();
		if (artifactFile != null) {
			String path = artifactFile.getAbsolutePath();
			int lastIndexOfDot = path.lastIndexOf("."); //$NON-NLS-1$
			if (lastIndexOfDot > 0) {
				String pomPath = path.substring(0, lastIndexOfDot)+".pom"; //$NON-NLS-1$
				File pomFile = new File(pomPath);
				if (pomFile.exists() && pomFile.canRead()) {
					return pomFile;
				}
			}
			//TODO look inside the artifact jar if pom isn't available
		}
		return null;
	}
}
