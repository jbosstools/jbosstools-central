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
package org.jboss.tools.maven.sourcelookup.internal.identification;

import org.jboss.tools.maven.sourcelookup.identification.ArtifactIdentifier;

abstract class AbstractArtifactIdentifier implements ArtifactIdentifier {

	private String name;
	
	AbstractArtifactIdentifier() {
	}
	
	AbstractArtifactIdentifier(String name) {
		this.name = name;
	}
	
	public String getName() {
		if (name == null) {
			name = getClass().getSimpleName();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return (name == null)? super.toString():name;
	}
}
