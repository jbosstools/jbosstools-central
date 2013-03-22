/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.maven.sourcelookup.internal.search.ServerLifecycleListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class SourceLookupActivator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.sourcelookup.core"; //$NON-NLS-1$
	public static final String AS7_LAUNCH_CONFIGURATION_ID = "org.jboss.ide.eclipse.as.core.server.JBoss7StartupConfiguration"; //$NON-NLS-1$
	public static final String AS_LAUNCH_CONFIGURATION_ID = "org.jboss.ide.eclipse.as.core.server.startupConfiguration"; //$NON-NLS-1$
	
	private static final String MAVEN_PLUGIN_ID = "org.eclipse.m2e.core"; //$NON-NLS-1$
	public static final String JBOSS_LAUNCH_SOURCE_PATH_COMPUTER_ID = "org.jboss.tools.maven.sourcelookup.SourcePathComputer"; //$NON-NLS-1$
	public static final String AUTO_ADD_JBOSS_SOURCE_CONTAINER = "autoAddJBossSourceContainer"; //$NON-NLS-1$
	public static final boolean AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT = false;

	public static final String INCLUDE_PATTERN = "includePattern"; //$NON-NLS-1$
	public static final String EXCLUDE_PATTERN = "excludePattern"; //$NON-NLS-1$
	
	public static final String INCLUDE_PATTERN_DEFAULT = ".*\\.jar"; //$NON-NLS-1$
	public static final String EXCLUDE_PATTERN_DEFAULT = ".*(jsf-impl-1\\.2|jsf-api-1\\.2|jpa-hibernate3).*"; //$NON-NLS-1$
	
	public static final String SEARCH_SERVERS = "searchServers"; //$NON-NLS-1$
	public static final String SEARCH_SERVERS_VALUE = ""; //$NON-NLS-1$
	
	// The shared instance
	private static SourceLookupActivator plugin;
	
	private SourcelookupLaunchConfigurationListener launchConfigurationListener;
	private ServerLifecycleListener serverLifecycleListener;
	private BundleContext context;
	
	/**
	 * The constructor
	 */
	public SourceLookupActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		plugin = this;
		launchConfigurationListener = new SourcelookupLaunchConfigurationListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(launchConfigurationListener);
		serverLifecycleListener = new ServerLifecycleListener();
		ServerCore.addRuntimeLifecycleListener(serverLifecycleListener);
		ServerCore.addServerLifecycleListener(serverLifecycleListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.context = null;
		if (launchConfigurationListener != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(launchConfigurationListener);
		}
		if (serverLifecycleListener != null) {
			ServerCore.removeRuntimeLifecycleListener(serverLifecycleListener);
			ServerCore.removeServerLifecycleListener(serverLifecycleListener);
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SourceLookupActivator getDefault() {
		return plugin;
	}
	
	public static void log(Exception e, String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		plugin.getLog().log(status);
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		plugin.getLog().log(status);
	}

	public ILog getLog() {
		Bundle bundle = context.getBundle();
		return InternalPlatform.getDefault().getLog(bundle);
	}
	
	public static boolean m2eExists() {
		Bundle bundle = Platform.getBundle(MAVEN_PLUGIN_ID);
		return bundle != null;
	}
	
	public static IEclipsePreferences getPreferences() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		return prefs;
	}
	
	public void savePreferences() {
		IEclipsePreferences prefs = getPreferences();
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			log(e);
		}
	}

	public boolean isAutoAddSourceContainer() {
		return getPreferences().getBoolean(AUTO_ADD_JBOSS_SOURCE_CONTAINER, AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
	}
	
	/**
	 * @return the include pattern preference; 
	 * a regular expression that serves to include libraries to the Java Search
	 */
	public String getIncludePattern() {
		return getPreferences().get(INCLUDE_PATTERN, INCLUDE_PATTERN_DEFAULT);
	}
	
	/**
	 * @return the exclude pattern preference; 
	 * a regular expression that serves to exclude libraries to the Java Search
	 */
	public String getExcludePattern() {
		return getPreferences().get(EXCLUDE_PATTERN, EXCLUDE_PATTERN_DEFAULT);
	}
	
	public String getSearchServers() {
		return getPreferences().get(SEARCH_SERVERS, SEARCH_SERVERS_VALUE);
	}
	
	/**
	 * 
	 * @param configuration
	 * @return the ids of the servers that will be included in the Java Search
	 * @throws CoreException
	 */
	public static boolean isJBossAsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		return AS7_LAUNCH_CONFIGURATION_ID
				.equals(configuration.getType().getIdentifier()) ||
				AS_LAUNCH_CONFIGURATION_ID
				.equals(configuration.getType().getIdentifier());
	}
	
}
