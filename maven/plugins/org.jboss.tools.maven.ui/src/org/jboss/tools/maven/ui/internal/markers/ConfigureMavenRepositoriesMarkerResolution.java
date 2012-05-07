/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.jboss.tools.maven.ui.wizard.ConfigureMavenRepositoriesWizard;

public class ConfigureMavenRepositoriesMarkerResolution implements IMarkerResolution,
		IMarkerResolution2 {

	
	public ConfigureMavenRepositoriesMarkerResolution() {
	}
	
	public String getDescription() {
		return getLabel();
	}

	public Image getImage() {					
		return null;
	}

	public String getLabel() {
		return "Configure Maven Repositories";
	}

	public void run(IMarker arg0) {

		ConfigureMavenRepositoriesWizard wizard = new ConfigureMavenRepositoriesWizard();
		WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
		dialog.create();
		dialog.open(); 
		
	}
	
}
