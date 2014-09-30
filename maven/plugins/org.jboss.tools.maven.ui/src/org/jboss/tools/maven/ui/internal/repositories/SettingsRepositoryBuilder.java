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
package org.jboss.tools.maven.ui.internal.repositories;

import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;

/**
 * Builder for Maven settings {@link Repository}.
 * 
 * @author Fred Bricon
 */
public class SettingsRepositoryBuilder {

	private static final String POLICY_NEVER = "never"; //$NON-NLS-1$

	private static final String POLICY_DAILY = "daily"; //$NON-NLS-1$

	private static final String LAYOUT_DEFAULT = "default"; //$NON-NLS-1$

	private Repository repository;

	public SettingsRepositoryBuilder() {
		repository = getDefaultRepository();
	}
	
	public Repository get() {
		return repository;
	}
	
    public static Repository getDefaultRepository() {
		Repository repository = new Repository();
		repository.setLayout(LAYOUT_DEFAULT);
		RepositoryPolicy releases = new RepositoryPolicy();
		releases.setEnabled(true);
		releases.setUpdatePolicy(POLICY_NEVER);
		repository.setReleases(releases);
		RepositoryPolicy snapshots = new RepositoryPolicy();
		snapshots.setEnabled(false);
		snapshots.setUpdatePolicy(POLICY_DAILY);
		repository.setSnapshots(snapshots);
		return repository;
	}
    

	public SettingsRepositoryBuilder setId(String id) {
		repository.setId(id);
		return this;
	}

	public SettingsRepositoryBuilder setName(String name) {
		repository.setName(name);
		return this;
	}
	
	public SettingsRepositoryBuilder setUrl(String url) {
		repository.setUrl(url);
		return this;
	}	
    

}
