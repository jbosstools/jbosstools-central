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
package org.jboss.tools.maven.hibernate.configurators;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.hibernate.eclipse.console.properties.HibernatePropertiesConstants;
import org.hibernate.eclipse.console.utils.ProjectUtils;
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
		if (!isHibernateConfigurable(mavenProject) || project.hasNature(HibernatePropertiesConstants.HIBERNATE_NATURE)) {
			return;
		}
		
		if (project.hasNature(JavaCore.NATURE_ID) && isHibernateProject(mavenProject)) {
			IScopeContext scope = new ProjectScope(project);
			Preferences node = scope.getNode(HibernatePropertiesConstants.HIBERNATE_CONSOLE_NODE);
			if (node != null) {
				boolean enabled = node.getBoolean(HibernatePropertiesConstants.HIBERNATE3_ENABLED, false);
				if (enabled) {
					return;
				}
			}
            ProjectUtils.toggleHibernateOnProject(project, true, project.getName());
		}
	}

	private boolean isHibernateConfigurable(MavenProject mavenProject) {
		String hibernateActivation = mavenProject.getProperties().getProperty("m2e.hibernate.activation");
		
		boolean configureHibernate; 
		if (hibernateActivation == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			configureHibernate = store.getBoolean(Activator.CONFIGURE_HIBERNATE);
		} else {
		  configureHibernate = Boolean.valueOf(hibernateActivation);
		}
		return configureHibernate;
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
