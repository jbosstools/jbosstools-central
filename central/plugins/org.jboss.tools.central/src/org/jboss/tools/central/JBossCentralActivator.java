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
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IServiceLocator;
import org.jboss.tools.central.editors.JBossCentralEditor;
import org.jboss.tools.central.editors.JBossCentralEditorInput;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossCentralActivator extends AbstractUIPlugin {

	public static final Object JBOSS_CENTRAL_FAMILY = new Object();

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

	public static final String FORM_END_TAG = "</p></form>";
	public static final String FORM_START_TAG = "<form><p>";
	public static final String CANCELED = FORM_START_TAG
			+ "<span color=\"header\" font=\"header\">Canceled.</span>"
			+ FORM_END_TAG;
	public static final String LOADING = FORM_START_TAG
			+ "<span color=\"header\" font=\"header\">Loading...</span>"
			+ FORM_END_TAG;

	public static final String SEARCH_THE_COMMUNITY = "Search JBoss Community";

	public static final String SEARCH_RED_HAT_CUSTOMER_PORTAL = "Search Red Hat Customer Portal ";

	private BundleContext bundleContext;

	public static final int MAX_FEEDS = 100;

	private static final String ORG_ECLIPSE_UI_INTERNAL_INTROVIEW = "org.eclipse.ui.internal.introview";

	// The shared instance
	private static JBossCentralActivator plugin;

	private static Boolean isInternalWebBrowserAvailable;

	/**
	 * The constructor
	 */
	public JBossCentralActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.bundleContext = context;
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		Job.getJobManager().cancel(JBOSS_CENTRAL_FAMILY); 
		Job.getJobManager().join(JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		
		plugin = null;
		bundleContext = null;
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
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
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

	public static void log(Throwable e, String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		plugin.getLog().log(status);
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID,
				e.getLocalizedMessage(), e);
		plugin.getLog().log(status);
	}

	public static void log(String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message);
		plugin.getLog().log(status);
	}

	public static void logWarning(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
		plugin.getLog().log(status);
	}

	public boolean showJBossCentralOnStartup() {
		IEclipsePreferences prefs = JBossCentralActivator.getDefault()
				.getPreferences();
		return prefs.getBoolean(SHOW_JBOSS_CENTRAL_ON_STARTUP,
				SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE);
	}

	public static void openUrl(String location, Shell shell) {
		openUrl(location, shell, false);
	}

	public static void openUrl(String location, Shell shell, boolean asExternal) {
		URL url = null;
		try {
			if (location != null) {
				url = new URL(location);
			}

			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL
					|| asExternal) {
				IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
						.getBrowserSupport();
				support.getExternalBrowser().openURL(url);
			} else {
				IWebBrowser browser = null;
				int flags;
				if (WorkbenchBrowserSupport.getInstance()
						.isInternalWebBrowserAvailable()) {
					flags = IWorkbenchBrowserSupport.AS_EDITOR
							| IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				} else {
					flags = IWorkbenchBrowserSupport.AS_EXTERNAL
							| IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = JBossCentralActivator.PLUGIN_ID
						+ System.currentTimeMillis();
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(
						flags, generatedId, null, null);
				browser.openURL(url);
			}
		} catch (PartInitException e) {
			Status status = new Status(IStatus.ERROR,
					JBossCentralActivator.PLUGIN_ID,
					"Browser initialization failed");
			JBossCentralActivator.getDefault().getLog().log(status);
			MessageDialog
					.openError(shell, "Open Location", status.getMessage());
		} catch (MalformedURLException e) {
			Status status = new Status(IStatus.ERROR,
					JBossCentralActivator.PLUGIN_ID, "Invalid URL");
			JBossCentralActivator.getDefault().getLog().log(status);
			MessageDialog
					.openError(shell, "Open Location", status.getMessage());
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

	public static CommandContributionItem createContributionItem(
			IServiceLocator serviceLocator, String commandId) {
		CommandContributionItemParameter parameter = new CommandContributionItemParameter(
				serviceLocator, commandId, commandId,
				CommandContributionItem.STYLE_PUSH);
		return new CommandContributionItem(parameter);
	}

	public static JBossCentralEditor getJBossCentralEditor() {
		final WorkbenchWindow window = (WorkbenchWindow) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		if (page.findView(ORG_ECLIPSE_UI_INTERNAL_INTROVIEW) != null
				&& !window.getCoolBarVisible()
				&& !window.getPerspectiveBarVisible()) {
			IViewReference viewRef = page
					.findViewReference(ORG_ECLIPSE_UI_INTERNAL_INTROVIEW);
			if (page.getPartState(viewRef) == IWorkbenchPage.STATE_MAXIMIZED) {
				window.addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent event) {
						String property = event.getProperty();
						if (WorkbenchWindow.PROP_COOLBAR_VISIBLE
								.equals(property)
								|| WorkbenchWindow.PROP_COOLBAR_VISIBLE
										.equals(property)) {
							Object newValue = event.getNewValue();
							if (newValue instanceof Boolean
									&& ((Boolean) newValue).booleanValue()) {
								openJBossCentralEditor(page);
								window.removePropertyChangeListener(this);
							}
						}
					}
				});
			} else {
				return openJBossCentralEditor(page);
			}
		} else {
			return openJBossCentralEditor(page);
		}
		return null;
	}

	protected static JBossCentralEditor openJBossCentralEditor(
			IWorkbenchPage page) {
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

	
	public Image getImage(String imagePath) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(imagePath);
		if (image != null) {
			return image;
		}
		ImageDescriptor imageDescriptor = getImageDescriptor(imagePath);
		if (imageDescriptor == null) {
			logWarning(imagePath + " can not be found!");
			return null;
		}
		image = imageDescriptor.createImage();
		registry.put(imagePath, image);
		return image;
	}

	public String getDescription(ProjectExample project) {
		StringBuilder buffer = new StringBuilder();
    if (project.getDescription() != null) {
      buffer.append(project.getDescription());
    }
		buffer.append("\n\n");
		buffer.append("Size: ");
		buffer.append(project.getSizeAsText());
		if (project.getUnsatisfiedFixes() != null && project.getUnsatisfiedFixes().size() > 0) {
			buffer.append("\n\n");
		}
		return buffer.toString();
	}

	public static Dictionary<Object, Object> getEnvironment() {
		Dictionary<Object, Object> environment = new Hashtable<Object, Object>(
				System.getProperties());
		Bundle bundle = Platform.getBundle("org.jboss.tools.central"); //$NON-NLS-1$
		Version version = bundle.getVersion();
		environment.put("org.jboss.tools.central.version", version.toString()); //$NON-NLS-1$
		environment.put(
				"org.jboss.tools.central.version.major", version.getMajor()); //$NON-NLS-1$
		environment.put(
				"org.jboss.tools.central.version.minor", version.getMinor()); //$NON-NLS-1$
		environment.put(
				"org.jboss.tools.central.version.micro", version.getMicro()); //$NON-NLS-1$
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

	public static boolean isInternalWebBrowserAvailable() {
		if (isInternalWebBrowserAvailable != null) {
			return isInternalWebBrowserAvailable.booleanValue();
		}
		Shell shell = null;
		try {
			shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			new Browser(shell, SWT.NONE);
			isInternalWebBrowserAvailable = Boolean.TRUE.booleanValue();
			return true;
		} catch (Throwable t) {
			try {
				new Browser(shell, SWT.WEBKIT);
				isInternalWebBrowserAvailable = Boolean.TRUE.booleanValue();
				return true;
			} catch (Throwable e) {
				isInternalWebBrowserAvailable = Boolean.FALSE.booleanValue();
				return false;
			}
		} finally {
			if (shell != null)
				shell.dispose();
		}
	}

}
