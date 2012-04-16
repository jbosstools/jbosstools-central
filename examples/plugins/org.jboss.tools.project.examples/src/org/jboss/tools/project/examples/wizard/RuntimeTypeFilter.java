/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectModelElement;

/**
 * 
 * @author Fred Bricon
 *
 */
public class RuntimeTypeFilter extends ViewerFilter {

	private String runtimeType;
	
	public RuntimeTypeFilter() {
		super();
		runtimeType = Messages.ProjectExamplesActivator_All;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (! (element instanceof ProjectModelElement) ) {
			return false;
		}
		if (runtimeType.equals(ProjectExamplesActivator.ALL_RUNTIMES)) {
			return true;
		} 
		
		if (element instanceof ProjectExampleCategory) {
			ProjectExampleCategory category = (ProjectExampleCategory) element;
			List<ProjectExample> projects = category.getProjects();
			for (ProjectExample project:projects) {
				if (hasServer(project)) {
						return true;
				}
			}
			return false;
		}
		boolean select = false;
		if (element instanceof ProjectExample) {
			select = hasServer((ProjectExample) element);
		}
		return select;
	}

	private boolean hasServer(ProjectExample project) {
		Set<IRuntimeType> runtimes = WTPRuntimeFix.getTargetedServerRuntimes(project);
		for (IRuntimeType r : runtimes) {
			if (runtimeType.equals(r.getName())) {
				return true;
			}
		}
		return false;
	}

	public void setRuntimeType(String runtimeType) {
		this.runtimeType = runtimeType;
	}

}
