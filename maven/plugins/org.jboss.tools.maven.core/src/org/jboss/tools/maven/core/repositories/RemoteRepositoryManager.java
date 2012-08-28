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
package org.jboss.tools.maven.core.repositories;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.jboss.tools.maven.core.MavenCoreActivator;

public class RemoteRepositoryManager {

	private static final String NEXUS_REPOSITORIES = "nexusRepositories";
	private static final String NEXUS_REPOSITORY = "nexusRepository";
	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String ENABLED = "enabled";
	
	private Set<NexusRepository> nexusRepositories;
	
	public Set<NexusRepository> getNexusRepositories() {
		if (nexusRepositories == null) {
			try {
				nexusRepositories = loadNexusRepositoriesFromPreferences();
			} catch (WorkbenchException e) {
				MavenCoreActivator.log(e);
			}
			if (nexusRepositories == null) {
				getDefaultRepositories();
			}
		}
		return nexusRepositories;
	}

	public Set<NexusRepository> loadNexusRepositoriesFromPreferences() throws WorkbenchException {
		String repositories = getPreferences().get(NEXUS_REPOSITORIES, null);
		if (repositories == null || repositories.isEmpty()) {
			return null;
		}
		Reader reader = new StringReader(repositories);
		XMLMemento memento = XMLMemento.createReadRoot(reader);
		IMemento[] nodes = memento.getChildren(NEXUS_REPOSITORY);
		for (IMemento node:nodes) {
			if (nexusRepositories == null) {
				nexusRepositories = new LinkedHashSet<NexusRepository>();
			}
			String name = node.getString(NAME);
			String url = node.getString(URL);
			boolean enabled = node.getBoolean(ENABLED);
			NexusRepository repository = new NexusRepository(name, url, enabled);
			nexusRepositories.add(repository);
		}
		return nexusRepositories;
	}

	public Set<NexusRepository> getDefaultRepositories() {
		nexusRepositories = new LinkedHashSet<NexusRepository>();
		addRepository("JBoss Nexus Repository", "https://repository.jboss.org/nexus", true);
		addRepository("Sonatype Nexus Repository", "http://repository.sonatype.org", false);
		addRepository("Apache Nexus Repository", "https://repository.apache.org", false);
		addRepository("Sonatype OSS Repository", "http://oss.sonatype.org", false);
		addRepository("Codehaus Repository", "https://nexus.codehaus.org", false);
		addRepository("Java.net Repository", "https://maven.java.net", false);
		return nexusRepositories;
	}

	private void addRepository(String name, String url, boolean enabled) {
		NexusRepository repository = new NexusRepository(name, url, enabled);
		nexusRepositories.add(repository);
	}

	public void setNexusRepositories(Set<NexusRepository> nexusRepositories) {
		this.nexusRepositories = nexusRepositories;
	}

	public void saveNexusRepositories() {
		if (nexusRepositories == null || nexusRepositories.size() == 0) {
			return;
		}
		XMLMemento memento = XMLMemento.createWriteRoot(NEXUS_REPOSITORIES);
		Writer writer = null;
		try {
			for (NexusRepository repository : nexusRepositories) {
				IMemento repositoryNode = memento.createChild(NEXUS_REPOSITORY);
				repositoryNode.putString(NAME, repository.getName());
				repositoryNode.putString(URL, repository.getUrl());
				repositoryNode.putBoolean(ENABLED, repository.isEnabled());
			}
			writer = new StringWriter();
			memento.save(writer);
			writer.flush();
			String repositories = writer.toString();
			getPreferences().put(NEXUS_REPOSITORIES, repositories);
			getPreferences().flush();
		} catch (Exception e) {
			MavenCoreActivator.log(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public static IEclipsePreferences getPreferences() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(MavenCoreActivator.PLUGIN_ID);
		return prefs;
	}
	
}
