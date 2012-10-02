/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.core;

import org.eclipse.core.runtime.IPath;

public abstract class ProjectDependency {

	public enum DependencyKind {
		Project, Archive, Unsupported;
	}
	
	private DependencyKind dependencyKind;

	public DependencyKind getDependencyKind() {
		if (dependencyKind == null) {
			dependencyKind = DependencyKind.Unsupported;
		}
		return dependencyKind;
	}

	public void setDependencyKind(DependencyKind dependencyKind) {
		this.dependencyKind = dependencyKind;
	}

	/**
	 * @return get full path of underlying archive or workspace path of underlying project 
	 */
	public abstract IPath getPath();
	
	
}
