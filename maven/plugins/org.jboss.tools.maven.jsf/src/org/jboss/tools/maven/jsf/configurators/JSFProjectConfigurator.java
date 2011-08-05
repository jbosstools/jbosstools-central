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

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.jsf.core.internal.project.facet.IJSFFacetInstallDataModelProperties;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.common.util.EclipseJavaUtil;
import org.jboss.tools.maven.core.IJBossMavenConstants;
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

	protected static final IProjectFacet dynamicWebFacet;
	protected static final IProjectFacetVersion dynamicWebVersion;
	
	public static final IProjectFacet JSF_FACET;
	public static final IProjectFacetVersion JSF_FACET_VERSION_2_0;
	public static final IProjectFacetVersion JSF_FACET_VERSION_1_2;
	public static final IProjectFacetVersion JSF_FACET_VERSION_1_1;
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	public static final String JSF_VERSION_2_0 = "2.0";
	public static final String JSF_VERSION_1_2 = "1.2";
	public static final String JSF_VERSION_1_1 = "1.1";
	
	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5");  //$NON-NLS-1$
		JSF_FACET = ProjectFacetsManager.getProjectFacet("jst.jsf"); //$NON-NLS-1$
		JSF_FACET_VERSION_2_0 = JSF_FACET.getVersion(JSF_VERSION_2_0); 
		JSF_FACET_VERSION_1_2 = JSF_FACET.getVersion(JSF_VERSION_1_2); //$NON-NLS-1$
		JSF_FACET_VERSION_1_1 = JSF_FACET.getVersion(JSF_VERSION_1_1); //$NON-NLS-1$
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
		
		if ("war".equals(mavenProject.getPackaging()))  {//$NON-NLS-1$
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
		
	    String jsfVersion = getJSFVersion(mavenProject, project);
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
		
		if (!fproj.hasProjectFacet(dynamicWebFacet)) {
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
			String warSourceDir = getWarSourceDirectory(mavenProject,fproj.getProject());
			IPath facesConfigPath = new Path("WEB-INF/faces-config.xml");
			IFile facesConfig = fproj.getProject().getFolder(warSourceDir).getFile(facesConfigPath);
			IFile generatedFacesConfig = getFileFromUnderlyingresources(fproj.getProject(), facesConfigPath);

			//faces-config.xml will not be created in the source folder and it doesn't exist yet
			// => We'll have to fix it after setting the JSF facet
			boolean shouldFixFacesConfig = generatedFacesConfig != null 
					                    && !generatedFacesConfig.getLocation().equals(facesConfig.getLocation()) 
					                    && !generatedFacesConfig.exists();  
			
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
				//configureServlet = configureWebxml();
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
				if (facesConfig.exists()) { 
					//We have 2 config files. Delete the gen'd one
					generatedFacesConfig.delete(true, monitor);
				}
				else { 
					//move the gen'd config file to the appropriate source folder
					IContainer destination = facesConfig.getParent();
					if (destination != null && !destination.exists()) {
						  destination.getLocation().toFile().mkdirs();
					}
					generatedFacesConfig.move(facesConfig.getFullPath(), true, monitor);
				}
			}
			
		}
	}
	
	private void addErrorMarker(IProject project, String message) {
	    markerManager.addMarker(project, 
	    		MavenJSFConstants.JSF_CONFIGURATION_ERROR_MARKER_ID, 
	    		message
	    		,-1,  IMarker.SEVERITY_ERROR);
		
	}

	private IFile getFileFromUnderlyingresources(final IProject project, final IPath filePath) {
			  IVirtualComponent component = ComponentCore.createComponent(project);
			  if (component == null) {
				  return null;
			  }
			  IContainer underlyingFolder = component.getRootFolder().getUnderlyingFolder();
			  return project.getFile(underlyingFolder.getProjectRelativePath().append(filePath));
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
	

	public String getCustomWebXml(MavenProject mavenProject, IProject project) {
		Plugin plugin = mavenProject
				.getPlugin("org.apache.maven.plugins:maven-war-plugin");
		if (plugin == null) {
			return null;
		}
		Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
		if (config != null) {
			Xpp3Dom webXmlDom = config.getChild("webXml");
			if (webXmlDom != null && webXmlDom.getValue() != null) {
				String webXmlFile = webXmlDom.getValue().trim();
				webXmlFile = getRelativePath(project, webXmlFile);
				return webXmlFile;
			}
		}
		return null;
	}

	private static String getRelativePath(IProject project, String absolutePath) {
		File basedir = project.getLocation().toFile();
		String relative;
		if (absolutePath.equals(basedir.getAbsolutePath())) {
			relative = ".";
		} else if (absolutePath.startsWith(basedir.getAbsolutePath())) {
			relative = absolutePath.substring(basedir.getAbsolutePath()
					.length() + 1);
		} else {
			relative = absolutePath;
		}
		return relative.replace('\\', '/'); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean configureWebxml() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getBoolean(Activator.CONFIGURE_WEBXML_JSF20);
	}


	private String getJSFVersion(MavenProject mavenProject, IProject project) {
		String version = null;
		version = Activator.getDefault().getDependencyVersion(mavenProject, JSF_API_GROUP_ID, JSF_API_ARTIFACT_ID);
		if (version == null) {
			//Check if there's a JSF 2 dependency 
			version = Activator.getDefault().getDependencyVersion(mavenProject, JSF_API2_GROUP_ID, JSF_API_ARTIFACT_ID);
		}
		if (version == null) {
			//JBIDE-9242 determine JSF version from classpath  
			version = getJSFVersionFromClasspath(project);
		}
		if (version == null) {
			version = inferJsfVersionFromDependencies(mavenProject, JSF_API2_GROUP_ID, JSF_API_ARTIFACT_ID, JSF_VERSION_2_0);
		}
	    return version;
	}

	/**
	 * Determines the JSF version by searching for the methods of javax.faces.application.Application 
	 * in the project's classpath.
	 * @param project : the java project to analyze
	 * @return the JSF version (1.1, 1.2, 2.0) found in the classpath, 
	 * or null if the project doesn't depend on JSF 
	 */
	private String getJSFVersionFromClasspath(IProject project) {
		String version = null;
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject != null) {
			IType type = null;
			try {
				type = EclipseJavaUtil.findType(javaProject, 
												"javax.faces.application.Application");//$NON-NLS-1$ 
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			if (type != null) {
				String[] emptyParams = new String[0];
				if (type.getMethod("getResourceHandler", emptyParams).exists() &&    
					type.getMethod("getProjectStage", emptyParams).exists()) {      
					return JSF_VERSION_2_0;
			    }
				if (type.getMethod("getELResolver", emptyParams).exists() &&        
					type.getMethod("getExpressionFactory", emptyParams).exists()) { 
					return JSF_VERSION_1_2;
				} 
				version = JSF_VERSION_1_1;
			}
		}
		return version;
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
