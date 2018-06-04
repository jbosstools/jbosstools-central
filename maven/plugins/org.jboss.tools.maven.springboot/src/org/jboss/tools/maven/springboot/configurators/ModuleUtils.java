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
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jst.j2ee.internal.project.facet.UtilityFacetInstallDataModelProvider;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
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
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.springboot.MavenSpringBootActivator;

public class ModuleUtils {

	private static final IProjectFacet utilityFacet = 
			ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_UTILITY_MODULE);

	private ModuleUtils() {
	}

	public static boolean hasModuleCoreNature(IProject project) {
		return ModuleCoreNature.getModuleCoreNature(project) != null;
	}

	public static void addUtilityFacet(IProject project, IProgressMonitor monitor) throws CoreException {
		final IFacetedProject facetedProj = ProjectFacetsManager.create(project, true, monitor);
		addUtilityFacet(facetedProj, monitor);
	}

	private static void addUtilityFacet(IFacetedProject facetedProj, IProgressMonitor monitor) throws CoreException {
		if (facetedProj == null
				|| facetedProj.hasProjectFacet(utilityFacet)) {
			return;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.beginTask(NLS.bind("Adding utility facet to project {0}", 
				facetedProj.getProject() != null ? facetedProj.getProject().getName() : ""), 1);
		try {
			Set<Action> actions = new HashSet<>();
			ModuleCoreNature.addModuleCoreNatureIfNecessary(facetedProj.getProject(), monitor);
			addUtilityFacetAction(actions);
			WTPProjectsUtil.installJavaFacet(actions, facetedProj.getProject(), facetedProj);
			facetedProj.modify(actions, monitor);
		} finally {
			subMonitor.done();
		}
	}

	private static void addUtilityFacetAction(Set<Action> actions) throws CoreException {
		IProjectFacetVersion facetVersion = utilityFacet.getDefaultVersion();
		if (facetVersion == null) {
			throw new CoreException(StatusFactory.errorStatus(MavenCoreActivator.PLUGIN_ID, 
					"Could not get utility facet default version."));
		}
		IDataModel model = (IDataModel) new UtilityFacetInstallDataModelProvider().create();
		actions.add(new Action(Action.Type.INSTALL, facetVersion, model));
	}


	/**
	 * Adds the given dependencies as child modules to the given project
	 * @param project
	 * @param dependencies
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addChildModules(List<IMavenProjectFacade> dependencies, IProject project, IProgressMonitor monitor) throws CoreException {
		if (dependencies == null
				|| dependencies.isEmpty()) {
			return;
		}

		IVirtualComponent projectComponent = createVirtualComponent(project);
		MultiStatus status = new MultiStatus(MavenSpringBootActivator.PLUGIN_ID, 0,
				NLS.bind("Could not add child modules to project {0}", project), null);
		dependencies.forEach(dependency -> {
			IProject dependencyProject = dependency.getProject();
			try {
				addChildModule(dependencyProject, projectComponent, monitor);
			} catch (CoreException e) {
				status.add(StatusFactory.errorStatus(MavenSpringBootActivator.PLUGIN_ID,
								NLS.bind("Could not add project {0} as child module to project {1}", 
										dependencyProject.getName(),
										project.getName()), 
								e));
			}
		});
		if (!status.isOK()) {
			throw new CoreException(status);
		}

	}

	/**
	 * Returns a new virtual component for the given project.
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 * 
	 * @see IVirtualComponent
	 */
	public static IVirtualComponent createVirtualComponent(IProject project) throws CoreException {
		IVirtualComponent projectComponent = ComponentCore.createComponent(project);
		if (projectComponent == null) {
			throw new CoreException(
					StatusFactory.errorStatus(MavenSpringBootActivator.PLUGIN_ID, 
							NLS.bind("Project {0} is missing the module core nature", project.getName())));
		}
		return projectComponent;
	}

	/**
	 * Adds the given dependency as a child module to the given project. If the child
	 * module already exists, no new child is added.
	 * 
	 * @param dependency
	 * @param project
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addChildModule(IProject dependency, IProject project, IProgressMonitor monitor) 
			throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 
				NLS.bind("Adding child module {0} to project {1}.", dependency.getName(), project.getName()), 1);
		addChildModule(dependency, ModuleUtils.createVirtualComponent(project), monitor);
		subMonitor.done();
	}

	/**
	 * Adds the given dependency as a child module to the given project. If the
	 * child module already exists, no new child is added.
	 * 
	 * @param dependency
	 * @param project
	 * @param monitor
	 * @throws CoreException
	 */
	private static void addChildModule(IProject dependency, IVirtualComponent project, IProgressMonitor monitor) 
			throws CoreException {
		IVirtualComponent dependencyComponent = createVirtualComponent(dependency);
		IVirtualReference dependencyReference = new VirtualReference(project, dependencyComponent);
		addReference(dependencyReference, project, monitor);
	}

	private static void addReference(IVirtualReference dependency, IVirtualComponent project, IProgressMonitor monitor) 
			throws CoreException {
		if (hasReference(project, dependency)) {
			log(NLS.bind("Project {0} already has a child module for {1}. Skipping.",
					project.getName(), dependency.getReferencedComponent().getName()));
			return;
		}
		IDataModel model = DataModelFactory.createDataModel(new AddReferenceDataModelProvider());
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

	/**
	 * Returns {@code true} if the given project has the given dependency as child
	 * module. Returns {@code false} if it's not. Return {@code true}, too, if
	 * dependency resolution fails for the given project.
	 * 
	 * @param project
	 * @param dependency
	 * @param model
	 * @return
	 */
	private static boolean hasReference(IVirtualComponent project, IVirtualReference dependency) {
		try {
			IVirtualReference[] references = project.getReferences();
			for (int i = 0; i < references.length; i++) {
				if (references[i].getReferencedComponent().equals(dependency.getReferencedComponent())) {
					return true; // already present
				}
			}
			return false;
		} catch (Exception e) {
			// reference resolution failed
			return true;
		}
	}

	private static void log(String message) {
		IStatus status = StatusFactory.infoStatus(MavenCoreActivator.PLUGIN_ID, message);
		MavenCoreActivator.getDefault().getLog().log(status);
	}
}
