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

package org.jboss.tools.central.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.jobs.RefreshNewsJob;

/** 
 * 
 * @author snjeza
 *
 */
public class RefreshJBossNewsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (RefreshNewsJob.INSTANCE.getState() == Job.NONE) {
			final RefreshNewsJob job = RefreshNewsJob.INSTANCE;
			job.setForcedDownload(true);
			job.setNeedsRefresh(true);
			job.setException(null);
			job.addJobChangeListener(new IJobChangeListener() {
				
				@Override
				public void sleeping(IJobChangeEvent event) {
				}
				
				@Override
				public void scheduled(IJobChangeEvent event) {
				}
				
				@Override
				public void running(IJobChangeEvent event) {
				}
				
				@Override
				public void done(IJobChangeEvent event) {
					job.setForcedDownload(false);
					job.removeJobChangeListener(this);
				}
				
				@Override
				public void awake(IJobChangeEvent event) {
				}
				
				@Override
				public void aboutToRun(IJobChangeEvent event) {
				}
			});
			job.schedule();
		}
		return null;
	}

}
