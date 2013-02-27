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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * 
 * @author snjeza
 *
 */
public class ShowJBossCentral implements IStartup {

	private static final String ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW = "org.jboss.tools.central.donotshow"; //$NON-NLS-1$
	private static final String ORG_JBOSS_TOOLS_USAGE = "org.jboss.tools.usage"; //$NON-NLS-1$

	@Override
	public void earlyStartup() {
		String show = System.getProperty(ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW);
		if (show != null && "true".equals(show)) {
			return;
		}
		boolean showJBossCentral = JBossCentralActivator.getDefault()
				.showJBossCentralOnStartup();
		IEclipsePreferences prefs = JBossCentralActivator.getDefault()
				.getPreferences();
		Bundle usage = Platform.getBundle(ORG_JBOSS_TOOLS_USAGE);
		Bundle central = Platform.getBundle(JBossCentralActivator.PLUGIN_ID);
		if (!showJBossCentral) {
			if (usage != null) {
				Version version = usage.getVersion();
				String versionString = version.toString();
				String savedVersion = prefs.get(ORG_JBOSS_TOOLS_USAGE, "");
				if (!savedVersion.equals(versionString)) {
					showJBossCentral = true;
				} else {
					if (central != null) {
						version = central.getVersion();
						versionString = version.toString();
						savedVersion = prefs.get(
								JBossCentralActivator.PLUGIN_ID, "");
						if (!savedVersion.equals(versionString)) {
							showJBossCentral = true;
						}
					}
				}
			}
		}
		saveVersion(prefs, usage, ORG_JBOSS_TOOLS_USAGE);
		saveVersion(prefs, central, JBossCentralActivator.PLUGIN_ID);
		
		if (!showJBossCentral) {
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				JBossCentralActivator.getJBossCentralEditor(false);
			}
		});
	}

	protected void saveVersion(IEclipsePreferences prefs, Bundle bundle, String preference) {
		if (bundle == null || prefs == null || preference == null) {
			return;
		}
		Version version = bundle.getVersion();
		String versionString = version.toString();
		String savedVersion = prefs.get(preference, "");
		if (!savedVersion.equals(versionString)) {
			prefs.put(preference, versionString);
		}
	}

}
