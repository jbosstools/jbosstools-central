/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jdt.internal.markers;


import org.eclipse.core.resources.IMarker;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.jboss.tools.maven.jdt.internal.jobs.ExecutePhaseJob;

public class ExecuteDependencyCopyMarkerResolution implements IMarkerResolution,
		IMarkerResolution2 {

	private final String phase;
	private final IMavenProjectFacade mavenProjectFacade;

	public ExecuteDependencyCopyMarkerResolution(IMavenProjectFacade mavenProjectFacade, String phaseToExecute) {
		this.mavenProjectFacade = mavenProjectFacade;
		this.phase = phaseToExecute;
	}

	public String getDescription() {
		return getLabel();
	}

	public Image getImage() {					
		return null;
	}

	public String getLabel() {
		return "Run 'mvn "+ phase +"' to execute dependency:copy";
	}

	public void run(IMarker marker) {
		ExecutePhaseJob job = new ExecutePhaseJob("Run 'mvn "+ phase +"'", mavenProjectFacade, phase);
		job.schedule();
	}
	
}
