/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;

/**
 * Manager for {@link ProjectExample}
 * 
 * @provisional This class is considered provisional API and can be changed,
 *              moved or removed without notice
 * @author Fred Bricon
 * @since 3.0
 */

public interface IProjectExampleManager {

	ProjectExampleWorkingCopy createWorkingCopy(ProjectExample example);

	Collection<ProjectExample> getExamples(IProgressMonitor monitor) throws CoreException;

}
