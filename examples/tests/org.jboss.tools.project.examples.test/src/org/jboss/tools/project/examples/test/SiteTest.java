/*************************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.test;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.junit.Test;

/**
 * 
 * @author snjeza
 *
 */
public class SiteTest {

	@Test
	public void sitesPresent() {
		Set<IProjectExampleSite> sites = new HashSet<IProjectExampleSite>();
		sites.addAll(ProjectExampleUtil.getPluginSites());
		sites.addAll(ProjectExampleUtil.getUserSites());
		sites.addAll(ProjectExampleUtil.getRuntimeSites());
		assertTrue(sites.size() > 0);
	}
	
	@Test
	public void testInvalidSites() {
		ProjectExampleUtil.getProjects(new NullProgressMonitor());
		HashSet<IProjectExampleSite> invalidSites = ProjectExampleUtil.getInvalidSites();
		assertTrue(invalidSites.size() <= 0);
	}
	
	
}
