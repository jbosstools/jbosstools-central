package org.jboss.tools.project.examples.job;

import org.eclipse.core.resources.WorkspaceJob;
import org.jboss.tools.project.examples.ProjectExamplesActivator;

public abstract class ProjectExamplesJob extends WorkspaceJob {

	public ProjectExamplesJob(String name) {
		super(name);
	}

	@Override
	public boolean belongsTo(Object family) {
		if (family == ProjectExamplesActivator.PROJECT_EXAMPLES_FAMILY)
			return true;

		return super.belongsTo(family);
	}

}
