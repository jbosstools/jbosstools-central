/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.jsf.configurators;

import static org.jboss.tools.maven.jsf.configurators.JSFUtils.JSF_VERSION_1_1;
import static org.jboss.tools.maven.jsf.configurators.JSFUtils.JSF_VERSION_1_2;
import static org.jboss.tools.maven.jsf.configurators.JSFUtils.*;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.jsf.core.internal.project.facet.IJSFFacetInstallDataModelProperties;
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
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.MavenUtil;
import org.jboss.tools.maven.core.ProjectUtil;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.jsf.MavenJSFActivator;
import org.jboss.tools.maven.jsf.MavenJSFConstants;
import org.jboss.tools.maven.jsf.Messages;
import org.jboss.tools.maven.ui.Activator;

/**
 * 
 * @author snjeza
 *
 */
public class JSFProjectConfigurator extends AbstractProjectConfigurator {
	
	private static final String JSF_API_GROUP_ID = "javax.faces"; //$NON-NLS-1$
	private static final String JSF_API2_GROUP_ID = "com.sun.faces"; //$NON-NLS-1$
	private static final String JSF_API_ARTIFACT_ID = "jsf-api"; //$NON-NLS-1$
	
	private static final String WAR_SOURCE_FOLDER = "/src/main/webapp";

	public static final IProjectFacet JSF_FACET;
	public static final IProjectFacetVersion JSF_FACET_VERSION_2_1;
	public static final IProjectFacetVersion JSF_FACET_VERSION_2_0;
	public static final IProjectFacetVersion JSF_FACET_VERSION_1_2;
	public static final IProjectFacetVersion JSF_FACET_VERSION_1_1;
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;

