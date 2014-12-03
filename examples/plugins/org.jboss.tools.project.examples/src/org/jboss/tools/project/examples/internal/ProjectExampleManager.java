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
package org.jboss.tools.project.examples.internal;

import org.jboss.tools.project.examples.IProjectExampleManager;
import org.jboss.tools.project.examples.fixes.ProjectFixManager;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;

public class ProjectExampleManager implements IProjectExampleManager {

	private ProjectFixManager projectFixManager;

	public ProjectExampleManager(ProjectFixManager projectFixManager) {
		this.projectFixManager = projectFixManager;
	}
	
	@Override
	public ProjectExampleWorkingCopy createWorkingCopy(ProjectExample example) {
		ProjectExampleWorkingCopy workingCopy = new ProjectExampleWorkingCopy(example);
		projectFixManager.loadFixes(workingCopy);
		return workingCopy;
	}

}
