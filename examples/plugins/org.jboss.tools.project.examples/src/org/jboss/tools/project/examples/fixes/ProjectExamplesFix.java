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
package org.jboss.tools.project.examples.fixes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;

/**
 * 
 * @author snjeza
 *
 */
public interface ProjectExamplesFix {

	boolean canFix(Project project, ProjectFix fix);
	boolean fix(Project project, ProjectFix fix, IProgressMonitor monitor);
}
