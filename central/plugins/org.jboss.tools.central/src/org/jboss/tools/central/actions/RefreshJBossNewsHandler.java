package org.jboss.tools.central.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.jobs.RefreshNewsJob;

public class RefreshJBossNewsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (RefreshNewsJob.INSTANCE.getState() == Job.NONE) {
			RefreshNewsJob.INSTANCE.schedule();
		}
		return null;
	}

}
