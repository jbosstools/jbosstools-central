package org.jboss.tools.maven.seam.configurators;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.j2ee.application.Application;
import org.eclipse.jst.j2ee.application.EjbModule;
import org.eclipse.jst.j2ee.application.Module;
import org.eclipse.jst.j2ee.application.WebModule;
import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.common.model.util.EclipseResourceUtil;
import org.jboss.tools.jst.web.kb.IKbProject;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.seam.MavenSeamActivator;
import org.jboss.tools.seam.core.ISeamProject;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.core.SeamUtil;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.core.project.facet.SeamVersion;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.jboss.tools.seam.internal.core.project.facet.SeamFacetInstallDataModelProvider;
import org.jboss.tools.seam.ui.wizard.SeamWizardUtils;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.MavenProjectChangedEvent;
import org.maven.ide.eclipse.project.MavenProjectManager;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;
import org.osgi.service.prefs.BackingStoreException;

public class JSFProjectConfigurator extends AbstractProjectConfigurator {

	private static final String JSF_API_GROUP_ID = "javax.faces"; //$NON-NLS-1$
	private static final String JSF_API2_GROUP_ID = "com.sun.faces"; //$NON-NLS-1$
	private static final String JSF_API_ARTIFACT_ID = "jsf-api"; //$NON-NLS-1$
	
	protected static final IProjectFacet dynamicWebFacet;
	protected static final IProjectFacetVersion dynamicWebVersion;
	
	protected static final IProjectFacet jsfFacet;
	protected static final IProjectFacetVersion jsfVersion12;
	protected static final IProjectFacetVersion jsfVersion11;
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	
	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5");  //$NON-NLS-1$
		jsfFacet = ProjectFacetsManager.getProjectFacet("jst.jsf"); //$NON-NLS-1$
		jsfVersion12 = jsfFacet.getVersion("1.2"); //$NON-NLS-1$
		jsfVersion11 = jsfFacet.getVersion("1.1"); //$NON-NLS-1$
		m2Facet = ProjectFacetsManager.getProjectFacet("jboss.m2"); //$NON-NLS-1$
		m2Version = m2Facet.getVersion("1.0"); //$NON-NLS-1$
	}
	
	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		// adds Seam capabilities if there are Seam dependencies
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureInternal(mavenProject,project, monitor);
	}
	
	private void configureInternal(MavenProject mavenProject,IProject project,
			IProgressMonitor monitor) throws CoreException {
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureJSF = store.getBoolean(MavenSeamActivator.CONFIGURE_JSF);
		if (!configureJSF) {
			return;
		}
		
		String packaging = mavenProject.getPackaging();
	    String jsfVersion = getJSFVersion(mavenProject);
	    if (jsfVersion != null) {
	    	final IFacetedProject fproj = ProjectFacetsManager.create(project);
	    	if ("war".equals(packaging)) { //$NON-NLS-1$
	    		installWarFacets(fproj, jsfVersion, monitor);
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

	
	private void installWarFacets(IFacetedProject fproj, String jsfVersion,IProgressMonitor monitor) throws CoreException {
		
		if (!fproj.hasProjectFacet(dynamicWebFacet)) {
			MavenSeamActivator.log("The project doesn't contain the Web Module facet.");
		}
		installJSFFacet(fproj, jsfVersion, monitor);
		installM2Facet(fproj, monitor);
		
	}


	private void installJSFFacet(IFacetedProject fproj, String jsfVersionString, IProgressMonitor monitor)
			throws CoreException {
		if (!fproj.hasProjectFacet(jsfFacet)) {
			if (jsfVersionString.startsWith("1.1")) { //$NON-NLS-1$
				fproj.installProjectFacet(jsfVersion11, null, monitor);	
			}
			if (jsfVersionString.startsWith("1.2")) { //$NON-NLS-1$
				fproj.installProjectFacet(jsfVersion12, null, monitor);	
			}
			// FIXME
			if (jsfVersionString.startsWith("2.0")) { //$NON-NLS-1$
				fproj.installProjectFacet(jsfVersion12, null, monitor);	
			}
		}
	}

	private String getJSFVersion(MavenProject mavenProject) {
		List<Dependency> dependencies = mavenProject.getDependencies();
		for (Dependency dependency:dependencies) {
	    	String groupId = dependency.getGroupId();
    		if (groupId != null && (JSF_API_GROUP_ID.equals(groupId) || JSF_API2_GROUP_ID.equals(groupId)) ) {
    			String artifactId = dependency.getArtifactId();
    			if (artifactId != null && JSF_API_ARTIFACT_ID.equals(artifactId)) {
	    			return dependency.getVersion();
	    		} 
	    	}
	    }
	    return null;
	}

}
