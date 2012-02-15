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

package org.jboss.tools.project.examples.model;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author snjeza
 * 
 */
public interface IImportProjectExample {

	boolean importProject(ProjectExample projectDescription, File file,
			Map<String, Object> propertiesMap, IProgressMonitor monitor) throws Exception;
	
	void fix(ProjectExample project, IProgressMonitor monitor);
	
	void setName(String name);
	
	void setType(String type);
	
	String getName();
	
	String getType();
	
	IPath getLocation();
}
