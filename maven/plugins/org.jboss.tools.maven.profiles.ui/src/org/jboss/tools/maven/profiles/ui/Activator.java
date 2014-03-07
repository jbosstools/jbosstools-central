/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.profiles.ui;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.maven.profiles.ui.internal.M2eProfilePropertyTester;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.jboss.tools.maven.profiles.ui";
	// The shared instance
	private static Activator plugin;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		if (M2eProfilePropertyTester.isConflicting()) {
			
			Job job = new Job("Deactivate JBoss Maven Profile Management Key Binding") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final IWorkbench workbench = PlatformUI.getWorkbench();
					final BindingManager bindingManager= (BindingManager) workbench.getService(BindingManager.class);
					final IBindingService bindingService= (IBindingService) workbench.getService(IBindingService.class);

					Binding[] aBindings = bindingManager.getBindings();

					final Binding[] conflictedBinding = new Binding[1];
					for (Binding b : aBindings) {
						if (b instanceof KeyBinding && b.getParameterizedCommand() != null 
								&& "org.jboss.tools.maven.ui.commands.selectMavenProfileCommand".equals(b.getParameterizedCommand().getId())
								) {
							conflictedBinding[0] = b;
							break;
						}
					}
					if (conflictedBinding[0] != null) {
					 workbench.getDisplay().asyncExec(new Runnable() {
						 public void run() {
					          try {
								log("Found org.eclipse.m2e.profiles.ui, removing conflicting binding for org.jboss.tools.maven.ui.commands.selectMavenProfileCommand");
								bindingManager.removeBinding(conflictedBinding[0]);
					        	bindingService.savePreferences(bindingManager.getActiveScheme(), bindingManager.getBindings());
					          } catch (IOException e) {
					            throw new RuntimeException(e);
					          }
					        }
					      });
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(false);
			job.schedule(2000);
			
		}
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}

	public static void log(String message) {
		IStatus status = new Status(IStatus.INFO, PLUGIN_ID, message);
		getDefault().getLog().log(status);
	}

}
