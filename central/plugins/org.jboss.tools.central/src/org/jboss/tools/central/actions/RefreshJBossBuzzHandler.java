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

package org.jboss.tools.central.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.jboss.tools.central.jobs.RefreshBuzzJob;

/** 
 * 
 * @author snjeza
 *
 */
public class RefreshJBossBuzzHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (RefreshBuzzJob.INSTANCE.getState() == Job.NONE) {
			final RefreshBuzzJob job = RefreshBuzzJob.INSTANCE;
			job.setForcedDownload(true);
			job.setException(null);
			job.setNeedsRefresh(true);
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					job.setForcedDownload(false);
					job.removeJobChangeListener(this);
				}
			});
			job.schedule();
		}
		return null;
	}

}
