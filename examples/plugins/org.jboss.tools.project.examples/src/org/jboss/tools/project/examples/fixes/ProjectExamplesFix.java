package org.jboss.tools.project.examples.fixes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;

public interface ProjectExamplesFix {

	boolean canFix(Project project, ProjectFix fix);
	boolean fix(Project project, ProjectFix fix, IProgressMonitor monitor);
}
