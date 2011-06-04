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

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.project.examples.ProjectExamplesActivator;

/**
 * @author snjeza
 * 
 */

public abstract class AbstractImportProjectExample implements
		IImportProjectExample {

	private String name;
	private String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void fix(Project project, IProgressMonitor monitor) {
		ProjectExamplesActivator.fix(project, monitor);
	}

}
