/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.util.Set;

/**
 * 
 * @author snjeza
 *
 */
public class SiteCategory implements IProjectExampleSite {

	private String name;
	private Set<ProjectExampleSite> sites;
	
	public SiteCategory(String name) {
		this.name = name;
	}

	public String getName() {		
		return name;
	}

	public Set<ProjectExampleSite> getSites() {
		return sites;
	}

	public void setSites(Set<ProjectExampleSite> sites) {
		this.sites = sites;
	}
}
