/*******************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.portlet.configurators;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryInstallDelegate;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ILifecycleMappingConfiguration;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.portlet.MavenPortletActivator;
import org.jboss.tools.maven.portlet.Messages;
import org.jboss.tools.maven.ui.Activator;
import org.jboss.tools.portlet.core.IPortletConstants;
import org.jboss.tools.portlet.core.internal.project.facet.JSFPortletFacetInstallDataModelProvider;
import org.jboss.tools.portlet.core.internal.project.facet.PortletFacetInstallDataModelProvider;

/**
 * 
 * @author snjeza
 *
 */
public class PortletProjectConfigurator extends AbstractProjectConfigurator {

	private static final String PORTLET_API_GROUP_ID = "javax.portlet"; //$NON-NLS-1$
	private static final String PORTLET_API_ARTIFACT_ID = "portlet-api"; //$NON-NLS-1$

	private static final String PORTLETBRIDGE_API_GROUP_ID = "org.jboss.portletbridge"; //$NON-NLS-1$
	private static final String PORTLETBRIDGE_API_ARTIFACT_ID = "portletbridge-api"; //$NON-NLS-1$

	public static final String PORTLET_CONFIGURATION_ERROR_MARKER_ID = "org.jboss.tools.maven.portlet.problem.configuration"; //$NON-NLS-1$

	
	protected static final IProjectFacet dynamicWebFacet;
	protected static final IProjectFacetVersion dynamicWebVersion;
	
	protected static final IProjectFacet jsfFacet;
	protected static final IProjectFacet portletFacet;
	protected static final IProjectFacetVersion portletVersion10;
	protected static final IProjectFacetVersion portletVersion20;
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	protected static final IProjectFacetVersion jsfportletFacetVersion;
	
	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5");  //$NON-NLS-1$
		portletFacet = ProjectFacetsManager.getProjectFacet("jboss.portlet"); //$NON-NLS-1$
		portletVersion20 = portletFacet.getVersion("2.0"); //$NON-NLS-1$
		portletVersion10 = portletFacet.getVersion("1.0"); //$NON-NLS-1$
		m2Facet = ProjectFacetsManager.getProjectFacet("jboss.m2"); //$NON-NLS-1$
		m2Version = m2Facet.getVersion("1.0"); //$NON-NLS-1$
		jsfFacet = ProjectFacetsManager.getProjectFacet("jst.jsf"); //$NON-NLS-1$
		jsfportletFacetVersion = ProjectFacetsManager.getProjectFacet("jboss.jsfportlet").getVersion("1.0");  //$NON-NLS-1$//$NON-NLS-2$
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
		String packaging = mavenProject.getPackaging();
		if (!"war".equals(packaging)) { //$NON-NLS-1$
			return;
		}
		
