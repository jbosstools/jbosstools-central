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
package org.jboss.tools.project.examples.cheatsheet.internal.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.jboss.tools.project.examples.cheatsheet.Activator;

/**
 * 
 * @author snjeza
 * 
 */
public class CheatSheetPropertyTester extends PropertyTester {

	private static final String IS_CHEATSHEET = "isCheatSheet"; //$NON-NLS-1$
	
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (!IS_CHEATSHEET.equals(property)) {
			return false;
		} 
		IResource resource = (IResource) receiver;
		if (resource instanceof IProject) {
			IProject project = (IProject) resource;
			if (!project.isOpen()) {
				return false;
			}
			final boolean[] result = new boolean[1];
			result[0] = false;
			try {
				project.accept(new IResourceVisitor() {
					
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if (resource instanceof IProject) {
							return true;
						}
						if (resource instanceof IFile) {
							if (Activator.isCheatcheet((IFile) resource)) {
								result[0] = true;
							}
						}
						return false;
					}
				});
			} catch (CoreException e) {
				Activator.log(e);
			}
			return result[0];
		}
		if (resource instanceof IFile) {
			try {
				IFile file = (IFile) resource;
				IContentDescription contentDescription = file.getContentDescription();
				if (contentDescription == null) {
					return false;
				}
				IContentType contentType = contentDescription.getContentType();
				return (contentType != null && "org.eclipse.pde.simpleCheatSheet".equals(contentType.getId())); //$NON-NLS-1$
			} catch (CoreException e) {
				// ignore
			}
		}
		return false;
	}
		
}
