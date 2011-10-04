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
package org.jboss.tools.project.examples.preferences;

import java.util.HashSet;
import java.util.Set;

import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectUtil;
import org.jboss.tools.project.examples.model.SiteCategory;

/**
 * 
 * @author snjeza
 *
 */
public class Sites {
	private SiteCategory[] siteCategories;
	private SiteCategory userSite;
	private Set<ProjectExampleSite> sites;

	public SiteCategory[] getSiteCategories() {
		if (siteCategories == null) {
			siteCategories = new SiteCategory[2];
			userSite = new SiteCategory(Messages.Sites_User_sites);
			Set<ProjectExampleSite> userSites = ProjectUtil.getUserSites();
			userSite.setSites(userSites);
			siteCategories[0]=userSite;
			SiteCategory pluginSite = new SiteCategory(Messages.Sites_Plugin_provided_sites);
			Set<ProjectExampleSite> pluginSites = ProjectUtil.getPluginSites();
			pluginSite.setSites(pluginSites);
			siteCategories[1]=pluginSite;
			sites = new HashSet<ProjectExampleSite>();
			sites.addAll(pluginSites);
			sites.addAll(userSites);
		}
		return siteCategories;
	}

	public void remove(IProjectExampleSite site) {
		userSite.getSites().remove(site);
		sites.remove(site);
	}

	public void add(ProjectExampleSite site) {
		userSite.getSites().add(site);
		sites.add(site);
	}

	public Set<ProjectExampleSite> getSites() {
		return sites;
	}

	public Set<ProjectExampleSite> getUserSites() {
		return userSite.getSites();
	}
}