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
package org.jboss.tools.maven.conversion.core.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.jboss.tools.maven.conversion.core.ProjectDependency;

public class ComponentDependency extends ProjectDependency {

	private IVirtualComponent component;

	public ComponentDependency(IVirtualComponent comp) {
		super();
		component = comp;
		if (comp.isBinary()) {
			setDependencyKind(DependencyKind.Archive);
		} else {
			setDependencyKind(DependencyKind.Project);
		}
	}

	public IVirtualComponent getComponent() {
		return component;
	}

	@Override
	public IPath getPath() {
		if (component.isBinary()) {
			return (IPath) component.getAdapter(IPath.class);
		}
		return component.getProject().getFullPath();
	}
}
