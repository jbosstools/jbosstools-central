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
package org.jboss.tools.maven.conversion.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.jboss.tools.common.util.FileUtil;

public abstract class AbstractMavenConversionTest extends
		AbstractMavenProjectTestCase {

	protected String toString(File file) {
		return FileUtil.readFile(file);
	}

	protected String toString(IFile file) {
		return FileUtil.getContentFromEditorOrFile(file);
	}

	protected void assertHasError(IProject project, String errorMessage) {
		try {
			for (IMarker m : findErrorMarkers(project)) {
				String message = (String)m.getAttribute(IMarker.MESSAGE);
				if (errorMessage.equals(message)){
					return;
				}
			}
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		fail("Error Message '"+ errorMessage +"' was not found on "+project.getName());
	}
	
	protected static String getMessage(IMarker marker) throws CoreException {
		return (String)marker.getAttribute(IMarker.MESSAGE);
	}
	
}
