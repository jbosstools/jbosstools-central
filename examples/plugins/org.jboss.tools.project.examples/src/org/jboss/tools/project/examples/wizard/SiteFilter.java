/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectModelElement;

/**
 * 
 * @author snjeza
 *
 */
public class SiteFilter extends ViewerFilter {

	private String site;
	
	public SiteFilter() {
		super();
		site = ProjectExamplesActivator.ALL_SITES;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (! (element instanceof ProjectModelElement) ) {
			return false;
		}
		
		if (site.equals(ProjectExamplesActivator.ALL_SITES)) {
			return true;
		}
		
		if (element instanceof ProjectExampleCategory) {
			ProjectExampleCategory category = (ProjectExampleCategory) element;
			List<ProjectExample> projects = category.getProjects();
			for (ProjectExample project:projects) {
				IProjectExampleSite projectSite = project.getSite();
				if (projectSite != null && site.equals(projectSite.getName())) {
					return true;
				}
			}
			return false;
		}
		ProjectExample model = (ProjectExample) element;
		if (model.getSite() != null && site.equals(model.getSite().getName())) {
			return true;
		}
		return false;
	}

	public void setSite(String site) {
		this.site = site;
	}

}
