/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.internal.p2.ui.IProvHelpContextIds;
import org.eclipse.equinox.internal.p2.ui.dialogs.InstallWizard;
import org.eclipse.equinox.internal.p2.ui.dialogs.ProvisioningWizardDialog;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jboss.tools.project.examples.Messages;

/**
 * 
 * @author snjeza
 *
 */
public class ProjectFix {

	public final static String WTP_RUNTIME = "wtpruntime"; //$NON-NLS-1$
	public final static String SEAM_RUNTIME = "seam"; //$NON-NLS-1$
	public final static String DROOLS_RUNTIME = "drools"; //$NON-NLS-1$
	public final static String PLUGIN_TYPE = "plugin"; //$NON-NLS-1$
	public final static String ALLOWED_VERSIONS = "allowed-versions"; //$NON-NLS-1$
	public final static String ECLIPSE_PROJECTS = "eclipse-projects"; //$NON-NLS-1$
	public final static String ALLOWED_TYPES = "allowed-types"; //$NON-NLS-1$
	public final static String ID = "id"; //$NON-NLS-1$
	public final static String VERSION = "VERSION"; //$NON-NLS-1$
	public final static String DESCRIPTION = "description"; //$NON-NLS-1$
	public final static String ANY = "any"; //$NON-NLS-1$
	
	public static final String SEAM_PREFERENCES_ID = "org.jboss.tools.common.model.ui.seam"; //$NON-NLS-1$
	public static final String WTP_PREFERENCES_ID = "org.eclipse.wst.server.ui.runtime.preferencePage"; //$NON-NLS-1$
	private String type;
	private Map<String,String> properties = new HashMap<String,String>();
	private static Map<String,String> shortDescriptions = new HashMap<String, String>();
	private static Map<String,Boolean> fixableMaps = new HashMap<String, Boolean>();
	
	static {
		shortDescriptions.put(WTP_RUNTIME, Messages.ProjectFix_Missing_WTP_Runtime);
		shortDescriptions.put(SEAM_RUNTIME, Messages.ProjectFix_Missing_Seam_Runtime);
		shortDescriptions.put(PLUGIN_TYPE, Messages.ProjectFix_Missing_plugin);
		shortDescriptions.put(DROOLS_RUNTIME, Messages.ProjectFix_Missing_Drools_Runtime);
		fixableMaps.put(WTP_RUNTIME,true);
		fixableMaps.put(SEAM_RUNTIME,true);
		fixableMaps.put(PLUGIN_TYPE,true);
		fixableMaps.put(DROOLS_RUNTIME,true);
	 }
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public String getShortDescription() {
		if (type == null) {
			return ""; //$NON-NLS-1$
		}
		String shortDescription = shortDescriptions.get(type);
		if (shortDescription == null) {
			return ""; //$NON-NLS-1$
		}
		return shortDescription;
	}
	
	public boolean isFixable() {
		if (type == null) {
			return false;
		}
		Boolean fixable = fixableMaps.get(type);
		if (fixable == null) {
			return false;
		}
		return fixable;
	}
	public void fix() {
		if (SEAM_RUNTIME.equals(type)) {
			Shell shell = getShell();
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell,SEAM_PREFERENCES_ID, null ,null);
			if (dialog != null) {
				dialog.open();
			}
		}
		if (WTP_RUNTIME.equals(type)) {
			Shell shell = getShell();
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell,WTP_PREFERENCES_ID, null ,null);
			if (dialog != null) {
				dialog.open();
			}
		}
		if (PLUGIN_TYPE.equals(type)) {

			ProvisioningUI ui = ProvisioningUI.getDefaultUI();
			InstallWizard wizard = new InstallWizard(ui, null, null, null);
			WizardDialog dialog = new ProvisioningWizardDialog(getShell(), wizard);
			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),IProvHelpContextIds.INSTALL_WIZARD);
			dialog.open();

		}
	}
	private Shell getShell() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		return shell;
	}
}
