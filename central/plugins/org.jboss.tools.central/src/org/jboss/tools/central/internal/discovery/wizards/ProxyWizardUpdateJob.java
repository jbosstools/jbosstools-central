/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.JBossCentralActivator;

public class ProxyWizardUpdateJob extends Job {

	private ProxyWizardManager proxyWizardManager;

	public static class ProxyWizardUpdaterRule implements ISchedulingRule {

	    public boolean contains(ISchedulingRule rule) {
	      return rule == this;
	    }

	    public boolean isConflicting(ISchedulingRule rule) {
	      return rule == this;
	    }

	}

	public ProxyWizardUpdateJob(ProxyWizardManager proxyWizardManager) {
		super("Update project wizard list");
		this.proxyWizardManager = proxyWizardManager;
		setRule(new ProxyWizardUpdaterRule());
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		proxyWizardManager.loadWizards(true, monitor);
		return Status.OK_STATUS;
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return family == JBossCentralActivator.JBOSS_CENTRAL_FAMILY;
	}
}