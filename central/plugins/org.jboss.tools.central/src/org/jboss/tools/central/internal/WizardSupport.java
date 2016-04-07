/*************************************************************************************
 * Copyright (c) 2013-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.wizards.AbstractJBossCentralProjectWizard;
import org.osgi.framework.Bundle;

public class WizardSupport {

	private static final String CLASS_ATTRIBUTE = "class";
	
	private WizardSupport() {}

	public static Map<String, IConfigurationElement> getInstalledWizards() {
	    IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
	    IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.eclipse.ui.newWizards");
	    IExtension[] extensions = extensionPoint.getExtensions();
	    Map<String, IConfigurationElement> installedWizards = new HashMap<>(extensions.length);
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				boolean isProjectWizard=Boolean.parseBoolean(element.getAttribute("project"));
				if (isProjectWizard) {
					String id = element.getAttribute("id");
					installedWizards.put(id, element);
				}
			}
		}
		return installedWizards;
	}
	
	public static void openWizard(IConfigurationElement element) throws CoreException {
		Object object = createExtension(element);
		if (object instanceof INewWizard) {
			INewWizard wizard = (INewWizard) object;
			wizard.init(PlatformUI.getWorkbench(), null);
			if (wizard instanceof AbstractJBossCentralProjectWizard) {
				if (((AbstractJBossCentralProjectWizard) wizard).getProjectExample() == null) {
					// new
					// WizardLoadingErrorDialog(getDisplay().getActiveShell()).open();
					return;
				}
			}
			WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
			dialog.open();
		}
	}
	
	public static Object createExtension(final IConfigurationElement element) throws CoreException {
		if (element == null) {
			return null;
		}
		try {
			Bundle bundle = Platform.getBundle(element.getContributor().getName());
			if (isActive(bundle)) {
				return element.createExecutableExtension(CLASS_ATTRIBUTE);
			}
			final Object[] ret = new Object[1];
			final CoreException[] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable() {
				@Override
				public void run() {
					try {
						ret[0] = element.createExecutableExtension(CLASS_ATTRIBUTE);
					} catch (CoreException e) {
						exc[0] = e;
					}
				}
			});
			if (exc[0] != null) {
				throw exc[0];
			}
			return ret[0];
		} catch (InvalidRegistryObjectException e) {
			throw new CoreException(new Status(IStatus.ERROR, JBossCentralActivator.PLUGIN_ID, IStatus.ERROR,
					"Cannot create extension", e));
		}
	}
	

	private static boolean isActive(Bundle bundle) {
		return bundle != null && bundle.getState() == Bundle.ACTIVE;
	}
}
