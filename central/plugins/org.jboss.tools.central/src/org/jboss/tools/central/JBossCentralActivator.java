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
package org.jboss.tools.central;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IServiceLocator;
import org.jboss.tools.central.editors.JBossCentralEditor;
import org.jboss.tools.central.editors.JBossCentralEditorInput;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossCentralActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.central"; //$NON-NLS-1$

	public static final String DOWNLOAD_JBOSS_RUNTIMES_EXTENSION_ID = "org.jboss.tools.central.downloadJBossRuntimes";
	
	public static final String SHOW_JBOSS_CENTRAL_ON_STARTUP = "showJBossCentralOnStartup";

	public static final boolean SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE = true;

	private static final String JBDS_PRODUCT_PLUGIN_ID = "com.jboss.jbds.product";
	
	public static final String NEW_PROJECT_EXAMPLES_WIZARD_ID = "org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard";
	
	public static final String NEWS_URL = "http://planet.jboss.org/view/all";

	public static final String NEWS_ATOM_URL = "http://planet.jboss.org/xml/all?type=atom";

	public static final String FORM_END_TAG = "</p></form>";
	public static final String FORM_START_TAG = "<form><p>";
	public static final String CANCELED = FORM_START_TAG + "<span color=\"header\" font=\"header\">Canceled.</span>" + FORM_END_TAG;
	public static final String LOADING = FORM_START_TAG + "<span color=\"header\" font=\"header\">Loading...</span>" + FORM_END_TAG;
	
	public static final int MAX_FEEDS = 40;
	
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String VERSION = "version"; //$NON-NLS-1$
	private static final String URL = "url"; //$NON-NLS-1$
	
	// The shared instance
	private static JBossCentralActivator plugin;

	private Map<String, DownloadRuntime> downloadRuntimes;
	
	/**
	 * The constructor
	 */
	public JBossCentralActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
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
	public static JBossCentralActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public IEclipsePreferences getPreferences() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		return prefs;
	}
	
	public void savePreferences() {
		IEclipsePreferences prefs = getPreferences();
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			log(e);
		}
	}
	
	public static void log(Exception e, String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		plugin.getLog().log(status);
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		plugin.getLog().log(status);
	}
	
	public static void log(String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message);
		plugin.getLog().log(status);
	}

	public boolean showJBossCentralOnStartup() {
		IEclipsePreferences prefs = JBossCentralActivator.getDefault().getPreferences();
		return prefs.getBoolean(SHOW_JBOSS_CENTRAL_ON_STARTUP, SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE);
	}
	
	public static boolean isJBDS() {
		Bundle bundle = Platform.getBundle(JBDS_PRODUCT_PLUGIN_ID);
		return bundle != null;
	}
	
	public static void openUrl(String location, Shell shell) {
		URL url = null;
		try {
			if (location != null) {
				url = new URL(location);
			}

			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
				IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
						.getBrowserSupport();
				support.getExternalBrowser().openURL(url);
			} else {
				IWebBrowser browser = null;
				int flags;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags = IWorkbenchBrowserSupport.AS_EDITOR
							| IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				} else {
					flags = IWorkbenchBrowserSupport.AS_EXTERNAL
							| IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = JBossCentralActivator.PLUGIN_ID + System.currentTimeMillis();
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, generatedId, null, null);
				browser.openURL(url);
			}
		} catch (PartInitException e) {
			Status status = new Status(IStatus.ERROR,
					JBossCentralActivator.PLUGIN_ID,
					"Browser initialization failed");
			JBossCentralActivator.getDefault().getLog().log(status);
			MessageDialog.openError(shell, "Open Location",
					status.getMessage());
		} catch (MalformedURLException e) {
			Status status = new Status(IStatus.ERROR,
					JBossCentralActivator.PLUGIN_ID, "Invalid URL");
			JBossCentralActivator.getDefault().getLog().log(status);
			MessageDialog.openError(shell, "Open Location",
					status.getMessage());
		}
	}
	
	public Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry imageRegistry = getImageRegistry();
		String id = getImageId(imageDescriptor);
		Image image = imageRegistry.get(id);
		if (image == null) {
			image = imageDescriptor.createImage(true);
			imageRegistry.put(id, image);
		}
		return image;
	}

	private String getImageId(ImageDescriptor imageDescriptor) {
		return PLUGIN_ID + "/" + imageDescriptor.hashCode();
	}
	
	public static CommandContributionItem createContributionItem(IServiceLocator serviceLocator, String commandId) {
		CommandContributionItemParameter parameter = new CommandContributionItemParameter(
				serviceLocator, commandId, commandId,
				CommandContributionItem.STYLE_PUSH);
		return new CommandContributionItem(parameter);
	}
	
	public static JBossCentralEditor getJBossCentralEditor() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorInput input = JBossCentralEditorInput.INSTANCE;
		try {
			IEditorPart editor = page.openEditor(input, JBossCentralEditor.ID);
			if (editor instanceof JBossCentralEditor) {
				return (JBossCentralEditor) editor;
			}
		} catch (PartInitException e) {
			JBossCentralActivator.log(e);
		}
		return null;
	}
	
	public Map<String, DownloadRuntime> getDownloadJBossRuntimes() {
		if (downloadRuntimes == null) {
			downloadRuntimes = new HashMap<String, DownloadRuntime>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(DOWNLOAD_JBOSS_RUNTIMES_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					String name = configurationElement.getAttribute(NAME);
					String id = configurationElement.getAttribute(ID);
					String version = configurationElement.getAttribute(VERSION);
					String url = configurationElement.getAttribute(URL);
					DownloadRuntime downloadRuntime = new DownloadRuntime(id, name, version, url);
					downloadRuntimes.put(id, downloadRuntime);
				}
			}
		}
		return downloadRuntimes;
	}

}
