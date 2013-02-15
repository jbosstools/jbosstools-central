/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.gwt;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.internal.IMavenConstants;

import com.google.gdt.eclipse.core.launch.LaunchConfigurationUtilities;
import com.google.gwt.eclipse.oophm.model.BrowserTab;
import com.google.gwt.eclipse.oophm.model.IWebAppDebugModelListener;
import com.google.gwt.eclipse.oophm.model.LaunchConfiguration;
import com.google.gwt.eclipse.oophm.model.Server;
import com.google.gwt.eclipse.oophm.model.WebAppDebugModelEvent;

/**
 * GWT Debug Mode launch configuration listener refreshing the project hierarchy 
 * to allow automatic publishing of Dev Mode generated files on WTP servers; 
 * 
 * @author Fred Bricon
 */
public class MavenGwtDebugModeListener implements IWebAppDebugModelListener {

	@Override
	public void browserTabCreated(WebAppDebugModelEvent<BrowserTab> arg0) {
	}

	@Override
	public void browserTabNeedsAttention(WebAppDebugModelEvent<BrowserTab> arg0) {
	}

	@Override
	public void browserTabRemoved(WebAppDebugModelEvent<BrowserTab> arg0) {
	}

	@Override
	public void browserTabTerminated(WebAppDebugModelEvent<BrowserTab> arg0) {
	}

	@Override
	public void launchConfigurationLaunchUrlsChanged(
			WebAppDebugModelEvent<LaunchConfiguration> arg0) {
	}

	@Override
	public void launchConfigurationLaunched(
			WebAppDebugModelEvent<LaunchConfiguration> event) {
	}


	@Override
	public void launchConfigurationRemoved(
			WebAppDebugModelEvent<LaunchConfiguration> arg0) {
	}

	@Override
	public void launchConfigurationRestartWebServerStatusChanged(
			WebAppDebugModelEvent<LaunchConfiguration> arg0) {
		try {
			LaunchConfiguration lc = arg0.getElement();
			IProject project = LaunchConfigurationUtilities.getProject(lc.getLaunch().getLaunchConfiguration());
			if (project.hasNature(IMavenConstants.NATURE_ID)) {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void launchConfigurationTerminated(
			WebAppDebugModelEvent<LaunchConfiguration> arg0) {
	}

	@Override
	public void serverCreated(WebAppDebugModelEvent<Server> arg0) {
	}

	@Override
	public void serverNeedsAttention(WebAppDebugModelEvent<Server> arg0) {
	}

	@Override
	public void serverTerminated(WebAppDebugModelEvent<Server> arg0) {
	}
	
}
