/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.fixes;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

/**
 * This is a trimmed down copy of <a href="https://github.com/jbosstools/jbosstools-portlet/blob/master/plugins/org.jboss.tools.portlet.core/src/org/jboss/tools/portlet/core/internal/PortletRuntimeComponentProvider.java">PortletRuntimeComponentProvider.java</a> to severe dependencies to the portlet plugin.
 * Hope it doesn't turn into a long-term temporary hack.
 * 
 * @author snjeza
 * @author Fred Bricon
 */
class PortletRuntimeComponentProvider {

	public static final String IS_PORTLET_RUNTIME = "isPortletRuntime"; //$NON-NLS-1$

	public static boolean isPortalPresent(final File location,
			IRuntime runtime, String property) {
		IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if (jbossRuntime != null) {
			// JBoss Portal server
			IPath jbossLocation = runtime.getLocation();
			IPath configPath = jbossLocation.append(IJBossServerConstants.SERVER).append(jbossRuntime.getJBossConfiguration());
			File configFile = configPath.toFile();
			// JBoss Portal server
			if (exists(configFile,
					TemporaryIPortletConstantsFork.SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR)) {
				return true;
			}
			// JBoss Portal clustering server
			if (exists(configFile,
					TemporaryIPortletConstantsFork.SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR)) {
				return true;
			}
			// JBoss portletcontainer
			if (exists(configFile,TemporaryIPortletConstantsFork.SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL) ||
					exists(configFile,TemporaryIPortletConstantsFork.SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR)) {
				return true;
			}
			// GateIn Portal Server
			if (exists(configFile, TemporaryIPortletConstantsFork.SERVER_DEFAULT_DEPLOY_GATEIN)) {
				return true;
			}
			// GateIn Portal Server 3.3 JBoss AS 7
			if (exists(jbossLocation.toFile(), TemporaryIPortletConstantsFork.SERVER_DEFAULT_DEPLOY_GATEIN33)) {
				return true;
			}
			// JBoss JPP 6.0
			if (exists(jbossLocation.toFile(), TemporaryIPortletConstantsFork.SERVER_DEFAULT_DEPLOY_JPP60)) {
				return true;
			}
			return false;
		}
		// Tomcat portletcontainer
		if (!IS_PORTLET_RUNTIME.equals(property)) {
			return false;
		}
		File tomcatLib = new File(location,TemporaryIPortletConstantsFork.TOMCAT_LIB);
		if (tomcatLib.exists() && tomcatLib.isDirectory()) {
			String[] files = tomcatLib.list(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.startsWith(TemporaryIPortletConstantsFork.PORTLET_API) && name.endsWith(TemporaryIPortletConstantsFork.JAR)) {
						return true;
					}
					return false;
				}
				
			});
			return files.length > 0;
		}
		
		return false;
	}


	private static boolean exists(final File location,String portalDir) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			portalDir = portalDir.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		File file = new File(location,portalDir);
		return file.exists();
	}
}