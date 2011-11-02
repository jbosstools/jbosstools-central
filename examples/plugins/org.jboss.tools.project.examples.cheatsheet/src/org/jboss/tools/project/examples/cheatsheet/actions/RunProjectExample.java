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
import org.jboss.tools.project.examples.model.Category;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectUtil;

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
		
		String[] projectExample = params[0].split("::"); //$NON-NLS-1$
		if (projectExample == null || projectExample.length != 2 || projectExample[0] == null || projectExample[1] == null) {
			Activator.log(NLS.bind(Messages.RunProjectExample_Invalid_project_example, params[0]));
			return;
		}
		List<Category> categories = ProjectUtil.getProjects(new NullProgressMonitor());
		Project project = null;
		for (Category category:categories) {
			if (projectExample[0].equals(category.getName())) {
				for (Project p:category.getProjects()) {
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
			List<Project> selectedProjects = new ArrayList<Project>();
			selectedProjects.add(project);
			ProjectExamplesActivator.importProjectExamples(selectedProjects, true);
		}
	}
	
}
