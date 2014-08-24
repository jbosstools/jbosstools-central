/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenDependencyConversionActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.conversion.ui"; //$NON-NLS-1$
	
	// The shared instance
	private static MavenDependencyConversionActivator plugin;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
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
	public static MavenDependencyConversionActivator getDefault() {
		return plugin;
	}
	
	public static void log(Exception e, String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		plugin.getLog().log(status);
	}

	public static void logWarning(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
		plugin.getLog().log(status);
	}
	
	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		plugin.getLog().log(status);
	}
	
	public static Image getJarIcon() {
		return getIcon("icons/jar_obj.gif");		
	}
	
	public static Image getProjectIcon() {
		return getIcon("icons/projects.gif");
	}

	public static Image getOkIcon() {
		return getIcon("icons/passed.png");		
	}
	
	public static Image getFailedIcon() {
		return getIcon("icons/error_st_obj.gif");
	}

	
	public static Image getLoadingIcon() {
		//animated gifs (loader.gif) )are not animated when used inside cells of tableviewer
		//using an alternate icon to represent search
		return getIcon("icons/find_obj.gif");
	}

	public static Image getIcon(String pathToIcon) {
		ImageDescriptor descriptor = imageDescriptorFromPlugin(PLUGIN_ID, pathToIcon);
        return getIcon(descriptor);
	}

	private static Image getIcon(ImageDescriptor descriptor) {
		Image img = null;
        if(descriptor != null) {
        	img = descriptor.createImage();
        }
        return img;
	}
	
	public static Image getWarningIcon() {
		return getIcon("icons/warning.gif");
	}

}