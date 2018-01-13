/*************************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.springboot.configurators;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.wtp.WTPProjectsUtil;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.springboot.MavenSpringBootActivator;
import org.jboss.tools.maven.ui.Activator;

/**
 * 
 * @author snjeza
 *
 */
public class SpringBootProjectConfigurator extends AbstractProjectConfigurator {

	protected static final IProjectFacet jarUtilityFacet;

	static {
		jarUtilityFacet = ProjectFacetsManager.getProjectFacet("jst.utility"); //$NON-NLS-1$
	}

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureInternal(mavenProject, project, monitor);
	}

	private void configureInternal(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {

		if (!isSpringBootConfigurable(mavenProject)) {
			return;
		}
		final IFacetedProject fproj = ProjectFacetsManager.create(project, true, null);
		String packaging = mavenProject.getPackaging();
		if ("jar".equals(packaging) && (fproj != null) && !fproj.hasProjectFacet(jarUtilityFacet)) {
			installJarUtilityFacet(fproj, monitor);
		}
	}

	private boolean isSpringBootConfigurable(MavenProject mavenProject) {
		String springBootActivation = mavenProject.getProperties().getProperty("m2e.springboot.activation");

		boolean configureSpringBoot;
		if (springBootActivation == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			configureSpringBoot = store.getBoolean(Activator.CONFIGURE_SPRING_BOOT);
		} else {
			configureSpringBoot = Boolean.valueOf(springBootActivation);
		}
		return configureSpringBoot;
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event, IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
		if (event.getFlags() == MavenProjectChangedEvent.FLAG_DEPENDENCIES && facade != null) {
			IProject project = facade.getProject();
			MavenProject mavenProject = facade.getMavenProject(monitor);
			configureInternal(mavenProject, project, monitor);
		}
		super.mavenProjectChanged(event, monitor);
	}

	private void installJarUtilityFacet(IFacetedProject fproj, IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(jarUtilityFacet)) {
			IProjectFacetVersion facetVersion = jarUtilityFacet.getDefaultVersion();
			if (facetVersion != null) { // $NON-NLS-1$
				Set<Action> actions = new HashSet<>();
				WTPProjectsUtil.installJavaFacet(actions, fproj.getProject(), fproj);
				IDataModel model = MavenSpringBootActivator.getDefault().createJarUtilityDataModel(fproj, facetVersion);
				actions.add(new Action(Action.Type.INSTALL, facetVersion, model));
				fproj.modify(actions, monitor);
			}
		}
	}

}
