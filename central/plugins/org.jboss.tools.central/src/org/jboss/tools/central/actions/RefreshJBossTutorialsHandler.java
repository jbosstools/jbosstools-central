package org.jboss.tools.central.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.jobs.RefreshTutorialsJob;

public class RefreshJBossTutorialsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (RefreshTutorialsJob.INSTANCE.getState() == Job.NONE) {
			JBossCentralActivator.getDefault().setTutorialCategories(null);
			RefreshTutorialsJob.INSTANCE.schedule();
		}
		return null;
	}

}
