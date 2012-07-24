/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Theodora Yeung (tyeung@bea.com) - ensure that JarPackageFragmentRoot make it into cache
 *                                                           before its contents
 *                                                           (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=102422)
 *     Stephan Herrmann - Contribution for Bug 346010 - [model] strange initialization dependency in OptionTests
 *     Terry Parker <tparker@google.com> - DeltaProcessor misses state changes in archive files, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=357425
 *     Red Hat - copied code from {@link org.eclipse.jdt.internal.core.JavaModelManager.#getZipFile(IPath)} 
 *******************************************************************************/
package org.jboss.tools.maven.conversion.ui.dialog;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;
import org.jboss.tools.maven.conversion.ui.internal.MavenDependencyConversionActivator;

@SuppressWarnings("restriction")
public class ConversionUtils {
	
	
	/**
	 * Returns the underlying {@link File} from a {@link IClasspathEntry}.
	 * <br/>
	 * Part of the code comes from {@link org.eclipse.jdt.internal.core.JavaModelManager.#getZipFile(IPath)} 
	 * @param cpe
	 * @return
	 * @throws CoreException
	 */
	public static File getFile(IClasspathEntry cpe) throws CoreException {
		IPath path = cpe.getPath();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource file = root.findMember(path);
		File localFile = null;
		if (file != null) {
			// internal resource
			URI location;
			if (file.getType() != IResource.FILE || (location = file.getLocationURI()) == null) {
				throw new CoreException(new Status(IStatus.ERROR, MavenDependencyConversionActivator.PLUGIN_ID, -1, Messages.bind(Messages.file_notFound, path.toString()), null));
			}
			localFile = Util.toLocalFile(location, null/*no progress availaible*/);
			if (localFile == null)
				throw new CoreException(new Status(IStatus.ERROR, MavenDependencyConversionActivator.PLUGIN_ID, -1, Messages.bind(Messages.file_notFound, path.toString()), null));
		} else {
			// external resource -> it is ok to use toFile()
			localFile= path.toFile();
		}
		return localFile;
	}

}
