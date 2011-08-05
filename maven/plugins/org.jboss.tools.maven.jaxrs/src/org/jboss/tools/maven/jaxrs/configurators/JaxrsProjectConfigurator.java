/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jaxrs.configurators;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryInstallDelegate;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
import org.eclipse.jst.ws.jaxrs.core.internal.IJAXRSCoreConstants;
import org.eclipse.jst.ws.jaxrs.core.internal.project.facet.IJAXRSFacetInstallDataModelProperties;
import org.eclipse.jst.ws.jaxrs.core.internal.project.facet.JAXRSFacetInstallDataModelProvider;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.common.util.EclipseJavaUtil;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.ui.Activator;
import org.maven.ide.eclipse.wtp.WarPluginConfiguration;

/**
 * JAX-RS maven project configurator.
 * <br/>
 * This configurator adds the JAX-RS facet to a project if it has a dependency on the JAX-RS API.
 * 
 * @author Fred Bricon
 *
 */
public class JaxrsProjectConfigurator extends AbstractProjectConfigurator {

	protected static final IProjectFacet dynamicWebFacet;
	public static final IProjectFacet JAX_RS_FACET; 
	public static final IProjectFacetVersion JAX_RS_FACET_1_0; 
	public static final IProjectFacetVersion JAX_RS_FACET_1_1; 

	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	
	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		JAX_RS_FACET = ProjectFacetsManager.getProjectFacet(IJAXRSCoreConstants.JAXRS_FACET_ID);
		JAX_RS_FACET_1_0 = JAX_RS_FACET.getVersion(IJAXRSCoreConstants.JAXRS_VERSION_1_0);
		JAX_RS_FACET_1_1 = JAX_RS_FACET.getVersion(IJAXRSCoreConstants.JAXRS_VERSION_1_1);
		m2Facet = ProjectFacetsManager.getProjectFacet("jboss.m2"); //$NON-NLS-1$
		m2Version = m2Facet.getVersion("1.0"); //$NON-NLS-1$
	}
	
	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureInternal(mavenProject,project, monitor);
	}
	
	private void configureInternal(MavenProject mavenProject,IProject project,
			IProgressMonitor monitor) throws CoreException {
		
		if(!"war".equals(mavenProject.getPackaging())) { //$NON-NLS-1$
			return;
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureJaxRs = store.getBoolean(Activator.CONFIGURE_JAXRS);
		if (!configureJaxRs) {
			return;
		}
		
    	final IFacetedProject fproj = ProjectFacetsManager.create(project);
    	if (fproj == null) {
    		return;
    	}
    	
		if (fproj.hasProjectFacet(JAX_RS_FACET) && fproj.hasProjectFacet(m2Facet)) {
			//everything already installed. Since there's no support for version update -yet- we bail
			return;
		}
		
	    IProjectFacetVersion jaxRsVersion = getJaxRsVersion(mavenProject, project);
	    if (jaxRsVersion != null) {
	      installFacets(fproj, jaxRsVersion, mavenProject, monitor);
	    }

	}

	private void installFacets(IFacetedProject fproj, IProjectFacetVersion facetVersion,
			MavenProject mavenProject, IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(dynamicWebFacet)) {
			Activator.log("The_project_does_not_contain_the_Web_Module_facet");
		} else {
   		   installJaxRsFacet(fproj, facetVersion, mavenProject, monitor);
        }
		installM2Facet(fproj, monitor);
	}

	private void installJaxRsFacet(IFacetedProject fproj, IProjectFacetVersion facetVersion,
			MavenProject mavenProject, IProgressMonitor monitor) throws CoreException {

		if (facetVersion != null) {
			IStatus status = facetVersion.getConstraint().check(fproj.getProjectFacets());
			if (status.isOK()) {
				IDataModel model = createJaxRsDataModel(fproj,facetVersion);
				WarPluginConfiguration config = new WarPluginConfiguration(mavenProject, fproj.getProject());
				String warSourceDirectory = config.getWarSourceDirectory();
				model.setProperty(IJAXRSFacetInstallDataModelProperties.WEBCONTENT_DIR, warSourceDirectory);
				model.setProperty(IJAXRSFacetInstallDataModelProperties.UPDATEDD, false);
				fproj.installProjectFacet(facetVersion, model, monitor);
			} else {
		        addErrorMarker(fproj.getProject(), facetVersion + " can not be installed : "+ status.getMessage());
				for (IStatus st : status.getChildren()) {
			        addErrorMarker(fproj.getProject(), st.getMessage());
				}
			}
		}
	}

	private IDataModel createJaxRsDataModel(IFacetedProject fproj,
			IProjectFacetVersion facetVersion) {
		IDataModel config = (IDataModel) new JAXRSFacetInstallDataModelProvider().create();
		LibraryInstallDelegate libraryDelegate = new LibraryInstallDelegate(fproj, facetVersion);
		ILibraryProvider provider = LibraryProviderFramework.getProvider(IJAXRSCoreConstants.NO_OP_LIBRARY_ID);
		libraryDelegate.setLibraryProvider(provider);
		config.setProperty(IJAXRSFacetInstallDataModelProperties.LIBRARY_PROVIDER_DELEGATE, libraryDelegate);
		return config;
	}

	private IProjectFacetVersion getJaxRsVersion(MavenProject mavenProject, IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject != null) {
			IType type = null;
			try {
				type = EclipseJavaUtil.findType(javaProject, "javax.ws.rs.ApplicationPath");//$NON-NLS-1$ 
				if (type != null) {
				   return JAX_RS_FACET_1_1;
				}

				type = EclipseJavaUtil.findType(javaProject, "javax.ws.rs.Path");//$NON-NLS-1$ 
				if (type != null) {
				   return JAX_RS_FACET_1_0;
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void addErrorMarker(IProject project, String message) {
	    markerManager.addMarker(project, 
	    		MavenJaxRsConstants.JAXRS_CONFIGURATION_ERROR_MARKER_ID, 
	    		message
	    		,-1,  IMarker.SEVERITY_ERROR);
		
	}
	
	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event,
			IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
	    if(facade != null) {
	      IProject project = facade.getProject();
	      if(isWTPProject(project) ) {
	        MavenProject mavenProject = facade.getMavenProject(monitor);
		    IMavenProjectFacade oldFacade = event.getOldMavenProject();
		    if (oldFacade != null) {
		    	MavenProject oldProject = oldFacade.getMavenProject(monitor);
		    	if (oldProject != null && oldProject.getArtifacts().equals(mavenProject.getArtifacts())) {
		    		//Nothing changed since last build, no need to lookup for new JSF facets
		    		return;
		    	}
		    }
	        configureInternal(mavenProject, project, monitor);
	      }
	    }
	}

	private boolean isWTPProject(IProject project) {
	    return ModuleCoreNature.getModuleCoreNature(project) != null;
	 }
	
	private void installM2Facet(IFacetedProject fproj, IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(m2Facet)) {
			IDataModel config = (IDataModel) new MavenFacetInstallDataModelProvider().create();
			config.setBooleanProperty(IJBossMavenConstants.MAVEN_PROJECT_EXISTS, true);
			fproj.installProjectFacet(m2Version, config, monitor);
		}
	}

}
