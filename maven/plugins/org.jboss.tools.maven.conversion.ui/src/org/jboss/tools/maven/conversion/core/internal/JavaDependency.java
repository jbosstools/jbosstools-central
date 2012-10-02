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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.jboss.tools.maven.conversion.core.ProjectDependency;

public class JavaDependency extends ProjectDependency {

	private IClasspathEntry classpathEntry;

	public JavaDependency(IClasspathEntry cpe) {
		super();
		Assert.isNotNull(cpe, "classpentry parameter can not be null");
		classpathEntry = cpe;
		if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
			setDependencyKind(DependencyKind.Project);
		} else if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
			setDependencyKind(DependencyKind.Archive);
		} else {
			setDependencyKind(DependencyKind.Unsupported);
		}
	}

	public IClasspathEntry getClasspathEntry() {
		return classpathEntry;
	}

	@Override
	public IPath getPath() {
		return classpathEntry.getPath();
	}
	
}