	static {
		JSF_FACET = ProjectFacetsManager.getProjectFacet("jst.jsf"); //$NON-NLS-1$
		JSF_FACET_VERSION_2_0 = JSF_FACET.getVersion(JSF_VERSION_2_0); 
		JSF_FACET_VERSION_1_2 = JSF_FACET.getVersion(JSF_VERSION_1_2); //$NON-NLS-1$
		JSF_FACET_VERSION_1_1 = JSF_FACET.getVersion(JSF_VERSION_1_1); //$NON-NLS-1$
		
		IProjectFacetVersion jsf21Version = null;
		try {
			jsf21Version = JSF_FACET.getVersion(JSF_VERSION_2_1); 
		} catch (Exception e) {
			Activator.log("JSF 2.1 Facet is unavailable, fall back to 2.0");
			jsf21Version = JSF_FACET_VERSION_2_0; 
		}
		JSF_FACET_VERSION_2_1 = jsf21Version;
		
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
		
		if (!"war".equals(mavenProject.getPackaging()))  {//$NON-NLS-1$
			return;
		}
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureJSF = store.getBoolean(Activator.CONFIGURE_JSF);
		if (!configureJSF) {
			return;
		}
		
    	final IFacetedProject fproj = ProjectFacetsManager.create(project);
		if (fproj != null && fproj.hasProjectFacet(JSF_FACET) && fproj.hasProjectFacet(m2Facet)) {
			//everything already installed. Since there's no support for version update -yet- we bail
			return;
		}
		
	    String jsfVersion = getJSFVersion(mavenProject, fproj);
	    if (fproj != null && jsfVersion != null) { 
	      installWarFacets(fproj, jsfVersion, mavenProject, monitor);
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

	
	private void installWarFacets(IFacetedProject fproj, 
			String jsfVersion, MavenProject mavenProject,
			IProgressMonitor monitor) throws CoreException {
		
		if (!fproj.hasProjectFacet(IJ2EEFacetConstants.DYNAMIC_WEB_FACET)) {
			Activator.log(Messages.JSFProjectConfigurator_The_project_does_not_contain_the_Web_Module_facet);
		} else {
   		   installJSFFacet(fproj, jsfVersion, mavenProject, monitor);
        }
		installM2Facet(fproj, monitor);
		
	}


	private void installJSFFacet(IFacetedProject fproj, 
			String jsfVersionString, MavenProject mavenProject,
			IProgressMonitor monitor)
			throws CoreException {

		markerManager.deleteMarkers(fproj.getProject(), MavenJSFConstants.JSF_CONFIGURATION_ERROR_MARKER_ID);
		if (!fproj.hasProjectFacet(JSF_FACET)) {
			IProject project = fproj.getProject();
			//JBIDE-10785 : refresh parent to prevent 
			// org.osgi.service.prefs.BackingStoreException: Resource '/parent/web/.settings' does not exist.
			MavenUtil.refreshParent(mavenProject);
			
			String warSourceDir = getWarSourceDirectory(mavenProject,project);
			String facesConfigPath = "WEB-INF/faces-config.xml";
			IFile defaultFacesConfig = project.getFolder(warSourceDir).getFile(facesConfigPath);
			IFile generatedFacesConfig = ProjectUtil.getGeneratedWebResourceFile(project, facesConfigPath);
			IFile actualFacesConfig = JSFUtils.getFacesconfig(project);
			IFolder libFolder = project.getFolder(warSourceDir).getFolder("WEB-INF/lib");
			
			//faces-config.xml will not be created in the source folder and it doesn't exist yet
			// => We'll have to fix it after setting the JSF facet
			boolean shouldFixFacesConfig = generatedFacesConfig != null 
					                    && !generatedFacesConfig.getLocation().equals(defaultFacesConfig.getLocation()) 
					                    && !generatedFacesConfig.exists();  
			boolean defaultFacesConfigAlreadyExists = defaultFacesConfig.exists();
			boolean libFolderAlreadyExists = libFolder.exists(); 
			
			IProjectFacetVersion facetVersion = null;
			boolean configureServlet = false;//Fix for JBIDE-9454, where existing web.xml is completely overwritten.
			if (jsfVersionString.startsWith(JSF_VERSION_1_1)) { 
				facetVersion = JSF_FACET_VERSION_1_1;
			}
			else if (jsfVersionString.startsWith(JSF_VERSION_1_2)) { 
				facetVersion = JSF_FACET_VERSION_1_2;	
			}
			else if (jsfVersionString.startsWith(JSF_VERSION_2_0)) { 
				facetVersion = JSF_FACET_VERSION_2_0;
			}
			else if (jsfVersionString.startsWith(JSF_VERSION_2_1)) { 
				facetVersion = JSF_FACET_VERSION_2_1;
			}

			if (facetVersion != null) {
				IStatus status = facetVersion.getConstraint().check(fproj.getProjectFacets());
				if (status.isOK()) {
					IDataModel model = MavenJSFActivator.getDefault().createJSFDataModel(fproj,facetVersion);
					model.setBooleanProperty(IJSFFacetInstallDataModelProperties.CONFIGURE_SERVLET, configureServlet );
					fproj.installProjectFacet(facetVersion, model, monitor);
				} else {
			        addErrorMarker(fproj.getProject(), facetVersion + " can not be installed : "+ status.getMessage());
					for (IStatus st : status.getChildren()) {
				        addErrorMarker(fproj.getProject(), st.getMessage());
					}
				}
			}
			
			if (shouldFixFacesConfig && generatedFacesConfig.exists()) {
				if (defaultFacesConfig.exists()) { 
					//We have 2 config files. Delete the gen'd one
					generatedFacesConfig.delete(true, monitor);
				}
				else { 
					//move the gen'd config file to the appropriate source folder
					IContainer destination = defaultFacesConfig.getParent();
					if (destination != null && !destination.exists()) {
						  destination.getLocation().toFile().mkdirs();
					}
					generatedFacesConfig.move(defaultFacesConfig.getFullPath(), true, monitor);
				}
			}
			if (actualFacesConfig != null 
				&& !defaultFacesConfigAlreadyExists 
			    && !defaultFacesConfig.getLocation().equals(actualFacesConfig.getLocation())
			    && defaultFacesConfig.exists()/*file has just been created*/) {
				defaultFacesConfig.delete(true, monitor);
			}
			//JBIDE-11413 : don't create an unnecessary lib folder
			libFolder.refreshLocal(IResource.DEPTH_ZERO, monitor);
			if (!libFolderAlreadyExists 
				&& libFolder.exists() 
				&& libFolder.members().length == 0){
				libFolder.delete(true, monitor);
			}
		}
	}
	
	private void addErrorMarker(IProject project, String message) {
	    markerManager.addMarker(project, 
	    		MavenJSFConstants.JSF_CONFIGURATION_ERROR_MARKER_ID, 
	    		message
	    		,-1,  IMarker.SEVERITY_ERROR);
		
	}

	private String getWarSourceDirectory(MavenProject mavenProject,
			IProject project) {
		Plugin plugin = mavenProject
				.getPlugin("org.apache.maven.plugins:maven-war-plugin");
		if (plugin == null) {
			return null;
		}
		Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
		if (config == null) {
			return WAR_SOURCE_FOLDER;
		}
		Xpp3Dom[] warSourceDirectory = config.getChildren("warSourceDirectory");
		if (warSourceDirectory != null && warSourceDirectory.length > 0) {
			String dir = warSourceDirectory[0].getValue();
			if (project != null) {
				return tryProjectRelativePath(project, dir).toOSString();
			}
			return dir;
		}
		return WAR_SOURCE_FOLDER;
	}

	private IPath tryProjectRelativePath(IProject project,
			String resourceLocation) {
		if (resourceLocation == null) {
			return null;
		}
		IPath projectLocation = project.getLocation();
		IPath directory = Path.fromOSString(resourceLocation);
		if (projectLocation == null || !projectLocation.isPrefixOf(directory)) {
			return directory;
		}
		return directory.removeFirstSegments(projectLocation.segmentCount())
				.makeRelative().setDevice(null);
	}
	
	/*
	private boolean configureWebxml() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getBoolean(Activator.CONFIGURE_WEBXML_JSF20);
	}
	*/

	private String getJSFVersion(MavenProject mavenProject, IFacetedProject fproj) {
		String version = null;
		IProject project = fproj.getProject();
		version = JSFUtils.getVersionFromFacesconfig(project);
		
		if (version == null) {
			version = Activator.getDefault().getDependencyVersion(mavenProject, JSF_API_GROUP_ID, JSF_API_ARTIFACT_ID);
		}
		if (version == null) {
			//Check if there's a JSF 2.x dependency 
			version = Activator.getDefault().getDependencyVersion(mavenProject, JSF_API2_GROUP_ID, JSF_API_ARTIFACT_ID);
		}
		if (version == null) {
			//JBIDE-9242 determine JSF version from classpath  
			version = JSFUtils.getJSFVersionFromClasspath(project);
		}
		if (version == null) {
			version = inferJsfVersionFromDependencies(mavenProject, JSF_API2_GROUP_ID, JSF_API_ARTIFACT_ID, JSF_VERSION_2_0);
		}
		
		if (version == null && hasFacesServletInWebXml(mavenProject, project)) {
			//No dependency on JSF, no faces-config, but uses faces-servlet
			//so we try to best guess the version depending on the installed web facet
			IProjectFacetVersion webVersion = fproj.getInstalledVersion(IJ2EEFacetConstants.DYNAMIC_WEB_FACET);
			if (webVersion.compareTo(IJ2EEFacetConstants.DYNAMIC_WEB_30) < 0) {
				version = JSF_VERSION_1_2;
			} else {
				version = JSF_VERSION_2_0;
			}
		}
		
	    return version;
	}

	private boolean hasFacesServletInWebXml(MavenProject mavenProject, IProject project) {
		//We look for javax.faces.webapp.FacesServlet in web.xml
		//We should look for a custom web.xml at this point, but WTP would then crash on the JSF Facet installation
		//if it's not in a standard location, so we stick with the regular file.
		IFile webXml = getWebXml(project);
		return webXml != null && webXml.exists() && JSFUtils.hasFacesServlet(webXml);
	}

	private IFile getWebXml(IProject project) {
		return ProjectUtil.getWebResourceFile(project, "WEB-INF/web.xml");
	}


	private String inferJsfVersionFromDependencies(MavenProject mavenProject, String groupId, String artifactId, String defaultVersion) {
		boolean hasCandidates = false;
		String jsfVersion = null;
		List<ArtifactRepository> repos = mavenProject.getRemoteArtifactRepositories();
		for (Artifact artifact : mavenProject.getArtifacts()) {
			if (isKnownJsfBasedArtifact(artifact)) {
				hasCandidates = true;
				jsfVersion = Activator.getDefault().getDependencyVersion(artifact, repos, groupId, artifactId);
				if (jsfVersion != null) {
					//TODO should probably not break and take the highest version returned from all dependencies
					break;
				}
			}
		}
		//Fallback to default JSF version
		if (hasCandidates && jsfVersion == null) {
			return defaultVersion;
		}
		return jsfVersion;
	}

	private boolean isKnownJsfBasedArtifact(Artifact artifact) {
		return artifact.getGroupId().startsWith("org.jboss.seam.") 	//$NON-NLS-1$ 
				&& artifact.getArtifactId().equals("seam-faces") 	//$NON-NLS-1$
				&& artifact.getVersion().startsWith("3.");			//$NON-NLS-1$
	}

	
}
