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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.j2ee.internal.project.facet.UtilityFacetInstallDataModelProvider;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.wtp.WTPProjectsUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.maven.springboot.MavenSpringBootActivator;
import org.jboss.tools.maven.ui.Activator;

/**
 * 
 * Spring Boot projects configurator. Adds a java and utility facet to a spring
 * boot project and it's dependencies.
 *
 * @author Jeff Maury
 * @contributor Andre Dietisheim
 */
public class SpringBootProjectConfigurator extends AbstractProjectConfigurator {

	private static final String PACKAGING_JAR = "jar";
	private static final String ACTIVATION_PROPERTY = "m2e.springboot.activation";
	private static final IProjectFacet utilityFacet = 
			ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_UTILITY_MODULE);

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureProject(mavenProject, project, monitor);
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event, IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
		if (facade != null) {
			MavenProject mavenProject = facade.getMavenProject(monitor);
			IProject project = facade.getProject();
			configureDepdendencies(mavenProject, project, monitor);
		}
		super.mavenProjectChanged(event, monitor);
	}

	private void configureProject(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (isSpringBootConfigurable(mavenProject)) {
			installUtilityFacet(mavenProject, project, monitor);
		}
	}

	private void configureDepdendencies(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (isSpringBootConfigurable(mavenProject)) {
			installUtilityFacet(mavenProject.getName(), getWorkspaceDependencies(mavenProject, project), monitor);
		}
	}

	private void installUtilityFacet(String dependentProject, List<IMavenProjectFacade> facades, 
			IProgressMonitor monitor) throws CoreException {
		MultiStatus status = new MultiStatus(MavenSpringBootActivator.PLUGIN_ID, 0,
				NLS.bind("Could not add utility facet to dependencies of project {0}", dependentProject), null);
		facades.forEach(facade -> {
			MavenProject mavenDependency = null;
			try {
				mavenDependency = facade.getMavenProject(monitor);
				installUtilityFacet(mavenDependency, facade.getProject(), monitor);
			} catch (CoreException e) {
				status.add(StatusFactory.errorStatus(MavenSpringBootActivator.PLUGIN_ID,
								NLS.bind("Could not add utility facet to project {0}", mavenDependency != null ? 
										mavenDependency.getName() : facade.getProject().getName()), e));
			}
		});
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	private void installUtilityFacet(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {
		final IFacetedProject facetedProj = ProjectFacetsManager.create(project, true, null);
		String packaging = mavenProject.getPackaging();
		if (PACKAGING_JAR.equals(packaging)
				&& facetedProj != null 
				&& !facetedProj.hasProjectFacet(utilityFacet)) {
			installUtilityFacet(facetedProj, monitor);
		}
	}

	private boolean isSpringBootConfigurable(MavenProject mavenProject) {
		String springBootActivation = mavenProject.getProperties().getProperty(ACTIVATION_PROPERTY);
		boolean configureSpringBoot = false;
		if (springBootActivation == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			configureSpringBoot = store.getBoolean(Activator.CONFIGURE_SPRING_BOOT);
		} else {
			configureSpringBoot = Boolean.valueOf(springBootActivation);
		}
		return configureSpringBoot;
	}

	private void installUtilityFacet(IFacetedProject facetedProj, IProgressMonitor monitor) throws CoreException {
		if (facetedProj.hasProjectFacet(utilityFacet)) {
			return;
		}
		IProjectFacetVersion facetVersion = utilityFacet.getDefaultVersion();
		if (facetVersion == null) { // $NON-NLS-1$
			return;
		}
		Set<Action> actions = new HashSet<>();
		WTPProjectsUtil.installJavaFacet(actions, facetedProj.getProject(), facetedProj);
		ModuleCoreNature.addModuleCoreNatureIfNecessary(facetedProj.getProject(), monitor);
		addUtilityFacetAction(actions, facetVersion);
		facetedProj.modify(actions, monitor);
	}

	private void addUtilityFacetAction(Set<Action> actions, IProjectFacetVersion facetVersion) {
		IDataModel model = (IDataModel) new UtilityFacetInstallDataModelProvider().create();
		actions.add(new Action(Action.Type.INSTALL, facetVersion, model));
	}

	protected List<IMavenProjectFacade> getWorkspaceDependencies(MavenProject mavenProject, IProject project) {
		return mavenProject.getArtifacts().stream()
			.filter(artifact -> isDeployableScope(artifact.getScope()))
			.map(artifact -> {
				IMavenProjectFacade dependency = 
						projectManager.getMavenProject(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
				if (dependency != null
					&& !dependency.getProject().equals(project) 
					&& dependency.getFullPath(artifact.getFile()) != null) {
					return dependency;
				} else {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
	}

	private boolean isDeployableScope(String scope) {
		return Artifact.SCOPE_COMPILE.equals(scope)
				//MNGECLIPSE-1578 Runtime dependencies should be deployed 
				|| Artifact.SCOPE_RUNTIME.equals(scope);
	}
}
