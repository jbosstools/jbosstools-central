package org.jboss.tools.maven.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryInstallDelegate;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
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
	
}
