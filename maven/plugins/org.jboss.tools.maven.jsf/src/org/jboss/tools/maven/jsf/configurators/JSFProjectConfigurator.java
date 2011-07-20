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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.jsf.core.internal.project.facet.IJSFFacetInstallDataModelProperties;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.jsf.MavenJSFActivator;
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
	
	private static final String WEB_XML = "WEB-INF/web.xml";
	private static final String WAR_SOURCE_FOLDER = "/src/main/webapp";

	protected static final IProjectFacet dynamicWebFacet;
	protected static final IProjectFacetVersion dynamicWebVersion;
	
	protected static final IProjectFacet jsfFacet;
	protected static final IProjectFacetVersion jsfVersion20;
	protected static final IProjectFacetVersion jsfVersion12;
	protected static final IProjectFacetVersion jsfVersion11;
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	private static final String JSF_VERSION_2_0 = "2.0";
	
	static {
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5");  //$NON-NLS-1$
		jsfFacet = ProjectFacetsManager.getProjectFacet("jst.jsf"); //$NON-NLS-1$
		jsfVersion20 = jsfFacet.getVersion(JSF_VERSION_2_0); 
		jsfVersion12 = jsfFacet.getVersion("1.2"); //$NON-NLS-1$
		jsfVersion11 = jsfFacet.getVersion("1.1"); //$NON-NLS-1$
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
		boolean configureJSF = store.getBoolean(Activator.CONFIGURE_JSF);
		if (!configureJSF) {
			return;
		}
		
    	final IFacetedProject fproj = ProjectFacetsManager.create(project);
		if (fproj != null && fproj.hasProjectFacet(jsfFacet) && fproj.hasProjectFacet(m2Facet)) {
			//everything already installed. Since there's no support for version update -yet- we bail
			return;
		}
		
		String packaging = mavenProject.getPackaging();
	    String jsfVersion = getJSFVersion(mavenProject);
	    if (fproj != null && jsfVersion != null && "war".equals(packaging)) { //$NON-NLS-1$
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
		if (!fproj.hasProjectFacet(jsfFacet)) {
			String warSourceDir = getWarSourceDirectory(mavenProject,fproj.getProject());
			IPath facesConfigPath = new Path("WEB-INF/faces-config.xml");
			IFile facesConfig = fproj.getProject().getFolder(warSourceDir).getFile(facesConfigPath);
			IFile generatedFacesConfig = getFileFromUnderlyingresources(fproj.getProject(), facesConfigPath);

			//faces-config.xml will not be created in the source folder and it doesn't exist yet
			// => We'll have to fix it after setting the JSF facet
			boolean shouldFixFacesConfig = !generatedFacesConfig.getLocation().equals(facesConfig.getLocation()) 
					                    && !generatedFacesConfig.exists();  
			
			if (jsfVersionString.startsWith("1.1")) { //$NON-NLS-1$
				IDataModel model = MavenJSFActivator.getDefault().createJSFDataModel(fproj,jsfVersion11);
				fproj.installProjectFacet(jsfVersion11, model, monitor);	
			}
			else if (jsfVersionString.startsWith("1.2")) { //$NON-NLS-1$
				IDataModel model = MavenJSFActivator.getDefault().createJSFDataModel(fproj,jsfVersion12);
				fproj.installProjectFacet(jsfVersion12, model, monitor);	
			}
			else if (jsfVersionString.startsWith("2.0")) { //$NON-NLS-1$
				IDataModel model = MavenJSFActivator.getDefault().createJSFDataModel(fproj,jsfVersion20);
				model.setBooleanProperty(IJSFFacetInstallDataModelProperties.CONFIGURE_SERVLET,configureWebxml());
				fproj.installProjectFacet(jsfVersion20, model, monitor);
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
	
	private IFile getFileFromUnderlyingresources(final IProject project, final IPath filePath) {
			  IContainer underlyingFolder = ComponentCore.createComponent(project).getRootFolder().getUnderlyingFolder();
			  return project.getFile(underlyingFolder.getProjectRelativePath().append(filePath));
	}

	private IFile getWebXml(IFacetedProject fproj, MavenProject mavenProject) {
		IFile webXml;
		String customWebXml = getCustomWebXml(mavenProject,
				fproj.getProject());
		if (customWebXml == null) {
			webXml = fproj.getProject().getFolder(getWarSourceDirectory(mavenProject,fproj.getProject())).getFile(WEB_XML);
		} else {
			webXml = fproj.getProject().getFile(customWebXml);
		}
		return webXml;
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


	private String getJSFVersion(MavenProject mavenProject) {
		String version = null;
		version = Activator.getDefault().getDependencyVersion(mavenProject, JSF_API_GROUP_ID, JSF_API_ARTIFACT_ID);
		if (version == null) {
			version = Activator.getDefault().getDependencyVersion(mavenProject, JSF_API2_GROUP_ID, JSF_API_ARTIFACT_ID);
		}
		if (version == null) {
			version = inferJsfVersionFromDependencies(mavenProject, JSF_API2_GROUP_ID, JSF_API_ARTIFACT_ID, JSF_VERSION_2_0);
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
