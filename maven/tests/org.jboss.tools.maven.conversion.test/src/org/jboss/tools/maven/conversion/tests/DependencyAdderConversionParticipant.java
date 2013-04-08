/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.maven.conversion.tests;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.conversion.AbstractProjectConversionParticipant;
import org.jboss.tools.maven.core.MavenUtil;


/**
 * Adds specific dependencies to some test projects. 
 * 
 * @author Fred Bricon
 */
public class DependencyAdderConversionParticipant extends AbstractProjectConversionParticipant {

  private static List<String> PROJECT_NAMES = Arrays.asList("JBIDE-13781-ear");	
	
	
  public boolean accept(IProject project) {
    return project != null && PROJECT_NAMES.contains(project.getName());
  }

  public void convert(IProject project, Model model, IProgressMonitor monitor) {
	  if ("JBIDE-13781-ear".equals(project.getName())) {
		  setJBIDE13781ear(model);
	  }
  }

  private void setJBIDE13781ear(Model model) {
	model.getDependencies().add(MavenUtil.createDependency("earClient", "earClient", "0.0.1-SNAPSHOT", "app-client"));
  }
}
