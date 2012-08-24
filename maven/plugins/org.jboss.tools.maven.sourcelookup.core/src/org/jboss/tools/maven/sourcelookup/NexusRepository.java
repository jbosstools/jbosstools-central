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
package org.jboss.tools.maven.sourcelookup;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.Assert;


/**
 * 
 * @author snjeza
 *
 */
public class NexusRepository {
	private String name;
	private String url;
	private boolean enabled;

	private static final String PATH_SEPARATOR = "/";

	public NexusRepository() {
		super();
	}

	public NexusRepository(String name, String url, boolean enabled) {
		super();
		this.name = name;
		this.url = url;
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean checked) {
		this.enabled = checked;
	}

	@Override
	public String toString() {
		return "NexusRepository [name=" + name + ", url=" + url + ", enabled="
				+ enabled + "]";
	}
	
	public String getSearchUrl(String sha1) throws UnsupportedEncodingException {
		// "https://repository.jboss.org/nexus/service/local/data_index?sha1=";
		Assert.isNotNull(sha1);
		StringBuilder searchUrl = getBaseUrl()
		                               .append("service/local/data_index?sha1=")
		                               .append(URLEncoder.encode(sha1, "UTF-8"));
		return searchUrl.toString();
	}
	
	public String getSearchUrl(String groupId, String artifactId, String version, String classifier) throws UnsupportedEncodingException {
		Assert.isNotNull(artifactId);
		StringBuilder searchUrl = getBaseUrl().append("service/local/data_index?");
		searchUrl.append("a=").append(URLEncoder.encode(artifactId, "UTF-8")).append("&");
		if (groupId != null) {
			searchUrl.append("g=").append(URLEncoder.encode(groupId, "UTF-8")).append("&");
		}
		if (version != null) {
			searchUrl.append("v=").append(URLEncoder.encode(version, "UTF-8")).append("&");
		}
		if (classifier != null) {
			searchUrl.append("c=").append(URLEncoder.encode(classifier, "UTF-8"));
		}
		return searchUrl.toString();
	}
	
	private StringBuilder getBaseUrl() {
		StringBuilder sb = new StringBuilder();
		String base = getUrl();
		sb.append(base);
		if (!base.endsWith(PATH_SEPARATOR)) {
			sb.append(PATH_SEPARATOR);
		}
		return sb;
	}
	
	
	
	
	
}
