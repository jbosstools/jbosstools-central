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
package org.jboss.tools.project.examples.wizard;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Category;
import org.jboss.tools.project.examples.model.Project;
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
		if (element instanceof Category) {
			Category category = (Category) element;
			int size = 0;
			if (site.equals(ProjectExamplesActivator.ALL_SITES)) {
				size += category.getProjects().size();
			} else {
				List<Project> projects = category.getProjects();
				for (Project project:projects) {
					if (site.equals(project.getSite())) {
						size++;
					}
				}
			}
			return size > 0;
		}
		ProjectModelElement model = (ProjectModelElement) element;
		if ( site.equals(ProjectExamplesActivator.ALL_SITES) || site.equals(model.getSite())) {
			return true;
		}
		return false;
	}

	public void setSite(String site) {
		this.site = site;
	}

}
