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

	private static final String ORG_JBOSS_TOOLS_USAGE = "org.jboss.tools.usage";

	@Override
	public void earlyStartup() {
		boolean showJBossCentral = JBossCentralActivator.getDefault()
				.showJBossCentralOnStartup();
		if (!showJBossCentral) {
			Bundle usage = Platform.getBundle(ORG_JBOSS_TOOLS_USAGE);
			if (usage != null) {
				Version version = usage.getVersion();
				String versionString = version.toString();
				IEclipsePreferences prefs = JBossCentralActivator.getDefault()
						.getPreferences();
				String savedVersion = prefs.get(ORG_JBOSS_TOOLS_USAGE, "");
				Bundle central = Platform
						.getBundle(JBossCentralActivator.PLUGIN_ID);
				if (!savedVersion.equals(versionString)) {
					showJBossCentral = true;
					prefs.put(ORG_JBOSS_TOOLS_USAGE, versionString);
					if (central != null) {
						prefs.put(JBossCentralActivator.PLUGIN_ID, central.getVersion().toString());
					}
				} else {
					if (central != null) {
						version = central.getVersion();
						versionString = version.toString();
						savedVersion = prefs.get(
								JBossCentralActivator.PLUGIN_ID, "");
						if (!savedVersion.equals(versionString)) {
							showJBossCentral = true;
							prefs.put(JBossCentralActivator.PLUGIN_ID, versionString);
						}
					}
				}
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
