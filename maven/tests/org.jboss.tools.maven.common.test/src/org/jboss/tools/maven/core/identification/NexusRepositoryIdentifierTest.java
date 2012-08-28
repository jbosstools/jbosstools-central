package org.jboss.tools.maven.core.identification;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.internal.identification.NexusRepositoryIdentifier;
import org.jboss.tools.maven.core.repositories.NexusRepository;
import org.jboss.tools.maven.core.repositories.RemoteRepositoryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NexusRepositoryIdentifierTest extends AbstractIdentificationTest {

	Map<NexusRepository, Boolean> initialRepoState = new HashMap<NexusRepository, Boolean>();
	
	@Before
	public void setupRepositories() {
		RemoteRepositoryManager repoManager = MavenCoreActivator.getDefault().getRepositoryManager();
		for (NexusRepository repo : repoManager.getDefaultRepositories()) {
			initialRepoState.put(repo, repo.isEnabled());
			if (repo.getName().toLowerCase().contains("jboss")) {
				repo.setEnabled(true);
			}
			else {
				repo.setEnabled(false);
			}
		}
	}
	
	@After
	public void restoreRepos() {
		for (Map.Entry<NexusRepository, Boolean> entry : initialRepoState.entrySet()) {
			entry.getKey().setEnabled(entry.getValue());
		}
	}
	
	@Test
	public void testIdentify() throws Exception {
		NexusRepositoryIdentifier identifier = new NexusRepositoryIdentifier();
		ArtifactKey key;
		key= identifier.identify(junit);
		assertEquals("junit", key.getArtifactId());
		assertEquals("4.10", key.getVersion());
		
		key= identifier.identify(jansi);
		assertEquals("jansi", key.getArtifactId());
		assertEquals("1.6", key.getVersion());
		
		key = identifier.identify(arquillian);
		assertEquals("arquillian-core-spi", key.getArtifactId());
		assertEquals("1.0.1.Final", key.getVersion());
	}

	
}
