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
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.springboot.MavenSpringBootActivator;
import org.jboss.tools.maven.ui.Activator;

/**
 * 
 * Spring Boot projects configurator. Adds a java and utility facet to a spring
 * boot project and it's dependencies.
 *
 * @author Jeff Maury
 * @author Andre Dietisheim
 */
public class SpringBootProjectConfigurator extends AbstractProjectConfigurator {

	private static final String ACTIVATION_PROPERTY = "m2e.springboot.activation";

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureProject(mavenProject, project, monitor);
		configureDependencies(mavenProject, project, monitor);
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event, IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
		if (facade == null) {
			return;
		}
		configureDependencies(facade.getMavenProject(monitor), facade.getProject(), monitor);
	}

	private void configureProject(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (isSpringBootConfigurable(mavenProject)) {
			addUtilityFacet(mavenProject, project, monitor);
		}
	}

	private void configureDependencies(MavenProject mavenProject, IProject project, IProgressMonitor monitor) throws CoreException {
		if (isSpringBootConfigurable(mavenProject)) {
			List<IMavenProjectFacade> dependencies = 
					MavenUtils.getWorkspaceDependencies(mavenProject, project, projectManager, monitor);
			addUtilityFacets(dependencies, project, monitor);
			if (ModuleUtils.hasModuleCoreNature(project)) {
				ModuleUtils.addChildModules(dependencies, project, monitor);
			}
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

	private void addUtilityFacets(List<IMavenProjectFacade> dependencies, IProject project, IProgressMonitor monitor)
			throws CoreException {
		MultiStatus status = new MultiStatus(MavenSpringBootActivator.PLUGIN_ID, 0,
				NLS.bind("Could not add utility facet to dependencies of project {0}", project.getName()), null);
		List<IMavenProjectFacade> unconfiguredDependencies = addUtilityFacetsIfBuilt(dependencies, status, monitor);
		if (!unconfiguredDependencies.isEmpty()) {
			new DelayedDependencyConfigurator(project, unconfiguredDependencies).listenToProjectChanges();			
		}
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	/**
	 * Installs the utility facet to the given projects if they're already built aka
	 * have the java nature. Returns the ones that were not processed.
	 * 
	 * @param projects
	 * @param monitor
	 * @param status
	 * @return
	 */
	private List<IMavenProjectFacade> addUtilityFacetsIfBuilt(List<IMavenProjectFacade> projects, MultiStatus status,
			IProgressMonitor monitor) {
		return projects.stream()
			.filter(facade -> {
				try {
					if (!MavenUtils.hasJavaNature(facade.getProject())) {
						// not built yet, delay facet installation
						return true;
					}
					addUtilityFacet(facade.getMavenProject(), facade.getProject(), monitor);
					return false;
				} catch (CoreException e) {
					status.add(StatusFactory.errorStatus(MavenSpringBootActivator.PLUGIN_ID,
									NLS.bind("Could not add utility facet to project {0}", facade.getProject().getName()), e));
					return true;
				}
			})
			.collect(Collectors.toList());
	}

	private void addUtilityFacet(MavenProject mavenProject, IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (mavenProject == null
				|| !MavenUtils.isJarPackaginging(mavenProject.getPackaging())) {
			return;
		}

		ModuleUtils.addUtilityFacet(project, monitor);
	}

	/**
	 * A configurator that configures spring-boot project dependency upon java core change notifications.
	 * 
	 * @see IElementChangeListener
	 */
	private class DelayedDependencyConfigurator implements IElementChangedListener {

		private final Queue<IMavenProjectFacade> unconfiguredDependencies;
		private final IProject project;

		public DelayedDependencyConfigurator(IProject project, List<IMavenProjectFacade> facades) {
			this.project = project;
			this.unconfiguredDependencies = new ConcurrentLinkedQueue<>(facades);
		}

		public void listenToProjectChanges() {
			JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
		}

		private void stop() {
			JavaCore.removeElementChangedListener(this);
		}
	
		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (IJavaElementDelta.REMOVED == event.getDelta().getKind()) {
				return;
			}

			IMavenProjectFacade unconfigured = getUnconfigured(event);
			if (unconfigured == null
				|| isRemoveDependency(unconfigured, event)) {
				return;
			}

			configure(unconfigured);
		}

		private boolean isRemoveDependency(IMavenProjectFacade dependency, ElementChangedEvent event) {
			boolean isRemoved = false;
			IJavaElementDelta eventSource = (IJavaElementDelta) event.getSource();
			if (eventSource.getElement().getResource() instanceof IWorkspaceRoot) {
				IJavaElementDelta[] removedElements = eventSource.getRemovedChildren();
				isRemoved = Arrays.stream(removedElements)
								.anyMatch(removedElement -> dependency.getProject().equals(
										removedElement.getElement().getResource()));
			}
			return isRemoved;
		}

		private void configure(IMavenProjectFacade facade) {

			new WorkspaceJob(NLS.bind("Adding utility facet to project {0}", facade.getProject().getName())) {
				
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					try {
						MavenProject mavenProject = facade.getMavenProject(monitor);
						addUtilityFacet(mavenProject, facade.getProject(), monitor);
						if (ModuleUtils.hasModuleCoreNature(project)) {
							ModuleUtils.addChildModule(facade.getProject(), project, monitor);
						}
						unconfiguredDependencies.remove(facade);
						if (unconfiguredDependencies.isEmpty()) {
							stop();
						}
						return Status.OK_STATUS;
					} catch (Exception e) {
						MavenCoreActivator.logError(e);
						return StatusFactory.errorStatus(MavenCoreActivator.PLUGIN_ID, 
								NLS.bind("Could not add utility facet to project {0}.", facade.getProject().getName()));
					}
				}
			}.schedule();
		}

		private IMavenProjectFacade getUnconfigured(ElementChangedEvent event) {
			return unconfiguredDependencies.stream()
				.filter(dependency -> Arrays.stream(event.getDelta().getAffectedChildren())
						.anyMatch(affected -> Objects.equals(affected.getElement().getResource(), dependency.getProject())))
				.findFirst()
				.orElse(null);
		}
	}
}
