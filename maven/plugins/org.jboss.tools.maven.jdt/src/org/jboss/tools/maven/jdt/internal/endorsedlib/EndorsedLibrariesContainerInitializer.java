/*******************************************************************************
 * Copyright (c) 2008-2012 Sonatype, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *      Red Hat, Inc.  - Changed behaviour to support Endorsed Libraries
 *******************************************************************************/

package org.jboss.tools.maven.jdt.internal.endorsedlib;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMavenConfiguration;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.jboss.tools.maven.jdt.MavenJdtActivator;
import org.jboss.tools.maven.jdt.endorsedlib.IEndorsedLibrariesManager;
import org.jboss.tools.maven.jdt.utils.ClasspathHelpers;


/**
 * Endorsed Libraries Container Initializer
 * 
 * @author Eugene Kuleshov
 * @author Fred Bricon
 */
public class EndorsedLibrariesContainerInitializer extends ClasspathContainerInitializer {

  public void initialize(IPath containerPath, IJavaProject project) {
    if(ClasspathHelpers.isEndorsedDirsClasspathContainer(containerPath)) {
      try {
        IClasspathContainer mavenContainer = getBuildPathManager().getSavedContainer(project.getProject());
        if(mavenContainer != null) {
          JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project},
              new IClasspathContainer[] {mavenContainer}, new NullProgressMonitor());
          return;
        }
      } catch(CoreException ex) {
    	  MavenJdtActivator.log("Exception initializing classpath container " + containerPath.toString(), ex);
      }

      // force refresh if can't read persisted state
      IMavenConfiguration configuration = MavenPlugin.getMavenConfiguration();
      MavenUpdateRequest request = new MavenUpdateRequest(project.getProject(), configuration.isOffline(), false);
      getMavenProjectManager().refresh(request);
    }
  }


  private IEndorsedLibrariesManager getBuildPathManager() {
    return MavenJdtActivator.getDefault().getEndorsedLibrariesManager();
  }

  private IMavenProjectRegistry getMavenProjectManager() {
    return MavenPlugin.getMavenProjectRegistry();
  }
}
