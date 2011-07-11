/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author snjeza
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.ui.messages"; //$NON-NLS-1$
	public static String ConfiguratorPreferencePage_Configure_Webxml_JSF20;
	public static String AutoResizeTableLayout_Unknown_column_layout_data;
	public static String MavenFacetInstallPage_Add_M2_capabilities_to_this_Web_Project;
	public static String MavenFacetInstallPage_Artifact_Id;
	public static String MavenFacetInstallPage_Description;
	public static String MavenFacetInstallPage_Group_Id;
	public static String MavenFacetInstallPage_JBoss_M2_capabilities;
	public static String MavenFacetInstallPage_Name;
	public static String MavenFacetInstallPage_Packaging;
	public static String MavenFacetInstallPage_Remove_WTP_Classpath_containers;
	public static String MavenFacetInstallPage_Seam_Maven_version;
	public static String MavenFacetInstallPage_Version;
	public static String MavenUserLibraryProviderInstallPanel_Add;
	public static String MavenUserLibraryProviderInstallPanel_Are_you_sure_you_want_to_remove_the_artifact;
	public static String MavenUserLibraryProviderInstallPanel_ArtifactId;
	public static String MavenUserLibraryProviderInstallPanel_Dependencies;
	public static String MavenUserLibraryProviderInstallPanel_Exclusions;
	public static String MavenUserLibraryProviderInstallPanel_GroupId;
	public static String MavenUserLibraryProviderInstallPanel_Remove;
	public static String MavenUserLibraryProviderInstallPanel_Remove_dependency;
	public static String MavenUserLibraryProviderInstallPanel_Remove_exclusion;
	public static String MavenUserLibraryProviderInstallPanel_Restore_Defaults;
	public static String MavenUserLibraryProviderInstallPanel_Scope;
	public static String MavenUserLibraryProviderInstallPanel_Type;
	public static String MavenUserLibraryProviderInstallPanel_Version;
	public static String ConfiguratorPreferencePage_Configure_JBoss_JSF_Portlet_facet;
	public static String ConfiguratorPreferencePage_Configure_JBoss_Portlet_Core_facet;
	public static String ConfiguratorPreferencePage_Configure_JBoss_Seam_Portlet_facet;
	public static String ConfiguratorPreferencePage_Configure_JSF_facet;
	public static String ConfiguratorPreferencePage_Configure_CDI_facet;
	public static String ConfiguratorPreferencePage_Configure_Hibernate;
	public static String ConfiguratorPreferencePage_Configure_Seam_Artifacts;
	public static String ConfiguratorPreferencePage_Configure_Seam_Runtime;
	public static String ConfiguratorPreferencePage_Configure_Seam_when_importing_Maven_projects;
	public static String ConfiguratorPreferencePage_When_importing_Maven_projects_configure_the_following;
	public static String ProfileManager_Updating_maven_profiles;
	
	public static String SelectProfilesDialog_autoactivated;
	public static String SelectProfilesDialog_Activate_menu;
	public static String SelectProfilesDialog_Available_profiles;
	public static String SelectProfilesDialog_deactivated;
	public static String SelectProfilesDialog_Deactivate_menu;
	public static String SelectProfilesDialog_DeselectAll;
	public static String SelectProfilesDialog_Force_update;
	public static String SelectProfilesDialog_Maven_profile_selection;
	public static String SelectProfilesDialog_Offline;
	public static String SelectProfilesDialog_Profile_id_header;
	public static String SelectProfilesDialog_Profile_source_header;
	public static String SelectProfilesDialog_Project_has_no_available_profiles;
	public static String SelectProfilesDialog_Select_Maven_profiles;
	public static String SelectProfilesDialog_Select_the_active_Maven_profiles;
	public static String SelectProfilesDialog_SelectAll;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
