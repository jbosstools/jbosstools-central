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
package org.jboss.tools.maven.core.internal.identification;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.internal.index.IIndex;
import org.eclipse.m2e.core.internal.index.IndexManager;
import org.eclipse.m2e.core.internal.index.IndexedArtifactFile;
import org.eclipse.m2e.core.internal.index.nexus.NexusIndex;
import org.eclipse.m2e.core.internal.index.nexus.NexusIndexManager;
import org.eclipse.m2e.core.repository.IRepository;
import org.eclipse.m2e.core.repository.IRepositoryRegistry;

@SuppressWarnings("restriction")
public class NexusIndexIdentifier extends AbstractArtifactIdentifier {

	private List<IRepository> globalRepositories;
	
	public NexusIndexIdentifier() {
		super("Nexus Index identifier");
		globalRepositories = initGlobalRepositories();
	}
	
	public ArtifactKey identify(File file) throws CoreException {
		return identify(file, null);
	}
		
	public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IndexManager indexManager = MavenPlugin.getIndexManager();
		IIndex index = indexManager.getAllIndexes();
		IndexedArtifactFile info = null;
		try {
			if (monitor.isCanceled()) {
				return null;
			}
			monitor.setTaskName("Checking global m2e Nexus index for "+file.getName()); 
			info = index.identify(file);
		} catch (Throwable e) {
			// ignore
		}
		ArtifactKey artifact = null;
		if (info != null) {
			artifact = info.getArtifactKey();
			if (artifact != null) {
				return artifact;
			}
		}
		if (indexManager instanceof NexusIndexManager) {
			NexusIndexManager nexusIndexManager = (NexusIndexManager) indexManager;
			for (IRepository repository : globalRepositories) {
				NexusIndex nexusIndex = nexusIndexManager.getIndex(repository);
				if (nexusIndex != null) {
					try {
						if (monitor.isCanceled()) {
							return null;
						}
						monitor.setTaskName("Checking Nexus index of '"+ repository.getId() + "' repository for "+file.getName()); 
						info = nexusIndex.identify(file);
					} catch (Throwable t) {
						// ignore
					}
					if (info != null) {
						artifact = info.getArtifactKey();
						if (artifact != null) {
							return artifact;
						}
					}
				}
			}
		}
		return artifact;
	}

	private List<IRepository> initGlobalRepositories() {
		IRepositoryRegistry repositoryRegistry = MavenPlugin.getRepositoryRegistry();
		List<IRepository> repositories = repositoryRegistry.
				getRepositories(IRepositoryRegistry.SCOPE_SETTINGS);
		return repositories == null? Collections.<IRepository>emptyList():repositories;
	}

}
