/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.hibernate.configurators;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jpt.common.core.resource.xml.JptXmlResource;
import org.eclipse.jpt.jpa.core.internal.resource.persistence.PersistenceXmlResourceProvider;
import org.eclipse.jpt.jpa.core.resource.persistence.XmlPersistence;
import org.eclipse.jpt.jpa.core.resource.persistence.XmlPersistenceUnit;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.hibernate.eclipse.console.properties.HibernatePropertiesConstants;
import org.hibernate.eclipse.console.utils.LaunchHelper;
import org.hibernate.eclipse.console.utils.ProjectUtils;
import org.hibernate.eclipse.launch.IConsoleConfigurationLaunchConstants;
import org.jboss.tools.maven.hibernate.MavenHibernateActivator;
import org.jboss.tools.maven.ui.Activator;
import org.osgi.service.prefs.Preferences;

/**
 * 
 * @author snjeza
 *
 */
public class HibernateProjectConfigurator extends AbstractProjectConfigurator {

	private static final String HIBERNATE_GROUP_ID = "org.hibernate"; //$NON-NLS-1$
	private static final String HIBERNATE_ARTIFACT_ID_PREFIX = "hibernate"; //$NON-NLS-1$
	
	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureInternal(mavenProject,project, monitor);
	}
	
	private void configureInternal(MavenProject mavenProject,IProject project,
			IProgressMonitor monitor) throws CoreException {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureHibernate = store.getBoolean(Activator.CONFIGURE_HIBERNATE);
		if (!configureHibernate) {
			return;
		}
		
		if (project.hasNature(JavaCore.NATURE_ID) && isHibernateProject(mavenProject)) {
			IScopeContext scope = new ProjectScope(project);
			Preferences node = scope.getNode(HibernatePropertiesConstants.HIBERNATE_CONSOLE_NODE);
			if (node != null) {
				boolean enabled = node.getBoolean(HibernatePropertiesConstants.HIBERNATE3_ENABLED, false);
				if (!enabled) {
					ProjectUtils.toggleHibernateOnProject(project, true, ""); //$NON-NLS-1$
				}
			}
			String persistenceUnitName = getMainPersistenceUnit(project);
			if (persistenceUnitName != null) {
				configureHibernateLaunchConfigurations(project, persistenceUnitName);
			}
			
		}
	}


	private void configureHibernateLaunchConfigurations(IProject project, String persistenceUnitName) throws CoreException {
		if (!project.hasNature(HibernatePropertiesConstants.HIBERNATE_NATURE) || persistenceUnitName == null) {
			return;
		}
		ILaunchConfiguration[] launchConfigs = LaunchHelper.findProjectRelatedHibernateLaunchConfigs(project.getName());
		if (launchConfigs != null && launchConfigs.length > 0) {
			for (ILaunchConfiguration lc : launchConfigs) {
				ILaunchConfigurationWorkingCopy wc = lc.getWorkingCopy();
				try {
					String configurationFactory = wc.getAttribute(IConsoleConfigurationLaunchConstants.CONFIGURATION_FACTORY, "");
					if ("JPA".equals(configurationFactory)) {
						if (wc.getAttribute(IConsoleConfigurationLaunchConstants.PERSISTENCE_UNIT_NAME,"").isEmpty()) {
							wc.setAttribute(IConsoleConfigurationLaunchConstants.PERSISTENCE_UNIT_NAME, persistenceUnitName);
							wc.doSave();
						}
					}
				} catch (CoreException e) {
					IStatus status = new Status(IStatus.ERROR, MavenHibernateActivator.PLUGIN_ID, e.getLocalizedMessage(), e);
					MavenHibernateActivator.getDefault().getLog().log(status );
				}
			}
		}
		
	}

	private String getMainPersistenceUnit(IProject project) {
		PersistenceXmlResourceProvider defaultXmlResourceProvider = PersistenceXmlResourceProvider.getDefaultXmlResourceProvider(project);
		if (defaultXmlResourceProvider == null) {
			return null;
		}
		final JptXmlResource resource = defaultXmlResourceProvider.getXmlResource();
		if (resource == null) {
			return null;
		}
		XmlPersistence persistence = (XmlPersistence) resource.getRootObject();					
		
		if (persistence != null && persistence.getPersistenceUnits() != null && !persistence.getPersistenceUnits().isEmpty()) {
			XmlPersistenceUnit persistenceUnit = persistence.getPersistenceUnits().get(0);
			if (persistenceUnit != null) {
				return persistenceUnit.getName();
			}
		}
		return null;
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event,
			IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
	    if(facade != null) {
	      IProject project = facade.getProject();
	      MavenProject mavenProject = facade.getMavenProject(monitor);
	      configureInternal(mavenProject, project, monitor);
	    }
		super.mavenProjectChanged(event, monitor);
	}

	private boolean isHibernateProject(MavenProject mavenProject) {
		ArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_TEST);
		for (Artifact artifact : mavenProject.getArtifacts()) {
			if (filter.include(artifact)) {
				String groupId = artifact.getGroupId();
				if (HIBERNATE_GROUP_ID.equals(groupId)) {
					String artifactId = artifact.getArtifactId();
					if (artifactId != null && artifactId.startsWith(HIBERNATE_ARTIFACT_ID_PREFIX)) {
						return true;
					} 
				}
			}
		}
	    return false;
	}
}
