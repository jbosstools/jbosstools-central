package org.jboss.tools.maven.project.examples;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.project.examples.messages"; //$NON-NLS-1$
	public static String ArchetypeExamplesWizardFirstPage_Existing_Project;
	public static String ArchetypeExamplesWizardFirstPage_No_TargetRuntime;
	public static String ArchetypeExamplesWizardFirstPage_Package_Label;
	public static String ArchetypeExamplesWizardFirstPage_ProjectName_Cant_Be_Empty;
	public static String ArchetypeExamplesWizardFirstPage_ProjectName_Label;
	public static String ArchetypeExamplesWizardFirstPage_Target_Runtime_Label;
	public static String ArchetypeExamplesWizardFirstPage_Title;
	public static String ArchetypeExamplesWizardFirstPage_Unresolved_Enterprise_Repo;
	public static String ArchetypeExamplesWizardFirstPage_Error_Package;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
