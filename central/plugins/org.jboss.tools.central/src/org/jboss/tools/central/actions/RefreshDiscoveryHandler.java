package org.jboss.tools.central.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.jobs.RefreshDiscoveryJob;

public class RefreshDiscoveryHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (RefreshDiscoveryJob.INSTANCE.getState() == Job.NONE) {
			RefreshDiscoveryJob.INSTANCE.schedule();
		}
		return null;
	}

}
