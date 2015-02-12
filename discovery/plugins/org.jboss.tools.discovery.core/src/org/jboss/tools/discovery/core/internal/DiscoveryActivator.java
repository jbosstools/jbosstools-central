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
package org.jboss.tools.discovery.core.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.foundation.core.properties.IPropertiesProvider;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DiscoveryActivator extends AbstractUIPlugin {

	private static final String SEPARATOR = "/"; //$NON-NLS-1$
	
	public static final String PROPERTY_PROJECT_NAME = "projectName"; //$NON-NLS-1$
	public static final String PROPERTY_LOCATION_PATH = "locationPath"; //$NON-NLS-1$
	public static final String PROPERTY_ARTIFACT_ID = "artifactId"; //$NON-NLS-1$
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.discovery.core"; //$NON-NLS-1$

	public static final String JBOSS_DISCOVERY_DIRECTORY = "jboss.discovery.directory.url"; //$NON-NLS-1$
	
	// The shared instance
	private static DiscoveryActivator plugin;

	private static final String CENTRAL_COMPONENT_NAME = "central"; //$NON-NLS-1$
	private static final String INSTALL_ACTION = "install"; //$NON-NLS-1$
	private UsageEventType installSoftwareEventType;

	/**
	 * The constructor
	 */
	public DiscoveryActivator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		String version = UsageEventType.getVersion(this);
		installSoftwareEventType = new UsageEventType(CENTRAL_COMPONENT_NAME, version , null, INSTALL_ACTION, Messages.UsageEventTypeInstallLabelDescription);
		UsageReporter.getInstance().registerEvent(installSoftwareEventType);
	}

	public UsageEventType getInstallSoftwareEventType() {
		return installSoftwareEventType;
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static DiscoveryActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		DiscoveryActivator.getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		log(IStatus.WARNING, message);
	}
	
	public static void logError(String message) {
		log(IStatus.ERROR, message);
	}

	public static void log(int severity, String message) {
		IStatus status = new Status(severity, PLUGIN_ID,message);
		DiscoveryActivator.getDefault().getLog().log(status);
	}
	
	public Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry imageRegistry = getImageRegistry();
		String id = getImageId(imageDescriptor);
		Image image = imageRegistry.get(id);
		if (image == null) {
			image = imageDescriptor.createImage(true);
			imageRegistry.put(id, image);
		}
		return image;
	}

	private String getImageId(ImageDescriptor imageDescriptor) {
		return PLUGIN_ID + SEPARATOR + imageDescriptor.hashCode(); //$NON-NLS-1$
	}
	
	public Image getImage(String imagePath) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(imagePath);
		if (image != null) {
			return image;
		}
		ImageDescriptor imageDescriptor = getImageDescriptor(imagePath);
		image = imageDescriptor.createImage();
		registry.put(imagePath, image);
		return image;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public String getJBossDiscoveryDirectory() {
		// use commandline override -Djboss.discovery.directory.url
		String directory = System.getProperty(JBOSS_DISCOVERY_DIRECTORY, null);
		if (directory == null) {
			IPropertiesProvider pp = PropertiesHelper.getPropertiesProvider();
			directory = pp.getValue(JBOSS_DISCOVERY_DIRECTORY);
		}
		if (directory == null) {
			logError(String.format("No URL set for discovery catalog. Property %s is missing!", DiscoveryActivator.JBOSS_DISCOVERY_DIRECTORY)); //$NON-NLS-1$
		}
		return directory;
	}

}
