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
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.codehaus.plexus.util.IOUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;
import org.jboss.tools.maven.core.internal.identification.FileIdentificationManager;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.containers.JBossSourceContainer;
import org.jboss.tools.maven.sourcelookup.ui.SourceLookupUIActivator;
import org.jboss.tools.maven.sourcelookup.ui.internal.util.SourceLookupUtil;

/**
 * 
 * @author snjeza
 *
 */
public class AttachSourcesActionDelegate implements IEditorActionDelegate {

	private String lastIdentifiedFile;
	
	@Override
	public void run(IAction action) {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * adds a source attachment to a package fragment root when the Java editor opens a binary file
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null) {
			try {
				String value = SourceLookupActivator.getDefault().getAutoAddSourceAttachment();
				if (SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_NEVER.equals(value)) {
					return;
				}
				IClassFileEditorInput input = (IClassFileEditorInput) targetEditor.getEditorInput();
				IJavaElement element = input.getClassFile();
				String className = element.getElementName(); 
				boolean isMavenProject = isMavenProject(element.getJavaProject());
				String packagePath = null;
				while (element.getParent() != null) {
					if (element instanceof IPackageFragment) {
						packagePath = element.getElementName().replace(".", "/")+"/"+className.replace(".class", ".java");
					} else 
					if (element instanceof IPackageFragmentRoot) {
						final IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
						
						IPath attachmentPath = fragment.getSourceAttachmentPath();
						if ((attachmentPath == null || attachmentPath.isEmpty()) && isMavenProject) {
							//Let m2e do its stuff for missing attachments only
							break;
						} 
						
						if (attachmentPath != null && !attachmentPath.isEmpty()) {
							File attachementSource = attachmentPath.toFile();
							if (attachementSource.exists() && hasSource(attachementSource, packagePath)) {
								break;
							}
						}
						if (fragment.isArchive()) {
							IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile(fragment.getPath());
							final File file = iFile == null || iFile.getLocation() == null ? fragment.getPath().toFile() : iFile.getLocation().toFile();
							if (file.getAbsolutePath().equals(lastIdentifiedFile)) {
								return;
							}
							final ArtifactKey[] result = new ArtifactKey[1];
							Job identificationJob = new Job("Identify "+file.getName()) {
								@Override
								protected IStatus run(IProgressMonitor monitor) {
									IFileIdentificationManager identificationManager = new FileIdentificationManager();
									IStatus status = Status.OK_STATUS;
									try {
										result[0] = identificationManager.identify(file, monitor);
									} catch (CoreException e) {
										status = new Status(IStatus.ERROR, SourceLookupUIActivator.PLUGIN_ID, "unable to identify "+file.getName(), e);
									}
									return status;
								}
							};
							identificationJob.addJobChangeListener(new JobChangeAdapter() {
								@Override
								public void done(IJobChangeEvent event) {
									postIdentification(fragment, file, result[0]);
								}
							});
							identificationJob.schedule();
							lastIdentifiedFile = file.getAbsolutePath();
							break;
						}
					}
					element = element.getParent();
				}
			} catch (Exception e) {
				SourceLookupUIActivator.log(e);
			}
		}
	}

	private boolean hasSource(File attachementSource, String className) {
		if (className == null) {
			return false;
		}
		if (attachementSource.isDirectory()) {
			return new File(attachementSource, className).exists();
		}
		//we assume it's a jar :
		ZipFile jar = null;
		try {
			jar = new ZipFile(attachementSource);
			ZipEntry entry = jar.getEntry(className);//$NON-NLS-1$
			return entry != null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (jar != null) jar.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private void postIdentification(final IPackageFragmentRoot fragment,
			File file, final ArtifactKey artifact) {
		if (artifact != null) {
			IPath sourcePath = JBossSourceContainer.getSourcePath(artifact);
			if (sourcePath == null || !sourcePath.toFile().exists()) {
				IJobChangeListener listener = new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						IPath sourcePath = JBossSourceContainer.getSourcePath(artifact);
						if (sourcePath != null && sourcePath.toFile().exists()) {
							SourceLookupUtil.attachSource(fragment, sourcePath);
						}
					}
				};
				JBossSourceContainer.downloadArtifact(file, artifact, listener);
			} else {
				SourceLookupUtil.attachSource(fragment, sourcePath);
			}
		}
	}

	private boolean isMavenProject(IJavaProject javaProject) {
		if (javaProject != null && javaProject.getProject() != null) {
			IProject project = javaProject.getProject();
			try {
				return project.hasNature(IMavenConstants.NATURE_ID);
			} catch (CoreException e) {
				SourceLookupUIActivator.log(e);
			}
		}
		return false;
	}

}
