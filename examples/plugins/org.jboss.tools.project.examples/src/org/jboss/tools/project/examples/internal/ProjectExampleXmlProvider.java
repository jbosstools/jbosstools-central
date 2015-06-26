/*************************************************************************************
 * Copyright (c) 2014-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.IProjectExampleProvider;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;

public class ProjectExampleXmlProvider implements IProjectExampleProvider {

	@Override
	public Collection<ProjectExample> getExamples(IProgressMonitor monitor) throws CoreException {
		List<ProjectExampleCategory> categories = ProjectExampleUtil.getCategories(monitor);
		List<ProjectExample> examples = new ArrayList<>();
		for (ProjectExampleCategory category : categories) {
			examples.addAll(category.getProjects());
		}
		return examples;
	}

}
