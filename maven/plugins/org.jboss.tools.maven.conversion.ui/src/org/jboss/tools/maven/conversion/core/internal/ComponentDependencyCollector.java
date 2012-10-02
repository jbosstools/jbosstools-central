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
package org.jboss.tools.maven.conversion.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.jboss.tools.maven.conversion.core.DependencyCollector;
import org.jboss.tools.maven.conversion.core.ProjectDependency;

/**
 * Collects {@link ProjectDependency}'s from a non-java component project's references
 * 
 * @author Fred Bricon
 *
 */
public class ComponentDependencyCollector extends DependencyCollector {

	@Override
	public List<ProjectDependency> collectDependencies(IProject project) throws CoreException {
		if (!appliesTo(project)) {
			return null;
		}
		List<ProjectDependency> moduleDependencies = new ArrayList<ProjectDependency>();
		collectReferences(project, moduleDependencies);
		return moduleDependencies;
	}

	private void collectReferences(IProject project, List<ProjectDependency> moduleDependencies) {
		IVirtualComponent component = ComponentCore.createComponent(project, true);
		if (component == null) {
			return;
		}
		IVirtualReference[] references = component.getReferences();
		if (references == null || references.length == 0) {
			return;
		}
		for (IVirtualReference r : references) {
			moduleDependencies.add(new ComponentDependency(r.getReferencedComponent()));
		}
	}
	
	@Override
	public boolean appliesTo(IProject project) throws CoreException {
		return project != null && 
			   ModuleCoreNature.isFlexibleProject(project) && 
			   !project.hasNature(JavaCore.NATURE_ID);
	}

}
