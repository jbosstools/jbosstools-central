/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
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
import java.net.MalformedURLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.maven.ui.Activator;
import org.jboss.tools.maven.ui.wizard.RepositoryWrapper;


abstract class AbstractRepositoryIdentifier implements ILocalRepositoryIdentifier {

	public RepositoryWrapper identifyRepository(File rootDirectory, IProgressMonitor monitor) {
		if (matches(rootDirectory)) {
			return getRepository(rootDirectory);
		}
		return null;
	}
	
	protected abstract boolean matches(File rootDirectory);
	
	protected abstract RepositoryWrapper getRepository(File rootDirectory);

	protected String getUrl(File directory) {
		String url;
		try {
			url = directory.toURI().toURL().toString();
		} catch (MalformedURLException e1) {
			Activator.log(e1);
			return null;
		}
		url = url.trim();
		if (!url.endsWith(RepositoryWrapper.SEPARATOR)) {
			url = url + RepositoryWrapper.SEPARATOR;
		}
		return url;
	}
	
}
