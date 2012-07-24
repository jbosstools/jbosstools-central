/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.internal.identification;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.sourcelookup.identification.ArtifactIdentifier;
import org.jboss.tools.maven.sourcelookup.identification.IFileIdentificationManager;

/**
 * 
 * @author Fred Bricon
 *
 */
public class FileIdentificationManager implements IFileIdentificationManager {

	private List<ArtifactIdentifier> artifactIdentifiers;
	
	public FileIdentificationManager() {
		initArtifactIdentifiers();
	}
	
	private void initArtifactIdentifiers() {
		//TODO read from extension points?
		artifactIdentifiers = new ArrayList<ArtifactIdentifier>(3);
		artifactIdentifiers.add(new MavenPropertiesIdentifier());
		artifactIdentifiers.add(new NexusIndexIdentifier());
		artifactIdentifiers.add(new NexusRepositoryIdentifier());
	}

	@Override
	public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
		ArtifactKey artifactKey = null;
		long start = System.currentTimeMillis();
		for (ArtifactIdentifier identifier : artifactIdentifiers) {
			if (monitor.isCanceled()) {
				return null;
			}
			artifactKey = identifier.identify(file);
			if (artifactKey != null) {
				long stop = System.currentTimeMillis();
				System.err.println(file.getName() + " identified as " + artifactKey + " in " + (stop-start) + " ms");
				break;
			}
		}
		return artifactKey;
	}
	
}
