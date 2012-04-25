/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.gwt;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.jboss.tools.common.model.project.ProjectHome;
import org.jboss.tools.maven.ui.Activator;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdt.eclipse.core.properties.WebAppProjectProperties;
import com.google.gwt.eclipse.core.modules.IModule;
import com.google.gwt.eclipse.core.modules.ModuleUtils;
import com.google.gwt.eclipse.core.properties.GWTProjectProperties;

public class GWTProjectConfigurator extends AbstractProjectConfigurator {
	private static final Logger log = LoggerFactory.getLogger(GWTProjectConfigurator.class);

	public static final String GWT_WAR_MAVEN_PLUGIN_KEY = "org.codehaus.mojo:gwt-maven-plugin";
	
	@Override
	public void configure(ProjectConfigurationRequest projectConfig, IProgressMonitor monitor) throws CoreException {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureGWT = store.getBoolean(Activator.CONFIGURE_GWT);
		log.debug("GWT Entry Point Modules configuration is {}",configureGWT ? "enabled" : "disabled");
		if(configureGWT && projectConfig.getMavenProject().getPlugin(GWT_WAR_MAVEN_PLUGIN_KEY)!=null) {
			String projectName = projectConfig.getProject().getName();
			IJavaProject javaProject = JavaCore.create(projectConfig.getProject());
			if(javaProject!=null) {
				log.debug("Configure Entry Point Modules for GWT Project {}", projectName);
				
				List<String> modNames = new ArrayList<String>();
				
				Plugin pluginConfig = projectConfig.getMavenProject().getPlugin(GWT_WAR_MAVEN_PLUGIN_KEY);
				
				Xpp3Dom gwtConfig = (Xpp3Dom)pluginConfig.getConfiguration();
                
                if (gwtConfig!=null) {
                    Xpp3Dom[] moduleNodes = gwtConfig.getChildren("module");
                    if (moduleNodes.length > 0) {
                    	String moduleQNameTrimmed = null;
                    	for (Xpp3Dom mNode : moduleNodes) {
                        	moduleQNameTrimmed = mNode.getValue().trim();
                        }
                    	if(moduleQNameTrimmed != null){
                    		modNames.add(moduleQNameTrimmed);
                    	}
                    } else { 
                        Xpp3Dom modulesNode = gwtConfig.getChild("modules");
                        if (modulesNode != null) {
                            moduleNodes = modulesNode.getChildren("module");
                            for (Xpp3Dom mNode : moduleNodes) {
                            	String moduleQNameTrimmed = mNode.getValue().trim();
                                modNames.add(moduleQNameTrimmed);
                            }
                        }
                    }
                }
				
				if(modNames.size() == 0){
					IModule[] modules = ModuleUtils.findAllModules(javaProject,false);
					modNames = new ArrayList<String>();
					for (IModule iModule : modules) {
						modNames.add(iModule.getQualifiedName());
						log.debug("\t{}",iModule.getQualifiedName());
					}
				}

				try {
					GWTProjectProperties.setEntryPointModules(projectConfig.getProject(), modNames);
				} catch (BackingStoreException e) {
					logError("Exception in Maven GWT Configurator, cannot set entry point modules", e);
				}
				
				log.debug("Configure Output location for GWT Project {}", projectName);
				try {
					IPath webContentPath = getWebContentFolder(projectConfig.getProject(), monitor);
					IFolder outputWorkspaceFolder = projectConfig.getProject().getWorkspace().getRoot().getFolder(webContentPath);
					WebAppProjectProperties.setLastUsedWarOutLocation(projectConfig.getProject(), outputWorkspaceFolder.getFullPath());
				} catch (BackingStoreException e) {
					logError("Exception in Maven GWT Configurator, cannot set war output location", e);
				}

			} else {
				log.debug("Skip configurator for non Java project {}",projectName);
			}
		}
	}

	/**
	 * Report error in logger and eclipse user interface
	 * @param message - exception context description 
	 * @param e - exception to report
	 */
	private void logError(final String message, BackingStoreException e) {
		log.error(message, e);
		MavenGWTPlugin.log(message,e);
	}
	
	private IPath getWebContentFolder(IProject project, IProgressMonitor monitor) throws CoreException {
		IPath webContentPath = ProjectHome.getFirstWebContentPath(project);
		Assert.isTrue(webContentPath != null && !webContentPath.isEmpty(),
				MessageFormat
						.format("No web content folder was found in project {0}", project.getName()));
		return webContentPath;
	}
}
