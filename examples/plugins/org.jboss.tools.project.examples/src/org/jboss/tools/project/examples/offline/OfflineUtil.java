/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.offline;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;

/**
 * Utility class related to offline support
 * 
 * @author Fred Bricon
 *
 */
public class OfflineUtil {

	private static final String CURRENT_VERSION;
	
	static {
		String version = Platform.getBundle(ProjectExamplesActivator.PLUGIN_ID).getHeaders().get("Bundle-Version"); //$NON-NLS-1$
		CURRENT_VERSION = version.replace('-', '_'); //$NON-NLS-1$
	}
	
	private OfflineUtil() {
	}

	/**
	 * Returns the go_offline.groovy script {@link File} under the workspace plugin location
	 */
	public static File getGoOfflineScript() {
		File baseDir = ProjectExamplesActivator.getDefault().getStateLocation().toFile();
		File offlineScript = new File(baseDir, "offline/go_offline_"+CURRENT_VERSION+".groovy"); //$NON-NLS-1$
		return offlineScript;
	}
	
	/**
	 * Get existing File from the offline cache directory. 
	 * Returns <code>null</code> if no cached file is available. 
	 */
	public static File getOfflineFile(URL url) {
		String offlineCachePath = ProjectExamplesActivator.getDefault().getPreferenceStore().getString(ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_DIRECTORY);
		File cachedFile = new File(offlineCachePath, url.getFile());
		return cachedFile.exists()?cachedFile:null;
	}
	
	public static boolean isOfflineEnabled() {
		return ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_ENABLED);
	}

}
