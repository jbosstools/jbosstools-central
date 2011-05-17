/*************************************************************************************
 * Copyright (c) 2008-2011 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.seam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.project.ResolverConfigurationIO;
import org.eclipse.m2e.core.internal.project.registry.MavenProjectManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.model.edit.pom.Configuration;
import org.eclipse.m2e.model.edit.pom.Plugin;
import org.eclipse.m2e.model.edit.pom.PluginExecution;
import org.eclipse.m2e.model.edit.pom.PomFactory;
import org.eclipse.m2e.model.edit.pom.util.PomResourceImpl;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.xpl.ProjectUpdater;
import org.jboss.tools.seam.core.SeamUtil;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.core.project.facet.SeamVersion;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.jboss.tools.seam.internal.core.project.facet.SeamFacetAbstractInstallDelegate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Node;

/**
 * The activator class controls the plug-in life cycle
 */
public class MavenSeamActivator extends AbstractUIPlugin {

	private static final String ORG_CODEHAUS_MOJO = "org.codehaus.mojo"; //$NON-NLS-1$

	private static final String PARENT_SUFFIX = "-parent"; //$NON-NLS-1$

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.seam"; //$NON-NLS-1$
	
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

	private String groupId;

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
		groupId = m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID);
		parentProjectName = webProjectName + PARENT_SUFFIX;
		parentArtifactId = artifactId + PARENT_SUFFIX;
		testProjectName = seamFacetModel.getStringProperty(ISeamFacetDataModelProperties.SEAM_TEST_PROJECT);
		testArtifactId = testProjectName;
		earProjectName = seamFacetModel.getStringProperty(ISeamFacetDataModelProperties.SEAM_EAR_PROJECT);
		earArtifactId = earProjectName;
		ejbProjectName = seamFacetModel.getStringProperty(ISeamFacetDataModelProperties.SEAM_EJB_PROJECT);;
		ejbArtifactId = ejbProjectName;
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
		if (project == null || !project.exists()) {
			return;
		}
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
			model.setName(m2FacetModel.getStringProperty(IJBossMavenConstants.NAME) + " - test"); //$NON-NLS-1$
			model.setPackaging("jar"); //$NON-NLS-1$
			model.setDescription(m2FacetModel
					.getStringProperty(IJBossMavenConstants.DESCRIPTION));
			
			Parent parent = new Parent();
			parent.setArtifactId(parentArtifactId);
			parent.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			parent.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			parent.setRelativePath("../" + parentProjectName); //$NON-NLS-1$
			model.setParent(parent);
			
			List dependencies = model.getDependencies();
			
			Dependency dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam.embedded"); //$NON-NLS-1$
			dependency.setArtifactId("hibernate-all"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam.embedded"); //$NON-NLS-1$
			dependency.setArtifactId("jboss-embedded-all"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam.embedded"); //$NON-NLS-1$
			dependency.setArtifactId("thirdparty-all"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = getSeamDependency();
			dependency.setScope("compile"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = getJSFApi();
			dependency.setScope("test"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.activation"); //$NON-NLS-1$
			dependency.setArtifactId("activation"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.testng"); //$NON-NLS-1$
			dependency.setArtifactId("testng"); //$NON-NLS-1$
			// FIXME
			dependency.setVersion("${testng.version}"); //$NON-NLS-1$
			dependency.setClassifier("jdk15"); //$NON-NLS-1$
			dependency.setScope("compile"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.slf4j"); //$NON-NLS-1$
			dependency.setArtifactId("slf4j-api"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.slf4j"); //$NON-NLS-1$
			dependency.setArtifactId("slf4j-nop"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.el"); //$NON-NLS-1$
			dependency.setArtifactId("el-api"); //$NON-NLS-1$
			dependency.setScope("test"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId(groupId);
			dependency.setArtifactId(artifactId);
			dependency.setType("war"); //$NON-NLS-1$
			dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			dependency.setScope("test"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency = new Dependency();
				dependency.setGroupId(groupId);
				dependency.setArtifactId(ejbArtifactId);
				dependency.setType("ejb"); //$NON-NLS-1$
				dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
				dependency.setScope("test"); //$NON-NLS-1$
				dependencies.add(dependency);
			}
			dependency = new Dependency();
			dependency.setGroupId("org.drools"); //$NON-NLS-1$
			dependency.setArtifactId("drools-compiler"); //$NON-NLS-1$
			dependency.setScope("test"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jbpm"); //$NON-NLS-1$
			dependency.setArtifactId("jbpm-jpdl"); //$NON-NLS-1$
			dependency.setScope("test"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.eclipse.jdt"); //$NON-NLS-1$
			dependency.setArtifactId("core"); //$NON-NLS-1$
			dependency.setVersion("3.4.2.v_883_R34x"); //$NON-NLS-1$
			dependency.setScope("test"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			Build build = new Build();
			try {
				//build.setFinalName(testProjectName);
				String sourceDirectory = MavenCoreActivator.getSourceDirectory(javaProject);
				if (sourceDirectory != null) {
					build.setTestSourceDirectory(sourceDirectory);
				}		
				String outputDirectory = MavenCoreActivator.getOutputDirectory(javaProject);	
				build.setOutputDirectory(outputDirectory);
				build.setTestOutputDirectory(outputDirectory);
				MavenCoreActivator.addResource(build, project, sourceDirectory);
				Resource resource = new Resource();
				
				resource.setDirectory(MavenCoreActivator.BASEDIR + "/bootstrap"); //$NON-NLS-1$
				List<String> excludes = new ArrayList<String>();
				excludes.add("**/*.java"); //$NON-NLS-1$
				resource.setExcludes(excludes);
				build.getResources().add(resource);
				
				resource = new Resource();
				IProject webProject = ResourcesPlugin.getWorkspace().getRoot().getProject(webProjectName);
				if (project == null || !project.exists()) {
					return;
				}
				String webContent = getRootComponent(webProject);
				resource.setDirectory(MavenCoreActivator.BASEDIR + "/../" + webProjectName + "/" + webContent); //$NON-NLS-1$ //$NON-NLS-2$
				excludes = new ArrayList<String>();
				excludes.add("**/*.java"); //$NON-NLS-1$
				resource.setExcludes(excludes);
				build.getResources().add(resource);
				
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
			model.setName(m2FacetModel.getStringProperty(IJBossMavenConstants.NAME) + " - EAR"); //$NON-NLS-1$
			model.setPackaging("ear"); //$NON-NLS-1$
			model.setDescription(m2FacetModel
					.getStringProperty(IJBossMavenConstants.DESCRIPTION));
			
			Parent parent = new Parent();
			parent.setArtifactId(parentArtifactId);
			parent.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			parent.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			parent.setRelativePath("../" + parentProjectName); //$NON-NLS-1$
			model.setParent(parent);
			
			List dependencies = model.getDependencies();
			
			Dependency dependency = new Dependency();
			dependency.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			dependency.setArtifactId(ejbProjectName);
			dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			dependency.setType("ejb"); //$NON-NLS-1$
			dependency.setScope("runtime"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			dependency.setArtifactId(webProjectName);
			dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			dependency.setType("war"); //$NON-NLS-1$
			dependency.setScope("runtime"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = getSeamDependency();
			dependency.setVersion("${seam.version}"); //$NON-NLS-1$
			dependency.setType("ejb"); //$NON-NLS-1$
			dependency.setScope("compile"); //$NON-NLS-1$
			List exclusions = dependency.getExclusions();
			Exclusion exclusion = new Exclusion();
			exclusion.setGroupId("javassist"); //$NON-NLS-1$
			exclusion.setArtifactId("javassist"); //$NON-NLS-1$
			exclusions.add(exclusion);
			
			exclusion = new Exclusion();
			exclusion.setGroupId("javax.el"); //$NON-NLS-1$
			exclusion.setArtifactId("el-api"); //$NON-NLS-1$
			exclusions.add(exclusion);
			
			exclusion = new Exclusion();
			exclusion.setGroupId("dom4j"); //$NON-NLS-1$
			exclusion.setArtifactId("dom4j"); //$NON-NLS-1$
			exclusions.add(exclusion);
			
			exclusion = new Exclusion();
			exclusion.setGroupId("xstream"); //$NON-NLS-1$
			exclusion.setArtifactId("xstream"); //$NON-NLS-1$
			exclusions.add(exclusion);
			
			exclusion = new Exclusion();
			exclusion.setGroupId("xpp3"); //$NON-NLS-1$
			exclusion.setArtifactId("xpp3_min"); //$NON-NLS-1$
			exclusions.add(exclusion);
			
			dependencies.add(dependency);
			
			dependency = getRichFacesApi();
			dependency.setType("jar"); //$NON-NLS-1$
			dependency.setScope("compile"); //$NON-NLS-1$
			exclusions = dependency.getExclusions();
			exclusion = new Exclusion();
			exclusion.setGroupId("commons-collections"); //$NON-NLS-1$
			exclusion.setArtifactId("commons-collections"); //$NON-NLS-1$
			exclusions.add(exclusion);
			exclusion = new Exclusion();
			exclusion.setGroupId("commons-logging"); //$NON-NLS-1$
			exclusion.setArtifactId("commons-logging"); //$NON-NLS-1$
			exclusions.add(exclusion);
			
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.drools"); //$NON-NLS-1$
			dependency.setArtifactId("drools-compiler"); //$NON-NLS-1$
			dependency.setType("jar"); //$NON-NLS-1$
			dependency.setScope("compile"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			String jbpmGroupId = "org.jbpm"; //$NON-NLS-1$
			// JBoss EAP 5.0 requires org.jbpm.jbpm3
			SeamRuntime seamRuntime = SeamRuntimeManager.getInstance().findRuntimeByName(seamFacetModel.getProperty(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME).toString());
			if(seamRuntime!=null) {
				SeamVersion seamVersion = seamRuntime.getVersion();
				if (SeamVersion.SEAM_2_2.equals(seamVersion)) {
					String fullVersion = SeamUtil.getSeamVersionFromManifest(seamRuntime);
					if (fullVersion != null && fullVersion.contains("EAP")) { //$NON-NLS-1$
						jbpmGroupId = "org.jbpm.jbpm3"; //$NON-NLS-1$
					}
				}
			}
			dependency.setGroupId(jbpmGroupId);
			dependency.setArtifactId("jbpm-jpdl"); //$NON-NLS-1$
			dependency.setType("jar"); //$NON-NLS-1$
			dependency.setScope("compile"); //$NON-NLS-1$
			dependencies.add(dependency);
			
//			dependency = new Dependency();
//			dependency.setGroupId("org.mvel");
//			if ("org.jbpm.jbpm3".equals(jbpmGroupId)) {
//				dependency.setArtifactId("mvel2");
//			} else {
//				dependency.setArtifactId("mvel14");
//			}
//			dependency.setType("jar");
//			dependency.setScope("compile");
//			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("commons-digester"); //$NON-NLS-1$
			dependency.setArtifactId("commons-digester"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			Build build = new Build();
			try {
				build.setFinalName(earProjectName);
				
				String sourceDirectory = MavenCoreActivator.getEarRoot(project);
				if (sourceDirectory != null) {
					build.setSourceDirectory(sourceDirectory);
				}
				build.setOutputDirectory("target/classes"); //$NON-NLS-1$
				MavenCoreActivator.addMavenEarPlugin(build, project, m2FacetModel, ejbArtifactId, true);
				model.setBuild(build);
				MavenCoreActivator.createMavenProject(earProjectName, null, model, true);
				removeWTPContainers(m2FacetModel, project);
			} catch (Exception e) {
				MavenSeamActivator.log(e);
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
			model.setName(m2FacetModel.getStringProperty(IJBossMavenConstants.NAME) + " - EJB"); //$NON-NLS-1$
			model.setPackaging("ejb"); //$NON-NLS-1$
			model.setDescription(m2FacetModel
					.getStringProperty(IJBossMavenConstants.DESCRIPTION));
			
			Parent parent = new Parent();
			parent.setArtifactId(parentArtifactId);
			parent.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
			parent.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
			parent.setRelativePath("../" + parentProjectName); //$NON-NLS-1$
			model.setParent(parent);
			
			List dependencies = model.getDependencies();
			
			Dependency dependency = getSeamDependency();
			dependency.setScope("provided"); //$NON-NLS-1$
			dependencies.add(dependency);
			dependencies.add(getJSFApi());
			dependencies.add(getRichFacesApi());
			
			dependency = new Dependency();
			dependency.setGroupId("javax.ejb"); //$NON-NLS-1$
			dependency.setArtifactId("ejb-api"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.annotation"); //$NON-NLS-1$
			dependency.setArtifactId("jsr250-api"); //$NON-NLS-1$
			dependencies.add(dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.persistence"); //$NON-NLS-1$
			dependency.setArtifactId("persistence-api"); //$NON-NLS-1$
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

	public void updateProject(IFile pomFile, ProjectUpdater updater) {
		File pom = pomFile.getLocation().toFile();
		PomResourceImpl resource = null;
		try {
			resource = MavenCoreActivator.loadResource(pomFile);
			updater.update(resource.getModel());
			resource.save(Collections.EMPTY_MAP);
		} catch (Exception ex) {
			String msg = "Unable to update " + pom;
			log(ex, msg);
		} finally {
			if (resource != null) {
				resource.unload();
			}
		}
	}
	
	public void addDependency(IFile pomFile,
			org.apache.maven.model.Dependency dependency) {
		updateProject(pomFile, new DependencyAdder(dependency));
	}
	
	private void configureWarProject(IDataModel m2FacetModel,IDataModel seamFacetModel) {
		try {
			IProject webProject = ResourcesPlugin.getWorkspace().getRoot().getProject(webProjectName);
			
			IFile pomFile = webProject.getFile(IMavenConstants.POM_FILE_NAME);
			
			String artifactId = parentProjectName;
			String groupId = m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID);
			String version = m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION);
			
			String relativePath = "../" + parentProjectName; //$NON-NLS-1$
			ParentAdder parentAdder = new ParentAdder(groupId, artifactId, version, relativePath);
			updateProject(pomFile, parentAdder);
			
			Dependency dependency = getHibernateValidator();
			//dependency.setScope("provided");
			addDependency(pomFile,dependency);
			
			dependency = getHibernateAnnotations();
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.hibernate"); //$NON-NLS-1$
			dependency.setArtifactId("hibernate-entitymanager"); //$NON-NLS-1$
			addDependency(pomFile,dependency);
			
			dependency = getSeamDependency();
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency.setScope("provided"); //$NON-NLS-1$
			}
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			dependency.setArtifactId("jboss-seam-ui"); //$NON-NLS-1$
			List<Exclusion> exclusions = dependency.getExclusions();
			Exclusion exclusion = new Exclusion();
			exclusion.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			exclusion.setArtifactId("jboss-seam"); //$NON-NLS-1$
			exclusions.add(exclusion);
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			dependency.setArtifactId("jboss-seam-ioc"); //$NON-NLS-1$
			exclusions = dependency.getExclusions();
			exclusion = new Exclusion();
			exclusion.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			exclusion.setArtifactId("jboss-seam"); //$NON-NLS-1$
			exclusions.add(exclusion);
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			dependency.setArtifactId("jboss-seam-debug"); //$NON-NLS-1$
			// FIXME
			dependency.setVersion("${seam.version}"); //$NON-NLS-1$
			
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			dependency.setArtifactId("jboss-seam-mail"); //$NON-NLS-1$
			
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			dependency.setArtifactId("jboss-seam-pdf"); //$NON-NLS-1$
			
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
			dependency.setArtifactId("jboss-seam-remoting"); //$NON-NLS-1$
			
			addDependency(pomFile,dependency);
			
			if (FacetedProjectFramework.hasProjectFacet(webProject, ISeamFacetDataModelProperties.SEAM_FACET_ID, ISeamFacetDataModelProperties.SEAM_FACET_VERSION_21)) {
				dependency = new Dependency();
				dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
				dependency.setArtifactId("jboss-seam-excel"); //$NON-NLS-1$
				
				addDependency(pomFile,dependency);
			}
			
			dependency = new Dependency();
			dependency.setGroupId("javax.servlet"); //$NON-NLS-1$
			dependency.setArtifactId("servlet-api"); //$NON-NLS-1$
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.richfaces.ui"); //$NON-NLS-1$
			dependency.setArtifactId("richfaces-ui"); //$NON-NLS-1$
			addDependency(pomFile,dependency);
			
			dependency = getRichFacesApi();
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency.setScope("provided"); //$NON-NLS-1$
			}
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("org.richfaces.framework"); //$NON-NLS-1$
			dependency.setArtifactId("richfaces-impl"); //$NON-NLS-1$
			addDependency(pomFile,dependency);
			
			dependency = getJSFApi();
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.faces"); //$NON-NLS-1$
			dependency.setArtifactId("jsf-impl"); //$NON-NLS-1$
			addDependency(pomFile,dependency);
			
			dependency = new Dependency();
			dependency.setGroupId("javax.el"); //$NON-NLS-1$
			dependency.setArtifactId("el-api"); //$NON-NLS-1$
			addDependency(pomFile,dependency);
			
			if (SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency = new Dependency();
				dependency.setGroupId("org.drools"); //$NON-NLS-1$
				dependency.setArtifactId("drools-compiler"); //$NON-NLS-1$
				dependency.setType("jar"); //$NON-NLS-1$
				dependency.setScope("compile"); //$NON-NLS-1$
				addDependency(pomFile,dependency);
				
				dependency = new Dependency();
				dependency.setGroupId("org.jbpm"); //$NON-NLS-1$
				dependency.setArtifactId("jbpm-jpdl"); //$NON-NLS-1$
				dependency.setType("jar"); //$NON-NLS-1$
				dependency.setScope("compile"); //$NON-NLS-1$
				addDependency(pomFile,dependency);
				
				dependency = new Dependency();
				dependency.setGroupId("commons-digester"); //$NON-NLS-1$
				dependency.setArtifactId("commons-digester"); //$NON-NLS-1$
				addDependency(pomFile,dependency);
				updateProject(pomFile, new WarProjectUpdater(webProject));
			}
			
			// ejb project
			
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				dependency = new Dependency();
				dependency.setGroupId(m2FacetModel.getStringProperty(IJBossMavenConstants.GROUP_ID));
				dependency.setArtifactId(ejbProjectName);
				dependency.setVersion(m2FacetModel.getStringProperty(IJBossMavenConstants.VERSION));
				dependency.setType("ejb"); //$NON-NLS-1$
				dependency.setScope("provided"); //$NON-NLS-1$
				addDependency(pomFile,dependency);
			}
			
			updateProject(pomFile, new WarProjectUpdater(webProject));
			removeWTPContainers(m2FacetModel, webProject);
		} catch (Exception e) {
			MavenSeamActivator.log(e);
		}
	}

	

	private static String getRootComponent(IProject webProject) {
		IVirtualComponent component = ComponentCore.createComponent(webProject);
		IVirtualFolder rootFolder = component.getRootFolder();
		IContainer root = rootFolder.getUnderlyingFolder();
		String webContentRoot = root.getProjectRelativePath().toString();
		return webContentRoot;
	}

	private static Plugin getPlugin(org.eclipse.m2e.model.edit.pom.Build build,
			String groupId, String artifactId) {
		EList<Plugin> plugins = build.getPlugins();
		for (Plugin plugin : plugins) {
			String group = plugin.getGroupId();
			if (group == null) {
				group = ORG_CODEHAUS_MOJO;
			}
			String artifact = plugin.getArtifactId();
			if (group.equals(groupId) && artifactId.equals(artifact)) {
				return plugin;
			}
		}
		Plugin newPlugin = PomFactory.eINSTANCE.createPlugin();
		newPlugin.setGroupId(groupId);
		newPlugin.setArtifactId(artifactId);
		build.getPlugins().add(newPlugin);
		return newPlugin;
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
						if (value.startsWith("org.eclipse.jst")) { //$NON-NLS-1$
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
		dependency.setGroupId("org.hibernate"); //$NON-NLS-1$
		dependency.setArtifactId("hibernate-validator"); //$NON-NLS-1$
		return dependency;
	}

	private Dependency getHibernateAnnotations() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.hibernate"); //$NON-NLS-1$
		dependency.setArtifactId("hibernate-annotations"); //$NON-NLS-1$
		return dependency;
	}
	
	private Dependency getHibernateCommonAnnotations() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.hibernate"); //$NON-NLS-1$
		dependency.setArtifactId("hibernate-commons-annotations"); //$NON-NLS-1$
		return dependency;
	}

	private Dependency getRichFacesApi() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.richfaces.framework"); //$NON-NLS-1$
		dependency.setArtifactId("richfaces-api"); //$NON-NLS-1$
		return dependency;
	}

	private Dependency getJSFApi() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("javax.faces"); //$NON-NLS-1$
		dependency.setArtifactId("jsf-api"); //$NON-NLS-1$
		return dependency;
	}

	private Dependency getSeamDependency() {
		Dependency dependency;
		dependency = new Dependency();
		dependency.setGroupId("org.jboss.seam"); //$NON-NLS-1$
		dependency.setArtifactId("jboss-seam"); //$NON-NLS-1$
		return dependency;
	}

	private void configureParentProject(IDataModel m2FacetModel, IDataModel seamFacetModel) {
		Bundle bundle = getDefault().getBundle();
		URL parentPomEntryURL = bundle.getEntry("/poms/parent-pom.xml"); //$NON-NLS-1$
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
				model.setName(name + " - parent"); //$NON-NLS-1$
			}
			String description= m2FacetModel.getStringProperty(IJBossMavenConstants.DESCRIPTION);
			if (description != null && description.trim().length() > 0) {
				model.setDescription(description + " - parent"); //$NON-NLS-1$
			}
			model.setVersion(projectVersion);
			
			Properties properties = model.getProperties();
			properties.put(IJBossMavenConstants.PROJECT_VERSION, projectVersion);
			SeamRuntime seamRuntime = SeamRuntimeManager.getInstance().findRuntimeByName(seamFacetModel.getProperty(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME).toString());
			if(seamRuntime==null) {
				getDefault().log(Messages.MavenSeamActivator_Cannot_get_seam_runtime + seamFacetModel.getProperty(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME).toString());
			}
			String seamVersion = m2FacetModel.getStringProperty(IJBossMavenConstants.SEAM_MAVEN_VERSION);
			if (seamVersion != null && seamVersion.trim().length() > 0) {
				properties.put(IJBossMavenConstants.SEAM_VERSION, seamVersion);
			}
			String seamHomePath = seamRuntime.getHomeDir();
			File seamHomeDir = new File(seamHomePath);
			if (seamHomeDir.exists()) {
				//String seamVersion = SeamUtil.getSeamVersionFromManifest(seamRuntime.getHomeDir());
				//properties.put(IJBossMavenConstants.SEAM_VERSION, seamVersion);
				File buildDir = new File(seamHomeDir,"build"); //$NON-NLS-1$
				File rootPom = new File(buildDir,"root.pom.xml"); //$NON-NLS-1$
				if (!rootPom.exists()) {
					MavenSeamActivator.log(NLS.bind(Messages.MavenSeamActivator_The_file_does_not_exist, rootPom.getAbsolutePath()));
				} else {
					try {
						Model rootPomModel = modelManager.readMavenModel(rootPom);
						List<Dependency> seamDependencies = rootPomModel.getDependencyManagement().getDependencies();
						setArtifactVersion("jsf.version", properties, "javax.faces", "jsf-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						String richfacesVersion = setArtifactVersion("richfaces.version", properties, "org.richfaces.framework", "richfaces-impl", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (richfacesVersion == null) {
							Properties seamProperties = rootPomModel.getProperties();
							richfacesVersion = seamProperties.getProperty("version.richfaces"); //$NON-NLS-1$
							if (richfacesVersion != null) {
								properties.put("richfaces.version", richfacesVersion); //$NON-NLS-1$
							}
						}
						setArtifactVersion("hibernate-validator.version", properties, "org.hibernate", "hibernate-validator", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("hibernate-annotations.version", properties, "org.hibernate", "hibernate-annotations", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("hibernate-entitymanager.version", properties, "org.hibernate", "hibernate-entitymanager", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						//setArtifactVersion("testng.version", properties, "org.hibernate", "hibernate-entitymanager", seamDependencies);
						//if (seamVersion != null && "2.2".equals(seamVersion.subSequence(0, 3))) {
						//	properties.put("testng.version", "5.9");
						//}
						setArtifactVersion("jboss.embedded.version", properties, "org.jboss.seam.embedded", "jboss-embedded-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("slf4j.version", properties, "org.slf4j", "slf4j-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("ejb.api.version", properties, "javax.ejb", "ejb-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("jsr250-api.version", properties, "javax.annotation", "jsr250-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("persistence-api.version", properties, "javax.persistence", "persistence-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("servlet.version", properties, "javax.servlet", "servlet-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setArtifactVersion("javax.el.version", properties, "javax.el", "el-api", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						String droolsVersion = setArtifactVersion("drools.version", properties, "org.drools", "drools-core", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (droolsVersion == null) {
							Properties seamProperties = rootPomModel.getProperties();
							droolsVersion = seamProperties.getProperty("version.drools"); //$NON-NLS-1$
							if (droolsVersion != null) {
								properties.put("drools.version", droolsVersion); //$NON-NLS-1$
							}
						}
						String jbpmVersion = setArtifactVersion("jbpm.version", properties, "org.jbpm", "jbpm-jpdl", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (jbpmVersion == null) {
							setArtifactVersion("jbpm3.version", properties, "org.jbpm.jbpm3", "jbpm-jpdl", seamDependencies); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
						//setArtifactVersion("mvel.version", properties, "org.mvel", "mvel14", seamDependencies);
								        
//				        <javax.activation.version>1.1</javax.activation.version>
//				        <hibernate-commons-annotations.version>3.3.0.ga</hibernate-commons-annotations.version>
//				        <commons.digester.version>1.8</commons.digester.version>
//				        <mvel.version>1.2.21</mvel.version>
						
					} catch (Exception e) {
						getDefault().log(e);
					}
				}
			} else {
				MavenSeamActivator.log(NLS.bind(Messages.MavenSeamActivator_The_folder_does_not_exist, seamHomePath));
			}
			
			List<String> modules = model.getModules();
			modules.add("../" + artifactId); //$NON-NLS-1$
			if (!SeamFacetAbstractInstallDelegate
					.isWarConfiguration(seamFacetModel)) {
				modules.add("../" + ejbArtifactId); //$NON-NLS-1$
				modules.add("../" + earArtifactId); //$NON-NLS-1$
			}
			webProjectName = seamFacetModel.getStringProperty(IFacetDataModelProperties.FACET_PROJECT_NAME);
			
			IProject seamWebProject = ResourcesPlugin.getWorkspace().getRoot().getProject(webProjectName);
			IPath location = seamWebProject.getLocation().removeLastSegments(1);
			location = location.append(parentProjectName);
			MavenCoreActivator.createMavenProject(parentProjectName, null, model, false, location);
			// disable workspace resolution
			MavenProjectManager projectManager = MavenPluginActivator.getDefault().getMavenProjectManager();
		    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(parentProjectName);
		    ResolverConfiguration configuration = ResolverConfigurationIO.readResolverConfiguration(project);
		    configuration.setResolveWorkspaceProjects(false);
		    ResolverConfigurationIO.saveResolverConfiguration(project, configuration);
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

	private String setArtifactVersion(String property, Properties properties, String groupId, String artifactId,
			List<Dependency> seamDependencies) {
		for (Dependency dependency:seamDependencies) {
			if (groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId())) {
				String version = dependency.getVersion();
				if (version != null && !version.startsWith("${")) { //$NON-NLS-1$
					properties.put(property, version);
					return version;
				}
			}
		}
		return null;
	}

	public static void log(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, e
				.getLocalizedMessage(), e);
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable e, String message) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message, null);
		getDefault().getLog().log(status);
	}
	
	public static class WarProjectUpdater extends ProjectUpdater {
	
		private static final String WAR_SOURCE_DIRECTORY = "warSourceDirectory"; //$NON-NLS-1$
		private static final String WAR_SOURCE_EXCLUDES = "warSourceExcludes"; //$NON-NLS-1$
		private IProject webProject;
		
		public WarProjectUpdater(IProject project) {
			webProject = project;
		}

		public void update(org.eclipse.m2e.model.edit.pom.Model projectModel) {
			org.eclipse.m2e.model.edit.pom.Build build = projectModel.getBuild();
			if (build == null) {
				return;
			}
			IJavaProject javaProject = JavaCore.create(webProject);
			if (javaProject == null) {
				return;
			}
			if (!javaProject.isOpen()) {
				try {
					javaProject.open(new NullProgressMonitor());
				} catch (JavaModelException e) {
					MavenSeamActivator.log(e);
					return;
				}
			}
			IPath projectOutput;
			IClasspathEntry[] entries;
			try {
				projectOutput = javaProject.getOutputLocation();
				entries = javaProject.getRawClasspath();
			} catch (JavaModelException e) {
				MavenSeamActivator.log(e);
				return;
			}
			List<IPath> sources = new ArrayList<IPath>();
			List<IPath> outputs = new ArrayList<IPath>();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = entries[i].getPath();
					sources.add(path);
					IPath output = entries[i].getOutputLocation();
					if (output != null && !output.equals(projectOutput)) {
						outputs.add(output);
					}
				}
			}
			int indexSource = 0;
			for (IPath source:sources) {
				if (source != null && source.toString().contains("main")) { //$NON-NLS-1$
					indexSource = sources.indexOf(source);
				}
			}
			if (sources.size() > 0) {
				
				IPath path = sources.get(indexSource);
				path = path.makeRelativeTo(javaProject.getPath());
				String value = path.toString();
				if (value.startsWith(MavenCoreActivator.SEPARATOR)) {
					value = MavenCoreActivator.BASEDIR + value;
				} else {
					value = MavenCoreActivator.BASEDIR + MavenCoreActivator.SEPARATOR + value;
				}
				build.setSourceDirectory(value);
			}
			
			if (projectOutput != null) {
				String value = projectOutput.toString();
				if (value.startsWith(MavenCoreActivator.SEPARATOR)) {
					value = MavenCoreActivator.BASEDIR + value;
				} else {
					value = MavenCoreActivator.BASEDIR + MavenCoreActivator.SEPARATOR + value;
				}
				build.setOutputDirectory(value);
			}
			
			EList<org.eclipse.m2e.model.edit.pom.Resource> resources = build.getResources();
			resources.clear();
			for (IPath source:sources) {
				org.eclipse.m2e.model.edit.pom.Resource resource = PomFactory.eINSTANCE.createResource();
				String value = source.makeRelativeTo(javaProject.getPath()).toString();
				if (value.startsWith(MavenCoreActivator.SEPARATOR)) {
					value = MavenCoreActivator.BASEDIR + value;
				} else {
					value = MavenCoreActivator.BASEDIR + MavenCoreActivator.SEPARATOR + value;
				}
				resource.setDirectory(value);
				resource.getExcludes().add("**/*.java"); //$NON-NLS-1$
				resources.add(resource);
			}
			
			if (outputs.size() > 0) {
				Plugin plugin = getPlugin(build, ORG_CODEHAUS_MOJO, "maven-war-plugin"); //$NON-NLS-1$
				Configuration configuration = plugin.getConfiguration();
				
				if (configuration == null) {
					configuration = PomFactory.eINSTANCE.createConfiguration();
					plugin.setConfiguration(configuration);
					configuration.createNode(WAR_SOURCE_DIRECTORY);
					String value = getRootComponent(webProject);
					if (value.startsWith(MavenCoreActivator.SEPARATOR)) {
						value = MavenCoreActivator.BASEDIR + value;
					} else {
						value = MavenCoreActivator.BASEDIR + MavenCoreActivator.SEPARATOR + value;
					}
					configuration.setStringValue(WAR_SOURCE_DIRECTORY, value);
				}
				StringBuffer buffer = new StringBuffer();
				boolean first = true;
				for (IPath output:outputs) {
					if (first) {
						first = false;
					} else {
						buffer.append(","); //$NON-NLS-1$
					}
					String root = getRootComponent(webProject);
					output=output.makeRelativeTo(javaProject.getPath());
					String outputString = output.toString();
					if (outputString.startsWith(root)) {
						outputString = outputString.substring(root.length());
					}
					outputString = outputString.trim();
					buffer.append(outputString);
					buffer.append("/**"); //$NON-NLS-1$
					
				}
				String excludeString = buffer.toString().trim();
				if (excludeString.startsWith(MavenCoreActivator.SEPARATOR)) {
					excludeString = excludeString.substring(1);
				}
				configuration.setStringValue(WAR_SOURCE_EXCLUDES, excludeString);
			}
			sources.remove(indexSource);
			if (sources.size() > 0) {
				
				Plugin plugin = getPlugin(build, ORG_CODEHAUS_MOJO , "build-helper-maven-plugin"); //$NON-NLS-1$
				plugin.setVersion("1.5"); //$NON-NLS-1$
				plugin.getExecutions().clear();
				
				PluginExecution execution = PomFactory.eINSTANCE.createPluginExecution();
				execution.setId("add-source"); //$NON-NLS-1$
				execution.setPhase("generate-sources"); //$NON-NLS-1$
				execution.getGoals().add("add-source"); //$NON-NLS-1$
				plugin.getExecutions().add(execution);
				Configuration configuration = PomFactory.eINSTANCE.createConfiguration();	
				execution.setConfiguration(configuration);
				Node n = configuration.createNode("sources"); //$NON-NLS-1$
				for (IPath source:sources) {
					Node node = n.getOwnerDocument().createElement("source"); //$NON-NLS-1$
					n.appendChild(node);
					source = source.makeRelativeTo(javaProject.getPath());
					String value = source.toString();
					if (value.startsWith(MavenCoreActivator.SEPARATOR)) {
						value = MavenCoreActivator.BASEDIR + value;
					} else {
						value = MavenCoreActivator.BASEDIR + MavenCoreActivator.SEPARATOR + value;
					}
					node.appendChild(node.getOwnerDocument().createTextNode(value));
				}
			}
		}
	}
	
	
	public static class DependencyAdder extends ProjectUpdater {

		private final org.apache.maven.model.Dependency dependency;

		public DependencyAdder(org.apache.maven.model.Dependency dependency) {
			this.dependency = dependency;
		}

		public void update(org.eclipse.m2e.model.edit.pom.Model model) {
			org.eclipse.m2e.model.edit.pom.Dependency dependency = PomFactory.eINSTANCE
					.createDependency();

			dependency.setGroupId(this.dependency.getGroupId());
			dependency.setArtifactId(this.dependency.getArtifactId());

			if (this.dependency.getVersion() != null) {
				dependency.setVersion(this.dependency.getVersion());
			}

			if (this.dependency.getClassifier() != null) {
				dependency.setClassifier(this.dependency.getClassifier());
			}

			if (this.dependency.getType() != null //
					&& !"jar".equals(this.dependency.getType()) //
					&& !"null".equals(this.dependency.getType())) { // guard
																	// against
																	// MNGECLIPSE-622
				dependency.setType(this.dependency.getType());
			}

			if (this.dependency.getScope() != null
					&& !"compile".equals(this.dependency.getScope())) {
				dependency.setScope(this.dependency.getScope());
			}

			if (this.dependency.getSystemPath() != null) {
				dependency.setSystemPath(this.dependency.getSystemPath());
			}

			if (this.dependency.isOptional()) {
				dependency.setOptional("true");
			}

			if (!this.dependency.getExclusions().isEmpty()) {

				Iterator<org.apache.maven.model.Exclusion> it = this.dependency
						.getExclusions().iterator();
				while (it.hasNext()) {
					Exclusion e = it.next();
					org.eclipse.m2e.model.edit.pom.Exclusion exclusion = PomFactory.eINSTANCE
							.createExclusion();
					exclusion.setGroupId(e.getGroupId());
					exclusion.setArtifactId(e.getArtifactId());
					dependency.getExclusions().add(exclusion);
				}
			}

			// search for dependency with same GAC and remove if found
			Iterator<org.eclipse.m2e.model.edit.pom.Dependency> it = model
					.getDependencies().iterator();
			boolean mergeScope = false;
			String oldScope = Artifact.SCOPE_COMPILE;
			while (it.hasNext()) {
				org.eclipse.m2e.model.edit.pom.Dependency dep = it.next();
				if (dep.getGroupId().equals(dependency.getGroupId())
						&& dep.getArtifactId().equals(
								dependency.getArtifactId())
						&& compareNulls(dep.getClassifier(),
								dependency.getClassifier())) {
					oldScope = dep.getScope();
					it.remove();
					mergeScope = true;
				}
			}

			if (mergeScope) {
				// merge scopes
				if (oldScope == null) {
					oldScope = Artifact.SCOPE_COMPILE;
				}

				String newScope = this.dependency.getScope();
				if (newScope == null) {
					newScope = Artifact.SCOPE_COMPILE;
				}

				if (!oldScope.equals(newScope)) {
					boolean systemScope = false;
					boolean providedScope = false;
					boolean compileScope = false;
					boolean runtimeScope = false;
					boolean testScope = false;

					// test old scope
					if (Artifact.SCOPE_COMPILE.equals(oldScope)) {
						systemScope = true;
						providedScope = true;
						compileScope = true;
						runtimeScope = false;
						testScope = false;
					} else if (Artifact.SCOPE_RUNTIME.equals(oldScope)) {
						systemScope = false;
						providedScope = false;
						compileScope = true;
						runtimeScope = true;
						testScope = false;
					} else if (Artifact.SCOPE_TEST.equals(oldScope)) {
						systemScope = true;
						providedScope = true;
						compileScope = true;
						runtimeScope = true;
						testScope = true;
					}

					// merge with new one
					if (Artifact.SCOPE_COMPILE.equals(newScope)) {
						systemScope = systemScope || true;
						providedScope = providedScope || true;
						compileScope = compileScope || true;
						runtimeScope = runtimeScope || false;
						testScope = testScope || false;
					} else if (Artifact.SCOPE_RUNTIME.equals(newScope)) {
						systemScope = systemScope || false;
						providedScope = providedScope || false;
						compileScope = compileScope || true;
						runtimeScope = runtimeScope || true;
						testScope = testScope || false;
					} else if (Artifact.SCOPE_TEST.equals(newScope)) {
						systemScope = systemScope || true;
						providedScope = providedScope || true;
						compileScope = compileScope || true;
						runtimeScope = runtimeScope || true;
						testScope = testScope || true;
					}

					if (testScope) {
						newScope = Artifact.SCOPE_TEST;
					} else if (runtimeScope) {
						newScope = Artifact.SCOPE_RUNTIME;
					} else if (compileScope) {
						newScope = Artifact.SCOPE_COMPILE;
					} else {
						// unchanged
					}

					dependency.setScope(newScope);
				}
			}

			model.getDependencies().add(dependency);
		}

		@SuppressWarnings("null")
		private boolean compareNulls(String s1, String s2) {
			if (s1 == null && s2 == null) {
				return true;
			}
			if ((s1 == null && s2 != null) || (s2 == null && s1 != null)) {
				return false;
			}
			return s1.equals(s2);
		}
	}

}
