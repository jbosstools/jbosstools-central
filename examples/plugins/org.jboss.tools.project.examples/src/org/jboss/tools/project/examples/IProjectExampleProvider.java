package org.jboss.tools.project.examples;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.model.ProjectExample;

public interface IProjectExampleProvider {

	Collection<ProjectExample> getExamples(IProgressMonitor monitor) throws CoreException;

}
