/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jdt.internal.markers;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.jboss.tools.maven.jdt.MavenJdtActivator;

@SuppressWarnings({"nls", "restriction"})
public class MissingEndorsedLibMarkerResolutionGenerator implements	IMarkerResolutionGenerator2 {

	private static Set<String> INTERESTING_PHASES;
	
	static {
		INTERESTING_PHASES = new LinkedHashSet<String>(
			 Arrays.asList("validate",
			      "initialize",
			      "generate-sources",
			      "process-sources",
			      "generate-resources",
			      "process-resources",
			      "compile",
			      "process-classes",
			      "generate-test-sources",
			      "process-test-sources",
			      "generate-test-resources",
			      "process-test-resources",
			      "test-compile",
			      "process-test-classes"
			 )
		);
		
	}
	
	public IMarkerResolution[] getResolutions(IMarker marker) {
		String phase;
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			phase = getPhaseToExecute(marker, monitor);
			if (phase != null) {
				IMavenProjectFacade facade = getMavenProjectFacade(marker, monitor);
				return new IMarkerResolution[]{new ExecuteDependencyCopyMarkerResolution(facade, phase)};
			}
		} catch (CoreException e) {
			MavenJdtActivator.log(e);
		}
		return new IMarkerResolution[0];
	}

	public boolean hasResolutions(IMarker marker) {
		try {
			return null != getPhaseToExecute(marker, new NullProgressMonitor());
		} catch (CoreException e) {
			MavenJdtActivator.log(e);
		}
		return false;
	}

	private String getPhaseToExecute(IMavenProjectFacade facade, String absolutePath, IProgressMonitor monitor) throws CoreException {
		Plugin p = facade.getMavenProject(monitor).getPlugin("org.apache.maven.plugins:maven-dependency-plugin");
		if (p != null) {
			for (PluginExecution pe : p.getExecutions()) {
				String phase = pe.getPhase();
				if (phase == null) {
					phase = "process-sources";
				}
				if (INTERESTING_PHASES.contains(phase) && pe.getGoals().contains("copy")) {
					return phase;
				}
			}
		}
		return null;
	}

	
	private String getPhaseToExecute(IMarker marker, IProgressMonitor monitor) throws CoreException {
		String phase = null;
		IMavenProjectFacade facade = getMavenProjectFacade(marker, monitor);
		if (facade != null) {
			String path = (String) marker.getAttribute("outputDirectory");
			phase = getPhaseToExecute(facade, path, monitor);
		}
		return phase;
	}

	private IMavenProjectFacade getMavenProjectFacade(IMarker marker, IProgressMonitor monitor) {
		IProject project = (marker.getResource() == null)?null:marker.getResource().getProject();
		try {
			if (project != null && project.isAccessible() && project.hasNature(IMavenConstants.NATURE_ID)) {
				IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().create(project, monitor);
				return facade;
			}
		} catch (CoreException e) {
			MavenJdtActivator.log("Can't access the project facade from marker", e);
		}
		return null;
	}

}
