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
package org.jboss.tools.central.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza
 *
 */
public class RefreshDiscoveryJob extends Job {

	public static RefreshDiscoveryJob INSTANCE = new RefreshDiscoveryJob();
	
	private RefreshDiscoveryJob() {
		super("Discovering...");
		setPriority(LONG);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		final JBossCentralEditor[] editors = new JBossCentralEditor[1];
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				editors[0] = JBossCentralActivator.getJBossCentralEditor(false);
			}
		});
		if (editors[0] != null) {
			SoftwarePage softwarePage = editors[0].getSoftwarePage();
			if (softwarePage != null) {
				softwarePage.getDiscoveryViewer().updateDiscovery();
			}
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return family == JBossCentralActivator.JBOSS_CENTRAL_FAMILY;
	}

}
