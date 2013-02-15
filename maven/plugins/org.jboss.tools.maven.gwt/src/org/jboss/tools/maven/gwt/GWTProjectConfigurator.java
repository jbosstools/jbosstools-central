/*******************************************************************************
 * Copyright (c) 2012-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.gwt;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.jboss.tools.common.model.project.ProjectHome;
import org.jboss.tools.maven.ui.Activator;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdt.eclipse.core.properties.WebAppProjectProperties;
import com.google.gwt.eclipse.core.compile.GWTCompileSettings;
import com.google.gwt.eclipse.core.modules.IModule;
import com.google.gwt.eclipse.core.modules.ModuleUtils;
import com.google.gwt.eclipse.core.properties.GWTProjectProperties;

public class GWTProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {
	private static final String ERRAI_MARSHALLING_SERVER_CLASS_OUTPUT = " -Derrai.marshalling.server.classOutput=";

	private static final Logger log = LoggerFactory
			.getLogger(GWTProjectConfigurator.class);

	public static final String GWT_WAR_MAVEN_PLUGIN_KEY = "org.codehaus.mojo:gwt-maven-plugin";


	public void configure(ProjectConfigurationRequest projectConfig,
			IProgressMonitor monitor) throws CoreException {
		configureInternal(projectConfig.getMavenProjectFacade(), monitor);
	}
	
	private void configureInternal(IMavenProjectFacade facade, IProgressMonitor monitor) throws CoreException {

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureGWT = store.getBoolean(Activator.CONFIGURE_GWT);
		log.debug("GWT Entry Point Modules configuration is {}",
				configureGWT ? "enabled" : "disabled");
		if (configureGWT
				&& facade.getMavenProject().getPlugin(
						GWT_WAR_MAVEN_PLUGIN_KEY) != null) {
			
			IProject project = facade.getProject();
			String projectName = project.getName();
			IJavaProject javaProject = JavaCore.create(project);
			if (javaProject != null && javaProject.exists()) {

				Plugin pluginConfig = facade.getMavenProject()
						.getPlugin(GWT_WAR_MAVEN_PLUGIN_KEY);

				if (pluginConfig == null) {
					//nothing to do
					return;
				}
				log.debug("Configure Entry Point Modules for GWT Project {}",
						projectName);
				
				List<String> modNames = findModules(pluginConfig, javaProject);

				try {
					List<String> oldModNames = GWTProjectProperties.getEntryPointModules(project);
					if (oldModNames == null || !oldModNames.equals(modNames)) {
						GWTProjectProperties.setEntryPointModules(
								project, modNames);
					}
				} catch (BackingStoreException e) {
					logError(
							"Exception in Maven GWT Configurator, cannot set entry point modules",
							e);
				}

				log.debug("Configure Output location for GWT Project {}",
						projectName);
				IFolder m2ewtpFolder = project.getFolder("target/m2e-wtp/web-resources/");
				IPath fullpath = null;
				IFolder outputfolder = null;
				if (!runsInplace(pluginConfig) && m2ewtpFolder.exists()) {
					fullpath = m2ewtpFolder.getFullPath();
					outputfolder = m2ewtpFolder;
				} else {
					fullpath = ProjectHome
							.getFirstWebContentPath(project);
					if (fullpath != null) {
						outputfolder = project.getWorkspace().getRoot().getFolder(fullpath);
					}
				}
				if (fullpath == null) {
					log.warn("Can't find output folder for project {}. GWT Configuration incomplete",
							projectName);
					return;
				}
				try {
					IPath lastUsedWarOutLocation = WebAppProjectProperties.getLastUsedWarOutLocation(project);
					if (!fullpath.equals(lastUsedWarOutLocation)) {
						WebAppProjectProperties.setLastUsedWarOutLocation(project, fullpath);
					}
				} catch (BackingStoreException e) {
					logError(
							"Exception in Maven GWT Configurator, cannot set war output location",
							e);
				}
				if (isErraiProject(facade)) {
					setErraiVmParams(project, outputfolder.getFolder("WEB-INF/classes").getProjectRelativePath().toPortableString());
				}

			} else {
				log.debug("Skip configurator for non Java project {}",
						projectName);
			}
		}
	}

	private boolean isErraiProject(IMavenProjectFacade mavenFacade) {
		for (Artifact a : mavenFacade.getMavenProject().getArtifacts()) {
			if (a.getArtifactId().contains("errai")){
				return true;
			}
		}
		return false;
	}

	private void setErraiVmParams(IProject project, String path) {
		GWTCompileSettings settings = GWTProjectProperties
				.getGwtCompileSettings(project);
		if (settings == null) {
			settings = new GWTCompileSettings(project);
		}
		String vmArgs = (settings.getVmArgs() == null) ? "" : settings
				.getVmArgs();
		if (!vmArgs.contains(ERRAI_MARSHALLING_SERVER_CLASS_OUTPUT)) {
			vmArgs += ERRAI_MARSHALLING_SERVER_CLASS_OUTPUT + path;
			settings.setVmArgs(vmArgs);

			try {
				GWTProjectProperties.setGwtCompileSettings(project, settings);
			} catch (BackingStoreException e) {
				logError(
						"Exception in Maven GWT Configurator, cannot set VM Parameters for Errai",
						e);
			}
		}
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event,
			IProgressMonitor monitor) throws CoreException {
		configureInternal(event.getMavenProject(), monitor);
	}

	
	private boolean runsInplace(Plugin pluginConfig) {
		Xpp3Dom gwtConfig = (Xpp3Dom) pluginConfig.getConfiguration();
		if (gwtConfig != null) {
			Xpp3Dom inplaceNode = gwtConfig.getChild("inplace");
			if (inplaceNode != null) {
				return Boolean.parseBoolean(inplaceNode.getValue());
			}
		}
		return false;
	}
	
	private List<String> findModules(Plugin pluginConfig,
			IJavaProject javaProject) {
		List<String> modNames = new ArrayList<String>();
		Xpp3Dom gwtConfig = (Xpp3Dom) pluginConfig.getConfiguration();

		if (gwtConfig != null) {
			Xpp3Dom[] moduleNodes = gwtConfig.getChildren("module");
			if (moduleNodes.length > 0) {
				String moduleQNameTrimmed = null;
				for (Xpp3Dom mNode : moduleNodes) {
					moduleQNameTrimmed = mNode.getValue() == null? null : mNode.getValue().trim();
				}
				if (moduleQNameTrimmed != null) {
					modNames.add(moduleQNameTrimmed);
				}
			} else {
				Xpp3Dom modulesNode = gwtConfig.getChild("modules");
				if (modulesNode != null) {
					moduleNodes = modulesNode.getChildren("module");
					for (Xpp3Dom mNode : moduleNodes) {
						String moduleQNameTrimmed = mNode.getValue() == null? null : mNode.getValue().trim();
						if (moduleQNameTrimmed != null) {
							modNames.add(moduleQNameTrimmed);
						}
					}
				}
			}
		}
		if (modNames.isEmpty()) {
			IModule[] modules = ModuleUtils.findAllModules(javaProject, false);
			for (IModule iModule : modules) {
				modNames.add(iModule.getQualifiedName());
				log.debug("\t{}", iModule.getQualifiedName());
			}
		}
		return modNames;
	}

	/**
	 * Report error in logger and eclipse user interface
	 * 
	 * @param message
	 *            - exception context description
	 * @param e
	 *            - exception to report
	 */
	private void logError(final String message, BackingStoreException e) {
		log.error(message, e);
		MavenGWTPlugin.log(message, e);
	}

	@Override
	public void configureClasspath(IMavenProjectFacade facade,
			IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureRawClasspath(final ProjectConfigurationRequest request,
			IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException {
		//Remove non existing source classpath entries as it makes Dev Mode crash
		classpath.removeEntry(new IClasspathDescriptor.EntryFilter() {
			
			@Override
			public boolean accept(IClasspathEntryDescriptor descriptor) {
				if (descriptor.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath p = descriptor.getPath();
					return !request.getProject().getWorkspace().getRoot().getFolder(p).exists();
				}
				return false;
			}
		});
	}
}
