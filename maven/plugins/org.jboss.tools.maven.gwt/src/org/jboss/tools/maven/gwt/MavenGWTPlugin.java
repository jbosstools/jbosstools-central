/*******************************************************************************
 * Copyright (c) 2012-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.gwt;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gwt.eclipse.oophm.model.IWebAppDebugModelListener;
import com.google.gwt.eclipse.oophm.model.WebAppDebugModel;

public class MavenGWTPlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private IWebAppDebugModelListener listener;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		MavenGWTPlugin.context = bundleContext;
		listener = new MavenGwtDebugModeListener();
		WebAppDebugModel.getInstance().addWebAppDebugModelListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		MavenGWTPlugin.context = null;
		WebAppDebugModel.getInstance().removeWebAppDebugModelListener(listener);
	}

	public static void log(String message, Exception e) {
		Platform.getLog(MavenGWTPlugin.getContext().getBundle()).log(new Status(IStatus.ERROR,context.getBundle().getSymbolicName(),message));
	}
	
}
