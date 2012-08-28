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
package org.jboss.tools.maven.core.internal.identification;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.core.identification.ArtifactIdentifier;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;

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

	public FileIdentificationManager(Collection<ArtifactIdentifier> identifiers) {
		for (ArtifactIdentifier identifier : identifiers) {
			addArtifactIdentifier(identifier);
		}
	}
	
	protected void initArtifactIdentifiers() {
		//TODO read from extension points?
		addArtifactIdentifier(new MavenPropertiesIdentifier());
		addArtifactIdentifier(new NexusIndexIdentifier());
		addArtifactIdentifier(new NexusRepositoryIdentifier());
	}

	public synchronized void addArtifactIdentifier(ArtifactIdentifier identifier) {
		Assert.isNotNull(identifier, "Artifact identifier can not be null");
		if (artifactIdentifiers == null) {
			artifactIdentifiers = new ArrayList<ArtifactIdentifier>();
		}
		artifactIdentifiers.add(identifier);
		//System.err.println("Added "+ identifier);
	}

	public synchronized void removeArtifactIdentifier(ArtifactIdentifier identifier) {
		if (identifier != null) {
			getArtifactIdentifiers().remove(identifier);
		}
	}

	protected List<ArtifactIdentifier> getArtifactIdentifiers() {
		if (artifactIdentifiers == null) {
			initArtifactIdentifiers();
		}
		return artifactIdentifiers;
	}

	public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
		if (file == null) {
			return null;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		ArtifactKey artifactKey = null;
		//long start = System.currentTimeMillis();
		for (ArtifactIdentifier identifier : artifactIdentifiers) {
			if (monitor.isCanceled()) {
				return null;
			}
			artifactKey = identifier.identify(file);
			if (artifactKey != null) {
				//long stop = System.currentTimeMillis();
				//System.err.println(file.getName() + " identified as " + artifactKey + " in " + (stop-start) + " ms");
				break;
			}
		}
		//if (artifactKey == null)
			//System.err.println("Could not identify "+file);
		return artifactKey;
	}
	
	
}
