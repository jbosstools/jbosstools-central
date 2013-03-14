/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.ui.internal.filter;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jboss.tools.maven.sourcelookup.internal.util.SourceLookupUtil;

/**
 * Filters the .JBoss Servers project in the Package Explorer and Project Explorer view.
 * @see org.eclipse.jface.viewers.ViewerFilter
 * 
 * @author snjeza
 */
public class JBossServersProjectFilter extends ViewerFilter {

	/**
	 * Returns <code>false</code> if the given element is the .JBoss Servers project,
	 * and <code>true</code> otherwise.
	 *  
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IProject project = null;
		if (element instanceof IJavaProject) {
			project = ((IJavaProject) element).getProject();
		} else if (element instanceof IProject) {
			project = (IProject) element;
		}
		if (project != null) {
			String projectName = project.getName();
			if (projectName.equals(SourceLookupUtil.SEARCH_PROJECT_NAME)) {
				return false;
			}
		}
		return true;
	}

}
