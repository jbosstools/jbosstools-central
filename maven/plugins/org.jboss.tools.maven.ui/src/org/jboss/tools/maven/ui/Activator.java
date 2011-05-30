/*************************************************************************************
 * Copyright (c) 2009-2011 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.embedder.MavenImpl;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.ui"; //$NON-NLS-1$

	public static final String CONFIGURE_SEAM = "configureSeam"; //$NON-NLS-1$

	public static final String CONFIGURE_PORTLET = "configurePortlet"; //$NON-NLS-1$

	public static final boolean CONFIGURE_SEAM_VALUE = true;

	public static final String CONFIGURE_SEAM_RUNTIME = "configureSeamRuntime"; //$NON-NLS-1$
  
	public static final boolean CONFIGURE_SEAM_RUNTIME_VALUE = true;

	public static final String CONFIGURE_SEAM_ARTIFACTS = "configureSeamArtifacts"; //$NON-NLS-1$
	
	public static final boolean CONFIGURE_SEAM_ARTIFACTS_VALUE = true;

	public static final String CONFIGURE_JSF = "configureJSF"; //$NON-NLS-1$
	
	public static final boolean CONFIGURE_JSF_VALUE = true;
	
	public static final String CONFIGURE_WEBXML_JSF20 = "configureWebxmlJSF20"; //$NON-NLS-1$
	
	public static final boolean CONFIGURE_WEBXML_JSF20_VALUE = false;

	public static final boolean CONFIGURE_PORTLET_VALUE = true;

	public static final String CONFIGURE_JSFPORTLET = "configureJSFPortlet"; //$NON-NLS-1$
	
	public static final boolean CONFIGURE_JSFPORTLET_VALUE = true;

	public static final String CONFIGURE_SEAMPORTLET = "configureSeamPortlet"; //$NON-NLS-1$
	
	public static final boolean CONFIGURE_SEAMPORTLET_VALUE = true;
	
	public static final String CONFIGURE_CDI = "configureCDI"; //$NON-NLS-1$
	
	public static final boolean CONFIGURE_CDI_VALUE = true;
	
	public static final String CONFIGURE_HIBERNATE = "configureHibernate"; //$NON-NLS-1$
	
	public static final boolean CONFIGURE_HIBERNATE_VALUE = true;

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}

	public static void log(String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message);
		getDefault().getLog().log(status);
	}

	public String getDependencyVersion(MavenProject mavenProject, String gid, String aid) {
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
	public String getDependencyVersion(Artifact artifact, List<ArtifactRepository> remoteRepos, String gid, String aid) {
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
		    			log(e);
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
				log(e);
			} 
	    }
    	return version; 	
	}

	private File getPomFile(Artifact artifact) {
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
