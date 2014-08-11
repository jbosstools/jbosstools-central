/*************************************************************************************
 * Copyright (c) 2009-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.markers;

import org.eclipse.m2e.core.embedder.ArtifactKey;

public class ConfigureRedHatRepositoryMarkerResolution extends ConfigureMavenRepositoriesMarkerResolution {

	public ConfigureRedHatRepositoryMarkerResolution(ArtifactKey artifactKey) {
		super(artifactKey);
	}

	public String getLabel() {
		return "Configure the Red Hat Maven repository"; //$NON-NLS-1$
	}

	@Override
	protected String getRepositoryProfileId() {
		return "redhat-techpreview-all-repository"; //$NON-NLS-1$ 
	}
	
}
