package org.jboss.tools.maven.seam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.j2ee.application.Application;
import org.eclipse.jst.j2ee.application.EjbModule;
import org.eclipse.jst.j2ee.application.WebModule;
import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.jboss.tools.seam.internal.core.project.facet.SeamFacetAbstractInstallDelegate;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.embedder.MavenModelManager;
import org.maven.ide.eclipse.project.MavenProjectManager;
import org.maven.ide.eclipse.project.ResolverConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenSeamActivator extends AbstractUIPlugin {

	private static final String WAR_ARCHIVE_SUFFIX = ".war";
	
	private static final String EJB_ARCHIVE_SUFFIX = ".jar";

	private static final String TEST_SUFFIX = "-test";

	private static final String EJB_SUFFIX = "-ejb";

	private static final String EAR_SUFFIX = "-ear";

	private static final String PARENT_SUFFIX = "-parent";

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.seam";

	public static final String CONFIGURE_SEAM = "configureSeam"; //$NON-NLS-1$

	public static final boolean CONFIGURE_SEAM_VALUE = true;
  
	// The shared instance
	private static MavenSeamActivator plugin;

	private String webProjectName;
	private String artifactId;

	private String parentProjectName;
	private String parentArtifactId;

	private String earProjectName;
	private String earArtifactId;

	private String ejbProjectName;
	private String ejbArtifactId;

	private String testProjectName;
	private String testArtifactId;

	/**
	 * The constructor
	 */
	public MavenSeamActivator() {
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
	public static MavenSeamActivator getDefault() {
		return plugin;
	}

	
	public void configureSeamProject(IDataModel seamFacetModel,
			IDataModel m2FacetModel) {
		Assert.isNotNull(seamFacetModel);
		Assert.isNotNull(m2FacetModel);
		webProjectName = seamFacetModel.getStringProperty(IFacetDataModelProperties.FACET_PROJECT_NAME);
		artifactId = m2FacetModel.getStringProperty(IJBossMavenConstants.ARTIFACT_ID);
		parentProjectName = webProjectName + PARENT_SUFFIX;
		parentArtifactId = artifactId + PARENT_SUFFIX;
		testProjectName = webProjectName + TEST_SUFFIX;
		testArtifactId = artifactId + TEST_SUFFIX;
		earProjectName = webProjectName + EAR_SUFFIX;
		earArtifactId = artifactId + EAR_SUFFIX;
		ejbProjectName = webProjectName + EJB_SUFFIX;
		ejbArtifactId = artifactId + EJB_SUFFIX;
		configureParentProject(m2FacetModel, seamFacetModel);
		configureWarProject(m2FacetModel, seamFacetModel);
		configureTestProject(m2FacetModel, seamFacetModel);
		if (!SeamFacetAbstractInstallDelegate
				.isWarConfiguration(seamFacetModel)) {
			configureEjbProject(m2FacetModel, seamFacetModel);
			configureEarProject(m2FacetModel, seamFacetModel);
			
		} 
	}

	private void configureTestProject(IDataModel m2FacetModel,
			IDataModel seamFacetModel) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(testProjectName);
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		IJavaProject javaProject = JavaCore.create(project);
		if (!pom.exists()) {
			Model model = new Model();
			model.setModelVersion(IJBossMavenConstants.MAVEN_MODEL_VERSION);
			model.setGroupId(m2FacetModel
					.getStringProperty(IJBossMavenConstants.GROUP_ID));
			model.setArtifactId(testArtifactId);
			model.setVersion(m2FacetModel
					.getStringProperty(IJBossMavenConstants.VERSION));
			model.setName(m2FacetModel.getStringProperty(IJBossMavenConstants.NAME) + " - test");
			model.setPackaging("jar");
			model.setDescription(m2FacetModel
					.getStringProperty(IJBossMavenConstants.DESCRIPTION));
			
			Parent parent = new Parent();
			parent.setArtifactId(parentArtifactId);
			parent.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			parent.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			model.setParent(parent);
			
			List dependencies = model.getDependencies();
			
			Dependency dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam.embedded");
			dependency.setArtifactId("hibernate-all");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam.embedded");
			dependency.setArtifactId("jboss-embedded-all");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam.embedded");
			dependency.setArtifactId("thirdparty-all");
			dependencies.add(dependency);
			
			dependency = getSeamDependency();
			dependency.setScope("test");
			dependencies.add(dependency);
			
			dependency = getJSFApi();
			dependency.setScope("test");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.activation");
			dependency.setArtifactId("activation");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.testng");
			dependency.setArtifactId("testng");
			// FIXME
			dependency.setVersion("${testng.version}");
			dependency.setClassifier("jdk15");
			dependency.setScope("test");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.slf4j");
			dependency.setArtifactId("slf4j-api");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.slf4j");
			dependency.setArtifactId("slf4j-nop");
			dependencies.add(dependency);
			
			Build build = new Build();
			try {
				//build.setFinalName(testProjectName);
				String sourceDirectory = MavenCoreActivator.getSourceDirectory(javaProject);
				if (sourceDirectory != null) {
					build.setSourceDirectory(sourceDirectory);
				}		
				String outputDirectory = MavenCoreActivator.getOutputDirectory(javaProject);	
				build.setOutputDirectory(outputDirectory);
				MavenCoreActivator.addResource(build, project, sourceDirectory);
				model.setBuild(build);
				MavenCoreActivator.createMavenProject(testProjectName, null, model, true);
			} catch (Exception e) {
				MavenSeamActivator.log(e);
			}
			
		}
		
	}

	private void configureEarProject(IDataModel m2FacetModel,
			IDataModel seamFacetModel) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(earProjectName);
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		//IJavaProject javaProject = JavaCore.create(project);
		if (!pom.exists()) {
			Model model = new Model();
			model.setModelVersion(IJBossMavenConstants.MAVEN_MODEL_VERSION);
			model.setGroupId(m2FacetModel
					.getStringProperty(IJBossMavenConstants.GROUP_ID));
			model.setArtifactId(earArtifactId);
			model.setVersion(m2FacetModel
					.getStringProperty(IJBossMavenConstants.VERSION));
			model.setName(m2FacetModel.getStringProperty(IJBossMavenConstants.NAME) + " - EAR");
			model.setPackaging("ear");
			model.setDescription(m2FacetModel
					.getStringProperty(IJBossMavenConstants.DESCRIPTION));
			
			Parent parent = new Parent();
			parent.setArtifactId(parentArtifactId);
			parent.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			parent.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			model.setParent(parent);
			
			List dependencies = model.getDependencies();
			
			Dependency dependency = new Dependency();
			dependency.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			dependency.setArtifactId(ejbProjectName);
			dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			dependency.setType("ejb");
			dependency.setScope("compile");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			dependency.setArtifactId(webProjectName);
			dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			dependency.setType("war");
			dependency.setScope("compile");
			dependencies.add(dependency);
			
			dependency = getSeamDependency();
			dependency.setVersion("${seam.version}");
			dependency.setType("ejb");
			dependency.setScope("compile");
			List exclusions = dependency.getExclusions();
			Exclusion exclusion = new Exclusion();
			exclusion.setGroupId("javassist");
			exclusion.setArtifactId("javassist");
			exclusions.add(exclusion);
			
			exclusion = new Exclusion();
			exclusion.setGroupId("javax.el");
			exclusion.setArtifactId("el-api");
			exclusions.add(exclusion);
			
			exclusion = new Exclusion();
			exclusion.setGroupId("dom4j");
			exclusion.setArtifactId("dom4j");
			exclusions.add(exclusion);
			
			dependencies.add(dependency);
			
			dependency = getRichFacesApi();
			dependency.setType("jar");
			dependency.setScope("compile");
			exclusions = dependency.getExclusions();
			exclusion = new Exclusion();
			exclusion.setGroupId("commons-collections");
			exclusion.setArtifactId("commons-collections");
			exclusions.add(exclusion);
			exclusion = new Exclusion();
			exclusion.setGroupId("commons-logging");
			exclusion.setArtifactId("commons-logging");
			exclusions.add(exclusion);
			
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.drools");
			dependency.setArtifactId("drools-compiler");
			dependency.setType("jar");
			dependency.setScope("compile");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.mvel");
			dependency.setArtifactId("mvel14");
			dependency.setType("jar");
			dependency.setScope("compile");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jbpm");
			dependency.setArtifactId("jbpm-jpdl");
			dependency.setType("jar");
			dependency.setScope("compile");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("commons-digester");
			dependency.setArtifactId("commons-digester");
			dependencies.add(dependency);
			
			Build build = new Build();
			try {
				build.setFinalName(earProjectName);
				
				String sourceDirectory = MavenCoreActivator.getEarRoot(project);
				if (sourceDirectory != null) {
					build.setSourceDirectory(sourceDirectory);
				}
				build.setOutputDirectory("target/classes");
				MavenCoreActivator.addMavenEarPlugin(build, project, m2FacetModel, true);
				model.setBuild(build);
				MavenCoreActivator.createMavenProject(earProjectName, null, model, true);
				removeWTPContainers(m2FacetModel, project);
				configureApplicationXml(project, m2FacetModel, null);
				//removeRuntime(project);
				//IProject ejbProject = ResourcesPlugin.getWorkspace().getRoot().getProject(ejbProjectName);
				//removeRuntime(ejbProject);
				//EarFacetRuntimeHandler.updateModuleProjectRuntime(project, ejbProject, null);
				//IProject webProject = ResourcesPlugin.getWorkspace().getRoot().getProject(webProjectName);
				//removeRuntime(webProject);
				//EarFacetRuntimeHandler.updateModuleProjectRuntime(project, webProject, null);
			} catch (Exception e) {
				MavenSeamActivator.log(e);
			}
			
		}
	}
	
	protected void configureApplicationXml(IProject project, IDataModel m2FacetModel, IProgressMonitor monitor) {
		EARArtifactEdit earArtifactEdit = null;
		try {
			earArtifactEdit = EARArtifactEdit.getEARArtifactEditForWrite(project);
			if(earArtifactEdit!=null) {
				Application application = earArtifactEdit.getApplication();
				EList modules = application.getModules();
				for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
					Object module = (Object) iterator.next();
					if (module instanceof WebModule) {
						WebModule webModule = (WebModule) module;
						String uri = webModule.getUri();
						String value = webProjectName + WAR_ARCHIVE_SUFFIX;
						if (value.equals(uri)) {
							String newUri = artifactId + "-" + m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION) + WAR_ARCHIVE_SUFFIX;
							webModule.setUri(newUri);
						}
					}
					if (module instanceof EjbModule) {
						EjbModule ejbModule = (EjbModule) module;
						String uri = ejbModule.getUri();
						String value = ejbProjectName + EJB_ARCHIVE_SUFFIX;
						if (value.equals(uri)) {
							String newUri = ejbArtifactId + "-" + m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION) + EJB_ARCHIVE_SUFFIX;
							ejbModule.setUri(newUri);
						}
					}
				}
				
				earArtifactEdit.save(monitor);
			}
		} finally {
			if(earArtifactEdit!=null) {
				earArtifactEdit.dispose();
			}
		}
	}
	private void removeRuntime(IProject project) throws CoreException {
		IFacetedProject facetedProject = ProjectFacetsManager.create( project );
		facetedProject.setRuntime(null, null);
	
	}

	private void configureEjbProject(IDataModel m2FacetModel,
			IDataModel seamFacetModel) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(ejbProjectName);
		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		IJavaProject javaProject = JavaCore.create(project);
		if (!pom.exists()) {
			Model model = new Model();
			model.setModelVersion(IJBossMavenConstants.MAVEN_MODEL_VERSION);
			model.setGroupId(m2FacetModel
					.getStringProperty(IJBossMavenConstants.GROUP_ID));
			model.setArtifactId(ejbArtifactId);
			model.setVersion(m2FacetModel
					.getStringProperty(IJBossMavenConstants.VERSION));
			model.setName(m2FacetModel.getStringProperty(IJBossMavenConstants.NAME) + " - EJB");
			model.setPackaging("ejb");
			model.setDescription(m2FacetModel
					.getStringProperty(IJBossMavenConstants.DESCRIPTION));
			
			Parent parent = new Parent();
			parent.setArtifactId(parentArtifactId);
			parent.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			parent.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			model.setParent(parent);
			
			List dependencies = model.getDependencies();
			
			Dependency dependency = getSeamDependency();
			dependency.setScope("provided");
			dependencies.add(dependency);
			dependencies.add(getJSFApi());
			dependencies.add(getRichFacesApi());
			
			dependency = new Dependency();
			dependency.setGroupId("javax.ejb");
			dependency.setArtifactId("ejb-api");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.annotation");
			dependency.setArtifactId("jsr250-api");
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.persistence");
			dependency.setArtifactId("persistence-api");
			dependencies.add(dependency);
			
			dependencies.add(getHibernateAnnotations());
			dependencies.add(getHibernateCommonAnnotations());
			dependencies.add(getHibernateValidator());
			
			Build build = new Build();
			try {
				// FIXME
				//build.setFinalName(ejbArtifactId);
				String outputDirectory = MavenCoreActivator.getOutputDirectory(javaProject);	
				build.setOutputDirectory(outputDirectory);
				String sourceDirectory = MavenCoreActivator.getSourceDirectory(javaProject);
				if (sourceDirectory != null) {
					build.setSourceDirectory(sourceDirectory);
				}
				MavenCoreActivator.addMavenEjbPlugin(build, project);
				model.setBuild(build);
				MavenCoreActivator.createMavenProject(ejbProjectName, null, model, true);
				removeWTPContainers(m2FacetModel, project);
			} catch (Exception e) {
				MavenSeamActivator.log(e);
			}
			
		}
		
	}

	private void configureWarProject(IDataModel m2FacetModel,IDataModel seamFacetModel) {
		try {
			IProject webProject = ResourcesPlugin.getWorkspace().getRoot().getProject(webProjectName);
			
			IFile pomFile = webProject.getFile(IMavenConstants.POM_FILE_NAME);
			MavenModelManager modelManager = MavenPlugin.getDefault().getMavenModelManager();
			
			String artifactId = parentProjectName;
			String groupId = m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID);
			String version = m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION);
			modelManager.updateProject(pomFile, new ParentAdder(groupId, artifactId, version));
			
			Dependency dependency = getHibernateValidator();
			//dependency.setScope("provided");
			modelManager.addDependency(pomFile,dependency);
			
			dependency = getHibernateAnnotations();
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.hibernate");
			dependency.setArtifactId("hibernate-entitymanager");
			modelManager.addDependency(pomFile,dependency);
			
			dependency = getSeamDependency();
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency.setScope("provided");
			}
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam");
			dependency.setArtifactId("jboss-seam-ui");
			List<Exclusion> exclusions = dependency.getExclusions();
			Exclusion exclusion = new Exclusion();
			exclusion.setGroupId("org.jboss.seam");
			exclusion.setArtifactId("jboss-seam");
			exclusions.add(exclusion);
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam");
			dependency.setArtifactId("jboss-seam-ioc");
			exclusions = dependency.getExclusions();
			exclusion = new Exclusion();
			exclusion.setGroupId("org.jboss.seam");
			exclusion.setArtifactId("jboss-seam");
			exclusions.add(exclusion);
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam");
			dependency.setArtifactId("jboss-seam-debug");
			
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam");
			dependency.setArtifactId("jboss-seam-mail");
			
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam");
			dependency.setArtifactId("jboss-seam-pdf");
			
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam");
			dependency.setArtifactId("jboss-seam-remoting");
			
			modelManager.addDependency(pomFile,dependency);
			
			if (FacetedProjectFramework.hasProjectFacet(webProject, ISeamFacetDataModelProperties.SEAM_FACET_ID, ISeamFacetDataModelProperties.SEAM_FACET_VERSION_21)) {
				dependency = new Dependency();
				dependency.setGroupId("org.jboss.seam");
				dependency.setArtifactId("jboss-seam-excel");
				
				modelManager.addDependency(pomFile,dependency);
			}
			
			dependency = new Dependency();
			dependency.setGroupId("javax.servlet");
			dependency.setArtifactId("servlet-api");
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.richfaces.ui");
			dependency.setArtifactId("richfaces-ui");
			modelManager.addDependency(pomFile,dependency);
			
			dependency = getRichFacesApi();
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency.setScope("provided");
			}
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.richfaces.framework");
			dependency.setArtifactId("richfaces-impl");
			modelManager.addDependency(pomFile,dependency);
			
			dependency = getJSFApi();
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.faces");
			dependency.setArtifactId("jsf-impl");
			modelManager.addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.el");
			dependency.setArtifactId("el-api");
			modelManager.addDependency(pomFile,dependency);
			
			if (SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency = new Dependency();
				dependency.setGroupId("org.drools");
				dependency.setArtifactId("drools-compiler");
				dependency.setType("jar");
				dependency.setScope("compile");
				modelManager.addDependency(pomFile,dependency);
				
				dependency = new Dependency();
				dependency.setGroupId("org.mvel");
				dependency.setArtifactId("mvel14");
				dependency.setType("jar");
				dependency.setScope("compile");
				modelManager.addDependency(pomFile,dependency);
				
				dependency = new Dependency();
				dependency.setGroupId("org.jbpm");
				dependency.setArtifactId("jbpm-jpdl");
				dependency.setType("jar");
				dependency.setScope("compile");
				modelManager.addDependency(pomFile,dependency);
				
				dependency = new Dependency();
				dependency.setGroupId("commons-digester");
				dependency.setArtifactId("commons-digester");
				modelManager.addDependency(pomFile,dependency);
			}
			
			// ejb project
			
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency = new Dependency();
				dependency.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
				dependency.setArtifactId(ejbProjectName);
				dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
				dependency.setType("ejb");
				dependency.setScope("provided");
				modelManager.addDependency(pomFile,dependency);
			}
			removeWTPContainers(m2FacetModel, webProject);
		} catch (Exception e) {
			MavenSeamActivator.log(e);
		}
	}

	private void removeWTPContainers(IDataModel m2FacetModel,
			IProject webProject) throws JavaModelException {
		if (m2FacetModel.getBooleanProperty(IJBossMavenConstants.REMOVE_WTP_CLASSPATH_CONTAINERS)) {
			IJavaProject javaProject = JavaCore.create(webProject);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			List<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				boolean add = true;
				if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					// FIXME
					IPath path = entry.getPath();
					if (path != null) {
						String value = path.toString();
						if (value.startsWith("org.eclipse.jst")) {
							add = false;
						}
					}
				}
				if (add) {
					newEntries.add(entry);
				}
			}
			javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[0]), null);
		}
	}

	private Dependency getHibernateValidator() {
		Dependency dependency = new Dependency();
		dependency.setGroupId("org.hibernate");
		dependency.setArtifactId("hibernate-validator");
		return dependency;
	}

	private Dependency getHibernateAnnotations() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.hibernate");
		dependency.setArtifactId("hibernate-annotations");
		return dependency;
	}
	
	private Dependency getHibernateCommonAnnotations() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.hibernate");
		dependency.setArtifactId("hibernate-commons-annotations");
		return dependency;
	}

	private Dependency getRichFacesApi() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.richfaces.framework");
		dependency.setArtifactId("richfaces-api");
		return dependency;
	}

	private Dependency getJSFApi() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("javax.faces");
		dependency.setArtifactId("jsf-api");
		return dependency;
	}

	private Dependency getSeamDependency() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.jboss.seam");
		dependency.setArtifactId("jboss-seam");
		return dependency;
	}

	private void configureParentProject(IDataModel m2FacetModel, IDataModel seamFacetModel) {
		Bundle bundle = getDefault().getBundle();
		URL parentPomEntryURL = bundle.getEntry("/poms/parent-pom.xml");
		InputStream inputStream = null;
		try {
			URL resolvedURL = FileLocator.resolve(parentPomEntryURL);
			MavenModelManager modelManager = MavenPlugin.getDefault().getMavenModelManager();
			inputStream = resolvedURL.openStream();
			Model model = modelManager.readMavenModel(inputStream);
			model.setArtifactId(parentArtifactId);
			model.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			String projectVersion = m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION);
			String name = m2FacetModel.getStringProperty(IJBossMavenConstants.NAME);
			if (name != null && name.trim().length() > 0) {
				model.setName(name + " - parent");
			}
			String description= m2FacetModel.getStringProperty(IJBossMavenConstants.DESCRIPTION);
			if (description != null && description.trim().length() > 0) {
				model.setDescription(description + " - parent");
			}
			model.setVersion(projectVersion);
			
			Properties properties = model.getProperties();
			properties.put(IJBossMavenConstants.PROJECT_VERSION, projectVersion);
			String seamVersion = m2FacetModel.getStringProperty(IJBossMavenConstants.SEAM_MAVEN_VERSION);
			if (seamVersion != null && seamVersion.trim().length() > 0) {
				properties.put(IJBossMavenConstants.SEAM_VERSION, seamVersion);
			}
			
			List<String> modules = model.getModules();
			modules.add("../" + artifactId);
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				modules.add("../" + ejbArtifactId);
				modules.add("../" + earArtifactId);
			}
			webProjectName = seamFacetModel.getStringProperty(IFacetDataModelProperties.FACET_PROJECT_NAME);
			
			IProject seamWebProject = ResourcesPlugin.getWorkspace().getRoot().getProject(webProjectName);
			IPath location = seamWebProject.getLocation().removeLastSegments(1);
			location = location.append(parentProjectName);
			MavenCoreActivator.createMavenProject(parentProjectName, null, model, false, location);
			// disable workspace resolution
			MavenProjectManager projectManager = MavenPlugin.getDefault().getMavenProjectManager();
		    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(parentProjectName);
		    ResolverConfiguration configuration = projectManager.getResolverConfiguration(project);
		    configuration.setResolveWorkspaceProjects(false);
		    projectManager.setResolverConfiguration(project, configuration);
		} catch (Exception e) {
			log(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ignore) {}
			}
		}
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}
}
