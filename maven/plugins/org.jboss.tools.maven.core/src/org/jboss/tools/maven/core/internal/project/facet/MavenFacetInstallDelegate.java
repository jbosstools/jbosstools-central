/*************************************************************************************
 * Copyright (c) 2009-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.internal.project.facet;

import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderOperationConfig;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.Messages;
import org.jboss.tools.maven.core.ProjectUtil;
import org.jboss.tools.maven.core.libprov.MavenLibraryProviderInstallOperation;

/**
 * @author snjeza
 * 
 */
public class MavenFacetInstallDelegate implements IDelegate {

	private static final String SEAM_FACET_ID = "jst.seam"; //$NON-NLS-1$

	public void execute(IProject project, IProjectFacetVersion fv, Object cfg,
			IProgressMonitor monitor) throws CoreException {
		IDataModel config = null;

		if (cfg != null) {
			config = (IDataModel) cfg;
		} else {
			throw new CoreException(
					MavenCoreActivator
							.getStatus(Messages.MavenFacetInstallDelegate_Internal_Error_creating_JBoss_Maven_Facet));
		}

		IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
		IJavaProject javaProject = JavaCore.create(project);
		IFacetedProjectWorkingCopy fpwc = null;
		try {
			fpwc = (IFacetedProjectWorkingCopy) config.getProperty(IFacetDataModelProperties.FACETED_PROJECT_WORKING_COPY);
			if (!pom.exists()) {
				Model model = new Model();
				model.setModelVersion(IJBossMavenConstants.MAVEN_MODEL_VERSION);
				model.setGroupId(config.getStringProperty(IJBossMavenConstants.GROUP_ID));
				String artifactId = config.getStringProperty(IJBossMavenConstants.ARTIFACT_ID);
				model.setArtifactId(artifactId);
				model.setVersion(config.getStringProperty(IJBossMavenConstants.VERSION));
				model.setName(config.getStringProperty(IJBossMavenConstants.NAME));
				String packaging = config.getStringProperty(IJBossMavenConstants.PACKAGING);
				model.setPackaging(packaging);
				String description = config.getStringProperty(IJBossMavenConstants.DESCRIPTION);
				if (description != null && description.trim().length() > 0) {
					model.setDescription(description);
				}
				Build build = new Build();
				model.setBuild(build);

				// build.setFinalName(artifactId);
				if (fpwc.hasProjectFacet(JavaFacet.FACET)) {
//					String outputDirectory = MavenCoreActivator.getOutputDirectory(javaProject);
//					if (!"${basedir}/target/classes".equals(outputDirectory)) {
//						build.setOutputDirectory(outputDirectory);
//					}
					String sourceDirectory = MavenCoreActivator.getSourceDirectory(javaProject);
					if (sourceDirectory != null && !"${basedir}/src/main/java".equals(sourceDirectory)) {
						build.setSourceDirectory(sourceDirectory);
					}
					MavenCoreActivator.addResource(build, project, sourceDirectory);
					
				}

				IProjectFacetVersion webFacetVersion = fpwc.getProjectFacetVersion(IJ2EEFacetConstants.DYNAMIC_WEB_FACET); 
				if (webFacetVersion != null && "war".equals(packaging)) {
					MavenCoreActivator.addMavenWarPlugin(build, project, webFacetVersion);
				}
				IProjectFacetVersion ejbFacetVersion = fpwc.getProjectFacetVersion(IJ2EEFacetConstants.EJB_FACET); 
				if (ejbFacetVersion != null && "ejb".equals(packaging)) {
					MavenCoreActivator.addMavenEjbPlugin(build, project, ejbFacetVersion);
				}
				IProjectFacetVersion earFacetVersion = fpwc.getProjectFacetVersion(IJ2EEFacetConstants.ENTERPRISE_APPLICATION_FACET); 
				if (earFacetVersion != null  && "ear".equals(packaging)) {
					MavenCoreActivator.addMavenEarPlugin(build, project, config, null, earFacetVersion, false);
					MavenCoreActivator.createMavenProject(project.getName(), monitor, model, true);
				}
				IProjectFacet seamFacet = null; 
				if (ProjectFacetsManager.isProjectFacetDefined(SEAM_FACET_ID)) {
					seamFacet = ProjectFacetsManager.getProjectFacet(SEAM_FACET_ID);
				}
				if (!"pom".equals(packaging) && (seamFacet == null || !fpwc.hasProjectFacet(seamFacet))) {
					MavenCoreActivator.addCompilerPlugin(build.getPlugins(), project);
				}

				if (!pom.exists()) {
					MavenModelManager modelManager = MavenPlugin.getMavenModelManager();
					modelManager.createMavenModel(pom, model);
				}
			}

			MavenCoreActivator.addMavenNature(project,	monitor);

			if (fpwc.hasProjectFacet(WebFacetUtils.WEB_FACET)) {
				IClasspathAttribute attribute = JavaCore
						.newClasspathAttribute(
								IClasspathDependencyConstants.CLASSPATH_COMPONENT_DEPENDENCY,
								ClasspathDependencyUtil.getDefaultRuntimePath(true).toString());
				MavenCoreActivator.addClasspathAttribute(javaProject, attribute, monitor);
			}
			// FIXME
			IClasspathAttribute attribute = JavaCore.newClasspathAttribute(
					MavenCoreActivator.OWNER_PROJECT_FACETS_ATTR,
					IJBossMavenConstants.M2_FACET_ID);
			MavenCoreActivator.addClasspathAttribute(javaProject, attribute, monitor);

			List<LibraryProviderOperationConfig> configs = MavenCoreActivator.getLibraryProviderOperationConfigs();
			if (configs.size() > 0) {
				MavenLibraryProviderInstallOperation operation = new MavenLibraryProviderInstallOperation();
				for (LibraryProviderOperationConfig libraryProviderOperationConfig : configs) {
					operation.execute(libraryProviderOperationConfig, monitor);
				}
				configs.clear();
			}
			
			ProjectUtil.removeWTPContainers(config, project);
		} finally {
			if (fpwc != null) {
				fpwc.dispose();
			}
		}
	}

}
