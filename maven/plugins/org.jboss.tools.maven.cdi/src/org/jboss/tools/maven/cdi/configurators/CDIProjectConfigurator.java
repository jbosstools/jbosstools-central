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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.cdi.core.CDICoreNature;
import org.jboss.tools.cdi.core.CDIUtil;
import org.jboss.tools.maven.cdi.MavenCDIActivator;
import org.jboss.tools.maven.cdi.Messages;
import org.jboss.tools.maven.core.IJBossMavenConstants;
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
	protected static final IProjectFacetVersion cdiVersion;
	
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	private static final String DEFAULT_CDI_VERSION = "1.0";
	
	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5");  //$NON-NLS-1$
		ejbFacet = ProjectFacetsManager.getProjectFacet("jst.ejb"); //$NON-NLS-1$
		ejbVersion = ejbFacet.getVersion("3.0");  //$NON-NLS-1$
		cdiFacet = ProjectFacetsManager.getProjectFacet("jst.cdi"); //$NON-NLS-1$
		cdiVersion = cdiFacet.getVersion(DEFAULT_CDI_VERSION); //$NON-NLS-1$
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
	    String cdiVersion = getCDIVersion(mavenProject);
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
				|| (currentEjbVersion != null && currentEjbVersion.compareTo(dynamicWebVersion)> -1)) {
			installCDIFacet(fproj, cdiVersion, monitor);
		} else {
			String name = "";
			if (fproj.getProject() != null) {
				name = fproj.getProject().getName();
			}
			MavenCDIActivator.log(NLS.bind(Messages.CDIProjectConfigurator_The_project_does_not_contain_required_facets, name));
		}
		installM2Facet(fproj, monitor);
		
	}


	private void installCDIFacet(IFacetedProject fproj, String cdiVersionString, IProgressMonitor monitor)
			throws CoreException {
		if (!fproj.hasProjectFacet(cdiFacet)) {
			if (cdiVersionString.startsWith("1.0")) { //$NON-NLS-1$
				IDataModel model = MavenCDIActivator.getDefault().createCDIDataModel(fproj,cdiVersion);
				fproj.installProjectFacet(cdiVersion, model, monitor);	
			}
		}
	}
	
	private String getCDIVersion(MavenProject mavenProject) {
		String version = Activator.getDefault().getDependencyVersion(mavenProject, CDI_API_GROUP_ID, CDI_API_ARTIFACT_ID);
		if (version == null) {
			version = inferCdiVersionFromDependencies(mavenProject);
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
		return artifact.getGroupId().startsWith("org.jboss.seam.") 
			&& artifact.getVersion().startsWith("3.");
	}

}
