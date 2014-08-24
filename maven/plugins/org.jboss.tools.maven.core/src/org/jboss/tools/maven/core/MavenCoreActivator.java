/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.common.project.facet.core.internal.JavaFacetUtil;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperationConfig;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.jdt.IClasspathManager;
import org.eclipse.m2e.jdt.internal.BuildPathManager;
import org.eclipse.m2e.model.edit.pom.Dependency;
import org.eclipse.m2e.model.edit.pom.DependencyManagement;
import org.eclipse.m2e.model.edit.pom.PomFactory;
import org.eclipse.m2e.model.edit.pom.PropertyElement;
import org.eclipse.m2e.model.edit.pom.Repository;
import org.eclipse.m2e.model.edit.pom.util.PomResourceFactoryImpl;
import org.eclipse.m2e.model.edit.pom.util.PomResourceImpl;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.jboss.tools.maven.core.internal.resolution.ArtifactResolutionService;
import org.jboss.tools.maven.core.repositories.RemoteRepositoryManager;
import org.jboss.tools.maven.core.settings.MavenSettingsChangeListener;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenCoreActivator extends Plugin {

	private static final String ROOT_DIR = "/"; //$NON-NLS-1$

	public static final String SEPARATOR = "/"; //$NON-NLS-1$

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.core"; //$NON-NLS-1$

	public static final String OWNER_PROJECT_FACETS_ATTR = "owner.project.facets"; //$NON-NLS-1$
    
	public static final String BASEDIR = "${basedir}"; //$NON-NLS-1$
	
	public static final String ENCODING = "UTF-8"; //$NON-NLS-1$
	
	public static final List<LibraryProviderOperationConfig> libraryProviderOperationConfigs = new ArrayList<LibraryProviderOperationConfig>();

	private static final String DEFAULT_COMPILER_LEVEL = "1.5"; //$NON-NLS-1$

	private static final String DEFAULT_WEBCONTENT_ROOT = "src/main/webapp"; //$NON-NLS-1$
	
	// The shared instance
	private static MavenCoreActivator plugin;

	private static PomResourceImpl resource;

	private  IArtifactResolutionService artifactResolutionService;
	
	private RemoteRepositoryManager repositoryManager;

	//Concurrent HashSet
	private Set<MavenSettingsChangeListener> mavenSettingsListeners = Collections.newSetFromMap(new ConcurrentHashMap<MavenSettingsChangeListener, Boolean>());
	
	/**
	 * The constructor
	 */
	public MavenCoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		mavenSettingsListeners.clear();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		artifactResolutionService = null;
		plugin = null;
		mavenSettingsListeners.clear();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MavenCoreActivator getDefault() {
		return plugin;
	}

	public static IStatus getStatus(String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message);

	}
	
	public static IStatus getStatus(String message, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message,e);
	}
	
	public static IProject createMavenProject(String projectName, IProgressMonitor monitor, Model model, boolean force) throws CoreException {
		return createMavenProject(projectName, monitor, model, force, null);
	}
	
	public static IProject createMavenProject(String projectName, IProgressMonitor monitor, Model model, boolean force, IPath location) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (project.exists() && !force ) {
			return project;
		}
		if (!project.exists()) {
			if (location != null) {
				IPath workspacePath = project.getWorkspace().getRoot().getLocation();
				location = location.makeRelativeTo(workspacePath);
				if (projectName.equals(location.toString())) {
					project.create(monitor);
				} else {
					IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
					desc.setLocation(location);
					project.create(desc, monitor);
				}
			} else {
				project.create(monitor);
			}
			project.open(monitor);
		}
		addMavenCapabilities(project, monitor, model);
		return project;
	}
	
	public static void addMavenCapabilities(IProject project, IProgressMonitor monitor, Model model) throws CoreException {
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		if (!pom.exists() && model != null) {
			MavenModelManager modelManager = MavenPlugin.getMavenModelManager();
			modelManager.createMavenModel(pom, model);	
		}
		
		boolean hasMavenNature = MavenCoreActivator.addMavenNature(project, monitor);
		boolean hasJavaNature = project.hasNature(JavaCore.NATURE_ID);
		if (!hasMavenNature && hasJavaNature) {
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(
					MavenCoreActivator.OWNER_PROJECT_FACETS_ATTR,
					IJBossMavenConstants.M2_FACET_ID);
			IJavaProject javaProject = JavaCore.create(project);
			MavenCoreActivator.addClasspathAttribute(javaProject, attribute, monitor);
			MavenCoreActivator.updateMavenProjectConfiguration(project);
		}
	}

	public static boolean addMavenNature(IProject project,
			IProgressMonitor monitor) throws CoreException {
		boolean hasMavenNature = project.hasNature(IMavenConstants.NATURE_ID);
		if (!hasMavenNature) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
			newNatures[0] = IMavenConstants.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		}
		boolean hasJavaNature = project.hasNature(JavaCore.NATURE_ID);
		if (hasJavaNature) {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathContainer mavenContainer = BuildPathManager.getMaven2ClasspathContainer(javaProject);
			if (mavenContainer == null) {
				IPath path = new Path(BuildPathManager.CONTAINER_ID);
				setContainerPath(monitor, javaProject, path);
			}
		}
		return hasMavenNature;
	}

	public static String getSourceDirectory(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		IPath path = null;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				path = entries[i].getPath();
				break;
			}
		}
		if (path == null) {
			return null;
		}
		path = path.makeRelativeTo(javaProject.getPath());
		String value = path.toString();
		if (value.startsWith(SEPARATOR)) {
			return BASEDIR + path.toString();
		} else {
			return BASEDIR + SEPARATOR + path.toString();
		}
	}

	public static void addClasspathAttribute(IJavaProject javaProject,
			IClasspathAttribute attribute, IProgressMonitor monitor) throws JavaModelException {
		if (javaProject == null || !javaProject.exists()) {
			return;
		}

		IClasspathEntry[] cp = javaProject.getRawClasspath();
		for (int i = 0; i < cp.length; i++) {
			if (IClasspathEntry.CPE_CONTAINER == cp[i].getEntryKind()
					&& cp[i].getPath() != null && cp[i].getPath().segmentCount() > 0
					&& IClasspathManager.CONTAINER_ID.equals(cp[i].getPath().segment(0))) {
				LinkedHashMap<String, IClasspathAttribute> attrs = new LinkedHashMap<String, IClasspathAttribute>();
				for (IClasspathAttribute attr : cp[i].getExtraAttributes()) {
					attrs.put(attr.getName(), attr);
				}
				attrs.put(attribute.getName(), attribute);
				IClasspathAttribute[] newAttrs = attrs.values().toArray(
						new IClasspathAttribute[attrs.size()]);
				cp[i] = JavaCore.newContainerEntry(cp[i].getPath(), cp[i]
						.getAccessRules(), newAttrs, cp[i].isExported());
				break;
			}
		}
		javaProject.setRawClasspath(cp, monitor);
		
	}

	public static void setContainerPath(IProgressMonitor monitor,
			IJavaProject javaProject, IPath containerPath) throws CoreException {
		IClasspathEntry entry = JavaCore
				.newContainerEntry(containerPath, false);
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
		System.arraycopy(entries, 0, newEntries, 0, entries.length);
		newEntries[entries.length] = entry;
		javaProject.setRawClasspath(newEntries, monitor);
	}

	public static void updateMavenProjectConfiguration(IProject project)
			throws CoreException {
		IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();
		configurationManager.updateProjectConfiguration(project, new NullProgressMonitor());
	}
	
	public static void addMavenWarPlugin(Build build, IProject project, IProjectFacetVersion webFacetversion) throws JavaModelException {
		IVirtualComponent component = ComponentCore.createComponent(project);
		if (component == null) {
			return;
		}
		IVirtualFolder rootFolder = component.getRootFolder();
		IContainer root = rootFolder.getUnderlyingFolder();
		String webContentRoot = root.getProjectRelativePath().toString();
		boolean isDefaultWarSource = DEFAULT_WEBCONTENT_ROOT.equals(webContentRoot);
		boolean needsFailOnMissingWebXml = webFacetversion != null && JavaEEProjectUtilities.DYNAMIC_WEB_25.compareTo(webFacetversion) < 1;
		if (isDefaultWarSource && !needsFailOnMissingWebXml) {
			return;
		}
		
		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
		plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
		plugin.setArtifactId("maven-war-plugin"); //$NON-NLS-1$
		plugin.setVersion("2.3");//$NON-NLS-1$
		Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
		if (!isDefaultWarSource){
			Xpp3Dom warSourceDirectory = new Xpp3Dom("warSourceDirectory"); //$NON-NLS-1$
			if (webContentRoot.startsWith(SEPARATOR)) {
				warSourceDirectory.setValue(MavenCoreActivator.BASEDIR + webContentRoot);
			} else {
				warSourceDirectory.setValue(MavenCoreActivator.BASEDIR + SEPARATOR + webContentRoot);
			}
			configuration.addChild(warSourceDirectory); 
		}
		if (needsFailOnMissingWebXml) {
			Xpp3Dom failOnMissingWebXml = new Xpp3Dom("failOnMissingWebXml"); //$NON-NLS-1$
			failOnMissingWebXml.setValue("false");//$NON-NLS-1$
			configuration.addChild(failOnMissingWebXml); 
		}
		
		plugin.setConfiguration(configuration);
		build.getPlugins().add(plugin);
	}

	public static void addResource(Build build, IProject project, String sourceDirectory)
			throws JavaModelException {
		Resource resource = new Resource();
		if (sourceDirectory == null) {
			IJavaProject javaProject = JavaCore.create(project);
			if (javaProject != null && javaProject.exists()) {
				sourceDirectory = getSourceDirectory(javaProject);
			}
		}
		if (sourceDirectory != null) {
			resource.setDirectory(sourceDirectory);
			List<String> excludes = new ArrayList<String>();
			excludes.add("**/*.java"); //$NON-NLS-1$
			resource.setExcludes(excludes);
		}
		build.getResources().add(resource);
	}
	
	public static void addMavenEarPlugin(Build build, IProject project, IDataModel m2FacetModel, String ejbArtifactId, 
			IProjectFacetVersion earFacetVersion, boolean addSeamModules) throws JavaModelException {
		String sourceDirectory = getEarRoot(project);
		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
		plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
		plugin.setArtifactId("maven-ear-plugin"); //$NON-NLS-1$
		plugin.setVersion("2.8");//$NON-NLS-1$
		Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
		if (earFacetVersion != null) {
			String earVersion = earFacetVersion.getVersionString();
			if (earVersion.endsWith(".0")) {//$NON-NLS-1$
				//YYiikes
				earVersion = ""+Double.valueOf(earVersion).intValue();//$NON-NLS-1$
			}
			Xpp3Dom version = new Xpp3Dom("version"); //$NON-NLS-1$
			version.setValue(earVersion); 
			configuration.addChild(version);
		}
		Xpp3Dom defaultLibBundleDir = new Xpp3Dom("defaultLibBundleDir"); //$NON-NLS-1$
		defaultLibBundleDir.setValue("lib"); //$NON-NLS-1$
		configuration.addChild(defaultLibBundleDir);
		if(!"src/main/application".equals(sourceDirectory)) {//$NON-NLS-1$
			Xpp3Dom earSourceDirectory = new Xpp3Dom("earSourceDirectory"); //$NON-NLS-1$
			earSourceDirectory.setValue(sourceDirectory);
			configuration.addChild(earSourceDirectory);
		}
		
		if (addSeamModules) {
			Xpp3Dom modules = new Xpp3Dom("modules"); //$NON-NLS-1$
			configuration.addChild(modules);
			
			if (ejbArtifactId != null) {
				String ejbModuleName = ejbArtifactId + ".jar"; //$NON-NLS-1$
				Xpp3Dom ejbProject = getEarModule(
						"ejbModule", //$NON-NLS-1$
						m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID),
						ejbArtifactId, ROOT_DIR, ejbModuleName); 
				modules.addChild(ejbProject);
			}

			String webModuleName = m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID) + ".war"; //$NON-NLS-1$
			
			Xpp3Dom webProject = getEarModule(
					"webModule", //$NON-NLS-1$
					m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID),
					m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID),
					ROOT_DIR, webModuleName); 
			Xpp3Dom contextRoot = new Xpp3Dom("contextRoot"); //$NON-NLS-1$
			contextRoot.setValue(m2FacetModel
					.getStringProperty(IJBossMavenConstants.ARTIFACT_ID));
			webProject.addChild(contextRoot);
			modules.addChild(webProject);

			Xpp3Dom richFacesApi = getEarModule("jarModule", //$NON-NLS-1$
					"org.richfaces.framework", "richfaces-api", "/lib", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			modules.addChild(richFacesApi);

			Xpp3Dom commonDigester = getEarModule("jarModule", //$NON-NLS-1$
					"commons-digester", "commons-digester", "/lib", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			modules.addChild(commonDigester);
			
		}
		plugin.setConfiguration(configuration);
		
		build.getPlugins().add(plugin);
	}
	
	private static Xpp3Dom getEarModule(String module,
			String groupIdString,String artifactIdString, String bundleDirString, String bundleFileNameString) {
		Xpp3Dom earModule = new Xpp3Dom(module);
		//modules.addChild(earModule);
		Xpp3Dom groupId = new Xpp3Dom("groupId"); //$NON-NLS-1$
		groupId.setValue(groupIdString);
		earModule.addChild(groupId);
		Xpp3Dom artifactId = new Xpp3Dom("artifactId"); //$NON-NLS-1$
		artifactId.setValue(artifactIdString);
		earModule.addChild(artifactId);
		if (!ROOT_DIR.equals(bundleDirString)) {
			Xpp3Dom bundleDir = new Xpp3Dom("bundleDir"); //$NON-NLS-1$
			bundleDir.setValue(bundleDirString);
			earModule.addChild(bundleDir);
		}
		if (bundleFileNameString != null) {
			Xpp3Dom bundleFileName = new Xpp3Dom("bundleFileName"); //$NON-NLS-1$
			bundleFileName.setValue(bundleFileNameString);
			earModule.addChild(bundleFileName);
			
		}
		return earModule;
	}
	
	public static void addMavenEjbPlugin(Build build, IProject project, IProjectFacetVersion ejbFacetVersion) throws JavaModelException {
		if (ejbFacetVersion != null) {
			String version = ejbFacetVersion.getVersionString();
			if (!"2.1".equals(version)) { //$NON-NLS-1$
				org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
				plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
				plugin.setArtifactId("maven-ejb-plugin"); //$NON-NLS-1$
				plugin.setVersion("2.3"); //$NON-NLS-1$
				Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
				Xpp3Dom ejbVersion = new Xpp3Dom("ejbVersion"); //$NON-NLS-1$
				ejbVersion.setValue(version);
				configuration.addChild(ejbVersion); 
				plugin.setConfiguration(configuration);
				build.getPlugins().add(plugin);
			}
		}
	}
	
	public static String getOutputDirectory(IJavaProject javaProject) throws CoreException {
		IPath path = javaProject.getOutputLocation();
		path = path.makeRelativeTo(javaProject.getPath());
		if (path == null) {
			return null;
		}
		String value = path.toString();
		if (value.startsWith(SEPARATOR)) {
			return MavenCoreActivator.BASEDIR + path.toString();
		} else {
			return MavenCoreActivator.BASEDIR + SEPARATOR + path.toString();
		}
	}

	public static String getEarRoot(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		IVirtualFolder rootFolder = component.getRootFolder();
		IContainer root = rootFolder.getUnderlyingFolder();
		String sourceDirectory = root.getProjectRelativePath().toString();
		return sourceDirectory;
	}
	
	public static void mergeModel(org.eclipse.m2e.model.edit.pom.Model projectModel, org.eclipse.m2e.model.edit.pom.Model libraryModel) {
		if (projectModel == null || libraryModel == null) {
			return;
		}
		addProperties(projectModel,libraryModel);
		addRepositories(projectModel,libraryModel);
		addPlugins(projectModel,libraryModel);
		
		DependencyManagement depMgtProject = projectModel.getDependencyManagement();
		DependencyManagement depMgtLibrary = libraryModel.getDependencyManagement();
		if (depMgtLibrary != null && !depMgtLibrary.getDependencies().isEmpty()) {
			if (depMgtProject == null) {
				depMgtProject = PomFactory.eINSTANCE.createDependencyManagement();
				projectModel.setDependencyManagement(depMgtProject);
			}
			addDependencies(projectModel.getDependencyManagement().getDependencies(),libraryModel.getDependencyManagement().getDependencies());
		}
		//getDependencies() never returns null
		addDependencies(projectModel.getDependencies(),libraryModel.getDependencies());
	}

	private static void addDependencies(List<org.eclipse.m2e.model.edit.pom.Dependency> projectDependencies , List<org.eclipse.m2e.model.edit.pom.Dependency> libraryDependencies) {
		for (Dependency dependency:libraryDependencies) {
			if (!dependencyExists(dependency,projectDependencies)) {
				Dependency newDependency = (Dependency) EcoreUtil.copy(dependency);
				projectDependencies.add(newDependency);
			}
		}
		
	}

	private static boolean dependencyExists(Dependency dependency, List<Dependency> projectDependencies) {
		String groupId = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		if (artifactId == null) {
			return false;
		}
		for (Dependency projectDependency:projectDependencies) {
			String projectGroupId = projectDependency.getGroupId();
			String projectArtifactId = projectDependency.getArtifactId();
			if (!artifactId.equals(projectArtifactId)) {
				return false;
			}
			if (groupId == null && projectGroupId == null) {
				return true;
			}
			if (groupId != null && groupId.equals(projectGroupId)) {
				return true;
			}
		}
		return false;
	}

	private static void addPlugins(org.eclipse.m2e.model.edit.pom.Model projectModel, org.eclipse.m2e.model.edit.pom.Model libraryModel) {
		org.eclipse.m2e.model.edit.pom.Build libraryBuild = libraryModel.getBuild();
		if (libraryBuild == null) {
			return;
		}
		List<org.eclipse.m2e.model.edit.pom.Plugin> libraryPlugins = libraryModel.getBuild().getPlugins();
		for (org.eclipse.m2e.model.edit.pom.Plugin plugin:libraryPlugins) {
			org.eclipse.m2e.model.edit.pom.Build projectBuild = projectModel.getBuild();
			if (projectBuild == null) {
				projectBuild = PomFactory.eINSTANCE.createBuild();
		        projectModel.setBuild(projectBuild);
			}
			List<org.eclipse.m2e.model.edit.pom.Plugin> projectPlugins = projectBuild.getPlugins();
			if (!pluginExists(plugin,projectPlugins)) {
				org.eclipse.m2e.model.edit.pom.Plugin newPlugin = (org.eclipse.m2e.model.edit.pom.Plugin) EcoreUtil.copy(plugin);
				projectPlugins.add(newPlugin);
			}
		}
	}

	private static boolean pluginExists(org.eclipse.m2e.model.edit.pom.Plugin plugin, List<org.eclipse.m2e.model.edit.pom.Plugin> projectPlugins) {
		String groupId = plugin.getGroupId();
		String artifactId = plugin.getArtifactId();
		if (artifactId == null) {
			return false;
		}
		for (org.eclipse.m2e.model.edit.pom.Plugin projectPlugin:projectPlugins) {
			String projectGroupId = projectPlugin.getGroupId();
			String projectArtifactId = projectPlugin.getArtifactId();
			if (!artifactId.equals(projectArtifactId)) {
				return false;
			}
			if (groupId == null && projectGroupId == null) {
				return true;
			}
			if (groupId != null && groupId.equals(projectGroupId)) {
				return true;
			}
		}
		return false;
	}

	private static void addRepositories(org.eclipse.m2e.model.edit.pom.Model projectModel, org.eclipse.m2e.model.edit.pom.Model libraryModel) {
		List<Repository> projectRepositories = projectModel.getRepositories();
		List<Repository> libraryRepositories = libraryModel.getRepositories();
		for (Repository repository:libraryRepositories) {
			if (!repositoryExists(repository,projectRepositories)) {
				Repository newRepository = (Repository) EcoreUtil.copy(repository);
				projectRepositories.add(newRepository);
			}
		}
	}

	private static boolean repositoryExists(Repository repository,
			List<Repository> projectRepositories) {
		String url = repository.getUrl();
		if (url == null) {
			return false;
		}
		for(Repository projectRepository:projectRepositories) {
			if (url.equals(projectRepository.getUrl())) {
				return true;
			}
		}
		return false;
	}

	private static void addProperties(org.eclipse.m2e.model.edit.pom.Model projectModel, org.eclipse.m2e.model.edit.pom.Model libraryModel) {
		List<PropertyElement> projectProperties = projectModel.getProperties();
		List<PropertyElement> libraryProperties = libraryModel.getProperties();
		for (PropertyElement libraryProperty:libraryProperties) {
			String propertyName = libraryProperty.getName();
			if (!propertyExists(propertyName,projectProperties)) {
				PropertyElement newProperty = (PropertyElement) EcoreUtil.copy(libraryProperty);
				projectProperties.add(newProperty);
			}
		}
	}

	private static boolean propertyExists(String propertyName,
			List<PropertyElement> projectProperties) {
		if (propertyName == null) {
			return false;
		}
		for (PropertyElement propertyElement:projectProperties) {
			if (propertyName.equals(propertyElement.getName())) {
				return true;
			}
		}
		return false;
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}

	public static void log(String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message);
		getDefault().getLog().log(status);
	}
	
	public static File getProviderFile(ILibraryProvider provider) {
		String id = provider.getId();
		IPath providerDir = MavenCoreActivator.getDefault().getStateLocation().append(id);
		File providerDirFile = providerDir.toFile();
		providerDirFile.mkdir();
		File providerFile = new File(providerDirFile, "template.xml"); //$NON-NLS-1$
		return providerFile;
	}
	
	public static PomResourceImpl loadResource(URL url) throws CoreException {
		try {
			URI uri = URI.createURI(url.toString());
			if ( !( uri.isFile() || uri.isPlatformResource()) ) {
				// the m2eclipse pom model can read only a file URL or platform resource URL
				// see https://jira.jboss.org/jira/browse/JBIDE-4972
				InputStream is = null;
		        OutputStream os = null;
		        try {
					File temp = File.createTempFile("mavenCoreActivator", ".pom");  //$NON-NLS-1$//$NON-NLS-2$
					temp.deleteOnExit();
					os = new FileOutputStream(temp);
					is = url.openStream();
					copy(is,os);
					URL tempURL = temp.toURL();
					uri = URI.createURI(tempURL.toString());
				} catch (Exception e) {
					log(e);
					throw new CoreException(new Status(IStatus.ERROR,
							PLUGIN_ID, -1, e.getMessage(), e));
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (Exception ignore) {}
					}
					if (os != null) {
						try {
							os.close();
						} catch (Exception ignore) {}
					}
				}
			}
			org.eclipse.emf.ecore.resource.Resource resource = new PomResourceFactoryImpl()
					.createResource(uri);
			resource.load(Collections.EMPTY_MAP);
			return (PomResourceImpl) resource;
		} catch (Exception ex) {
			log(ex);
			throw new CoreException(new Status(IStatus.ERROR,
					PLUGIN_ID, -1, ex.getMessage(), ex));
		}
	}

	public static void copy(InputStream is, OutputStream os)
			throws IOException {
		byte[] buffer = new byte[1024];
		int count;
		while ( (count = is.read(buffer)) > 0) {
			os.write(buffer,0,count);
		}
	}

	public static void addLibraryProviderOperationConfig(LibraryProviderOperationConfig config) {
		libraryProviderOperationConfigs.add(config);
	}

	public static List<LibraryProviderOperationConfig> getLibraryProviderOperationConfigs() {
		return libraryProviderOperationConfigs;
	}

	public static PomResourceImpl getResource() {
		return resource;
	}

	public static void setResource(PomResourceImpl resource2) {
		resource = resource2;
	}

	public static void addCompilerPlugin(List<org.apache.maven.model.Plugin> plugins, IProject project) {
		String compilerLevel = JavaFacetUtil.getCompilerLevel(project);
		if (compilerLevel == null || DEFAULT_COMPILER_LEVEL.equals(compilerLevel)) {
			return;
		}
		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
		plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
		plugin.setArtifactId("maven-compiler-plugin"); //$NON-NLS-1$
		plugin.setVersion("3.0");
		Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
		Xpp3Dom source = new Xpp3Dom("source"); //$NON-NLS-1$
		source.setValue(compilerLevel); //$NON-NLS-1$
		configuration.addChild(source);
		Xpp3Dom target = new Xpp3Dom("target"); //$NON-NLS-1$
		target.setValue(compilerLevel); //$NON-NLS-1$
		configuration.addChild(target);
		plugin.setConfiguration(configuration);
		plugins.add(plugin);
	}
	
	public static PomResourceImpl loadResource(IFile pomFile)
			throws CoreException {
		String path = pomFile.getFullPath().toOSString();
		URI uri = URI.createPlatformResourceURI(path, true);
		try {
			org.eclipse.emf.ecore.resource.Resource pomResource = new PomResourceFactoryImpl()
					.createResource(uri);
			pomResource.load(new HashMap());
			return (PomResourceImpl) pomResource;
		} catch (Exception ex) {
			String msg = "Can't load model " + pomFile;
			log(ex);
			throw new CoreException(new Status(IStatus.ERROR,
					IMavenConstants.PLUGIN_ID, -1, msg, ex));
		}
	}
        
	
	public RemoteRepositoryManager getRepositoryManager() {
		if (repositoryManager == null) {
			repositoryManager = new RemoteRepositoryManager();
		}
		return repositoryManager;
	}
	
	public static IEclipsePreferences getPreferences() {
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
	
	public void notifyMavenSettingsChanged() {
		for (MavenSettingsChangeListener mscl : mavenSettingsListeners) {
			try {
				mscl.onSettingsChanged();
			} catch(Exception e) {
				log(e);
			}
		}
	}
	
	public void registerMavenSettingsChangeListener(MavenSettingsChangeListener listener) {
		mavenSettingsListeners.add(listener);
	}
	
	public void unregisterMavenSettingsChangeListener(MavenSettingsChangeListener listener) {
		mavenSettingsListeners.remove(listener);
	}

	/**
	 * @since 1.5.2
	 */
	public IArtifactResolutionService getArtifactResolutionService() {
		if (artifactResolutionService == null) {
			artifactResolutionService = new ArtifactResolutionService();
		}
		return artifactResolutionService;
	}
}
