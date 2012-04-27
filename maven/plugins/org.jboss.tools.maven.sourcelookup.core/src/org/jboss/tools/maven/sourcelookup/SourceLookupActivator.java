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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
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
	public static final String AUTO_ADD_JBOSS_SOURCE_CONTAINER = "autoAddJBossSourceContainer";
	public static final boolean AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT = false;
	private static final String NEXUS_REPOSITORIES = "nexusRepositories";
	private static final String NEXUS_REPOSITORY = "nexusRepository";
	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String ENABLED = "enabled";

	private static Set<NexusRepository> nexusRepositories;

	// The shared instance
	private static SourceLookupActivator plugin;
	
	private SourcelookupLaunchConfigurationListener listener;
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
		listener = new SourcelookupLaunchConfigurationListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.context = null;
		if (listener != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(listener);
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
	
	public static boolean isJBossAsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		return AS7_LAUNCH_CONFIGURATION_ID
				.equals(configuration.getType().getIdentifier()) ||
				AS_LAUNCH_CONFIGURATION_ID
				.equals(configuration.getType().getIdentifier());
	}
	
	public static Set<NexusRepository> getNexusRepositories() {
		if (nexusRepositories == null) {
			try {
				nexusRepositories = loadNexusRepositoriesFromPreferences();
			} catch (WorkbenchException e) {
				log(e);
			}
			if (nexusRepositories == null) {
				getDefaultRepositories();
			}
		}
		return nexusRepositories;
	}

	public static Set<NexusRepository> loadNexusRepositoriesFromPreferences() throws WorkbenchException {
		String repositories = getPreferences().get(NEXUS_REPOSITORIES, null);
		if (repositories == null || repositories.isEmpty()) {
			return null;
		}
		Reader reader = new StringReader(repositories);
		XMLMemento memento = XMLMemento.createReadRoot(reader);
		IMemento[] nodes = memento.getChildren(NEXUS_REPOSITORY);
		for (IMemento node:nodes) {
			if (nexusRepositories == null) {
				nexusRepositories = new LinkedHashSet<NexusRepository>();
			}
			String name = node.getString(NAME);
			String url = node.getString(URL);
			boolean enabled = node.getBoolean(ENABLED);
			NexusRepository repository = new NexusRepository(name, url, enabled);
			nexusRepositories.add(repository);
		}
		return nexusRepositories;
	}

	public static Set<NexusRepository> getDefaultRepositories() {
		nexusRepositories = new LinkedHashSet<NexusRepository>();
		addRepository("JBoss Nexus Repository", "https://repository.jboss.org/nexus", true);
		addRepository("Sonatype Nexus Repository", "http://repository.sonatype.org", false);
		addRepository("Apache Nexus Repository", "https://repository.apache.org", false);
		addRepository("Sonatype OSS Repository", "http://oss.sonatype.org", false);
		addRepository("Codehaus Repository", "https://nexus.codehaus.org", false);
		addRepository("Java.net Repository", "https://maven.java.net", false);
		return nexusRepositories;
	}

	private static void addRepository(String name, String url, boolean enabled) {
		NexusRepository repository = new NexusRepository(name, url, enabled);
		nexusRepositories.add(repository);
	}

	public static void setNexusRepositories(Set<NexusRepository> nexusRepositories) {
		SourceLookupActivator.nexusRepositories = nexusRepositories;
	}

	public static void saveNexusRepositories() {
		if (nexusRepositories == null || nexusRepositories.size() == 0) {
			return;
		}
		XMLMemento memento = XMLMemento.createWriteRoot(NEXUS_REPOSITORIES);
		Writer writer = null;
		try {
			for (NexusRepository repository:nexusRepositories) {
				IMemento repositoryNode = memento.createChild(NEXUS_REPOSITORY);
				repositoryNode.putString(NAME, repository.getName());
				repositoryNode.putString(URL, repository.getUrl());
				repositoryNode.putBoolean(ENABLED, repository.isEnabled());
			}
			writer = new StringWriter();
			memento.save(writer);
			writer.flush();
			String repositories = writer.toString();
			getPreferences().put(NEXUS_REPOSITORIES, repositories);
			getPreferences().flush();
		} catch (Exception e) {
			log(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}
