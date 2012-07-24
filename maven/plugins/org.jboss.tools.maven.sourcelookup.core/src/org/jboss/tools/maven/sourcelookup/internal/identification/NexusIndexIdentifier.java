package org.jboss.tools.maven.sourcelookup.internal.identification;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.internal.index.IIndex;
import org.eclipse.m2e.core.internal.index.IndexManager;
import org.eclipse.m2e.core.internal.index.IndexedArtifactFile;
import org.eclipse.m2e.core.internal.index.nexus.NexusIndex;
import org.eclipse.m2e.core.internal.index.nexus.NexusIndexManager;
import org.eclipse.m2e.core.repository.IRepository;
import org.eclipse.m2e.core.repository.IRepositoryRegistry;
import org.jboss.tools.maven.sourcelookup.identification.ArtifactIdentifier;

@SuppressWarnings("restriction")
public class NexusIndexIdentifier implements ArtifactIdentifier {

	private List<IRepository> globalRepositories;

	public NexusIndexIdentifier() {
		globalRepositories = initGlobalRepositories();
	}
	
	@Override
	public ArtifactKey identify(File file) throws CoreException {
		IndexManager indexManager = MavenPlugin.getIndexManager();
		IIndex index = indexManager.getAllIndexes();
		IndexedArtifactFile info = null;
		try {
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
