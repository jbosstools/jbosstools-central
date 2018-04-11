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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.j2ee.internal.project.facet.UtilityFacetInstallDataModelProvider;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.wtp.WTPProjectsUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.datamodel.properties.IAddReferenceDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.operation.AddReferenceDataModelProvider;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualReference;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
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
		if (facade == null) {
			return;
		}
		MavenProject mavenProject = facade.getMavenProject(monitor);
		IProject project = facade.getProject();
		configureDepdendencies(mavenProject, project, monitor);
	}

	private void configureProject(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (isSpringBootConfigurable(mavenProject)) {
			installUtilityFacet(mavenProject, project, monitor);
			addChildModules(project, getWorkspaceDependencies(mavenProject, project), monitor);
		}
	}

	private void configureDepdendencies(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (isSpringBootConfigurable(mavenProject)) {
			List<IMavenProjectFacade> dependencies = getWorkspaceDependencies(mavenProject, project);
			installUtilityFacet(mavenProject.getName(), dependencies, monitor);
			addChildModules(project, dependencies, monitor);
		}
	}

	private void addChildModules(IProject project, List<IMavenProjectFacade> dependencies, IProgressMonitor monitor) throws CoreException {
		IVirtualComponent projectComponent = ComponentCore.createComponent(project);
		if (projectComponent == null) {
			throw new CoreException(
					StatusFactory.errorStatus(MavenSpringBootActivator.PLUGIN_ID, 
							NLS.bind("Project {0} is missing the module core nature", project.getName())));
		}
		MultiStatus status = new MultiStatus(MavenSpringBootActivator.PLUGIN_ID, 0,
				NLS.bind("Could not add child modules to project {0}", project), null);
		dependencies.forEach(dependency -> {
			IProject dependencyProject = dependency.getProject();
			try {
				IVirtualComponent dependencyComponent = ComponentCore.createComponent(dependencyProject);
				IVirtualReference dependencyReference = new VirtualReference(projectComponent, dependencyComponent);
				addReference(dependencyReference, projectComponent, monitor);
			} catch (CoreException e) {
				status.add(StatusFactory.errorStatus(MavenSpringBootActivator.PLUGIN_ID,
								NLS.bind("Could not add project {0} as child module to project {1}", 
										dependencyProject.getName(),
										project.getName()), e));
			}
		});
		if (!status.isOK()) {
			throw new CoreException(status);
		}

	}

	protected void addReference(IVirtualReference dependency, IVirtualComponent project, IProgressMonitor monitor) throws CoreException {
		IDataModelProvider provider = new AddReferenceDataModelProvider();
		IDataModel model = DataModelFactory.createDataModel(provider);
		if (hasChildModule(project, dependency, model)) {
			return;
		}
		model.setProperty(IAddReferenceDataModelProperties.SOURCE_COMPONENT, project);
		model.setProperty(IAddReferenceDataModelProperties.TARGET_REFERENCE_LIST, Arrays.asList(dependency));
		
		IStatus status = model.validateProperty(IAddReferenceDataModelProperties.TARGET_REFERENCE_LIST);
		if (!status.isOK())
			throw new CoreException(status);
		try {
			model.getDefaultOperation().execute(monitor, null);
		} catch (ExecutionException e) {
			throw new CoreException(status);
		}	
	}

	private boolean hasChildModule(IVirtualComponent project, IVirtualReference dependency, IDataModel dm) {
		IVirtualReference[] references = project.getReferences();
		for (int i = 0; i < references.length; i++) {
			if (references[i].getReferencedComponent().equals(dependency.getReferencedComponent())) {
				return true; // already present
			}
		}
		return false;
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
		if (PACKAGING_JAR.equals(packaging)) {
			addNature(ModuleCoreNature.MODULE_NATURE_ID, project, monitor);
			installUtilityFacet(facetedProj, monitor);
		}
	}

	private void addNature(String nature, IProject project, IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(nature != null && !nature.isEmpty());
		if (project != null && project.isAccessible() 
				&& !project.hasNature(nature)) {

			IProjectDescription desc = project.getDescription();
			String[] natureIds = desc.getNatureIds();
			String[] newNatureIds = new String[natureIds.length + 1];

			System.arraycopy(natureIds, 0, newNatureIds, 1, natureIds.length);
			newNatureIds[0] = nature;
			desc.setNatureIds(newNatureIds);

			project.getProject().setDescription(desc, monitor);
		}
	}
	
	private void installUtilityFacet(IFacetedProject facetedProj, IProgressMonitor monitor) throws CoreException {
		if (facetedProj != null
				&& facetedProj.hasProjectFacet(utilityFacet)) {
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
