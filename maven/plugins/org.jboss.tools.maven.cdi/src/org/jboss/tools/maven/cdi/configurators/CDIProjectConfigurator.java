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
package org.jboss.tools.maven.cdi.configurators;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.jboss.tools.cdi.core.CDICoreNature;
import org.jboss.tools.cdi.core.CDIUtil;
import org.jboss.tools.common.model.util.EclipseResourceUtil;
import org.jboss.tools.maven.cdi.MavenCDIActivator;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.ProjectUtil;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.ui.Activator;

/**
 * 
 * @author snjeza
 *
 */
public class CDIProjectConfigurator extends AbstractProjectConfigurator {

	private static final String CDI_API_GROUP_ID = "javax.enterprise"; //$NON-NLS-1$
	private static final String CDI_API_ARTIFACT_ID = "cdi-api"; //$NON-NLS-1$
	
	protected static final IProjectFacet dynamicWebFacet;
	protected static final IProjectFacet ejbFacet;
	protected static final IProjectFacetVersion dynamicWebVersion;
	protected static final IProjectFacetVersion ejbVersion;
	
	protected static final IProjectFacet cdiFacet;
	
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	private static final String DEFAULT_CDI_VERSION;
	
	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5");  //$NON-NLS-1$
		ejbFacet = ProjectFacetsManager.getProjectFacet("jst.ejb"); //$NON-NLS-1$
		ejbVersion = ejbFacet.getVersion("3.0");  //$NON-NLS-1$
		cdiFacet = ProjectFacetsManager.getProjectFacet("jst.cdi"); //$NON-NLS-1$
		DEFAULT_CDI_VERSION = cdiFacet.getDefaultVersion().getVersionString();
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
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureCDI = store.getBoolean(Activator.CONFIGURE_CDI);
		if (!configureCDI) {
			return;
		}
    	final IFacetedProject fproj = ProjectFacetsManager.create(project);
		if (project.hasNature(CDICoreNature.NATURE_ID) 
				&& (fproj == null || (fproj.hasProjectFacet(cdiFacet) && fproj.hasProjectFacet(m2Facet)))) {
			//everything already installed. Since there's no support for version update -yet- we stop here
			return;
		}
		String packaging = mavenProject.getPackaging();
	    String cdiVersion = getCDIVersion(project, mavenProject);
	    if (cdiVersion != null) {
	    	if ( (fproj != null) && ("war".equals(packaging) || "ejb".equals(packaging)) ) { //$NON-NLS-1$
	    		installDefaultFacets(fproj, cdiVersion, monitor);
	    	}
	    	CDIUtil.enableCDI(project, false, new NullProgressMonitor());
	    }
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

	
	@SuppressWarnings("unchecked")
	private void installDefaultFacets(IFacetedProject fproj, String cdiVersion,IProgressMonitor monitor) throws CoreException {
		IProjectFacetVersion currentWebVersion = fproj.getProjectFacetVersion(dynamicWebFacet); 
		IProjectFacetVersion currentEjbVersion = fproj.getProjectFacetVersion(ejbFacet); 
		
		if ((currentWebVersion != null && currentWebVersion.compareTo(dynamicWebVersion)> -1)
				|| (currentEjbVersion != null && currentEjbVersion.compareTo(ejbVersion)> -1)) {
			installCDIFacet(fproj, cdiVersion, monitor);
		}
		installM2Facet(fproj, monitor);
	}


	private void installCDIFacet(IFacetedProject fproj, String cdiVersionString, IProgressMonitor monitor)
			throws CoreException {
		if (!fproj.hasProjectFacet(cdiFacet)) {
			IProjectFacetVersion facetVersion = getCdiFacetVersion(cdiVersionString);
			if (facetVersion != null) { //$NON-NLS-1$
				IDataModel model = MavenCDIActivator.getDefault().createCDIDataModel(fproj,facetVersion);
				fproj.installProjectFacet(facetVersion, model, monitor);	
			}
		}
	}
	
	private IProjectFacetVersion getCdiFacetVersion(String cdiVersionString) {
		String majorMinor = getMajorMinorVersion(cdiVersionString);
		IProjectFacetVersion facetVersion; 
		try {
			facetVersion = cdiFacet.getVersion(majorMinor);
		} catch(IllegalArgumentException iae) {
			//ignore missing version
			facetVersion = cdiFacet.getDefaultVersion();
			MavenCDIActivator.log("CDI version " + majorMinor + " has no corresponding Facet, falling back to "+facetVersion.getVersionString());
		}
		return facetVersion;
	}

	private String getMajorMinorVersion(String versionString) {
		String[] pointVersions = versionString.split("\\.");
		if (pointVersions.length > 1) {
			return pointVersions[0] + "." + pointVersions[1];
		} 
		return versionString;
	}
	
	private String getCDIVersion(IProject project, MavenProject mavenProject) throws CoreException {
		String version = Activator.getDefault().getDependencyVersion(mavenProject, CDI_API_GROUP_ID, CDI_API_ARTIFACT_ID);
		if (version == null) {
			version = inferCdiVersionFromDependencies(mavenProject);
		}
		if (version == null) {
			version = (hasBeansXml(project))?DEFAULT_CDI_VERSION:null;
		}
	    return version;
	}

	private String inferCdiVersionFromDependencies(MavenProject mavenProject) {
		boolean hasCandidates = false;
		String cdiVersion = null;
		List<ArtifactRepository> repos = mavenProject.getRemoteArtifactRepositories();
		for (Artifact artifact : mavenProject.getArtifacts()) {
			if (isKnownCdiExtension(artifact)) {
				hasCandidates = true;
				cdiVersion = Activator.getDefault().getDependencyVersion(artifact, repos, CDI_API_GROUP_ID, CDI_API_ARTIFACT_ID);
				if (cdiVersion != null) {
					//TODO should probably not break and take the highest version returned from all dependencies
					break;
				}
			}
		}
		//Fallback to default CDI version
		if (hasCandidates && cdiVersion == null) {
			return DEFAULT_CDI_VERSION;
		}
		return cdiVersion;
	}

	private boolean isKnownCdiExtension(Artifact artifact) {
		return (artifact.getGroupId().startsWith("org.jboss.seam.") 
			&& artifact.getVersion().startsWith("3."))
			|| artifact.getGroupId().startsWith("org.apache.deltaspike.");
	}

	private boolean hasBeansXml(IProject project) throws CoreException {
		IFacetedProject facetedProject = ProjectFacetsManager.create(project);
		if(facetedProject!=null && facetedProject.hasProjectFacet(dynamicWebFacet)) {
			IFile beansXml = ProjectUtil.getWebResourceFile(project, "WEB-INF/beans.xml");
			if (beansXml != null && beansXml.exists()) {
				return true;
			}
		} 
		if(project.hasNature(JavaCore.NATURE_ID)) {
			Set<IFolder> sources = EclipseResourceUtil.getSourceFolders(project);
			IPath beansPath = new Path("META-INF/beans.xml");
			for (IFolder src : sources) {
				IFile beansXml = src.getFile(beansPath);
				if(beansXml!=null && beansXml.exists()) {
					return true;
				}
			}
		}
		return false;
	}	
}
