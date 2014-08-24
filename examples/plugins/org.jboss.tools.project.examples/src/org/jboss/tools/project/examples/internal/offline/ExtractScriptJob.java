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
package org.jboss.tools.project.examples.internal.offline;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.offline.OfflineUtil;


/**
 * Extracts the embedded go_offline.groovy script to the workspace plugin state location.
 * 
 * @author Fred Bricon
 *
 */
public class ExtractScriptJob extends Job {

	public static class ExtractScriptJobRule implements ISchedulingRule {

	    public boolean contains(ISchedulingRule rule) {
	      return rule == this;
	    }

	    public boolean isConflicting(ISchedulingRule rule) {
	      return rule == this;
	    }

	}
	
	public ExtractScriptJob() {
		super("Extract go_offline script"); //$NON-NLS-1$
		setRule(new ExtractScriptJobRule());
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		File offlineScript = OfflineUtil.getGoOfflineScript();
		//No need to overwrite existing script if not in dev mode
		if (!offlineScript.exists() || offlineScript.getName().contains(".qualifier")) { //$NON-NLS-1$
			try {
				URL scriptUrl = new URL("platform:/plugin/org.jboss.tools.project.examples/offline/go_offline.groovy"); //$NON-NLS-1$
				URL sourceUrl = FileLocator.resolve(scriptUrl);
				FileUtils.copyURLToFile(sourceUrl, offlineScript);
			} catch (Exception e) {
				Status error = new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID, "Impossible to copy the go_offline script", e); //$NON-NLS-1$
				return error;
			}
		}
		return Status.OK_STATUS;
	}
}
