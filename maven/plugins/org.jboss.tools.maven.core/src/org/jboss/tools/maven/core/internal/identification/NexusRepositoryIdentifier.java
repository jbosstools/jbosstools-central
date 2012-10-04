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
package org.jboss.tools.maven.core.internal.identification;

import static org.jboss.tools.maven.core.identification.IdentificationUtil.getSHA1;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.repositories.NexusRepository;
import org.jboss.tools.maven.core.repositories.RemoteRepositoryManager;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;

public class NexusRepositoryIdentifier extends AbstractArtifactIdentifier {

	public NexusRepositoryIdentifier() {
		super("Nexus repository identifier");
	}
	
	public ArtifactKey identify(File file) throws CoreException {
		return getArtifactFromRemoteNexusRepository(file, null);
	}

	public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
		return getArtifactFromRemoteNexusRepository(file, monitor);
	}

	private ArtifactKey getArtifactFromRemoteNexusRepository(File file, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (monitor.isCanceled()) {
			return null;
		}
		String sha1;
		try {
			sha1 = getSHA1(file);
		} catch (Exception e) {
			return null;
		}
		 RemoteRepositoryManager repoManager = MavenCoreActivator.getDefault().getRepositoryManager();
		Set<NexusRepository> nexusRepositories = new LinkedHashSet<NexusRepository>(repoManager.getNexusRepositories());
		for (NexusRepository repository : nexusRepositories) {
			if (monitor.isCanceled()) {
				return null;
			}
			if (!repository.isEnabled()) {
				continue;
			}
			monitor.setTaskName("Querying "+repository.getUrl() + " for "+file.getName()); 

			try {
				ArtifactKey key = searchArtifactFromRemoteNexusRepository(repository.getSearchUrl(sha1));
				if (key != null) {
					return key;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static ArtifactKey searchArtifactFromRemoteNexusRepository(String searchUrl) {
		if (searchUrl == null) {
			return null;
		}
		HttpURLConnection connection = null;//TODO use eclipse connections to handle proxies
		try {
			JAXBContext context = JAXBContext.newInstance(SearchResponse.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			connection = (HttpURLConnection) new URL(searchUrl).openConnection();
			connection.connect();
			Object object = unmarshaller.unmarshal(connection.getInputStream());
			if (object instanceof SearchResponse) {
				return extractArtifactKey((SearchResponse)object);
			}
		} catch (UnknownHostException uhe) {
			System.err.println("NexusRepositoryIdentifier can't connect to remote repository :"+ uhe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}

	private static ArtifactKey extractArtifactKey(SearchResponse searchResponse) {
		for (NexusArtifact nexusArtifact : searchResponse.getData()) {
			String groupId = nexusArtifact.getGroupId();
			String artifactId = nexusArtifact.getArtifactId();
			String version = nexusArtifact.getVersion();
			String classifier = nexusArtifact.getClassifier();
			ArtifactKey artifact = new ArtifactKey(groupId, artifactId,
					version, classifier);
			return artifact;
		}
		return null;
	}
	
}
