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
package org.jboss.tools.project.examples.cheatsheet.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.tools.project.examples.cheatsheet.Activator;
import org.jboss.tools.project.examples.cheatsheet.internal.util.CheatSheetUtil;


/**
 * 
 * <p>Action that opens a cheatsheet in the Cheat Sheets view.</p>
 * 
 * @author snjeza
 *
 */
public class OpenInCheatSheetsView extends AbstractHandler {

	/**
	 * Execution of the action
	 * 
	 */
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel= HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) sel;
			Object object = structuredSelection.getFirstElement();
			IResource resource = null;
			if (object instanceof IResource) {
				resource = (IResource) object;
			} else  if (object instanceof IAdaptable) {
				Object res= ((IAdaptable) object).getAdapter(IResource.class);
				if (res != null) {
					resource = (IResource) res;
				}
			}
			if (resource instanceof IProject) {
				IProject project = (IProject) resource;
				final List<IFile> cheatsheets = new ArrayList<IFile>();
				try {
					project.accept(new IResourceVisitor() {
						
						@Override
						public boolean visit(IResource resource) throws CoreException {
							if (resource instanceof IProject) {
								return true;
							}
							if (resource instanceof IFile) {
								if (Activator.isCheatcheet((IFile) resource)) {
									cheatsheets.add((IFile) resource);
								}
							}
							return false;
						}
					});
				} catch (CoreException e) {
					Activator.log(e);
				}
				if (cheatsheets.size() > 0) {
					CheatSheetUtil.showCheatsheet(cheatsheets.get(0));
				}
			}
			if (resource instanceof IFile) {
				CheatSheetUtil.showCheatsheet((IFile) resource);
			}
		}
		return null;
	}
	
}
