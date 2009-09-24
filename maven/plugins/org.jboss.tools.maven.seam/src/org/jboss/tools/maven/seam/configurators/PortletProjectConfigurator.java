package org.jboss.tools.maven.seam.configurators;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryInstallDelegate;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.seam.MavenSeamActivator;
import org.jboss.tools.portlet.core.IPortletConstants;
import org.jboss.tools.portlet.core.internal.project.facet.JSFPortletFacetInstallDataModelProvider;
import org.jboss.tools.portlet.core.internal.project.facet.PortletFacetInstallDataModelProvider;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.MavenProjectChangedEvent;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;

public class PortletProjectConfigurator extends AbstractProjectConfigurator {

	private static final String PORTLET_API_GROUP_ID = "javax.portlet"; //$NON-NLS-1$
	private static final String PORTLET_API_ARTIFACT_ID = "portlet-api"; //$NON-NLS-1$

	private static final String PORTLETBRIDGE_API_GROUP_ID = "org.jboss.portletbridge"; //$NON-NLS-1$
	private static final String PORTLETBRIDGE_API_ARTIFACT_ID = "portletbridge-api"; //$NON-NLS-1$
	
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
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configurePortlet = store.getBoolean(MavenSeamActivator.CONFIGURE_PORTLET);
		if (!configurePortlet) {
			return;
		}
		
		String packaging = mavenProject.getPackaging();
	    String portletVersion = MavenSeamActivator.getDefault().getDependencyVersion(mavenProject, PORTLET_API_GROUP_ID, PORTLET_API_ARTIFACT_ID);
	    String jsfportletVersion = MavenSeamActivator.getDefault().getDependencyVersion(mavenProject, PORTLETBRIDGE_API_GROUP_ID, PORTLETBRIDGE_API_ARTIFACT_ID);
	    if (portletVersion != null) {
	    	final IFacetedProject fproj = ProjectFacetsManager.create(project);
	    	if ("war".equals(packaging)) { //$NON-NLS-1$
	    		installWarFacets(fproj, portletVersion, jsfportletVersion, monitor);
	    	}
	    }
	}


	@Override
	protected void mavenProjectChanged(MavenProjectChangedEvent event,
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
			MavenSeamActivator.log("The project doesn't contain the Web Module facet.");
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
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureJSFPortlet = store.getBoolean(MavenSeamActivator.CONFIGURE_JSFPORTLET);
		if (!configureJSFPortlet) {
			return;
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
}
