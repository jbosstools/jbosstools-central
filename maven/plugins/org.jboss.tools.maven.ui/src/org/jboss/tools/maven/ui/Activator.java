/*************************************************************************************
 * Copyright (c) 2009-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.maven.core.MavenUtil;
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

	public static final String CONFIGURE_JAXRS = "configureJAX-RS"; //$NON-NLS-1$

	public static final boolean CONFIGURE_JAXRS_VALUE = true;

	public static final String CONFIGURE_JPA = "configureJPA"; //$NON-NLS-1$

	public static final boolean CONFIGURE_JPA_VALUE = true;

	public static final String CONFIGURE_GWT = "configureGWT"; //$NON-NLS-1$

	public static final boolean CONFIGURE_GWT_VALUE = true;

	public static final String CONFIGURE_ARQUILLIAN = "configureArquillian"; //$NON-NLS-1$

	public static final boolean CONFIGURE_ARQUILLIAN_VALUE = true;

	/**
	 * @since 1.6.0
	 */
  public static final String ENABLE_MAVEN_CLEAN_VERIFY_MENU = "enableMavenCleanVerifyMenu"; //$NON-NLS-1$

  /**
   * @since 1.6.0
   */
  public static final boolean ENABLE_MAVEN_CLEAN_VERIFY_MENU_VALUE = true;

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
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
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

	/**
	 * @deprecated use {@link MavenUtil#getDependencyVersion(MavenProject, String, String)} instead
	 */
	@Deprecated
	public String getDependencyVersion(MavenProject mavenProject, String gid, String aid) {
	    return MavenUtil.getDependencyVersion(mavenProject, gid, aid) ;
	}
	
	/**
	 * @deprecated use {@link MavenUtil#getDependencyVersion(Artifact, List, String, String)} instead
	 */
	@Deprecated
	public String getDependencyVersion(Artifact artifact, List<ArtifactRepository> remoteRepos, String gid, String aid) {
    	return MavenUtil.getDependencyVersion(artifact, remoteRepos, gid, aid); 	
	}

}
