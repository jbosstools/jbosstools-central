/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.tests;

import java.io.File;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.core.project.conversion.IProjectConversionEnabler;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.jboss.tools.common.util.FileUtil;

public abstract class AbstractMavenConversionTest extends
		AbstractMavenProjectTestCase {

	protected String toString(File file) {
		return FileUtil.readFile(file);
	}

	protected String toString(IFile file) {
		return FileUtil.getContentFromEditorOrFile(file);
	}

	protected void assertHasError(IProject project, String errorMessage) {
		try {
			for (IMarker m : findErrorMarkers(project)) {
				String message = (String) m.getAttribute(IMarker.MESSAGE);
				if (errorMessage.equals(message)) {
					return;
				}
			}
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		fail("Error Message '" + errorMessage + "' was not found on "
				+ project.getName());
	}

	protected static String getMessage(IMarker marker) throws CoreException {
		return (String) marker.getAttribute(IMarker.MESSAGE);
	}

	protected Model initDefaultModel(String projectName, String packaging) {
		Model model = new Model();
		model.setModelVersion("4.0.0"); //$NON-NLS-1$
		model.setArtifactId(projectName);
		model.setGroupId(projectName);
		model.setVersion("0.0.1-SNAPSHOT");//$NON-NLS-1$
		model.setPackaging(packaging);
		return model;
	}

	protected IMavenProjectFacade convert(IProject project) throws CoreException,
			InterruptedException {
		String packaging = "jar";
		IProjectConversionEnabler enabler = MavenPlugin
				.getProjectConversionManager().getConversionEnablerForProject(
						project);
		if (enabler != null) {
			String[] types = enabler.getPackagingTypes(project);
			if (types != null && types.length > 0) {
			  packaging = types[0];
			}
		}
		return convert(project, packaging);
	}

	/**
	 * Converts an Eclipse project to a Maven project with a given packaging
	 * (generates a pom.xm and enables the Maven nature)
	 */
	protected IMavenProjectFacade  convert(IProject project, String packaging)
			throws CoreException, InterruptedException {
		Model model = initDefaultModel(project.getName(), packaging);
		return convert(project, model);
	}

	protected IMavenProjectFacade convert(IProject project, Model model) throws CoreException,
			InterruptedException {
		MavenPlugin.getProjectConversionManager().convert(project, model,
				monitor);
		createPomXml(project, model);
		ResolverConfiguration configuration = new ResolverConfiguration();
		MavenPlugin.getProjectConfigurationManager().enableMavenNature(project,
				configuration, monitor);
		waitForJobsToComplete(monitor);
		
		return MavenPlugin.getMavenProjectRegistry().create(project, monitor);
		
	}
	

	  /**
	   * Serializes the maven model to &lt;project&gt;/pom.xml
	   */
	  protected void createPomXml(IProject project, Model model) throws CoreException {
	    MavenModelManager mavenModelManager = MavenPlugin.getMavenModelManager();
	    mavenModelManager.createMavenModel(project.getFile(IMavenConstants.POM_FILE_NAME), model);  
	  }


}
