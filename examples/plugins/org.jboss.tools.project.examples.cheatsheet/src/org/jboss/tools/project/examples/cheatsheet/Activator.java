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
package org.jboss.tools.project.examples.cheatsheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.cheatsheet.internal.util.CheatSheetUtil;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static final String DOT_CHEATSHEET_XML = ".cheatsheet.xml"; //$NON-NLS-1$

	private static final String CHEATSHEET_XML = "cheatsheet.xml"; //$NON-NLS-1$

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.jboss.tools.project.examples.cheatsheet"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	IResourceChangeListener listener = new IResourceChangeListener() {
		
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
				return;
			}
			IResourceDelta delta = event.getDelta();
			if (delta == null) {
				return;
			}
			String value = ProjectExamplesActivator.getDefault().getShowCheatsheets();
			if (ProjectExamplesActivator.SHOW_CHEATSHEETS_NEVER.equals(value)) {
				return;
			}
			final List<IFile> cheatsheets = new ArrayList<IFile>();
			final Boolean[] createdProject = new Boolean[1];
			createdProject[0] = false;
			try {
				delta.accept(new IResourceDeltaVisitor() {
				    public boolean visit(IResourceDelta delta) throws CoreException {
				    	if (delta == null) {
				    		return false;
				    	}
				    	IResource resource = delta.getResource();
				    	if (resource instanceof IWorkspaceRoot) {
				    		return true;
				    	}
						if (resource instanceof IProject) {
							if (((IProject) resource).isOpen()) {
								return true;
							}
						}
				        if (resource instanceof IFile && delta.getKind() == IResourceDelta.ADDED) {
				        	if (isCheatcheet((IFile) resource)) {
				        		cheatsheets.add((IFile) resource);
				        		return false;
				        	}
				        }
				        if (resource instanceof IFile && delta.getKind() == IResourceDelta.ADDED) {
				        	if (isCheatcheet((IFile) resource)) {
				        		cheatsheets.add((IFile) resource);
				        		//if we know we created (imported) the project, we can stop scanning
				        		return createdProject[0]?false:true;
				        	} else //check we're in the middle of an import 
				        		if (((IFile) resource).equals(resource.getProject().getFile(".project"))) { //$NON-NLS-1$
 				        		  createdProject[0] = true;
				        		  return true;
				        	}
				        }

				        return false;
				    }
				});
			} catch (CoreException e) {
				Activator.log(e);
			}
			if (createdProject[0] && cheatsheets.size() > 0) {
				CheatSheetUtil.showCheatsheet(cheatsheets);
			}
		}

	};
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		super.stop(context);
	}

	/**
	 * checks if a file is a cheatsheet
	 * 
	 * @param file
	 * @return
	 */
	public static boolean isCheatcheet(IFile file) {
		String fileName = file.getName().toLowerCase();
		boolean candidate = CHEATSHEET_XML.equals(fileName) || DOT_CHEATSHEET_XML.equals(fileName);
		if (candidate) {
			try {
				IContentDescription contentDescription = file.getContentDescription();
				IContentType contentType = contentDescription.getContentType();
				return (contentType != null && "org.eclipse.pde.simpleCheatSheet".equals(contentType.getId())); //$NON-NLS-1$
			} catch (CoreException e) {
				// ignore
			}
		}
		return false;
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * logs an exception
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		plugin.getLog().log(status);
	}
	
	/**
	 * logs a message
	 * 
	 * @param message
	 */
	public static void log(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID,message);
		plugin.getLog().log(status);
	}
	
	/**
	 * shows a cheatsheet
	 * 
	 * @param selection
	 */
	public static void showCheatsheet(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object object = structuredSelection.getFirstElement();
			if (object instanceof IFile) {
				IFile file = (IFile) object;
				CheatSheetUtil.showCheatsheet(file);
			} else if (object instanceof IAdaptable) {
				IProject project = (IProject) ((IAdaptable)object).getAdapter(IProject.class);
				if (project == null) {
					return;
				}
				
				final List<IFile> cheatsheets = new ArrayList<IFile>();
				try {
					project.accept(new IResourceVisitor() {
						
						public boolean visit(IResource resource) throws CoreException {
							if (resource instanceof IProject) {
								return true;
							}
							if (resource instanceof IFile) {
								IFile file = (IFile) resource;
								if (Activator.isCheatcheet(file)) {
									cheatsheets.add(file);
								}
								return false;
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
		}
	}

}
