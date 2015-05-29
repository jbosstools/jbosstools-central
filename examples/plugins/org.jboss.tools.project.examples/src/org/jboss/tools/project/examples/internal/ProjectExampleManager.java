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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.IProjectExampleManager;
import org.jboss.tools.project.examples.IProjectExampleProvider;
import org.jboss.tools.project.examples.fixes.ProjectFixManager;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;

@SuppressWarnings("nls")
public class ProjectExampleManager implements IProjectExampleManager {

	private ProjectFixManager projectFixManager;
	
	private List<IProjectExampleProvider> exampleProviders;

	public ProjectExampleManager(ProjectFixManager projectFixManager) {
		this.projectFixManager = projectFixManager;
		exampleProviders = new ArrayList<>();
		exampleProviders.add(new ProjectExampleJsonProvider());
		exampleProviders.add(new ProjectExampleXmlProvider());
	}

	@Override
	public ProjectExampleWorkingCopy createWorkingCopy(ProjectExample example) {
		ProjectExampleWorkingCopy workingCopy;
		if (example instanceof ProjectExampleWorkingCopy) {
			workingCopy = (ProjectExampleWorkingCopy)example;
		} else {
			workingCopy = new ProjectExampleWorkingCopy(example);
		}
		projectFixManager.loadFixes(workingCopy);
		return workingCopy;
	}

	public Collection<ProjectExample> getExamples(IProgressMonitor monitor) throws CoreException {
		List<ProjectExample> examples = new ArrayList<>();
		//TODO parallelize processing 
		for (IProjectExampleProvider provider : exampleProviders) {
			if (monitor.isCanceled()) {
				break;
			}
			//long start = System.currentTimeMillis();
			examples.addAll(provider.getExamples(monitor));
			//long elapsed = System.currentTimeMillis() - start;
			//System.err.println("Fetched examples from "+ provider.getClass().getSimpleName() + " in "+ elapsed +" ms");
		}
		return examples;
		
	}
}
