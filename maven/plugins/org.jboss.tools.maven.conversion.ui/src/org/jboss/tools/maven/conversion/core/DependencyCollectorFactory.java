/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.maven.conversion.core.internal.ComponentDependencyCollector;
import org.jboss.tools.maven.conversion.core.internal.JavaDependencyCollector;

public class DependencyCollectorFactory {

	public static final DependencyCollectorFactory INSTANCE = new DependencyCollectorFactory();
	
	Set<DependencyCollector> dependencyCollectors = new HashSet<DependencyCollector>();
	
	private DependencyCollectorFactory() {
		initDependencyCollectors();
	}
	
	private void initDependencyCollectors() {
		dependencyCollectors.clear();
		dependencyCollectors.add(new JavaDependencyCollector());
		dependencyCollectors.add(new ComponentDependencyCollector());
	}

	public DependencyCollector getDependencyCollector(IProject project) throws CoreException {
		for (DependencyCollector dc : dependencyCollectors) {
			if (dc.appliesTo(project)) {
				return dc;
			}
		}
		return null;
	}
}
