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
package org.jboss.tools.maven.profiles.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.jboss.tools.common.util.FileUtil;
import org.jboss.tools.maven.profiles.core.MavenProfilesCoreActivator;
import org.jboss.tools.maven.profiles.core.profiles.IProfileManager;

public abstract class AbstractMavenProfileTest extends AbstractMavenProjectTestCase {

	protected IProfileManager profileManager;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		profileManager = MavenProfilesCoreActivator.getDefault().getProfileManager();
	}
	
	protected IMavenProjectFacade getFacade(IProject project) {
		return MavenPlugin.getMavenProjectRegistry().create(project, monitor);
	}
	
	@Override
	protected void tearDown() throws Exception {
		profileManager = null;
		super.tearDown();
	}
	
	protected String toString(File file) {
		return FileUtil.readFile(file);
	}

	protected String toString(IFile file) {
		return FileUtil.getContentFromEditorOrFile(file);
	}
	
	/**
	 * Replace the project pom.xml with a new one, triggers new build
	 * 
	 * @param project
	 * @param newPomName
	 * @throws Exception
	 */
	protected void updateProject(IProject project, String newPomName) throws Exception {
		updateProject(project, newPomName, -1);
	}

	/**
	 * Replace the project pom.xml with a new one, triggers new build, wait for
	 * waitTime milliseconds.
	 * 
	 * @param project
	 * @param newPomName
	 * @param waitTime
	 * @throws Exception
	 */
	protected void updateProject(IProject project, String newPomName, int waitTime) throws Exception {

		if (newPomName != null) {
			copyContent(project, newPomName, "pom.xml");
		}
		IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
		ResolverConfiguration configuration = new ResolverConfiguration();
		configurationManager.enableMavenNature(project, configuration, monitor);
		configurationManager.updateProjectConfiguration(project, monitor);
		waitForJobsToComplete(monitor);

		project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
		if (waitTime > 0) {
			Thread.sleep(waitTime);
		}
		waitForJobsToComplete(monitor);
	}

	protected void updateProject(IProject project) throws Exception {
		updateProject(project, null, -1);
	}
	
}
