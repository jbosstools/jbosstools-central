/*************************************************************************************
 * Copyright (c) 2011 Red Hat, Inc. and others.
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
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectUtil;
import org.junit.Test;

/**
 * 
 * @author snjeza
 *
 */
public class SiteTest {

	@Test
	public void sitesPresent() {
		Set<ProjectExampleSite> sites = new HashSet<ProjectExampleSite>();
		sites.addAll(ProjectUtil.getPluginSites());
		sites.addAll(ProjectUtil.getUserSites());
		assertTrue(sites.size() > 0);
	}
	
	@Test
	public void testInvalidSites() {
		ProjectUtil.getProjects(new NullProgressMonitor());
		HashSet<ProjectExampleSite> invalidSites = ProjectUtil.getInvalidSites();
		assertTrue(invalidSites.size() <= 0);
	}
	
	
}
