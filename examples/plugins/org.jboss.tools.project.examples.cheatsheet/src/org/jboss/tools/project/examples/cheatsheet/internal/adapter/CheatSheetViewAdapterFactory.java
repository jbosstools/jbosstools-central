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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.jboss.tools.project.examples.cheatsheet.Activator;

/**
 * Shows cheatsheet xml files in the Cheat Sheets View 
 * 
 * @author snjeza
 *
 */
public class CheatSheetViewAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		return new IShowInTarget() {
			
			public boolean show(ShowInContext context) {
				if (context == null) {
					return false;
				}
				ISelection sel = context.getSelection();
				Activator.showCheatsheet(sel);
				return false;
			}
		};
	}

	public Class[] getAdapterList() {
		return new Class[] {IShowInTarget.class};
	}

}
