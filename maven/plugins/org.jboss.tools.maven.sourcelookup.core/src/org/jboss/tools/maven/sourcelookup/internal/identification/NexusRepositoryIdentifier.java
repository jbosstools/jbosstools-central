package org.jboss.tools.maven.sourcelookup.internal.identification;

import static org.jboss.tools.maven.sourcelookup.identification.IdentificationUtil.getSHA1;
import static org.jboss.tools.maven.sourcelookup.identification.IdentificationUtil.getSourcesClassifier;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.sourcelookup.NexusRepository;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.identification.ArtifactIdentifier;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;

public class NexusRepositoryIdentifier implements ArtifactIdentifier {

	private static final String PATH_SEPARATOR = "/";

	@Override
	public ArtifactKey identify(File file) throws CoreException {
		return getArtifactFromRemoteNexusRepository(file);
	}

	private static ArtifactKey getArtifactFromRemoteNexusRepository(File file) {
		String sha1;
		try {
			sha1 = getSHA1(file);
		} catch (Exception e) {
			return null;
		}
		Set<NexusRepository> nexusRepositories = SourceLookupActivator
				.getNexusRepositories();
		for (NexusRepository repository : nexusRepositories) {
			if (!repository.isEnabled()) {
				continue;
			}
			ArtifactKey key = getArtifactFromRemoteNexusRepository(sha1,	repository);
			if (key != null) {
				ArtifactKey sourcesArtifact = new ArtifactKey(
						key.getGroupId(), key.getArtifactId(),
						key.getVersion(),
						getSourcesClassifier(key.getClassifier()));
				ArtifactKey resolvedKey = getSourcesArtifactFromJBossNexusRepository(sourcesArtifact, repository);
				if (resolvedKey != null) {
					return key;
				}
			}
		}
		return null;
	}

	private static ArtifactKey getArtifactFromRemoteNexusRepository(String sha1,
			NexusRepository nexusRepository) {
		if (sha1 == null || nexusRepository == null	|| nexusRepository.getUrl() == null) {
			return null;
		}
		HttpURLConnection connection = null;
		try {
			String base = nexusRepository.getUrl();
			if (!base.endsWith(PATH_SEPARATOR)) {
				base = base + PATH_SEPARATOR;
			}
			// String url =
			// "https://repository.jboss.org/nexus/service/local/data_index?sha1=";
			String url = base + "service/local/data_index?sha1=";
			url = url + URLEncoder.encode(sha1, "UTF-8");
			JAXBContext context = JAXBContext.newInstance(SearchResponse.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.connect();
			Object object = unmarshaller.unmarshal(connection.getInputStream());
			if (object instanceof SearchResponse) {
				SearchResponse searchResponse = (SearchResponse) object;
				for (NexusArtifact nexusArtifact : searchResponse.getData()) {
					String groupId = nexusArtifact.getGroupId();
					String artifactId = nexusArtifact.getArtifactId();
					String version = nexusArtifact.getVersion();
					String classifier = nexusArtifact.getClassifier();
					ArtifactKey artifact = new ArtifactKey(groupId, artifactId,
							version, classifier);
					return artifact;
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}

	private static ArtifactKey getSourcesArtifactFromJBossNexusRepository(ArtifactKey key,
			NexusRepository nexusRepository) {
		if (key == null || nexusRepository == null
				|| nexusRepository.getUrl() == null) {
			return null;
		}
		HttpURLConnection connection = null;
		try {
			String base = nexusRepository.getUrl();
			if (!base.endsWith(PATH_SEPARATOR)) {
				base = base + PATH_SEPARATOR;
			}
			// String url =
			// "https://repository.jboss.org/nexus/service/local/data_index?g=groupId&a=artifactId&v=version&c=classifier";
			String url = base + "service/local/data_index?";
			url= url + "g=" + URLEncoder.encode(key.getGroupId(), "UTF-8") + "&";
			url= url + "a=" + URLEncoder.encode(key.getArtifactId(), "UTF-8") + "&";
			url= url + "v=" + URLEncoder.encode(key.getVersion(), "UTF-8") + "&";
			url= url + "c=" + URLEncoder.encode(key.getClassifier(), "UTF-8");
			JAXBContext context = JAXBContext.newInstance(SearchResponse.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.connect();
			Object object = unmarshaller.unmarshal(connection.getInputStream());
			if (object instanceof SearchResponse) {
				SearchResponse searchResponse = (SearchResponse) object;
				for (NexusArtifact nexusArtifact : searchResponse.getData()) {
					String groupId = nexusArtifact.getGroupId();
					String artifactId = nexusArtifact.getArtifactId();
					String version = nexusArtifact.getVersion();
					String classifier = nexusArtifact.getClassifier();
					ArtifactKey artifact = new ArtifactKey(groupId, artifactId,
							version, classifier);
					return artifact;
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
	
}
