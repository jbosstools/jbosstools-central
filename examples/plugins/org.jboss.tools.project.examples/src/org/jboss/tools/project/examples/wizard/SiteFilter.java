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
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExample;
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
		if (element instanceof ProjectExampleCategory) {
			ProjectExampleCategory category = (ProjectExampleCategory) element;
			int size = 0;
			if (site.equals(ProjectExamplesActivator.ALL_SITES)) {
				size += category.getProjects().size();
			} else {
				IProjectExampleSite categorySite = category.getSite();
				if (categorySite == null) {
					return false;
				}
				if (!site.equals(categorySite.getName())) {
					return false;
				}
				List<ProjectExample> projects = category.getProjects();
				for (ProjectExample project:projects) {
					IProjectExampleSite projectSite = project.getSite();
					if (projectSite == null) {
						continue;
					}
					
					if (site.equals(projectSite.getName())) {
						size++;
					}
				}
			}
			return size > 0;
		}
		ProjectModelElement model = (ProjectModelElement) element;
		if (model.getSite() == null) {
			return false;
		}
		if ( site.equals(ProjectExamplesActivator.ALL_SITES) || site.equals(model.getSite().getName())) {
			return true;
		}
		return false;
	}

	public void setSite(String site) {
		this.site = site;
	}

}
