/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.libprov;

import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderInstallOperationConfig;
import org.eclipse.m2e.model.edit.pom.Model;

/**
 * @author snjeza
 * 
 */
public class MavenLibraryProviderInstallOperationConfig extends
		LibraryProviderInstallOperationConfig {

	private Model model;

	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}
	
}
