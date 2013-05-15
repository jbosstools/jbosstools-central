/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.cheatsheet.internal.adapter;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.jboss.tools.project.examples.cheatsheet.Activator;

/**
 * Show Cheat Sheet xml files in the Cheat Sheet View 
 * 
 * @author snjeza
 *
 */
public class CheatSheetViewAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		return new IShowInTarget() {
			
			public boolean show(ShowInContext context) {
				if (context == null) {
					return false;
				}
				ISelection sel = context.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof IFile) {
						IFile file = (IFile) object;
						try {
							IContentDescription contentDescription = file.getContentDescription();
							IContentType contentType = contentDescription.getContentType();
							if (contentType != null && "org.eclipse.pde.simpleCheatSheet".equals(contentType.getId())) { //$NON-NLS-1$
								CheatSheetView view = ViewUtilities.showCheatSheetView();
								if (view == null) {
									return false;
								}
								IPath filePath = file.getFullPath();
								String id = filePath.lastSegment();
								if (id == null) {
									id = ""; //$NON-NLS-1$
								}
								URL url = file.getLocation().toFile().toURI().toURL();
								view.getCheatSheetViewer().setInput(id, id, url, new DefaultStateManager(), false);
								return true;
							}
						} catch (Exception e) {
							Activator.log(e);
						}
					}
				}
				return false;
			}
		};
	}

	public Class[] getAdapterList() {
		return new Class[] {IShowInTarget.class};
	}

}
