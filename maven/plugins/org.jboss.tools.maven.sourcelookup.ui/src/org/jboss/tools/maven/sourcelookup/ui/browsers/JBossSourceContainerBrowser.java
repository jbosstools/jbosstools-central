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
package org.jboss.tools.maven.sourcelookup.ui.browsers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.containers.JBossSourceContainer;
import org.jboss.tools.maven.sourcelookup.ui.SourceLookupUIActivator;

/**
 * 
 * @author snjeza
 *
 */
public class JBossSourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		ISourceContainer[] containers = new ISourceContainer[1];
		if (director != null) {
			ILaunchConfiguration configuration = director.getLaunchConfiguration();
			if (configuration != null) {
				try {
					if (SourceLookupActivator.isJBossAsLaunchConfiguration(configuration)) {
						containers[0] = new JBossSourceContainer(configuration);
						return containers;
					}
				} catch (CoreException e) {
					SourceLookupUIActivator.log(e);
				}
			}
		}
		
		JBossSourceContainerDialog dialog = new JBossSourceContainerDialog(shell);
		if (dialog.open() == Window.OK && dialog.getHomePath() != null) {
			containers[0] = new JBossSourceContainer(dialog.getHomePath());
			return containers;
		}
		return new ISourceContainer[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		if (containers.length == 1 && JBossSourceContainer.TYPE_ID.equals(containers[0].getType().getId())) {
			if (director != null) {
				ILaunchConfiguration configuration = director
						.getLaunchConfiguration();
				if (configuration != null) {
					try {
						if (SourceLookupActivator.isJBossAsLaunchConfiguration(configuration)) {
							return false;
						}
					} catch (CoreException e) {
						// ignore
					}
				}
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		if (containers.length == 1 && JBossSourceContainer.TYPE_ID.equals(containers[0].getType().getId()) ) {
			JBossSourceContainerDialog dialog = new JBossSourceContainerDialog(shell);
			if (dialog.open() == Window.OK && dialog.getHomePath() != null) {
				containers[0].dispose();
				containers[0] = new JBossSourceContainer(dialog.getHomePath());
				return containers;
			}
		}
		return containers;
	}
	
}
