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
package org.jboss.tools.maven.cdi;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.jboss.tools.cdi.internal.core.project.facet.CDIFacetInstallDataModelProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenCDIActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.cdi"; //$NON-NLS-1$

	// The shared instance
	private static MavenCDIActivator plugin;
	
	/**
	 * The constructor
	 */
	public MavenCDIActivator() {
	}

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
	public static MavenCDIActivator getDefault() {
		return plugin;
	}
	
	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message, null);
		getDefault().getLog().log(status);
	}
	
	public IDataModel createCDIDataModel(IFacetedProject fproj, IProjectFacetVersion facetVersion) {
		IDataModel config = (IDataModel) new CDIFacetInstallDataModelProvider().create();
		config.setBooleanProperty(CDIFacetInstallDataModelProvider.GENERATE_BEANS_XML,false);
		return config;
	}

}
