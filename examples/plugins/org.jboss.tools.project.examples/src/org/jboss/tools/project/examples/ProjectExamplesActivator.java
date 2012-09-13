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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
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
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.cheatsheets.state.DefaultStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.validation.internal.operations.ValidationBuilder;
import org.jboss.tools.project.examples.configurators.DefaultJBossCentralConfigurator;
import org.jboss.tools.project.examples.configurators.IJBossCentralConfigurator;
import org.jboss.tools.project.examples.dialog.MarkerDialog;
import org.jboss.tools.project.examples.fixes.PluginFix;
import org.jboss.tools.project.examples.fixes.ProjectExamplesFix;
import org.jboss.tools.project.examples.fixes.SeamRuntimeFix;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.wizard.ContributedPage;
import org.jboss.tools.project.examples.wizard.ImportDefaultProjectExample;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesJob;
import org.jboss.tools.project.examples.wizard.ProjectReadyWizard;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * The activator class controls the plug-in life cycle
 */
public class ProjectExamplesActivator extends AbstractUIPlugin {

	private static final String SEPARATOR = "/"; //$NON-NLS-1$

	private static final int DESCRIPTION_LENGTH = 100;
	
	public static final String PROPERTY_PROJECT_NAME = "projectName"; //$NON-NLS-1$
	public static final String PROPERTY_LOCATION_PATH = "locationPath"; //$NON-NLS-1$
	public static final String PROPERTY_ARTIFACT_ID = "artifactId"; //$NON-NLS-1$
	
	private static final String README_HTML = "/readme.html"; //$NON-NLS-1$
	private static final String CHEATSHEET_XML = "/cheatsheet.xml"; //$NON-NLS-1$
	private static final String PERIOD_CHEATSHEET_XML = "/.cheatsheet.xml"; //$NON-NLS-1$
	private static final String README_MD = "/readme.md"; //$NON-NLS-1$
	private static final String README_TXT = "/readme.txt"; //$NON-NLS-1$
	private static final String README_MDU = "/README.md"; //$NON-NLS-1$
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.project.examples"; //$NON-NLS-1$
	public static final String ALL_SITES = Messages.ProjectExamplesActivator_All;
	public static final String ALL_RUNTIMES = Messages.ProjectExamplesActivator_All;
	
	public static final String SHOW_EXPERIMENTAL_SITES = "showExperimentalSites"; //$NON-NLS-1$
	public static final String SHOW_RUNTIME_SITES = "showRuntimeSites"; //$NON-NLS-1$
	public static final String USER_SITES = "userSites"; //$NON-NLS-1$
	public static final boolean SHOW_EXPERIMENTAL_SITES_VALUE = false;
	public static final boolean SHOW_RUNTIME_SITES_VALUE = false;
	public static final String SHOW_INVALID_SITES = "invalidSites"; //$NON-NLS-1$
	public static final boolean SHOW_INVALID_SITES_VALUE = true;
	public static final String MAVEN_ARCHETYPE = "mavenArchetype"; //$NON-NLS-1$
	public static final Object PROJECT_EXAMPLES_FAMILY = new Object();
	public static final String PROJECT_EXAMPLES_OUTPUT_DIRECTORY = "projectExamplesOutputDirectory"; //$NON-NLS-1$
	public static final String PROJECT_EXAMPLES_DEFAULT = "projectExamplesDefaultLocation"; //$NON-NLS-1$
	public static final boolean PROJECT_EXAMPLES_DEFAULT_VALUE = true;

	public static final String SHOW_PROJECT_READY_WIZARD = "showProjectRadyWizard"; //$NON-NLS-1$
	public static final boolean SHOW_PROJECT_READY_WIZARD_VALUE = true;
	
	public static final String SHOW_README = "showReadme"; //$NON-NLS-1$
	public static final boolean SHOW_README_VALUE = true;
	
	public static final String SHOW_QUICK_FIX = "showQuickFix"; //$NON-NLS-1$
	public static final boolean SHOW_QUICK_FIX_VALUE = true;
	
