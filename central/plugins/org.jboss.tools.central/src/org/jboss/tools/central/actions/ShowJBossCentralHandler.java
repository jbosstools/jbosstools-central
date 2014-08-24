/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jboss.tools.central.JBossCentralActivator;

/**
* @author snjeza
* 
*/
public class ShowJBossCentralHandler extends AbstractHandler implements IWorkbenchWindowActionDelegate {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		JBossCentralActivator.getJBossCentralEditor(true);
		return null;
	}

	@Override
	public void run(IAction action) {
		JBossCentralActivator.getJBossCentralEditor(true);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		
	}

}
