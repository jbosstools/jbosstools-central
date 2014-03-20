/*************************************************************************************
 * Copyright (c) 2012-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.ui.internal.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.ui.internal.Messages;

public class SourceLookupUtil {

	public static void attachSource(final IPackageFragmentRoot fragment, final IPath newSourcePath) {
		attachSource(fragment, newSourcePath, true);
	}
	
	public static void attachSource(final IPackageFragmentRoot fragment, final IPath newSourcePath, boolean displayDialog) {
		try {
			if (fragment == null || fragment.getKind() != IPackageFragmentRoot.K_BINARY) {
				return;
			}
			String value = SourceLookupActivator.getDefault().getAutoAddSourceAttachment();
			if (SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_NEVER.equals(value)) {
				return;
			}
			if (displayDialog && SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_PROMPT.equals(value)) {
				final boolean[] attach = new boolean[1];
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						attach[0] = promptToAddSourceAttachment(fragment.getElementName(), newSourcePath.toString());
					}
				});
				if (!attach[0]) {
					return;
				};
			}
			IPath containerPath = null;
			IJavaProject jproject = fragment.getJavaProject();
			IClasspathEntry entry = fragment.getRawClasspathEntry();
			if (entry == null) {
				entry = JavaCore.newLibraryEntry(fragment.getPath(), null, null);
			} else {
				if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					containerPath = entry.getPath();
					ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(containerPath.segment(0));
					IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, jproject);
					if (initializer == null || container == null) {
						return;
					}
					IStatus status = initializer.getSourceAttachmentStatus(containerPath, jproject);
					if (status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_NOT_SUPPORTED) {
						return;
					}
					if (status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY) {
						return;
					}
					entry = JavaModelUtil.findEntryInContainer(container, fragment.getPath());
					if (entry == null) {
						return;
					}
				}
			}
			IClasspathEntry entry1;
			CPListElement elem = CPListElement.createFromExisting(entry, null);
			elem.setAttribute(CPListElement.SOURCEATTACHMENT, newSourcePath);
			entry1 = elem.getClasspathEntry();
			if (entry1.equals(entry)) {
				return;
			}
			IClasspathEntry newEntry = entry1;
			String[] changedAttributes = { CPListElement.SOURCEATTACHMENT };
			BuildPathSupport.modifyClasspathEntry(null, newEntry, changedAttributes, jproject, containerPath,
					 newEntry.getReferencingEntry() != null, new NullProgressMonitor());		
		} catch (CoreException e) {
			// ignore
		}
	}
	
	private static boolean promptToAddSourceAttachment(String jarName, String path) {
		IPreferenceStore store = SourceLookupActivator.getDefault().getPreferenceStore();
		String key = SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT;
		String value = store.getString(key);
		if (MessageDialogWithToggle.ALWAYS.equals(value)) {
			return true;
		}
		if (MessageDialogWithToggle.NEVER.equals(value)) {
			return false;
		}
		String title = NLS.bind(Messages.SourceLookupUtil_Found_Source_Title, jarName);
		String message = NLS.bind(Messages.SourceLookupUtil_Found_Source_Message, jarName, path);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(shell,
				title, message, null, false, store, key);
		int result = dialog.getReturnCode();
		// the result is equal to SWT.DEFAULT if the user uses the 'esc' key to close the dialog
		if (result == Window.CANCEL || result == SWT.DEFAULT) {
			throw new OperationCanceledException();
		}
		return dialog.getReturnCode() == IDialogConstants.YES_ID;
	}


}
