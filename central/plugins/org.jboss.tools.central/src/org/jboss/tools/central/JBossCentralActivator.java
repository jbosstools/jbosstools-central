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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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
import org.jboss.tools.central.model.Tutorial;
import org.jboss.tools.central.model.TutorialCategory;
import org.jboss.tools.project.examples.model.Project;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossCentralActivator extends AbstractUIPlugin {

	private static final String JBOSS_DISCOVERY_DIRECTORY = "jboss.discovery.directory";

	public static final String JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML = "http://download.jboss.org/jbosstools/updates/development/indigo/directory.xml";
	
	public static final String ICON = "icon";

	private static final String DESCRIPTION = "description";

	private static final String TUTORIAL = "tutorial";

	public static final String CATEGORY_ID = "categoryId";

	public static final String REFERENCE = "reference";

	public static final String TYPE = "type";

	public static final String PRIORITY = "priority";

	public static final String ID = "id";

	public static final String NAME = "name";

	public static final String CATEGORY = "category";
	
	public static final String PROJECT_EXAMPLE_TYPE = "projectExample";

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.central"; //$NON-NLS-1$

	public static final String SHOW_JBOSS_CENTRAL_ON_STARTUP = "showJBossCentralOnStartup";

	public static final boolean SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE = true;
	
	public static final String PROFILE_ID = "profileId";

	public static final String PROFILE_TIMESTAMP = "profileTimestamp";
	
	public static final String NEW_PROJECT_EXAMPLES_WIZARD_ID = "org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard";
	
	public static final String BLOG_URL = "http://planet.jboss.org/feeds/blogs";

	public static final String BLOGS_ATOM_URL = "http://planet.jboss.org/feeds/blogs";
	
	public static final String NEWS_URL = "http://pipes.yahoo.com/pipes/pipe.run?_id=660682be8ddf4b5db0cce318826f8a53";

	public static final String NEWS_ATOM_URL = "http://pipes.yahoo.com/pipes/pipe.run?_id=660682be8ddf4b5db0cce318826f8a53&_render=rss";

	public static final String FORM_END_TAG = "</p></form>";
	public static final String FORM_START_TAG = "<form><p>";
	public static final String CANCELED = FORM_START_TAG + "<span color=\"header\" font=\"header\">Canceled.</span>" + FORM_END_TAG;
	public static final String LOADING = FORM_START_TAG + "<span color=\"header\" font=\"header\">Loading...</span>" + FORM_END_TAG;
	
	public static final String TUTORIALS_EXTENSION_ID = "org.jboss.tools.central.tutorials";
	
	//public static final String SEARCH_PROJECT_PAGES = "Search Project Pages";

	public static final String SEARCH_THE_COMMUNITY = "Search JBoss Community";

	public static final String SEARCH_RED_HAT_CUSTOMER_PORTAL = "Search Red Hat Customer Portal ";

	public Map<String, TutorialCategory> tutorialCategories;

	private BundleContext bundleContext;
	
	public static final int MAX_FEEDS = 100;
	
	// The shared instance
	private static JBossCentralActivator plugin;

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
		this.bundleContext = context;
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		bundleContext = null;
		tutorialCategories = null;
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
	
	public Map<String, TutorialCategory> getTutorialCategories() {
		if (tutorialCategories == null) {
			tutorialCategories = new HashMap<String, TutorialCategory>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(TUTORIALS_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					if (CATEGORY.equals(configurationElement.getName())) {
						String name = configurationElement.getAttribute(NAME);
						String id = configurationElement.getAttribute(ID);
						String priorityString = configurationElement.getAttribute(PRIORITY);
						int priority = Integer.MAX_VALUE;
						if (priorityString != null) {
							try {
								priority = new Integer(priorityString)
										.intValue();
							} catch (NumberFormatException e) {
								log(e);
							}
						}
						TutorialCategory category = new TutorialCategory(id, name, priority);
						tutorialCategories.put(id, category);
					}
				}
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					if (TUTORIAL.equals(configurationElement.getName())) {
						String name = configurationElement.getAttribute(NAME);
						String id = configurationElement.getAttribute(ID);
						String type = configurationElement.getAttribute(TYPE);
						String reference = configurationElement.getAttribute(REFERENCE);
						String priorityString = configurationElement.getAttribute(PRIORITY);
						String description = configurationElement.getAttribute(DESCRIPTION);
						String iconPath = configurationElement.getAttribute(ICON);
						int priority = Integer.MAX_VALUE;
						if (priorityString != null) {
							try {
								priority = new Integer(priorityString)
										.intValue();
							} catch (NumberFormatException e) {
								log(e);
							}
						}
						String categoryId = configurationElement.getAttribute(CATEGORY_ID);
						TutorialCategory category = tutorialCategories.get(categoryId);
						if (category == null) {
							log("Invalid tutorial: id=" + id);
							continue;
						}
						Tutorial tutorial = new Tutorial(id, name, type, reference, priority, category, description, iconPath);
						category.getTutorials().add(tutorial);
					}
				}
			}
			List<TutorialCategory> emptyCategories = new ArrayList<TutorialCategory>();
			for (TutorialCategory category:tutorialCategories.values()) {
				if (category.getTutorials().size() == 0) {
					emptyCategories.add(category);
				}
			}
			for (TutorialCategory category:emptyCategories) {
				tutorialCategories.remove(category.getId());
			}
		}
		
		return tutorialCategories;
	}

	public Image getImage(String imagePath) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(imagePath);
		if (image != null) {
			return image;
		}
		ImageDescriptor imageDescriptor = getImageDescriptor(imagePath);
		image = imageDescriptor.createImage();
		registry.put(imagePath, image);
		return image;
	}

	public void setTutorialCategories(
			Map<String, TutorialCategory> tutorialCategories) {
		this.tutorialCategories = tutorialCategories;
	}
	
	public String getDescription(Tutorial tutorial) {
		String description = tutorial.getDescription();
		Project project = tutorial.getProjectExamples();
		if (project.getDescription() != null) {
			description = project.getDescription();
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(description);
		buffer.append("\n\n");
		buffer.append("Size: ");
		buffer.append(project.getSizeAsText());
		if (project.getUnsatisfiedFixes().size() > 0) {
			buffer.append("\n\n");	
		}
		return buffer.toString();
	}
	
	public static Dictionary<Object, Object> getEnvironment() {
		Dictionary<Object, Object> environment = new Hashtable<Object, Object>(System.getProperties());
		Bundle bundle = Platform.getBundle("org.jboss.tools.central"); //$NON-NLS-1$
		Version version = bundle.getVersion();
		environment.put("org.jboss.tools.central.version", version.toString()); //$NON-NLS-1$
		environment.put("org.jboss.tools.central.version.major", version.getMajor()); //$NON-NLS-1$
		environment.put("org.jboss.tools.central.version.minor", version.getMinor()); //$NON-NLS-1$
		environment.put("org.jboss.tools.central.version.micro", version.getMicro()); //$NON-NLS-1$
		return environment;
	}
	
	public Object getService(String name) {
		if (bundleContext == null)
			return null;
		ServiceReference<?> reference = bundleContext.getServiceReference(name);
		if (reference == null)
			return null;
		Object result = bundleContext.getService(reference);
		bundleContext.ungetService(reference);
		return result;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public static String getJBossDiscoveryDirectory() {
		String directory = System.getProperty(JBOSS_DISCOVERY_DIRECTORY, null);
		if (directory == null) {
			return JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML;
		}
		return directory;
	}
}
