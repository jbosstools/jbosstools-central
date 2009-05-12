package org.jboss.tools.project.examples.model;

import java.util.Set;

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