	private static final String IMPORT_PROJECT_EXAMPLES_EXTENSION_ID = "org.jboss.tools.project.examples.importProjectExamples"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String CLASS = "class"; //$NON-NLS-1$
	
	private static final String PRIORITY = "priority"; //$NON-NLS-1$
	
	public static final String CONFIGURATORS_EXTENSION_ID = "org.jboss.tools.project.examples.configurators"; //$NON-NLS-1$

	public static final String WIZARDPAGES_EXTENSION_ID = "org.jboss.tools.project.examples.wizardpages"; //$NON-NLS-1$

	private IJBossCentralConfigurator configurator;
	
	public static final String JBOSS_DISCOVERY_DIRECTORY = "jboss.discovery.directory.url"; //$NON-NLS-1$
	
	private static final Object CONFIGURATOR = "configurator"; //$NON-NLS-1$

	// The shared instance
	private static ProjectExamplesActivator plugin;

	private static BundleContext context;

	public static Job waitForBuildAndValidation = new Job(Messages.ProjectExamplesActivator_Waiting) {

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				try {
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
				} catch (CoreException e) {
					log(e);
				}
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
	private ImportDefaultProjectExample defaultImportProjectExample;

	private static final String VERSION = "version"; //$NON-NLS-1$
	
	private Map<String, List<ContributedPage>> contributedPages;
	
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

	public static List<IMarker> getMarkers(List<ProjectExample> projects) {
		List<IMarker> markers = new ArrayList<IMarker>();
		for (ProjectExample project : projects) {
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
		if (eclipseProject.isAccessible()) {
			IMarker[] projectMarkers = eclipseProject.findMarkers(
					IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < projectMarkers.length; i++) {
				if (projectMarkers[i].getAttribute(IMarker.SEVERITY,
						IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
					markers.add(projectMarkers[i]);
				}
			}
		} else {
			log(projectName + " is inaccessible");
		}
		return markers;
	}
	
	public static IProject[] getEclipseProject(ProjectExample project,
			ProjectFix fix) {
		String pName = fix.getProperties().get(
				ProjectFix.ECLIPSE_PROJECTS);
		if (pName == null) {
			List<String> projectNames = project.getIncludedProjects();
			List<IProject> projects = new ArrayList<IProject>();
			if (projectNames != null) {
				for (String projectName : projectNames) {
					IProject eclipseProject = ResourcesPlugin.getWorkspace()
							.getRoot().getProject(projectName);
					if (eclipseProject != null && eclipseProject.isOpen()) {
						projects.add(eclipseProject);
					}
				}
			}
			return projects.toArray(new IProject[0]);
		}
		pName = replace(pName, project);
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

	public static String replace(String name, ProjectExample project) {
		List<String> includedProjects = project.getIncludedProjects();
		if (includedProjects != null) {
			int i = 0;
			for (String includedProject : includedProjects) {
				String expression = "${project[" + i + "]}"; //$NON-NLS-1$ //$NON-NLS-2$
				name = name.replace(expression, includedProject);
				i++;
			}
		}
		return name;
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
			defaultImportProjectExample = new ImportDefaultProjectExample();
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
	
	public static void fix(ProjectExample project, IProgressMonitor monitor) {
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
	
	public static boolean downloadProject(ProjectExample project, IProgressMonitor monitor) {
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
			File file = ProjectExampleUtil.getProjectExamplesFile(url, name,
					".zip", monitor); //$NON-NLS-1$
			if (file == null) {
				return false;
			}
			project.setFile(file);
		}
		return true;
	}
	
	public static void copyFile(File inputFile, File outputFile)
			throws FileNotFoundException, IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(inputFile);
			out = new FileOutputStream(outputFile);
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
	public static void openWelcome(List<ProjectExample> projects) {
		if (projects == null) {
			return;
		}
		for(final ProjectExample project:projects) {
			fixWelcome(project);
			if (project.isWelcome()) {
				String urlString = project.getWelcomeURL();
				urlString = replace(urlString, project);
				URL url = null;
				if (urlString.startsWith(SEPARATOR)) { //$NON-NLS-1$
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
							if (ProjectExampleUtil.CHEATSHEETS.equals(project.getType())) {
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
									if (finalURL.toString().endsWith(".htm") || finalURL.toString().endsWith(".html")) {
										IWorkbenchBrowserSupport browserSupport = ProjectExamplesActivator.getDefault().getWorkbench().getBrowserSupport();
										IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, 
												null, null, null);
										browser.openURL(finalURL);
									} else {
										boolean txtFile = finalURL.toString().endsWith(".md") || finalURL.toString().endsWith(".txt");
										IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
										IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
										IFile[] files = null;
										try {
											files = root.findFilesForLocationURI(finalURL.toURI());
										} catch (URISyntaxException e1) {
											ProjectExamplesActivator.log(e1);
											return;
										}
										if (files.length > 0) {
											try {
												if (txtFile) {
													IFileEditorInput input = new FileEditorInput(files[0]);
													IDE.openEditor(page, input, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
												} else {
													IDE.openEditor(page, files[0]);
												}
											} catch (PartInitException e) {
												ProjectExamplesActivator.log(e);
											}
										} else {
											IFileStore store = EFS.getLocalFileSystem().getStore(
													new Path(finalURL.getPath()));
											try {
												FileStoreEditorInput input = new FileStoreEditorInput(store);
												if (txtFile) {
													IDE.openEditor(page, input,
														EditorsUI.DEFAULT_TEXT_EDITOR_ID);
												} else {
													IDE.openEditor(page, input,
															getEditorId(store));
												}
											} catch (PartInitException e) {
												ProjectExamplesActivator.log(e);
											}
										}
										
									} 
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

	private static String getEditorId(IFileStore fileStore) throws PartInitException {
		String name = fileStore.fetchInfo().getName();
		if (name == null) {
			throw new IllegalArgumentException();
		}

		IContentType contentType= null;
		try {
			InputStream is = null;
			try {
				is = fileStore.openInputStream(EFS.NONE, null);
				contentType= Platform.getContentTypeManager().findContentTypeFor(is, name);
			} finally {
				if (is != null) {
					is.close();
				}
			}
		} catch (CoreException ex) {
			// continue without content type
		} catch (IOException ex) {
			// continue without content type
		}

		IEditorRegistry editorReg= PlatformUI.getWorkbench().getEditorRegistry();

		return getEditorDescriptor(name, editorReg, editorReg.getDefaultEditor(name, contentType)).getId();
	}
	
	private static IEditorDescriptor getEditorDescriptor(String name,
			IEditorRegistry editorReg, IEditorDescriptor defaultDescriptor)
			throws PartInitException {

		if (defaultDescriptor != null) {
			return defaultDescriptor;
		}

		IEditorDescriptor editorDesc = defaultDescriptor;

		// next check the OS for in-place editor (OLE on Win32)
		if (editorReg.isSystemInPlaceEditorAvailable(name)) {
			editorDesc = editorReg
					.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		}

		// next check with the OS for an external editor
		if (editorDesc == null
				&& editorReg.isSystemExternalEditorAvailable(name)) {
			editorDesc = editorReg
					.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}

		// next lookup the default text editor
		if (editorDesc == null) {
			editorDesc = editorReg
					.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
		}

		// if no valid editor found, bail out
		if (editorDesc == null) {
			throw new PartInitException(
					IDEWorkbenchMessages.IDE_noFileEditorFound);
		}

		return editorDesc;
	}
	
	public static void fixWelcome(ProjectExample project) {
		if (project == null) {
			return;
		}
		if (!project.isWelcomeFixRequired()) {
			return;
		}
		checkCheatsheet(project);
	}

	protected static void checkCheatsheet(ProjectExample project) {
		List<String> includedProjects = project.getIncludedProjects();
		if (includedProjects == null || includedProjects.size() <= 0) {
			return;
		}
		for (String projectName : includedProjects) {
			if (projectName == null || projectName.isEmpty()) {
				continue;
			}
			IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			if (eclipseProject == null || !eclipseProject.exists()) {
				continue;
			}
			if (checkCheatsheet(project, eclipseProject, PERIOD_CHEATSHEET_XML,
					ProjectExampleUtil.CHEATSHEETS)) {
				return;
			}
			if (checkCheatsheet(project, eclipseProject, CHEATSHEET_XML,
					ProjectExampleUtil.CHEATSHEETS)) {
				return;
			}
			if (checkCheatsheet(project, eclipseProject, README_HTML,
					ProjectExampleUtil.EDITOR)) {
				return;
			}
			if (checkCheatsheet(project, eclipseProject, README_MD,
					ProjectExampleUtil.EDITOR)) {
				return;
			}
			if (checkCheatsheet(project, eclipseProject, README_MDU,
					ProjectExampleUtil.EDITOR)) {
				return;
			}
			if (checkCheatsheet(project, eclipseProject, README_TXT,
					ProjectExampleUtil.EDITOR)) {
				return;
			}
		}
	}

	private static boolean checkCheatsheet(ProjectExample project,
			IProject eclipseProject, String path, String type) {
		IResource cheatsheet = eclipseProject.findMember(path);
		if (cheatsheet != null && cheatsheet.exists() && cheatsheet.getType() == IResource.FILE) {
			project.setWelcome(true);
			project.setType(type);
			project.setWelcomeURL(cheatsheet.getFullPath().toString());
			return true;
		}
		return false;
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
	
	public static boolean canFix(ProjectExample project,ProjectFix fix) {
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
	
	public static void updatePerspective(List<ProjectExample> projects) {
		if (projects == null || projects.size() != 1) {
			return;
		}
		final String perspectiveId = projects.get(0).getPerspectiveId();
		if (perspectiveId == null || perspectiveId.length() <= 0) {
			return;
		}
		// Retrieve the new project open perspective preference setting
		String perspSetting = PrefUtil.getAPIPreferenceStore().getString(
				IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE);

		String promptSetting = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore().getString(
						IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);

		// Return if do not switch perspective setting and are not prompting
		if (!(promptSetting.equals(MessageDialogWithToggle.PROMPT))
				&& perspSetting.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE)) {
			return;
		}
		
		// Map perspective id to descriptor.
		IPerspectiveRegistry reg = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		// leave this code in - the perspective of a given project may map to
		// activities other than those that the wizard itself maps to.
		final IPerspectiveDescriptor finalPersp = reg
				.findPerspectiveWithId(perspectiveId);
		if (finalPersp != null && finalPersp instanceof IPluginContribution) {
			IPluginContribution contribution = (IPluginContribution) finalPersp;
			if (contribution.getPluginId() != null) {
				IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
						.getWorkbench().getActivitySupport();
				IActivityManager activityManager = workbenchActivitySupport
						.getActivityManager();
				IIdentifier identifier = activityManager
						.getIdentifier(WorkbenchActivityHelper
								.createUnifiedId(contribution));
				Set idActivities = identifier.getActivityIds();

				if (!idActivities.isEmpty()) {
					Set enabledIds = new HashSet(activityManager
							.getEnabledActivityIds());

					if (enabledIds.addAll(idActivities)) {
						workbenchActivitySupport
								.setEnabledActivityIds(enabledIds);
					}
				}
			}
		} else {
			IDEWorkbenchPlugin.log("Unable to find perspective " //$NON-NLS-1$
					+ perspectiveId
					+ " in NewProjectExamplesWizard.updatePerspective"); //$NON-NLS-1$
			return;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					switchPerspective(perspectiveId, finalPersp, win);
				}
			});
		} else {
			switchPerspective(perspectiveId, finalPersp, window);
		}
	}

	private static void switchPerspective(String perspectiveId,
			IPerspectiveDescriptor finalPersp, IWorkbenchWindow window) {
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IPerspectiveDescriptor currentPersp = page.getPerspective();
				if (currentPersp != null
						&& perspectiveId.equals(currentPersp.getId())) {
					return;
				}
			}
		}

		if (!confirmPerspectiveSwitch(window, finalPersp)) {
			return;
		}

		int workbenchPerspectiveSetting = WorkbenchPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.OPEN_PERSP_MODE);

		if (workbenchPerspectiveSetting == IPreferenceConstants.OPM_NEW_WINDOW) {
			openInNewWindow(finalPersp);
			return;
		}

		replaceCurrentPerspective(finalPersp);
	}

	/**
	 * Prompts the user for whether to switch perspectives.
	 * 
	 * @param window
	 *            The workbench window in which to switch perspectives; must not
	 *            be <code>null</code>
	 * @param finalPersp
	 *            The perspective to switch to; must not be <code>null</code>.
	 * 
	 * @return <code>true</code> if it's OK to switch, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmPerspectiveSwitch(IWorkbenchWindow window,
			IPerspectiveDescriptor finalPersp) {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore();
		String pspm = store
				.getString(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);
		if (!IDEInternalPreferences.PSPM_PROMPT.equals(pspm)) {
			// Return whether or not we should always switch
			return IDEInternalPreferences.PSPM_ALWAYS.equals(pspm);
		}
		String desc = finalPersp.getDescription();
		String message;
		if (desc == null || desc.length() == 0)
			message = NLS.bind(ResourceMessages.NewProject_perspSwitchMessage,
					finalPersp.getLabel());
		else
			message = NLS.bind(
					ResourceMessages.NewProject_perspSwitchMessageWithDesc,
					new String[] { finalPersp.getLabel(), desc });

		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openYesNoQuestion(window.getShell(),
						ResourceMessages.NewProject_perspSwitchTitle, message,
						null /* use the default message for the toggle */,
						false /* toggle is initially unchecked */, store,
						IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);
		int result = dialog.getReturnCode();

		// If we are not going to prompt anymore propogate the choice.
		if (dialog.getToggleState()) {
			String preferenceValue;
			if (result == IDialogConstants.YES_ID) {
				// Doesn't matter if it is replace or new window
				// as we are going to use the open perspective setting
				preferenceValue = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
			} else {
				preferenceValue = IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE;
			}

			// update PROJECT_OPEN_NEW_PERSPECTIVE to correspond
			PrefUtil.getAPIPreferenceStore().setValue(
					IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE,
					preferenceValue);
		}
		return result == IDialogConstants.YES_ID;
	}
	
	/*
	 * (non-Javadoc) Opens a new window with a particular perspective and input.
	 */
	private static void openInNewWindow(IPerspectiveDescriptor desc) {

		// Open the page.
		try {
			PlatformUI.getWorkbench().openWorkbenchWindow(desc.getId(),
					ResourcesPlugin.getWorkspace().getRoot());
		} catch (WorkbenchException e) {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window != null) {
				ErrorDialog.openError(window.getShell(), ResourceMessages.NewProject_errorOpeningWindow,
						e.getMessage(), e.getStatus());
			}
		}
	}
	
	/*
	 * (non-Javadoc) Replaces the current perspective with the new one.
	 */
	private static void replaceCurrentPerspective(IPerspectiveDescriptor persp) {

		// Get the active page.
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}

		// Set the perspective.
		page.setPerspective(persp);
	}

	public static void showReadyWizard(final List<ProjectExample> projects) {

		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		
		boolean showProjectReadyWizard = store.getBoolean(SHOW_PROJECT_READY_WIZARD);
		if (!showProjectReadyWizard) {
			boolean openWelcome = store.getBoolean(SHOW_README);
			if (openWelcome) {
				ProjectExamplesActivator.openWelcome(projects);
			}
			boolean showQuickFix = store.getBoolean(SHOW_QUICK_FIX);
			if (showQuickFix) {
				List<IMarker> markers = getMarkers(projects);
				if (markers != null && markers.size() > 0) {
				
					Display.getDefault().asyncExec(new Runnable() {
					
						@Override
						public void run() {
							Dialog dialog = new MarkerDialog(Display.getCurrent().getActiveShell(), projects);
							dialog.open();
						}
					});
				}
			}
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				IWizard wizard = new ProjectReadyWizard(projects);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
//				Dialog dialog = new MarkerDialog(shell, projects);
//				dialog.open();
			}

		});
	}
	
	public static void importProjectExamples(
			final List<ProjectExample> selectedProjects, IWorkingSet[] workingSets, Map<String, Object> propertiesMap) {
		final NewProjectExamplesJob workspaceJob = new NewProjectExamplesJob(
				Messages.NewProjectExamplesWizard_Downloading, selectedProjects, workingSets, propertiesMap);
		workspaceJob.setUser(true);
		workspaceJob.addJobChangeListener(new IJobChangeListener() {

			public void aboutToRun(IJobChangeEvent event) {

			}

			public void awake(IJobChangeEvent event) {

			}

			public void done(IJobChangeEvent event) {
				if (!workspaceJob.getResult().isOK()) {
					return;
				}
				List<ProjectExample> projects = workspaceJob.getProjects();
				try {
					ProjectExamplesActivator.updatePerspective(projects);
					ProjectExamplesActivator.waitForBuildAndValidation
							.schedule();
					ProjectExamplesActivator.waitForBuildAndValidation.join();
				} catch (InterruptedException e) {
					return;
				}
				if (projects != null && projects.size() > 0) {
					ProjectExamplesActivator.showReadyWizard(projects);
				}
				
			}

			public void running(IJobChangeEvent event) {

			}

			public void scheduled(IJobChangeEvent event) {

			}

			public void sleeping(IJobChangeEvent event) {

			}

		});

		workspaceJob.schedule();
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
		return PLUGIN_ID + SEPARATOR + imageDescriptor.hashCode(); //$NON-NLS-1$
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
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public Map<String, List<ContributedPage>> getContributedPages() {
		if (contributedPages == null) {
			contributedPages = new HashMap<String, List<ContributedPage>>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(WIZARDPAGES_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					String clazz = configurationElement.getAttribute(CLASS);
					String priorityString = configurationElement.getAttribute(PRIORITY);
					int priority = 0;
					if (priorityString != null) {
						try {
							priority = new Integer(priorityString);
						} catch (NumberFormatException e) {
							ProjectExamplesActivator.log(e);
						}
					}
					String type = configurationElement.getAttribute(TYPE);
					String pageType = configurationElement.getAttribute("pageType");
					ContributedPage contributedPage = new ContributedPage(configurationElement, type, pageType, priority, clazz);
					List<ContributedPage> contributions = contributedPages.get(type);
					if (contributions == null) {
						contributions = new ArrayList<ContributedPage>();
						contributedPages.put(type, contributions);
					}
					contributions.add(contributedPage);
					Collections.sort(contributions);
				}
			}
		}
		return contributedPages;
	}
	
	public IJBossCentralConfigurator getConfigurator() {
		if (configurator == null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(CONFIGURATORS_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions.length > 0) {
				IExtension extension = extensions[0];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					if (CONFIGURATOR.equals(configurationElement.getName())) {
						try {
							configurator = (IJBossCentralConfigurator) configurationElement
									.createExecutableExtension("class");
						} catch (CoreException e) {
							ProjectExamplesActivator.log(e);
							continue;
						}
						break;
					}
				}

			}
			if (configurator == null) {
				configurator = new DefaultJBossCentralConfigurator();
			}
		}
		return configurator;
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
	
	public static String getShortDescription(String description) {
		if (description.length() <= DESCRIPTION_LENGTH) {
			return description;
		}
		char[] chars = StringEscapeUtils.unescapeHtml(description.trim()).toCharArray();
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (char c:chars) {
			if (i++ < DESCRIPTION_LENGTH) {
				buffer.append(c);
			} else {
				if ( (c == '_') ||
					 (c >= 'a' && c <= 'z') ||
					 (c >= 'a' && c <= 'Z') ||
					 (c >= '0' && c <= '9') ) {
					buffer.append(c);
				} else {
					break;
				}
			}
		}
		if (buffer.length() > 0) {
			buffer.append("..."); //$NON-NLS-1$
		}
		return buffer.toString();
	}
}
