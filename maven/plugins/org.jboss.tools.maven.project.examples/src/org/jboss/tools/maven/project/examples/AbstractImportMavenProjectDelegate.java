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
package org.jboss.tools.maven.project.examples;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.model.ProjectExample;

abstract class AbstractImportMavenProjectDelegate {

	private IPath location; 

	protected abstract boolean importProject(ProjectExample projectDescription, File file,
			Map<String, Object> propertiesMap, IProgressMonitor monitor) throws Exception;
	
	protected void setLocation(IPath location) {
		this.location = location;
	}
	
	protected IPath getLocation() {
		return location;
	}
	
}
