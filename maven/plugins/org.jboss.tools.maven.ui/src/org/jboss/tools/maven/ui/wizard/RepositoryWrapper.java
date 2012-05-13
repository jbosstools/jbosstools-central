/*************************************************************************************
 * Copyright (c) 2010-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.wizard;

import org.apache.maven.settings.Repository;
import org.eclipse.core.runtime.Assert;

/**
 * 
 * @author snjeza
 *
 */
public class RepositoryWrapper implements Comparable<RepositoryWrapper>{
	public static final String SEPARATOR = "/"; //$NON-NLS-1$
	private Repository repository;
	private String profileId;
	private String url;

	public RepositoryWrapper(Repository repository, String profileId) {
		Assert.isNotNull(repository);
		Assert.isNotNull(profileId);
		this.repository = repository;
		this.profileId = profileId;
		url = repository.getUrl();
		if (url != null) {
			url = url.trim();
			if (!url.endsWith(SEPARATOR)) {
				url = url + SEPARATOR;
			}
		}
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}
	
	public boolean isJBossRepository() {
		return ConfigureMavenRepositoriesWizardPage.JBOSSTOOLS_MAVEN_PROFILE_ID.equals(profileId);
	}

	public String getDisplayName() {
		String name = repository.getName() == null ? "<no-name>" : repository.getName(); //$NON-NLS-1$
		return name + "-" + repository.getUrl(); //$NON-NLS-1$
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepositoryWrapper other = (RepositoryWrapper) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String name = repository.getName() == null ? "<no-name>" : repository.getName(); //$NON-NLS-1$
		return name + "-" + repository.getUrl(); //$NON-NLS-1$
	}

	public int compareTo(RepositoryWrapper o) {
		if (o == null) {
			return 1;
		}
		String s = getDisplayName();
		if (s == null) {
			return -1;
		}
		return s.compareTo(o.getDisplayName());
	}
}
