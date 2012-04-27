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

/**
 * 
 * @author snjeza
 *
 */
public class NexusRepository {
	private String name;
	private String url;
	private boolean enabled;

	
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
}
