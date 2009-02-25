/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;
import org.jboss.tools.project.examples.model.ProjectModelElement;

/**
 * @author snjeza
 * 
 */
public class ProjectExamplesPatternFilter extends PatternFilter {

	public ProjectExamplesPatternFilter() {
		super();
	}
	
	public boolean isElementSelectable(Object element) {
		return element instanceof ProjectModelElement;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (! (element instanceof ProjectModelElement) ) {
			return false;
		}
		ProjectModelElement model = (ProjectModelElement) element;
		
		if (wordMatches(model.getName()) || wordMatches(model.getDescription()) || wordMatches(model.getShortDescription())) {
			return true;
		}
		return false;
	}

}
