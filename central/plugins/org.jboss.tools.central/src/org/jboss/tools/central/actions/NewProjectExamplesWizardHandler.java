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

package org.jboss.tools.central.actions;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza
 *
 */
public class NewProjectExamplesWizardHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
	    IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.eclipse.ui.newWizards");
	    IExtension[] extensions = extensionPoint.getExtensions();
	    
	    for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				String id = element.getAttribute("id");
				if (JBossCentralActivator.NEW_PROJECT_EXAMPLES_WIZARD_ID.equals(id)) {
					try {
						Object object = WorkbenchPlugin.createExtension(element, "class");
						if (object instanceof INewWizard) {
					          INewWizard wizard = (INewWizard)object;
					          IWorkbenchPartSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart().getSite();
					          ISelection selection = site.getSelectionProvider().getSelection();
					          if (selection instanceof IStructuredSelection) {
					        	  wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);
					          }
					          WizardDialog dialog = new WizardDialog(site.getShell(), wizard);
					          dialog.open();
						}
					} catch (CoreException e) {
						JBossCentralActivator.log(e);
					}
					break;
				}
			}
		}
	    return null;
	}


}
