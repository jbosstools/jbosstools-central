/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jdt;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.maven.jdt.endorsedlib.IEndorsedLibrariesManager;
import org.jboss.tools.maven.jdt.internal.endorsedlib.EndorsedLibrariesManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenJdtActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.jdt"; //$NON-NLS-1$

	// The shared instance
	private static MavenJdtActivator plugin;

	private IEndorsedLibrariesManager endorsedLibrariesManager;
	
	/**
	 * The constructor
	 */
	public MavenJdtActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		File stateLocationDir = getStateLocation().toFile();
		endorsedLibrariesManager = new EndorsedLibrariesManager(stateLocationDir);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MavenJdtActivator getDefault() {
		return plugin;
	}
	
	public static void log(Throwable e) {
		log(e.getLocalizedMessage(), e);
	}
	
	public static void log(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message, null);
		getDefault().getLog().log(status);
	}
	
	
	public static void log(String message, Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		getDefault().getLog().log(status);
	}

	public IEndorsedLibrariesManager getEndorsedLibrariesManager() {
		return endorsedLibrariesManager;
	}	
}
