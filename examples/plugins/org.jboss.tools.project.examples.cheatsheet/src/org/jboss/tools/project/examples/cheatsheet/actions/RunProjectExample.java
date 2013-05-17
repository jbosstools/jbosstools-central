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
package org.jboss.tools.project.examples.cheatsheet.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.cheatsheet.Activator;
import org.jboss.tools.project.examples.cheatsheet.Messages;
import org.jboss.tools.project.examples.cheatsheet.internal.util.CheatSheetUtil;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;

/**
 * 
 * <p>Action that runs a project example.</p>
 * 
 * @author snjeza
 *
 */
public class RunProjectExample extends Action implements ICheatSheetAction {

	/**
	 * Execution of the action
	 * 
	 * @param params
	 *            Array of parameters
	 *            index 0: <category>::<name> project example
	 * @param manager
	 *            Cheatsheet Manager
	 */
	public void run(String[] params, ICheatSheetManager manager) {
		if(params == null || params[0] == null ) {
			return;
		}
		
		String params0 = params[0];
		String[] projectExample = params0.split("::"); //$NON-NLS-1$
		if (projectExample == null || projectExample.length != 2 || projectExample[0] == null || projectExample[1] == null) {
			Activator.log(NLS.bind(Messages.RunProjectExample_Invalid_project_example, params0));
			return;
		}
		List<ProjectExampleCategory> categories = ProjectExampleUtil.getProjects(new NullProgressMonitor());
		ProjectExample project = null;
		for (ProjectExampleCategory category:categories) {
			if (projectExample[0].equals(category.getName())) {
				for (ProjectExample p:category.getProjects()) {
					if (projectExample[1].equals(p.getName())) {
						project = p;
						break;
					}
				}
			}
			if (project != null) {
				break;
			}
		}
		if (project != null) {
			List<ProjectExample> selectedProjects = new ArrayList<ProjectExample>();
			selectedProjects.add(project);
			ProjectExamplesActivator.importProjectExamples(selectedProjects, null, null);
		}
	}
	
}
