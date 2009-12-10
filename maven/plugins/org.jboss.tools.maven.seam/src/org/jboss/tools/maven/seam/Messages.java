package org.jboss.tools.maven.seam;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.seam.messages"; //$NON-NLS-1$
	public static String JSFProjectConfigurator_The_project_does_not_contain_the_Web_Module_facet;
	public static String MavenSeamActivator_Cannot_get_seam_runtime;
	public static String MavenSeamActivator_The_file_does_not_exist;
	public static String MavenSeamActivator_The_folder_does_not_exist;
	public static String PortletProjectConfigurator_The_project_does_not_contain_the_Web_Module_facet;
	public static String SeamConfiguratorPreferencePage_Configure_JBoss_JSF_Portlet_facet;
	public static String SeamConfiguratorPreferencePage_Configure_JBoss_Portlet_Core_facet;
	public static String SeamConfiguratorPreferencePage_Configure_JBoss_Seam_Portlet_facet;
	public static String SeamConfiguratorPreferencePage_Configure_JSF_facet;
	public static String SeamConfiguratorPreferencePage_Configure_Seam_Artifacts;
	public static String SeamConfiguratorPreferencePage_Configure_Seam_Runtime;
	public static String SeamConfiguratorPreferencePage_Configure_Seam_when_importing_Maven_projects;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
