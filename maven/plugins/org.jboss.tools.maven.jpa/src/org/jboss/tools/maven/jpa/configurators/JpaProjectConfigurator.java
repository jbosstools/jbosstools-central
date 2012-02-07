/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jpa.configurators;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jpt.jpa.core.JpaFacet;
import org.eclipse.jpt.jpa.core.JptJpaCorePlugin;
import org.eclipse.jpt.jpa.core.internal.facet.JpaFacetDataModelProperties;
import org.eclipse.jpt.jpa.core.internal.facet.JpaFacetInstallDataModelProperties;
import org.eclipse.jpt.jpa.core.internal.facet.JpaFacetInstallDataModelProvider;
import org.eclipse.jpt.jpa.core.internal.resource.persistence.PersistenceXmlResourceProvider;
import org.eclipse.jpt.jpa.core.platform.JpaPlatformDescription;
import org.eclipse.jpt.jpa.core.resource.persistence.XmlPersistenceUnit;
import org.eclipse.jpt.jpa.core.resource.xml.JpaXmlResource;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.internal.JavaFacetUtil;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryInstallDelegate;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ILifecycleMappingConfiguration;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.core.MavenUtil;
import org.jboss.tools.maven.jpa.MavenJpaActivator;
import org.jboss.tools.maven.ui.Activator;

/**
 * JPA Project configurator. Will install the JPA facet on a maven project containing a persistence.xml.
 *
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class JpaProjectConfigurator extends AbstractProjectConfigurator {

	private static final String JPA_NO_OP_LIBRARY_PROVIDER = "jpa-no-op-library-provider";

	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		IProject project = request.getProject();
		
		if(!canConfigure(project)) {
			return;
		}

		IFile persistenceXml = JptUtils.getPersistenceXml(project);
		if (persistenceXml == null || !persistenceXml.exists()) {
			//No persistence.xml => not a JPA project 
			return;
		}
		
		IFacetedProject facetedProject = ProjectFacetsManager.create(project, true, monitor);
		if (facetedProject != null) {
			//Refresh parent in multi-module setups, or Dali pukes 
			MavenUtil.refreshParent(request.getMavenProject());
			
			configureFacets(monitor, project, facetedProject, persistenceXml);
		} 
	}

	private void configureFacets(IProgressMonitor monitor, IProject project,
			IFacetedProject facetedProject, IFile persistenceXml)
			throws CoreException {
		
		//Need to refresh the persistence.xml as the resource provider might crash badly on some occasions
		//if it finds the file is out-of-sync
		persistenceXml.refreshLocal(IResource.DEPTH_ZERO, null);
		
		PersistenceXmlResourceProvider provider = PersistenceXmlResourceProvider.getXmlResourceProvider(persistenceXml);
		
		JpaXmlResource jpaXmlResource = provider.getXmlResource(); 
		 
		IProjectFacetVersion version = JptUtils.getVersion(jpaXmlResource);
		
		JpaPlatformDescription platform = getPlatform(jpaXmlResource, version);
		
		IDataModel dataModel = getDataModel(facetedProject, version, platform);

		Set<Action> actions = new LinkedHashSet<Action>();
		installJavaFacet(actions, project, facetedProject);
		actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.INSTALL, 
								                version, 
								                dataModel));
	    facetedProject.modify(actions, monitor);
	}

	
	private JpaPlatformDescription getPlatform(JpaXmlResource persistenceXml, IProjectFacetVersion facetVersion) {
		XmlPersistenceUnit xmlPersistenceUnit = JptUtils.getFirstXmlPersistenceUnit(persistenceXml);
		if (xmlPersistenceUnit == null) {
			return null;
		}
		PlatformIdentifierManager identifierManager = MavenJpaActivator.getDefault().getPlatformIdentifierManager();
		String platformType = identifierManager.identify(xmlPersistenceUnit);
		if (platformType != null) {
			for (JpaPlatformDescription platform : JptJpaCorePlugin.getJpaPlatformManager().getJpaPlatforms()) {
				if (platform.supportsJpaFacetVersion(facetVersion) && platform.getId().contains(platformType)) {
					return platform;
				}
			}
		}
		//If no adequate platform found, Dali will use a default one.
		return null;
	}
	
	private IDataModel getDataModel(IFacetedProject facetedProject,
									IProjectFacetVersion version, 
									JpaPlatformDescription platform) {
		
		IDataModel dm = DataModelFactory.createDataModel(new JpaFacetInstallDataModelProvider()); 

		dm.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, version.getVersionString()); 
		dm.setProperty(JpaFacetDataModelProperties.PLATFORM, platform); 
		dm.setProperty(JpaFacetInstallDataModelProperties.CREATE_ORM_XML, false);
		dm.setProperty(JpaFacetInstallDataModelProperties.DISCOVER_ANNOTATED_CLASSES, true);
		
		LibraryInstallDelegate libraryInstallDelegate = getNoOpLibraryProvider(facetedProject, version);
		dm.setProperty(JpaFacetInstallDataModelProperties.LIBRARY_PROVIDER_DELEGATE, libraryInstallDelegate);
		
		return dm;
	}

	private boolean canConfigure(IProject project) throws CoreException {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureJpa = store.getBoolean(Activator.CONFIGURE_JPA);
		return configureJpa 
				&& !JpaFacet.isInstalled(project) 
				&& project.hasNature(JavaCore.NATURE_ID);
	}

	private LibraryInstallDelegate getNoOpLibraryProvider(IFacetedProject facetedProject, IProjectFacetVersion facetVersion) {
		LibraryInstallDelegate libraryDelegate = new LibraryInstallDelegate(facetedProject, facetVersion);
		ILibraryProvider provider = LibraryProviderFramework.getProvider(JPA_NO_OP_LIBRARY_PROVIDER); 
		libraryDelegate.setLibraryProvider(provider);
		return libraryDelegate;
	}
	
	@Override
	public boolean hasConfigurationChanged(IMavenProjectFacade newFacade,
			ILifecycleMappingConfiguration oldProjectConfiguration,
			MojoExecutionKey key, IProgressMonitor monitor) {
		//Changes to maven-compiler-plugin in pom.xml don't make it "dirty" wrt JPA config
		return false;
	}
	
	private void installJavaFacet(Set<Action> actions, IProject project, IFacetedProject facetedProject) {
	    IProjectFacetVersion javaFv = JavaFacet.FACET.getVersion(JavaFacetUtil.getCompilerLevel(project));
	    if(!facetedProject.hasProjectFacet(JavaFacet.FACET)) {
	      actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.INSTALL, javaFv, null));
	    } else if(!facetedProject.hasProjectFacet(javaFv)) {
	      actions.add(new IFacetedProject.Action(IFacetedProject.Action.Type.VERSION_CHANGE, javaFv, null));
	    } 
	}
}
