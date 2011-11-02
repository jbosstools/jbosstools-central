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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.actions.RunOnServerAction;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.project.examples.cheatsheet.Activator;

/**
 * 
 * <p>Action that runs an application on a server.</p>
 * 
 * @author snjeza
 *
 */
public class RunOnServer extends Action implements ICheatSheetAction {

	/**
	 * Execution of the action
	 * 
	 * @param params
	 *            Array of parameters
	 *            index 0: projectName,
	 *            index 1: a file path we want to mark as deployable.
	 * @param manager
	 *            Cheatsheet Manager
	 */
	public void run(String[] params, ICheatSheetManager manager) {
		if(params == null || params[0] == null) {
			return;
		}
		IWorkspaceRoot wRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = wRoot.getProject(params[0]);
		if (project == null || !project.isOpen()) {
			return;
		}
		if (params[1] != null) {
			IFile file = wRoot.getFile(new Path(params[1]));
			if (file != null && file.exists()) {
				try {
					SingleDeployableFactory.makeDeployable(file.getFullPath());
					IServer[] deployableServersAsIServers = ServerConverter
							.getDeployableServersAsIServers();
					if (deployableServersAsIServers.length == 1) {
						IServer server = deployableServersAsIServers[0];
						IServerWorkingCopy copy = server.createWorkingCopy();
						IModule[] modules = new IModule[1];
						modules[0] = SingleDeployableFactory.findModule(file
								.getFullPath());
						copy.modifyModules(modules, new IModule[0],
								new NullProgressMonitor());
						IServer saved = copy.save(false,
								new NullProgressMonitor());
						saved.publish(IServer.PUBLISH_INCREMENTAL,
								new NullProgressMonitor());
					}
				} catch (CoreException e) {
					IStatus status = new Status(IStatus.INFO,Activator.PLUGIN_ID,e.getMessage(),e);
					Activator.getDefault().getLog().log(status);
				}
			}
		}
		IAction action = new RunOnServerAction(project);
		action.run();
	}

}
