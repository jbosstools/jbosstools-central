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
package org.jboss.tools.maven.jsf.configurators;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.jsf.MavenJSFActivator;
import org.jboss.tools.maven.jsf.Messages;
import org.jboss.tools.maven.ui.Activator;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.MavenProjectChangedEvent;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;
import org.maven.ide.eclipse.wtp.WTPProjectsUtil;

/**
 * 
 * @author snjeza
 *
 */
public class JSFProjectConfigurator extends AbstractProjectConfigurator {

	private static final String JSF_API_GROUP_ID = "javax.faces"; //$NON-NLS-1$
	private static final String JSF_API2_GROUP_ID = "com.sun.faces"; //$NON-NLS-1$
	private static final String JSF_API_ARTIFACT_ID = "jsf-api"; //$NON-NLS-1$

	private static final String WEB_XML = "WEB-INF/web.xml";
	private static final String WAR_SOURCE_FOLDER = "/src/main/webapp";

	protected static final IProjectFacet dynamicWebFacet;
	protected static final IProjectFacetVersion dynamicWebVersion;

	protected static final IProjectFacet jsfFacet;
	protected static final IProjectFacetVersion jsfVersion20;
	protected static final IProjectFacetVersion jsfVersion12;
	protected static final IProjectFacetVersion jsfVersion11;
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;

	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5"); //$NON-NLS-1$
		jsfFacet = ProjectFacetsManager.getProjectFacet("jst.jsf"); //$NON-NLS-1$
		jsfVersion20 = jsfFacet.getVersion("2.0"); //$NON-NLS-1$
		jsfVersion12 = jsfFacet.getVersion("1.2"); //$NON-NLS-1$
		jsfVersion11 = jsfFacet.getVersion("1.1"); //$NON-NLS-1$
		m2Facet = ProjectFacetsManager.getProjectFacet("jboss.m2"); //$NON-NLS-1$
		m2Version = m2Facet.getVersion("1.0"); //$NON-NLS-1$
	}

	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureInternal(mavenProject, project, monitor);
	}

	private void configureInternal(MavenProject mavenProject, IProject project,
			IProgressMonitor monitor) throws CoreException {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureJSF = store.getBoolean(Activator.CONFIGURE_JSF);
		if (!configureJSF) {
			return;
		}

		String packaging = mavenProject.getPackaging();
		String jsfVersion = getJSFVersion(mavenProject);
		if (jsfVersion != null) {
			final IFacetedProject fproj = ProjectFacetsManager.create(project);
			if (fproj != null && "war".equals(packaging)) { //$NON-NLS-1$
				installWarFacets(fproj, jsfVersion, mavenProject, monitor);
			}
		}
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event,
			IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
		if (facade != null) {
			IProject project = facade.getProject();
			if (isWTPProject(project)) {
				MavenProject mavenProject = facade.getMavenProject(monitor);
				configureInternal(mavenProject, project, monitor);
			}
		}
		super.mavenProjectChanged(event, monitor);
	}

	private boolean isWTPProject(IProject project) {
		return ModuleCoreNature.getModuleCoreNature(project) != null;
	}

	private void installM2Facet(IFacetedProject fproj, IProgressMonitor monitor)
			throws CoreException {
		if (!fproj.hasProjectFacet(m2Facet)) {
			IDataModel config = (IDataModel) new MavenFacetInstallDataModelProvider()
					.create();
			config.setBooleanProperty(
					IJBossMavenConstants.MAVEN_PROJECT_EXISTS, true);
			fproj.installProjectFacet(m2Version, config, monitor);
		}
	}

	private void installWarFacets(IFacetedProject fproj, String jsfVersion,
			MavenProject mavenProject, IProgressMonitor monitor)
			throws CoreException {

		if (!fproj.hasProjectFacet(dynamicWebFacet)) {
			Activator
					.log(Messages.JSFProjectConfigurator_The_project_does_not_contain_the_Web_Module_facet);
		}
		installJSFFacet(fproj, jsfVersion, mavenProject, monitor);
		installM2Facet(fproj, monitor);

	}

	private void installJSFFacet(IFacetedProject fproj,
			String jsfVersionString, MavenProject mavenProject,
			IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(jsfFacet)) {
			if (jsfVersionString.startsWith("1.1")) { //$NON-NLS-1$
				IDataModel model = MavenJSFActivator.getDefault()
						.createJSFDataModel(fproj, jsfVersion11);
				fproj.installProjectFacet(jsfVersion11, model, monitor);
			}
			if (jsfVersionString.startsWith("1.2")) { //$NON-NLS-1$
				IDataModel model = MavenJSFActivator.getDefault()
						.createJSFDataModel(fproj, jsfVersion12);
				fproj.installProjectFacet(jsfVersion12, model, monitor);
			}
			if (jsfVersionString.startsWith("2.0")) { //$NON-NLS-1$
				String webXmlString = null;
				IFile webXml = null;
				webXml = getWebXml(fproj, mavenProject);
				boolean webXmlExists = webXml != null && webXml.exists();
				if (!configureWebxml() && webXmlExists) {
					IStructuredModel webXmlModel = null;
					try {
						webXmlModel = StructuredModelManager.getModelManager().getModelForRead(webXml);
						IStructuredDocument doc = webXmlModel.getStructuredDocument();
						webXmlString = doc.get();
					} catch (IOException e) {
						MavenJSFActivator.log(e);
					} finally {
						if (webXmlModel != null) {
							webXmlModel.releaseFromRead();
						}
					}
				}
				IDataModel model = MavenJSFActivator.getDefault()
						.createJSFDataModel(fproj, jsfVersion20);
				fproj.installProjectFacet(jsfVersion20, model, monitor);
				if (!configureWebxml() && webXmlExists && webXmlString != null) {
					IStructuredModel webXmlModel = null;
					try {
						webXmlModel = StructuredModelManager.getModelManager().getModelForEdit(webXml);
						IStructuredDocument doc = webXmlModel.getStructuredDocument();
						doc.set(webXmlString);
						webXmlModel.save();
					} catch (IOException e) {
						MavenJSFActivator.log(e);
					} finally {
						if (webXmlModel != null) {
							webXmlModel.releaseFromEdit();
						}
					}
				}
			}
		}
	}

	private IFile getWebXml(IFacetedProject fproj, MavenProject mavenProject) {
		IFile webXml;
		String customWebXml = getCustomWebXml(mavenProject,
				fproj.getProject());
		if (customWebXml == null) {
			webXml = fproj.getProject().getFolder(getWarSourceDirectory(mavenProject,fproj.getProject())).getFile(WEB_XML);
		} else {
			webXml = fproj.getProject().getFile(customWebXml);
		}
		return webXml;
	}

	private String getWarSourceDirectory(MavenProject mavenProject,
			IProject project) {
		Plugin plugin = mavenProject
				.getPlugin("org.apache.maven.plugins:maven-war-plugin");
		if (plugin == null) {
			return null;
		}
		Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
		if (config == null) {
			return WAR_SOURCE_FOLDER;
		}
		Xpp3Dom[] warSourceDirectory = config.getChildren("warSourceDirectory");
		if (warSourceDirectory != null && warSourceDirectory.length > 0) {
			String dir = warSourceDirectory[0].getValue();
			if (project != null) {
				return tryProjectRelativePath(project, dir).toOSString();
			}
			return dir;
		}
		return WAR_SOURCE_FOLDER;
	}

	private IPath tryProjectRelativePath(IProject project,
			String resourceLocation) {
		if (resourceLocation == null) {
			return null;
		}
		IPath projectLocation = project.getLocation();
		IPath directory = Path.fromOSString(resourceLocation);
		if (projectLocation == null || !projectLocation.isPrefixOf(directory)) {
			return directory;
		}
		return directory.removeFirstSegments(projectLocation.segmentCount())
				.makeRelative().setDevice(null);
	}
	

	public String getCustomWebXml(MavenProject mavenProject, IProject project) {
		Plugin plugin = mavenProject
				.getPlugin("org.apache.maven.plugins:maven-war-plugin");
		if (plugin == null) {
			return null;
		}
		Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
		if (config != null) {
			Xpp3Dom webXmlDom = config.getChild("webXml");
			if (webXmlDom != null && webXmlDom.getValue() != null) {
				String webXmlFile = webXmlDom.getValue().trim();
				webXmlFile = getRelativePath(project, webXmlFile);
				return webXmlFile;
			}
		}
		return null;
	}

	private static String getRelativePath(IProject project, String absolutePath) {
		File basedir = project.getLocation().toFile();
		String relative;
		if (absolutePath.equals(basedir.getAbsolutePath())) {
			relative = ".";
		} else if (absolutePath.startsWith(basedir.getAbsolutePath())) {
			relative = absolutePath.substring(basedir.getAbsolutePath()
					.length() + 1);
		} else {
			relative = absolutePath;
		}
		return relative.replace('\\', '/'); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean configureWebxml() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getBoolean(Activator.CONFIGURE_WEBXML_JSF20);
	}

	private String getJSFVersion(MavenProject mavenProject) {
		String version = null;
		version = Activator.getDefault().getDependencyVersion(mavenProject,
				JSF_API_GROUP_ID, JSF_API_ARTIFACT_ID);
		if (version == null) {
			version = Activator.getDefault().getDependencyVersion(mavenProject,
					JSF_API2_GROUP_ID, JSF_API_ARTIFACT_ID);
		}
		return version;
	}

}
