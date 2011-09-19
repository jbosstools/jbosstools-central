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
package org.jboss.tools.project.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.validation.internal.operations.ValidationBuilder;
import org.jboss.tools.project.examples.fixes.PluginFix;
import org.jboss.tools.project.examples.fixes.ProjectExamplesFix;
import org.jboss.tools.project.examples.fixes.SeamRuntimeFix;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.model.ProjectUtil;
import org.jboss.tools.project.examples.wizard.ImportDefaultMavenProjectExample;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ProjectExamplesActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.project.examples"; //$NON-NLS-1$
	public static final String ALL_SITES = Messages.ProjectExamplesActivator_All;
	public static final String SHOW_EXPERIMENTAL_SITES = "showExperimentalSites"; //$NON-NLS-1$
	public static final String USER_SITES = "userSites"; //$NON-NLS-1$
	public static final boolean SHOW_EXPERIMENTAL_SITES_VALUE = false;
	public static final String SHOW_INVALID_SITES = "invalidSites"; //$NON-NLS-1$
	public static final boolean SHOW_INVALID_SITES_VALUE = true;
	public static final String MAVEN_ARCHETYPE = "mavenArchetype"; //$NON-NLS-1$
	public static final Object PROJECT_EXAMPLES_FAMILY = new Object();
	
	private static final String IMPORT_PROJECT_EXAMPLES_EXTENSION_ID = "org.jboss.tools.project.examples.importProjectExamples"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	
	// The shared instance
	private static ProjectExamplesActivator plugin;

	private static BundleContext context;

	public static Job waitForBuildAndValidation = new Job(Messages.ProjectExamplesActivator_Waiting) {

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				Job.getJobManager().join(PROJECT_EXAMPLES_FAMILY, monitor);
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
						monitor);
				Job.getJobManager().join(
						ValidationBuilder.FAMILY_VALIDATION_JOB, monitor);
			} catch (OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}

	};
	private Map<String, IImportProjectExample> importProjectExamplesMap;
	private ImportDefaultMavenProjectExample defaultImportProjectExample;

	/**
	 * The constructor
	 */
	public ProjectExamplesActivator() {
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
		ProjectExamplesActivator.context = context;
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
		super.stop(context);
		plugin = null;
		context = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ProjectExamplesActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		ProjectExamplesActivator.getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID,message);
		ProjectExamplesActivator.getDefault().getLog().log(status);
	}

	public static BundleContext getBundleContext() {
		return context;
	}

	public static List<IMarker> getMarkers(List<Project> projects) {
		List<IMarker> markers = new ArrayList<IMarker>();
		for (Project project : projects) {
			try {
				if (project.getIncludedProjects() == null) {
					String projectName = project.getName();
					getMarkers(markers, projectName);
				} else {
					List<String> includedProjects = project.getIncludedProjects();
					for (String projectName:includedProjects) {
						getMarkers(markers, projectName);
					}
				}
			} catch (CoreException e) {
				ProjectExamplesActivator.log(e);
			}
		}
		return markers;
	}

	private static List<IMarker> getMarkers(List<IMarker> markers,
			String projectName) throws CoreException {
		IProject eclipseProject = ResourcesPlugin.getWorkspace()
				.getRoot().getProject(projectName);
		IMarker[] projectMarkers = eclipseProject.findMarkers(
				IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		for (int i = 0; i < projectMarkers.length; i++) {
			if (projectMarkers[i].getAttribute(IMarker.SEVERITY,
					IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
				markers.add(projectMarkers[i]);
			}
		}
		return markers;
	}
	
	public static IProject[] getEclipseProject(Project project,
			ProjectFix fix) {
		String pName = fix.getProperties().get(
				ProjectFix.ECLIPSE_PROJECTS);
		if (pName == null) {
			List<String> projectNames = project.getIncludedProjects();
			List<IProject> projects = new ArrayList<IProject>();
			for (String projectName:projectNames) {
				IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (eclipseProject != null && eclipseProject.isOpen()) {
					projects.add(eclipseProject);
				}
			}
			return projects.toArray(new IProject[0]);
		}
		StringTokenizer tokenizer = new StringTokenizer(pName,","); //$NON-NLS-1$
		List<IProject> projects = new ArrayList<IProject>();
		while (tokenizer.hasMoreTokens()) {
			String projectName = tokenizer.nextToken().trim();
			if (projectName != null && projectName.length() > 0) {
				IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (eclipseProject != null && eclipseProject.isOpen()) {
					projects.add(eclipseProject);
				}
			}
		}
		return projects.toArray(new IProject[0]);
	}

	public IImportProjectExample getImportProjectExample(String importType) {
		initImportProjectExamples();
		if (importType == null) {
			return defaultImportProjectExample;
		}
		return importProjectExamplesMap.get(importType);
	}

	private void initImportProjectExamples() {
		if (importProjectExamplesMap == null) {
			defaultImportProjectExample = new ImportDefaultMavenProjectExample();
			importProjectExamplesMap = new HashMap<String,IImportProjectExample>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(IMPORT_PROJECT_EXAMPLES_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					IImportProjectExample importProjectExample;
					try {
						importProjectExample = (IImportProjectExample) configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException e) {
						log(e);
						continue;
					}
					String name = configurationElement.getAttribute(NAME);
					String type = configurationElement.getAttribute(TYPE);
					importProjectExample.setName(name);
					importProjectExample.setType(type);
					importProjectExamplesMap.put(type, importProjectExample);
				}
			}
				
		}
	}
	
	public static void fix(Project project, IProgressMonitor monitor) {
		List<ProjectFix> fixes = project.getFixes();
		for (ProjectFix fix:fixes) {
			ProjectExamplesFix projectExamplesFix = ProjectExamplesFixFactory.getProjectFix(fix);
			if (projectExamplesFix != null) {
				projectExamplesFix.fix(project, fix, monitor);
			}
		}
	}
	
	private static class ProjectExamplesFixFactory {
		public static ProjectExamplesFix getProjectFix(ProjectFix fix) {
			if (ProjectFix.WTP_RUNTIME.equals(fix.getType())) {
				return new WTPRuntimeFix();
			}
			if (ProjectFix.SEAM_RUNTIME.equals(fix.getType())) {
				return new SeamRuntimeFix();
			}
			return null;
		}
	}
	
	public static boolean downloadProject(Project project, IProgressMonitor monitor) {
		if (project.isURLRequired()) {
			String urlString = project.getUrl();
			String name = project.getName();
			URL url = null;
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				ProjectExamplesActivator.log(e);
				return false;
			}
			File file = ProjectUtil.getProjectExamplesFile(url, name,
							".zip", monitor); //$NON-NLS-1$
			if (file == null) {
				return false;
			}
			project.setFile(file);
		}
		return true;
	}
	
	public static void openWelcome(List<Project> projects) {
		if (projects == null) {
			return;
		}
		for(final Project project:projects) {
			if (project.isWelcome()) {
				String urlString = project.getWelcomeURL();
				URL url = null;
				if (urlString.startsWith("/")) { //$NON-NLS-1$
					IPath path = new Path(urlString);
					IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
					if (resource instanceof IFile && resource.isAccessible()) {
						try {
							url = resource.getRawLocationURI().toURL();
						} catch (MalformedURLException e) {
							ProjectExamplesActivator.log(e);
						} 
					} else {
						ProjectExamplesActivator.log(NLS.bind(Messages.NewProjectExamplesWizard_File_does_not_exist,urlString));
					}
				} else {
					try {
						url = new URL(urlString);
					} catch (MalformedURLException e) {
						ProjectExamplesActivator.log(e);
					}
				}
				if (url!=null) {
					final URL finalURL = url;
					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							if (ProjectUtil.CHEATSHEETS.equals(project.getType())) {
								CheatSheetView view = ViewUtilities.showCheatSheetView();
								if (view == null) {
									return;
								}
								IPath filePath = new Path(finalURL.getPath());
								String id = filePath.lastSegment();
								if (id == null) {
									id = ""; //$NON-NLS-1$
								}
								view.getCheatSheetViewer().setInput(id, id, finalURL, new DefaultStateManager(), false);
							} else {
								try {
									IWorkbenchBrowserSupport browserSupport = ProjectExamplesActivator.getDefault().getWorkbench().getBrowserSupport();
									IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
									browser.openURL(finalURL);
								} catch (PartInitException e) {
									ProjectExamplesActivator.log(e);
								}
							}
						}
						
					});
					
				}
			}
		}
	}

	public static boolean extractZipFile(File file, File destination,
			IProgressMonitor monitor) {
		ZipFile zipFile = null;
		destination.mkdirs();
		try {
			zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				if (monitor.isCanceled()) {
					return false;
				}
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.isDirectory()) {
					monitor.setTaskName("Extracting " + entry.getName());
					File dir = new File(destination, entry.getName());
					dir.mkdirs();
					continue;
				}
				monitor.setTaskName("Extracting " + entry.getName());
				File entryFile = new File(destination, entry.getName());
				entryFile.getParentFile().mkdirs();
				InputStream in = null;
				OutputStream out = null;
				try {
					in = zipFile.getInputStream(entry);
					out = new FileOutputStream(entryFile);
					copy(in, out);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e) {
							// ignore
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (Exception e) {
							// ignore
						}
					}
				}
			}
		} catch (IOException e) {
			ProjectExamplesActivator.log(e);
			return false;
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return true;
	}
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[16 * 1024];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
	}
	
	public static boolean canFix(Project project,ProjectFix fix) {
		String type = fix.getType();
		if (ProjectFix.PLUGIN_TYPE.equals(type)) {
			return new PluginFix().canFix(project, fix);
		}
		
		if (ProjectFix.WTP_RUNTIME.equals(type)) {
			return new WTPRuntimeFix().canFix(project, fix);
		}
		
		if (ProjectFix.SEAM_RUNTIME.equals(type)) {
			return new SeamRuntimeFix().canFix(project, fix);
		}
		ProjectExamplesActivator.log(NLS.bind(Messages.NewProjectExamplesWizardPage_Invalid_fix, project.getName()));
		return true;
	}
	
}
