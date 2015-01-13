package org.jboss.tools.project.examples.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.model.ProjectExample;

public interface IProjectExampleParser {

	Collection<ProjectExample> parse(InputStream json, IProgressMonitor monitor) throws IOException;

}
