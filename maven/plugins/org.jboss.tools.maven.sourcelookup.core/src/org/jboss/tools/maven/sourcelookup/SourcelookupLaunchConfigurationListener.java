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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

/**
 * 
 * @author snjeza
 *
 */
public class SourcelookupLaunchConfigurationListener implements
		ILaunchConfigurationListener {

	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		updateLaunchConfiguration(configuration);
	}

	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		updateLaunchConfiguration(configuration);
	}

	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		// do nothing
	}

	private void updateLaunchConfiguration(ILaunchConfiguration configuration) {
		try {
			if (!SourceLookupActivator.getDefault().isAutoAddSourceContainer()) {
				return;
			}
			if (!SourceLookupActivator.isJBossAsLaunchConfiguration(configuration)) {
				return;
			}
			if (!SourceLookupActivator.m2eExists()) {
				return;
			}
		
			String sourcePathComputer = configuration.getAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, (String) null);
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			if (sourcePathComputer == null) {
				wc.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, SourceLookupActivator.JBOSS_LAUNCH_SOURCE_PATH_COMPUTER_ID);
				wc.doSave();
			}
		} catch (CoreException e) {
			SourceLookupActivator.log(e);
		}
	}

}
