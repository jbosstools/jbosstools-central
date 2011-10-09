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
package org.jboss.tools.central;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author snjeza
 *
 */
public class ShowJBossCentral implements IStartup {

	@Override
	public void earlyStartup() {
		boolean showJBossCentral = JBossCentralActivator.getDefault().showJBossCentralOnStartup();
		IProvisioningAgent agent = (IProvisioningAgent) JBossCentralActivator.getDefault().getService(IProvisioningAgent.SERVICE_NAME);
		IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
		if (profileRegistry == null) {
			showJBossCentral = true;
		} else {
			IProfile profile = profileRegistry
					.getProfile(IProfileRegistry.SELF);
			
			if (profile != null) { // got NPE's when running in PDE
				String profileId = profile.getProfileId();
				IEclipsePreferences prefs = JBossCentralActivator.getDefault()
						.getPreferences();
				String savedId = prefs.get(JBossCentralActivator.PROFILE_ID,
						null);
				if (savedId == null || !savedId.equals(profileId)) {
					prefs.put(JBossCentralActivator.PROFILE_ID, profileId);
					showJBossCentral = true;
				}
				long timestamp = profile.getTimestamp();
				long savedTimestamp = prefs.getLong(
						JBossCentralActivator.PROFILE_TIMESTAMP, -1);
				if (timestamp != savedTimestamp) {
					prefs.putLong(JBossCentralActivator.PROFILE_TIMESTAMP,
							timestamp);
					showJBossCentral = true;
				}
			} else {
				// TODO: Not sure what is supposed to happen here if profile doesn't exist ?
			}
		}
		if (!showJBossCentral) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				JBossCentralActivator.getJBossCentralEditor();
			}
		});
		
	}

}
