package org.jboss.tools.maven.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.common.project.facet.JavaFacetUtils;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperationConfig;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.maven.ide.components.pom.Dependency;
import org.maven.ide.components.pom.PomFactory;
import org.maven.ide.components.pom.PropertyElement;
import org.maven.ide.components.pom.Repository;
import org.maven.ide.components.pom.util.PomResourceFactoryImpl;
import org.maven.ide.components.pom.util.PomResourceImpl;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.embedder.IMavenConfiguration;
import org.maven.ide.eclipse.embedder.MavenModelManager;
import org.maven.ide.eclipse.jdt.BuildPathManager;
import org.maven.ide.eclipse.project.IProjectConfigurationManager;
import org.maven.ide.eclipse.project.ResolverConfiguration;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenCoreActivator extends Plugin {

	private static final String SEPARATOR = "/"; //$NON-NLS-1$

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.core"; //$NON-NLS-1$

	public static final String OWNER_PROJECT_FACETS_ATTR = "owner.project.facets"; //$NON-NLS-1$
    
	public static final String BASEDIR = "${basedir}"; //$NON-NLS-1$
	
	public static final String ENCODING = "UTF-8"; //$NON-NLS-1$
	
	public static final List<LibraryProviderOperationConfig> libraryProviderOperationConfigs = new ArrayList<LibraryProviderOperationConfig>();
	
	// The shared instance
	private static MavenCoreActivator plugin;

	private static PomResourceImpl resource;
	
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
		IJavaProject javaProject = JavaCore.create(project);
		IProjectDescription description = project.getDescription();
		String[] natureIds = description.getNatureIds();
		boolean hasJavaNature = false;
		for (int i = 0; i < natureIds.length; i++) {
			if (JavaCore.NATURE_ID.equals(natureIds[i])) {
				hasJavaNature = true;
				break;
			}
		}
		if (!hasJavaNature) {
			// EAR project
			createFolder("target",monitor, project); //$NON-NLS-1$
			IFolder binFolder = createFolder("target/classes",monitor, project);  //$NON-NLS-1$
			String[] newNatureIds = new String[natureIds.length + 1];
			for (int i = 0; i < natureIds.length; i++) {
				newNatureIds[i]=natureIds[i];
			}
			newNatureIds[natureIds.length] = JavaCore.NATURE_ID;
			description.setNatureIds(newNatureIds);
			project.setDescription(description, monitor);
			javaProject.setRawClasspath(new IClasspathEntry[0], monitor);
			javaProject.setOutputLocation(binFolder.getFullPath(), monitor);
			IClasspathEntry entry = JavaRuntime.getDefaultJREContainerEntry();
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
			System.arraycopy(entries, 0, newEntries, 0, entries.length);
			newEntries[entries.length] = entry;
			javaProject.setRawClasspath(newEntries, monitor);
		}
		if (FacetedProjectFramework.hasProjectFacet(project, IJ2EEFacetConstants.ENTERPRISE_APPLICATION)) {
			String sourceDirectory = getSourceDirectory(javaProject);
			if (sourceDirectory == null || sourceDirectory.trim().length() <= 0) {
				IVirtualComponent component = ComponentCore.createComponent(project);
				IVirtualFolder rootVFolder = component.getRootFolder();
				IContainer rootFolder = rootVFolder.getUnderlyingFolder();
				IPath path = rootFolder.getFullPath();
				IClasspathEntry[] entries = javaProject.getRawClasspath();
				IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
				System.arraycopy(entries, 0, newEntries, 0, entries.length);
				newEntries[entries.length] = JavaCore.newSourceEntry(path);
				javaProject.setRawClasspath(newEntries, monitor);
			}
		}
		addMavenCapabilities(project, monitor, model);
		return project;
	}
	
	public static void addMavenCapabilities(IProject project, IProgressMonitor monitor, Model model) throws CoreException {
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		if (!pom.exists() && model != null) {
			MavenModelManager modelManager = MavenPlugin.getDefault().getMavenModelManager();
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

	private static IFolder createFolder(String folderName,IProgressMonitor monitor,
			IProject project) throws CoreException {
		IFolder folder = project.getFolder(folderName);
		folder.create(false, true, monitor);
		return folder;
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
			IClasspathContainer mavenContainer = BuildPathManager
					.getMaven2ClasspathContainer(javaProject);
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
		IClasspathEntry[] cp = javaProject.getRawClasspath();
		for (int i = 0; i < cp.length; i++) {
			if (IClasspathEntry.CPE_CONTAINER == cp[i].getEntryKind()
					&& BuildPathManager.isMaven2ClasspathContainer(cp[i]
							.getPath())) {
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
		ResolverConfiguration resolverConfiguration = new ResolverConfiguration();
		resolverConfiguration.setIncludeModules(false);
		// FIXME
		resolverConfiguration.setResolveWorkspaceProjects(true);
		resolverConfiguration.setActiveProfiles(""); //$NON-NLS-1$
		IProjectConfigurationManager configurationManager = MavenPlugin
				.getDefault().getProjectConfigurationManager();
		IMavenConfiguration mavenConfiguration = MavenPlugin.lookup(IMavenConfiguration.class);
		configurationManager.updateProjectConfiguration(project,
				resolverConfiguration, //
				mavenConfiguration
						.getGoalOnUpdate(), new NullProgressMonitor());
	}
	
	public static void addMavenWarPlugin(Build build, IProject project) throws JavaModelException {
		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
		plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
		plugin.setArtifactId("maven-war-plugin"); //$NON-NLS-1$
		
		Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
		Xpp3Dom webappDirectory = new Xpp3Dom("webappDirectory"); //$NON-NLS-1$
		IVirtualComponent component = ComponentCore.createComponent(project);
		IVirtualFolder rootFolder = component.getRootFolder();
		IContainer root = rootFolder.getUnderlyingFolder();
		String webContentRoot = root.getProjectRelativePath().toString();
		configuration.addChild(webappDirectory); 
		Xpp3Dom warSourceDirectory = new Xpp3Dom("warSourceDirectory"); //$NON-NLS-1$
		if (webContentRoot.startsWith(SEPARATOR)) {
			webappDirectory.setValue(MavenCoreActivator.BASEDIR + webContentRoot);
			warSourceDirectory.setValue(MavenCoreActivator.BASEDIR + webContentRoot);
		} else {
			webappDirectory.setValue(MavenCoreActivator.BASEDIR + SEPARATOR + webContentRoot);
			warSourceDirectory.setValue(MavenCoreActivator.BASEDIR + SEPARATOR + webContentRoot);
		}
		
		configuration.addChild(warSourceDirectory); 
		plugin.setConfiguration(configuration);
		build.getPlugins().add(plugin);

		addResource(build, project, null);
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
	
	public static void addMavenEarPlugin(Build build, IProject project, IDataModel m2FacetModel, boolean addModule) throws JavaModelException {
		String sourceDirectory = getEarRoot(project);
		build.setSourceDirectory(sourceDirectory);
		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
		plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
		plugin.setArtifactId("maven-ear-plugin"); //$NON-NLS-1$
		
		Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
		Xpp3Dom version = new Xpp3Dom("version"); //$NON-NLS-1$
		version.setValue("5"); //$NON-NLS-1$
		configuration.addChild(version);
		Xpp3Dom generateApplicationXml = new Xpp3Dom("generateApplicationXml"); //$NON-NLS-1$
		generateApplicationXml.setValue("false"); //$NON-NLS-1$
		configuration.addChild(generateApplicationXml);
		Xpp3Dom defaultLibBundleDir = new Xpp3Dom("defaultLibBundleDir"); //$NON-NLS-1$
		defaultLibBundleDir.setValue("lib"); //$NON-NLS-1$
		configuration.addChild(defaultLibBundleDir);
		Xpp3Dom earSourceDirectory = new Xpp3Dom("earSourceDirectory"); //$NON-NLS-1$
		earSourceDirectory.setValue(sourceDirectory);
		configuration.addChild(earSourceDirectory);
		
		if (addModule) {
			Xpp3Dom modules = new Xpp3Dom("modules"); //$NON-NLS-1$
			configuration.addChild(modules);

			String ejbModuleName = m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID) + "-ejb.jar"; //$NON-NLS-1$
			Xpp3Dom ejbProject = getEarModule(
					"ejbModule", //$NON-NLS-1$
					m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID),
					m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID)
							+ "-ejb", "/", ejbModuleName ); //$NON-NLS-1$ //$NON-NLS-2$
			modules.addChild(ejbProject);

			Xpp3Dom seamModule = getEarModule("ejbModule", "org.jboss.seam", //$NON-NLS-1$ //$NON-NLS-2$
					"jboss-seam", "/", null); //$NON-NLS-1$ //$NON-NLS-2$
			modules.addChild(seamModule);

			String webModuleName = m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID) + ".war"; //$NON-NLS-1$
			
			Xpp3Dom webProject = getEarModule(
					"webModule", //$NON-NLS-1$
					m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID),
					m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID),
					"/", webModuleName); //$NON-NLS-1$
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
			
			//Xpp3Dom mvel14 = getEarModule("jarModule", //$NON-NLS-1$
			//		"org.mvel", "mvel14", "/", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//modules.addChild(mvel14);
		}
		plugin.setConfiguration(configuration);
		
		build.getPlugins().add(plugin);
	
		addResource(build, project, sourceDirectory);
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
		Xpp3Dom bundleDir = new Xpp3Dom("bundleDir"); //$NON-NLS-1$
		bundleDir.setValue(bundleDirString);
		earModule.addChild(bundleDir);
		if (bundleFileNameString != null) {
			Xpp3Dom bundleFileName = new Xpp3Dom("bundleFileName"); //$NON-NLS-1$
			bundleFileName.setValue(bundleFileNameString);
			earModule.addChild(bundleFileName);
			
		}
		return earModule;
	}
	
	public static void addMavenEjbPlugin(Build build, IProject project) throws JavaModelException {
		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
		plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
		plugin.setArtifactId("maven-ejb-plugin"); //$NON-NLS-1$
		plugin.setInherited("true"); //$NON-NLS-1$
		
		Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
		Xpp3Dom ejbVersion = new Xpp3Dom("ejbVersion"); //$NON-NLS-1$
		ejbVersion.setValue("3.0"); //$NON-NLS-1$
		configuration.addChild(ejbVersion); 
		plugin.setConfiguration(configuration);
		build.getPlugins().add(plugin);
	
		addResource(build, project, null);
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
	
	public static void mergeModel(org.maven.ide.components.pom.Model projectModel, org.maven.ide.components.pom.Model libraryModel) {
		if (projectModel == null || libraryModel == null) {
			return;
		}
		addProperties(projectModel,libraryModel);
		addRepositories(projectModel,libraryModel);
		addPlugins(projectModel,libraryModel);
		addDependencies(projectModel,libraryModel);
	}

	private static void addDependencies(org.maven.ide.components.pom.Model projectModel, org.maven.ide.components.pom.Model libraryModel) {
		List<org.maven.ide.components.pom.Dependency> projectDependencies = projectModel.getDependencies();
		List<org.maven.ide.components.pom.Dependency> libraryDependencies = libraryModel.getDependencies();
		for (Dependency dependency:libraryDependencies) {
			if (!dependencyExists(dependency,projectDependencies)) {
				Dependency newDependency = (Dependency) EcoreUtil.copy(dependency);
				projectDependencies.add(newDependency);
			}
		}
		
	}

	private static boolean dependencyExists(Dependency dependency,
			List<Dependency> projectDependencies) {
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

	private static void addPlugins(org.maven.ide.components.pom.Model projectModel, org.maven.ide.components.pom.Model libraryModel) {
		org.maven.ide.components.pom.Build libraryBuild = libraryModel.getBuild();
		if (libraryBuild == null) {
			return;
		}
		List<org.maven.ide.components.pom.Plugin> libraryPlugins = projectModel.getBuild().getPlugins();
		for (org.maven.ide.components.pom.Plugin plugin:libraryPlugins) {
			org.maven.ide.components.pom.Build projectBuild = projectModel.getBuild();
			if (projectBuild == null) {
				projectBuild = PomFactory.eINSTANCE.createBuild();
		        projectModel.setBuild(projectBuild);
			}
			List<org.maven.ide.components.pom.Plugin> projectPlugins = projectBuild.getPlugins();
			if (!pluginExists(plugin,projectPlugins)) {
				org.maven.ide.components.pom.Plugin newPlugin = (org.maven.ide.components.pom.Plugin) EcoreUtil.copy(plugin);
				projectPlugins.add(newPlugin);
			}
		}
	}

	private static boolean pluginExists(org.maven.ide.components.pom.Plugin plugin, List<org.maven.ide.components.pom.Plugin> projectPlugins) {
		String groupId = plugin.getGroupId();
		String artifactId = plugin.getArtifactId();
		if (artifactId == null) {
			return false;
		}
		for (org.maven.ide.components.pom.Plugin projectPlugin:projectPlugins) {
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

	private static void addRepositories(org.maven.ide.components.pom.Model projectModel, org.maven.ide.components.pom.Model libraryModel) {
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

	private static void addProperties(org.maven.ide.components.pom.Model projectModel, org.maven.ide.components.pom.Model libraryModel) {
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

	private static void copy(InputStream is, OutputStream os)
			throws IOException {
		byte[] buffer = new byte[1024];
		int count;
		while ( (count = is.read(buffer)) > 0) {
			os.write(buffer,0,count);
		}
	}

	public static void addLibraryProviderOperationConfig(
			LibraryProviderOperationConfig config) {
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

	public static void addCompilerPlugin(Build build, IProject project) {
		String compilerLevel = JavaFacetUtils.getCompilerLevel(project);
		if (compilerLevel == null) {
			return;
		}
		org.apache.maven.model.Plugin plugin = new org.apache.maven.model.Plugin();
		plugin.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
		plugin.setArtifactId("maven-compiler-plugin"); //$NON-NLS-1$
		Xpp3Dom configuration = new Xpp3Dom( "configuration" ); //$NON-NLS-1$
		Xpp3Dom source = new Xpp3Dom("source"); //$NON-NLS-1$
		source.setValue(compilerLevel); //$NON-NLS-1$
		configuration.addChild(source);
		Xpp3Dom target = new Xpp3Dom("target"); //$NON-NLS-1$
		target.setValue(compilerLevel); //$NON-NLS-1$
		configuration.addChild(target);
		plugin.setConfiguration(configuration);
		build.getPlugins().add(plugin);
	}
}
