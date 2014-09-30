/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.repositories;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.maven.ui.wizard.RepositoryWrapper;

/**
 * Identifies local Maven repositories
 * 
 * @author Fred Bricon
 */
public interface ILocalRepositoryIdentifier {

	/**
	 * Identifies a local folder as a Maven repository
	 * @param directory
	 * @param monitor
	 * @return a {@link RepositoryWrapper} if the directory is identified as a Maven repository, or else, <code>null</code>.
	 */
	RepositoryWrapper identifyRepository(File directory, IProgressMonitor monitor);
}
