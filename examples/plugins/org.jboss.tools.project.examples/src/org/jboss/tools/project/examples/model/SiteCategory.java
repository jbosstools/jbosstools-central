/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.net.URL;
import java.util.Set;

/**
 * 
 * @author snjeza
 *
 */
public class SiteCategory implements IProjectExampleSite {

	private String name;
	private Set<IProjectExampleSite> sites;
	
	public SiteCategory(String name) {
		this.name = name;
	}

	public String getName() {		
		return name;
	}

	public Set<IProjectExampleSite> getSites() {
		return sites;
	}

	public void setSites(Set<IProjectExampleSite> sites) {
		this.sites = sites;
	}

	public void setEditable(boolean editable) {
	}

	public boolean isEditable() {
		return false;
	}

	public void setExperimental(boolean experimental) {
	}

	public boolean isExperimental() {
		return false;
	}

	public void setName(String name) {
	}

	public void setUrl(URL url) {
	}

	public URL getUrl() {
		return null;
	}
}
