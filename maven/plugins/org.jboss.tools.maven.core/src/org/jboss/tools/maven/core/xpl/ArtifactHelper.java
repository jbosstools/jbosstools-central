/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.maven.core.xpl;

import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.jdt.internal.BuildPathManager;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;


/**
 * 
 * Helper for Maven artifacts. Class copied from m2e-wtp.
 *
 * @author Fred Bricon
 */
//XXX Should probably be refactored to another Maven helper class.
@SuppressWarnings("restriction")
public class ArtifactHelper {

  private static final String M2_REPO_PREFIX = VirtualArchiveComponent.VARARCHIVETYPE + IPath.SEPARATOR
  + BuildPathManager.M2_REPO + IPath.SEPARATOR;

  /**
   * Returns an artifact's path relative to the local repository
   */
  //XXX Does maven API provide that kind of feature? 
  public static IPath getLocalRepoRelativePath(Artifact artifact) {
    if (artifact == null) {
      throw new IllegalArgumentException("artifact must not be null");
    }
    
    IPath m2repo = JavaCore.getClasspathVariable(BuildPathManager.M2_REPO); //always set
    IPath absolutePath = new Path(artifact.getFile().getAbsolutePath());
    IPath relativePath = absolutePath.removeFirstSegments(m2repo.segmentCount()).makeRelative().setDevice(null);
    return relativePath;
  }
  
  /**
   * Returns an IProject from a maven artifact
   * @param artifact
   * @return an IProject if the artifact is a workspace project or null
   */
  public static IProject getWorkspaceProject(Artifact artifact) {
    IMavenProjectFacade facade = getWorkspaceProjectMavenFacade(artifact);
    return (facade == null)?null:facade.getProject();
  }

  /**
   * Returns an IMavenProjectFacade from a maven artifact
   * @param artifact
   * @return an IMavenProjectFacade if the artifact is a workspace project or null
   */
  public static IMavenProjectFacade getWorkspaceProjectMavenFacade(Artifact artifact) {
    IMavenProjectFacade workspaceProject = MavenPlugin.getMavenProjectRegistry()
    .getMavenProject(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());

    if(workspaceProject != null && workspaceProject.getFullPath(artifact.getFile()) != null) {
      return workspaceProject;
    }
    return null;
  }

  /**
   * Returns the M2_REPO variable path for an artifact. ex : var/M2_REPO/groupid/artifactid/version/filename
   * @param artifact
   * @return the M2_REPO variable path for the artifact, null if the artifact is a workspace project
   */
  public static String getM2REPOVarPath(Artifact artifact)
  {
    if (getWorkspaceProject(artifact) != null)
    {
     return null; 
    }
    return M2_REPO_PREFIX + ArtifactHelper.getLocalRepoRelativePath(artifact).toPortableString();
  }

  /**
   * Temporary fix for app-client type artifacts, where the artifactHandler is not correctly loaded 
   * thus the extension and the addtoclasspath value are incorrect.
   * @param artifactHandler
   */
  @Deprecated
  public static void fixArtifactHandler(ArtifactHandler artifactHandler) {
	  if ("app-client".equals(artifactHandler.getExtension()) && artifactHandler instanceof DefaultArtifactHandler) {
		  ((DefaultArtifactHandler)artifactHandler).setExtension("jar");
		  ((DefaultArtifactHandler)artifactHandler).setAddedToClasspath(true);
	  }
  }
  
  public static Artifact getArtifact(Collection<Artifact> artifacts, ArtifactKey key) {
    if (artifacts == null || key == null || artifacts.isEmpty()) {
      return null;
    }
    for (Artifact a : artifacts) {
      ArtifactKey ak = toArtifactKey(a);
      if (key.equals(ak)) {
        return a;
      }
    }
    return null;
  }
  
  /**
   * Gets an ArtifactKey from an Artifact. This method fixes the flawed ArtifactKey(Artifact a) constructor
   * which doesn't copy the artifact classifier; 
   */
  public static ArtifactKey toArtifactKey(Artifact a) {
    return new ArtifactKey(a.getGroupId(), a.getArtifactId(), a.getBaseVersion(), a.getClassifier());
  }
  
}
