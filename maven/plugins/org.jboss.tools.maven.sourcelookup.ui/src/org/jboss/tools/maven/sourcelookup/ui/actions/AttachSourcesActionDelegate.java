/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.ui.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;
import org.jboss.tools.maven.core.internal.identification.FileIdentificationManager;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.containers.JBossSourceContainer;
import org.jboss.tools.maven.sourcelookup.ui.SourceLookupUIActivator;

/**
 * 
 * @author snjeza
 *
 */
public class AttachSourcesActionDelegate implements IEditorActionDelegate {

	@Override
	public void run(IAction action) {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null) {
			try {
				boolean isAuto = SourceLookupActivator.getDefault().isAutoAddSourceContainer();
				if (!isAuto) {
					return;
				}
				IClassFileEditorInput input = (IClassFileEditorInput) targetEditor.getEditorInput();
				IJavaElement element = input.getClassFile();
				while (element.getParent() != null) {
					element = element.getParent();
					if (element instanceof IPackageFragmentRoot) {
						final IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
						IPath attachmentPath = fragment.getSourceAttachmentPath();
						if (attachmentPath != null && !attachmentPath.isEmpty() && attachmentPath.toFile().exists()) {
							break;
						}
						if (fragment.isArchive()) {
							IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile(fragment.getPath());
							File file = iFile == null || iFile.getLocation() == null ? fragment.getPath().toFile() : iFile.getLocation().toFile();
							IFileIdentificationManager identificationManager = new FileIdentificationManager();
							final ArtifactKey artifact = identificationManager.identify(file, new NullProgressMonitor());
							if (artifact != null) {
								IPath sourcePath = JBossSourceContainer.getSourcePath(artifact);
								if (sourcePath == null || !sourcePath.toFile().exists()) {
									Job job = JBossSourceContainer.downloadArtifact(file, artifact);
									job.addJobChangeListener(new IJobChangeListener() {

										@Override
										public void sleeping(IJobChangeEvent event) {
										}

										@Override
										public void scheduled(IJobChangeEvent event) {
										}

										@Override
										public void running(IJobChangeEvent event) {
										}

										@Override
										public void done(IJobChangeEvent event) {
											IPath sourcePath = JBossSourceContainer.getSourcePath(artifact);
											if (sourcePath != null && sourcePath.toFile().exists()) {
												attachSource(fragment, sourcePath);
											}
										}

										@Override
										public void awake(IJobChangeEvent event) {
										}

										@Override
										public void aboutToRun(IJobChangeEvent event) {
										}
									});
									job.schedule();
								} else {
									attachSource(fragment, sourcePath);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				SourceLookupUIActivator.log(e);
			}
		}
	}

	private void attachSource(IPackageFragmentRoot fragment, IPath newSourcePath) {
		try {
			if (fragment == null || fragment.getKind() != IPackageFragmentRoot.K_BINARY) {
				return;
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

}
