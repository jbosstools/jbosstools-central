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
package org.jboss.tools.project.examples.dialog;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;

/**
* @author snjeza
* 
*/
public class MarkerContentProvider implements IStructuredContentProvider {

	private List<Project> projects;

	public MarkerContentProvider(List<Project> projects) {
		this.projects = projects;
	}

	public Object[] getElements(Object inputElement) {
		List<IMarker> markers = ProjectExamplesActivator.getMarkers(projects);
		return markers.toArray();
	}

	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

}