		if (!isPortletConfigurable(mavenProject)) {
			return;
		}
	    String portletVersion = Activator.getDefault().getDependencyVersion(mavenProject, PORTLET_API_GROUP_ID, PORTLET_API_ARTIFACT_ID);
	    String jsfportletVersion = Activator.getDefault().getDependencyVersion(mavenProject, PORTLETBRIDGE_API_GROUP_ID, PORTLETBRIDGE_API_ARTIFACT_ID);
	    if (portletVersion != null) {
	      final IFacetedProject fproj = ProjectFacetsManager.create(project);
	      if (fproj != null) {
	        markerManager.deleteMarkers(project, PORTLET_CONFIGURATION_ERROR_MARKER_ID);
	        try {
	          installWarFacets(fproj, portletVersion, jsfportletVersion, monitor);
	        } catch (CoreException e) {
	          IStatus status = e.getStatus();
	          String errorMessage = (status == null || status.getMessage() == null) ? e.getMessage():status.getMessage();
	          String markerMessage = NLS.bind(Messages.PortletProjectConfigurator_Error_installing_facet, 
	            new Object[]{portletFacet.getLabel(), portletVersion, errorMessage});
	          addErrorMarker(fproj.getProject(), markerMessage);
	          for (IStatus st : status.getChildren()) {
	            addErrorMarker(fproj.getProject(), st.getMessage());
	          }
	        }
	      }
	    }
	}

	private boolean isPortletConfigurable(MavenProject mavenProject) {
		String portletActivation = mavenProject.getProperties().getProperty("m2e.portlet.activation");
		
		boolean configurePortlet; 
		if (portletActivation == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			configurePortlet = store.getBoolean(Activator.CONFIGURE_PORTLET);
		} else {
		  configurePortlet = Boolean.valueOf(portletActivation);
		}
		return configurePortlet;
	}
	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event,
			IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
	    if(facade != null) {
	      IProject project = facade.getProject();
	      if(isWTPProject(project)) {
	        MavenProject mavenProject = facade.getMavenProject(monitor);
	        configureInternal(mavenProject, project, monitor);
	      }
	    }
		super.mavenProjectChanged(event, monitor);
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

	
	private void installWarFacets(IFacetedProject fproj, String portletVersion, String jsfportletVersion, IProgressMonitor monitor) throws CoreException {
		
		if (!fproj.hasProjectFacet(dynamicWebFacet)) {
			MavenPortletActivator.log(NLS.bind(Messages.PortletProjectConfigurator_The_project_does_not_contain_the_Web_Module_facet, fproj.getProject().getName()));
			return;
		}
		installM2Facet(fproj, monitor);
		installPortletFacet(fproj, portletVersion, jsfportletVersion, monitor);
	}


	private void installPortletFacet(IFacetedProject fproj, String portletVersionString, String jsfportletVersion, IProgressMonitor monitor)
			throws CoreException {
		if (!fproj.hasProjectFacet(portletFacet)) {
			if (portletVersionString.startsWith("1.0")) { //$NON-NLS-1$
				IDataModel model = createPortletDataModel(fproj, portletVersion10);
				fproj.installProjectFacet(portletVersion10, model, monitor);	
			}
			if (portletVersionString.startsWith("2.0")) { //$NON-NLS-1$
				IDataModel model = createPortletDataModel(fproj, portletVersion20);
				fproj.installProjectFacet(portletVersion20, model, monitor);	
			}
		}
    
		if (fproj.hasProjectFacet(portletFacet) && fproj.hasProjectFacet(jsfFacet) && jsfportletVersion != null) {
			
			if (!fproj.hasProjectFacet(jsfportletFacetVersion)) {
				IDataModel model = createJSFPortletDataModel(fproj, jsfportletFacetVersion);
				fproj.installProjectFacet(jsfportletFacetVersion, model, monitor);
			}
		    
		}
	}

	private IDataModel createPortletDataModel(IFacetedProject fproj, IProjectFacetVersion facetVersion) {
		IDataModel config = (IDataModel) new PortletFacetInstallDataModelProvider().create();
		LibraryInstallDelegate libraryDelegate = new LibraryInstallDelegate(fproj, facetVersion);
		ILibraryProvider provider = LibraryProviderFramework.getProvider("portlet-no-op-library-provider"); //$NON-NLS-1$
		libraryDelegate.setLibraryProvider(provider);
		config.setProperty(IPortletConstants.PORTLET_LIBRARY_PROVIDER_DELEGATE, libraryDelegate);
		return config;
	}
	
	private IDataModel createJSFPortletDataModel(IFacetedProject fproj, IProjectFacetVersion facetVersion) {
		IDataModel config = (IDataModel) new JSFPortletFacetInstallDataModelProvider().create();
		LibraryInstallDelegate libraryDelegate = new LibraryInstallDelegate(fproj, facetVersion);
		ILibraryProvider provider = LibraryProviderFramework.getProvider("jsfportlet-no-op-library-provider"); //$NON-NLS-1$
		libraryDelegate.setLibraryProvider(provider);
		config.setProperty(IPortletConstants.JSFPORTLET_LIBRARY_PROVIDER_DELEGATE, libraryDelegate);
		return config;
	}
	
  @Override
  public boolean hasConfigurationChanged(IMavenProjectFacade newFacade,
      ILifecycleMappingConfiguration oldProjectConfiguration,
      MojoExecutionKey key, IProgressMonitor monitor) {
    return false;
  }

  @SuppressWarnings("restriction")
  private void addErrorMarker(IProject project, String message) {
      markerManager.addMarker(project, 
          PORTLET_CONFIGURATION_ERROR_MARKER_ID, 
          message
          ,-1,  IMarker.SEVERITY_ERROR);
  }

}
