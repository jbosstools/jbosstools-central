package org.jboss.tools.maven.core;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
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
	
	// The shared instance
	private static MavenCoreActivator plugin;
	
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
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (project.exists() && !force ) {
			return project;
		}
		if (!project.exists()) {
			project.create(monitor);
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
		
		if (!hasMavenNature) {
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
		IJavaProject javaProject = JavaCore.create(project);
		IPath path = new Path(BuildPathManager.CONTAINER_ID);
		setContainerPath(monitor, javaProject, path);
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
		configurationManager.updateProjectConfiguration(project,
				resolverConfiguration, //
				MavenPlugin.getDefault().getMavenRuntimeManager()
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

			Xpp3Dom ejbProject = getEarModule(
					"ejbModule", //$NON-NLS-1$
					m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID),
					m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID)
							+ "-ejb", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			modules.addChild(ejbProject);

			Xpp3Dom seamModule = getEarModule("ejbModule", "org.jboss.seam", //$NON-NLS-1$ //$NON-NLS-2$
					"jboss-seam", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			modules.addChild(seamModule);

			Xpp3Dom webProject = getEarModule(
					"webModule", //$NON-NLS-1$
					m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID),
					m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID),
					"/"); //$NON-NLS-1$
			Xpp3Dom contextRoot = new Xpp3Dom("contextRoot"); //$NON-NLS-1$
			contextRoot.setValue(m2FacetModel
					.getStringProperty(IJBossMavenConstants.ARTIFACT_ID));
			webProject.addChild(contextRoot);
			modules.addChild(webProject);

			Xpp3Dom richFacesApi = getEarModule("jarModule", //$NON-NLS-1$
					"org.richfaces.framework", "richfaces-api", "/lib"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			modules.addChild(richFacesApi);

			Xpp3Dom commonDigester = getEarModule("jarModule", //$NON-NLS-1$
					"commons-digester", "commons-digester", "/lib"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			modules.addChild(commonDigester);
		}
		plugin.setConfiguration(configuration);
		
		build.getPlugins().add(plugin);
	
		addResource(build, project, sourceDirectory);
	}
	
	private static Xpp3Dom getEarModule(String module,
			String groupIdString,String artifactIdString, String bundleDirString) {
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
}
