/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.project.examples.messages"; //$NON-NLS-1$
	public static String NewProjectExamplesWizardPage_Site;
	public static String Category_Other;
	public static String ECFExamplesTransport_Downloading;
	public static String ECFExamplesTransport_Internal_Error;
	public static String ECFExamplesTransport_IO_error;
	public static String ECFExamplesTransport_Loading;
	public static String ECFExamplesTransport_ReceivedSize_Of_FileSize_At_RatePerSecond;
	public static String ECFExamplesTransport_Server_redirected_too_many_times;
	public static String ECFExamplesTransport_Unexpected_interrupt_while_waiting_on_ECF_transfer;
	public static String MarkerDialog_Description;
	public static String MarkerDialog_Finish;
	public static String MarkerDialog_Markers;
	public static String MarkerDialog_Quick_Fix;
	public static String MarkerDialog_Resource;
	public static String MarkerDialog_Select_a_marker_and_click_the_Quick_Fix_button;
	public static String MarkerDialog_Type;
	public static String NewProjectExamplesWizard_Detail;
	public static String NewProjectExamplesWizard_Downloading;
	public static String NewProjectExamplesWizard_Error;
	public static String NewProjectExamplesWizard_File_does_not_exist;
	public static String NewProjectExamplesWizard_Importing;
	public static String NewProjectExamplesWizard_New_Project_Example;
	public static String NewProjectExamplesWizard_OverwriteProject;
	public static String NewProjectExamplesWizard_Question;
	public static String NewProjectExamplesWizardPage_Description;
	public static String NewProjectExamplesWizardPage_Import_Project_Example;
	public static String NewProjectExamplesWizardPage_Project_Example;
	public static String NewProjectExamplesWizardPage_Project_name;
	public static String NewProjectExamplesWizardPage_Project_size;
	public static String NewProjectExamplesWizardPage_Projects;
	public static String NewProjectExamplesWizardPage_Show_the_Quick_Fix_dialog;
	public static String NewProjectExamplesWizardPage_URL;
	public static String Project_JBoss_Tools_Team_from_jboss_org;
	public static String Project_Local;
	public static String Project_Unknown;
	public static String ProjectExamplesActivator_All;
	public static String ProjectExamplesActivator_Waiting;
	public static String ProjectUtil_Invalid_URL;
	public static String ProjectUtil_Invalid_welcome_element;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
