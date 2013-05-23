/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.offline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants;
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.preferences.OfflineSupportPreferencePage;

/**
 * Change m2e's offline setting according to the project example's own offline state.
 * 
 * @author Fred Bricon
 *
 */
public class MavenOfflinePropertyChangeListener implements IPropertyChangeListener{

	@Override
	@SuppressWarnings("nls")
	public void propertyChange(PropertyChangeEvent event) {
		if (ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_ENABLED.equals(event.getProperty())) {
			final boolean isOffline = Boolean.parseBoolean(event.getNewValue().toString());
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			final IPreferenceStore m2e = M2EUIPluginActivator.getDefault().getPreferenceStore();
			boolean m2eOfflineStatus = m2e.getBoolean(MavenPreferenceConstants.P_OFFLINE);
			if (isOffline == m2eOfflineStatus) {
				return;
			}
			//If not running in UI (eg. during tests), always set m2e's offline status
			boolean updateM2E = !OfflineSupportPreferencePage.VISIBLE;
			if (OfflineSupportPreferencePage.VISIBLE) {
				updateM2E = MessageDialog.openQuestion(shell, 
						"Change m2e offline status",
						"Do you want to put m2e "+ ((isOffline)?"offline?":"back online?"));
			}
			if (updateM2E) {
				Job updateMavenPrefsJob = new Job("Update Maven preferences") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						m2e.setValue(MavenPreferenceConstants.P_OFFLINE, isOffline);
						return Status.OK_STATUS;
					}
				};
				//Some weird things happen when preferences are stored. 
				//If the maven page was opened in the same 'session', 
				//its values would overwrite the ones we're trying to set here.
				//Trying another ugly hack that probably won't even work all the time
				updateMavenPrefsJob.schedule(1000);
			}
		}
	}

}
