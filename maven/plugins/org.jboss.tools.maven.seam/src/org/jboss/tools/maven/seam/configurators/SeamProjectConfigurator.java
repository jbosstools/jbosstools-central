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
package org.jboss.tools.maven.seam.configurators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.j2ee.application.internal.operations.RemoveComponentFromEnterpriseApplicationOperation;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.jsf.core.internal.project.facet.IJSFFacetInstallDataModelProperties;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.project.registry.MavenProjectManager;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
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
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.MavenUtil;
import org.jboss.tools.maven.core.internal.project.facet.MavenFacetInstallDataModelProvider;
import org.jboss.tools.maven.jsf.MavenJSFActivator;
import org.jboss.tools.maven.seam.MavenSeamActivator;
import org.jboss.tools.maven.ui.Activator;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.core.SeamUtil;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.core.project.facet.SeamVersion;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.jboss.tools.seam.internal.core.project.facet.SeamFacetInstallDataModelProvider;
import org.jboss.tools.seam.ui.wizard.SeamWizardUtils;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * @author snjeza
 *
 */
public class SeamProjectConfigurator extends AbstractProjectConfigurator {

	private static final String JBOSS_SEAM_ARTIFACT_PREFIX = "jboss-seam"; //$NON-NLS-1$
	private static final String ORG_JBOSS_SEAM_GROUP_ID = "org.jboss.seam"; //$NON-NLS-1$
	private static final String JBOSS_SEAM_ARTIFACT_ID = "jboss-seam"; //$NON-NLS-1$

	protected static final IProjectFacet dynamicWebFacet;
	protected static final IProjectFacetVersion dynamicWebVersion;
	protected static final IProjectFacet javaFacet;
	protected static final IProjectFacetVersion javaVersion;
	protected static final IProjectFacet jsfFacet;
	protected static final IProjectFacetVersion jsfVersion;
	protected static final IProjectFacet earFacet;
	protected static final IProjectFacetVersion earVersion;
	protected static final IProjectFacet ejbFacet;
	protected static final IProjectFacetVersion ejbVersion;
	protected static final IProjectFacet m2Facet;
	protected static final IProjectFacetVersion m2Version;
	private static final IProjectFacet seamFacet;
	private static final IProjectFacet portletFacet;
	private static final IProjectFacet jsfportletFacet;
	private static final IProjectFacet seamPortletFacet;
	private static final IProjectFacetVersion seamPortletVersion;
	
	static {
		seamFacet = ProjectFacetsManager.getProjectFacet("jst.seam"); //$NON-NLS-1$
		javaFacet = ProjectFacetsManager.getProjectFacet("jst.java"); //$NON-NLS-1$
		javaVersion = javaFacet.getVersion("5.0"); //$NON-NLS-1$
		dynamicWebFacet = ProjectFacetsManager.getProjectFacet("jst.web"); //$NON-NLS-1$
		dynamicWebVersion = dynamicWebFacet.getVersion("2.5");  //$NON-NLS-1$
		jsfFacet = ProjectFacetsManager.getProjectFacet("jst.jsf"); //$NON-NLS-1$
		jsfVersion = jsfFacet.getVersion("1.2"); //$NON-NLS-1$
		earFacet = ProjectFacetsManager.getProjectFacet("jst.ear"); //$NON-NLS-1$
		earVersion = earFacet.getVersion("5.0"); //$NON-NLS-1$
		m2Facet = ProjectFacetsManager.getProjectFacet("jboss.m2"); //$NON-NLS-1$
		m2Version = m2Facet.getVersion("1.0"); //$NON-NLS-1$
		ejbFacet = ProjectFacetsManager.getProjectFacet("jst.ejb"); //$NON-NLS-1$
		ejbVersion = ejbFacet.getVersion("3.0"); //$NON-NLS-1$
		portletFacet = ProjectFacetsManager.getProjectFacet("jboss.portlet"); //$NON-NLS-1$
		jsfportletFacet = ProjectFacetsManager.getProjectFacet("jboss.jsfportlet"); //$NON-NLS-1$
		seamPortletFacet = ProjectFacetsManager.getProjectFacet("jboss.seamportlet"); //$NON-NLS-1$
		seamPortletVersion = seamPortletFacet.getVersion("1.0"); //$NON-NLS-1$
	}
	
	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		// adds Seam capabilities if there are Seam dependencies
		MavenProject mavenProject = request.getMavenProject();
		IProject project = request.getProject();
		configureInternal(mavenProject,project, monitor);

	}

	private boolean isSeamSettingChangedByUser(IProject project) {
		IEclipsePreferences projectPreferences = SeamCorePlugin.getSeamPreferences(project);
		boolean seamSettingsChangedByUser = projectPreferences.getBoolean(ISeamFacetDataModelProperties.SEAM_SETTINGS_CHANGED_BY_USER, false);
		return seamSettingsChangedByUser;
	}
	
	private void configureInternal(MavenProject mavenProject,IProject project,
			IProgressMonitor monitor) throws CoreException {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean configureSeam = store.getBoolean(Activator.CONFIGURE_SEAM);
		if (!configureSeam) {
			return;
		}
		if (isSeamSettingChangedByUser(project)) {
			return;
		}
		IProject rootSeamProject = SeamWizardUtils.getRootSeamProject(project);
		if (rootSeamProject != null && isSeamSettingChangedByUser(rootSeamProject)) {
			return;
		}
		String packaging = mavenProject.getPackaging();
	    String seamVersion = getSeamVersion(mavenProject);
	    if (seamVersion != null) {
	    	IProject[] earProjects = J2EEProjectUtilities.getReferencingEARProjects(project);
	    	String deploying = packaging;
	    	if (earProjects.length > 0) {
	    		deploying = "ear"; //$NON-NLS-1$
	    	}
	    	final IFacetedProject fproj = ProjectFacetsManager.create(project);
	    	if (fproj == null) {
	    		return;
	    	}
	    	if ("war".equals(packaging)) { //$NON-NLS-1$
	    		IDataModel model = createSeamDataModel(deploying, seamVersion, project);
	    		//JBIDE-10785 : refresh parent to prevent 
				// org.osgi.service.prefs.BackingStoreException: Resource '/parent/web/.settings' does not exist.
				if (!fproj.hasProjectFacet(jsfFacet)) {
	    			MavenUtil.refreshParent(mavenProject);
	    		}
	    		
	    		installWarFacets(fproj, model, seamVersion, monitor);
	    	} else if ("ear".equals(packaging)) { //$NON-NLS-1$
	    		installEarFacets(fproj, monitor);
	    		installM2Facet(fproj, monitor);
	    		IProject webProject = getReferencingSeamWebProject(project);
	    		if (webProject != null) {
	    			IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(webProject);
	    			String deployingType = prefs.get(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS,null);
	    			if (deployingType == null || deployingType.equals(ISeamFacetDataModelProperties.DEPLOY_AS_WAR)) {
	    				prefs.put(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS,ISeamFacetDataModelProperties.DEPLOY_AS_EAR);
	    				storeSettings(webProject);
	    			}
	    			IProject ejbProject = getReferencingSeamEJBProject(project);
		    		if (ejbProject != null) {
		    			prefs.put(ISeamFacetDataModelProperties.SEAM_EJB_PROJECT, ejbProject.getName());
		    			IJavaProject javaProject = JavaCore.create(ejbProject);
		    			boolean configureSeamArtifacts = store.getBoolean(Activator.CONFIGURE_SEAM_ARTIFACTS);
						if (configureSeamArtifacts) {
							if (javaProject != null && javaProject.isOpen()) {
								try {
									IClasspathEntry[] entries = javaProject.getRawClasspath();
									for (int i = 0; i < entries.length; i++) {
										IClasspathEntry entry = entries[i];
										if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
											String sourceFolder = entry.getPath().toString();
											prefs.put(ISeamFacetDataModelProperties.SESSION_BEAN_SOURCE_FOLDER, sourceFolder);
											prefs.put(ISeamFacetDataModelProperties.ENTITY_BEAN_SOURCE_FOLDER, sourceFolder);
											break;
										}
									}
								} catch (JavaModelException e) {
									MavenSeamActivator.log(e);
								}
							}
							IPackageFragment[] packageFragments = javaProject.getPackageFragments();
							for (int i = 0; i < packageFragments.length; i++) {
								IPackageFragment pf = packageFragments[i];
								if (pf != null && pf.getKind() == IPackageFragmentRoot.K_SOURCE && !pf.isDefaultPackage()) {
									if (pf.hasSubpackages() && !pf.hasChildren()) {
										continue;
									}
									String packageName = pf.getElementName();
									prefs.put(ISeamFacetDataModelProperties.SESSION_BEAN_PACKAGE_NAME, packageName);
									prefs.put(ISeamFacetDataModelProperties.ENTITY_BEAN_PACKAGE_NAME, packageName);
								}
							}
						}
		    		}
		    		storeSettings(webProject);
	    		}
	    		
	    	} else if ("ejb".equals(packaging)) { //$NON-NLS-1$
	    		installM2Facet(fproj,monitor);
	    		installEjbFacets(fproj, monitor);
	    		addSeamSupport(project, earProjects);
	    		storeSettings(project);
	    		
	    	}
//	    	addSeamSupport(project);
//	    	
//	    	storeSettings(project);
	    }
	}

	private String getViewFolder(IProject project) {
		IVirtualComponent com = ComponentCore.createComponent(project);
		String viewFolder = null;
		if(com!=null) {
			IVirtualFolder webRootFolder = com.getRootFolder().getFolder(new Path("/")); //$NON-NLS-1$
			if(webRootFolder!=null) {
				viewFolder = webRootFolder.getUnderlyingFolder().getFullPath().toString();
			}
		}
		return viewFolder;
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

	private IProjectFacetVersion getSeamFacetVersion(String seamVersion) throws CoreException {
		String version = seamVersion.substring(0, 3);
		IProjectFacetVersion facetVersion = null;
		try {
		  facetVersion = seamFacet.getVersion(version);
		} catch (Exception e) {
		  MavenSeamActivator.log(e, "Seam version "+ version+ " is not supported, using latest supported facet version instead"); 
		  facetVersion = seamFacet.getLatestVersion();
		}
		return facetVersion;
	}

	private void installEarFacets(IFacetedProject fproj,IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(earFacet)) {
			fproj.installProjectFacet(earVersion, null, monitor);
		}
		
	}
	
	private void installEjbFacets(IFacetedProject fproj,IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(javaFacet)) {
			fproj.installProjectFacet(javaVersion, null, monitor);
		}
		if (!fproj.hasProjectFacet(ejbFacet)) {
			fproj.installProjectFacet(ejbVersion, null, monitor);
		}
	}

	private void installWarFacets(IFacetedProject fproj,IDataModel model, String seamVersion,IProgressMonitor monitor) throws CoreException {
		if (!fproj.hasProjectFacet(javaFacet)) {
			fproj.installProjectFacet(javaVersion, null, monitor);
		}
		if (!fproj.hasProjectFacet(dynamicWebFacet)) {
			fproj.installProjectFacet(dynamicWebVersion, null, monitor);
		}
		//Seam requires the JSF facet (!!!)
		installJSFFacet(fproj, monitor);
		installM2Facet(fproj, monitor);
		if (!fproj.hasProjectFacet(seamFacet)) {
			IProjectFacetVersion seamFacetVersion = getSeamFacetVersion(seamVersion);
			fproj.installProjectFacet(seamFacetVersion, model, monitor);
		} else {
			String deploying = model.getStringProperty(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS);
			if (deploying != null && deploying.equals(ISeamFacetDataModelProperties.DEPLOY_AS_WAR)) {
				IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
				boolean configureSeamArtifacts = store.getBoolean(Activator.CONFIGURE_SEAM_ARTIFACTS);
				if (!configureSeamArtifacts) {
					return;
				}
				IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(fproj.getProject());
				setModelProperty(model, prefs,ISeamFacetDataModelProperties.SESSION_BEAN_SOURCE_FOLDER);
				setModelProperty(model, prefs,ISeamFacetDataModelProperties.ENTITY_BEAN_SOURCE_FOLDER);
				setModelProperty(model, prefs,ISeamFacetDataModelProperties.SESSION_BEAN_PACKAGE_NAME);
				setModelProperty(model, prefs,ISeamFacetDataModelProperties.ENTITY_BEAN_PACKAGE_NAME);
				setModelProperty(model, prefs,ISeamFacetDataModelProperties.WEB_CONTENTS_FOLDER);
			}
		}
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeamPortlet = store.getBoolean(Activator.CONFIGURE_SEAMPORTLET);
		if (!configureSeamPortlet) {
			return;
		}
		if (fproj.hasProjectFacet(seamFacet) && fproj.hasProjectFacet(portletFacet) && fproj.hasProjectFacet(jsfportletFacet)) {
			if (!fproj.hasProjectFacet(seamPortletFacet)) {
				fproj.installProjectFacet(seamPortletVersion, null, monitor);
			}
		}
	}

	private void setModelProperty(IDataModel model, IEclipsePreferences prefs, String property) {
		String value = model.getStringProperty(property);
		if (value != null && value.trim().length() > 0) {
			prefs.put(property, value);
		}
	}

	private void installJSFFacet(IFacetedProject fproj, IProgressMonitor monitor)
			throws CoreException {
		if (!fproj.hasProjectFacet(jsfFacet)) {
			IDataModel model = MavenJSFActivator.getDefault().createJSFDataModel(fproj,jsfVersion);
			//Fix for JBIDE-9454, to prevent complete overwrite of web.xml 
			model.setBooleanProperty(IJSFFacetInstallDataModelProperties.CONFIGURE_SERVLET,false);
			fproj.installProjectFacet(jsfVersion, model, monitor);
		}
	}

	private void storeSettings(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences prefs = projectScope.getNode(SeamCorePlugin.PLUGIN_ID);
		String version = prefs.get(ISeamFacetDataModelProperties.SEAM_SETTINGS_VERSION, null);
		if (version == null) {
			prefs.put(ISeamFacetDataModelProperties.SEAM_SETTINGS_VERSION, 
				ISeamFacetDataModelProperties.SEAM_SETTINGS_VERSION_1_1);
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			MavenSeamActivator.log(e);
		}
	}

	private void addSeamSupport(IProject project, IProject[] earProjects) {
		if(project==null) {
			return;
		}
		try {
			SeamUtil.enableSeamSupport(project);
			for (int i = 0; i < earProjects.length; i++) {
				IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(project);
				String seamParentProject = prefs.get(ISeamFacetDataModelProperties.SEAM_PARENT_PROJECT,null);
				if (seamParentProject == null) {
					IProject earProject = earProjects[i];
					IProject webProject = getReferencingSeamWebProject(earProject);
					if (webProject != null) {
						prefs.put(ISeamFacetDataModelProperties.SEAM_PARENT_PROJECT,webProject.getName());
						break;
					}
				}
			}
			
		} catch (CoreException e) {
			MavenSeamActivator.log(e);
		}
		
	}

	private IProject getReferencingSeamWebProject(IProject earProject)
			throws CoreException {
		IVirtualComponent component = ComponentCore.createComponent(earProject);
		if (component != null) {
			IVirtualReference[] references = component.getReferences();
			for (int i = 0; i < references.length; i++) {
				IVirtualComponent refComponent = references[i].getReferencedComponent();
				IProject refProject = refComponent.getProject();
				if (JavaEEProjectUtilities.isDynamicWebProject(refProject)) {
					if (refProject.hasNature(IMavenConstants.NATURE_ID)) {
						IFile pom = refProject.getFile(IMavenConstants.POM_FILE_NAME);
						if (pom.exists()) {
							MavenProjectManager projectManager = MavenPluginActivator.getDefault().getMavenProjectManager();
						     IMavenProjectFacade facade = projectManager.create(pom, true, null);
						      if(facade!=null) {
						        MavenProject mavenProject = facade.getMavenProject(null);
						        if (mavenProject != null) {
						        	String version = getSeamVersion(mavenProject);
						        	if (version != null) {
						        		return refProject;
						        	}
						        }
						      }
						}
					      
					}
				}
			}
		}
		return null;
	}
	
	private IProject getReferencingSeamEJBProject(IProject earProject)
			throws CoreException {
		IVirtualComponent component = ComponentCore.createComponent(earProject);
		if (component != null) {
			IVirtualReference[] references = component.getReferences();
			for (int i = 0; i < references.length; i++) {
				IVirtualComponent refComponent = references[i].getReferencedComponent();
				IProject refProject = refComponent.getProject();
				if (JavaEEProjectUtilities.isEJBProject(refProject)) {
					if (refProject.hasNature(IMavenConstants.NATURE_ID)) {
						IFile pom = refProject
								.getFile(IMavenConstants.POM_FILE_NAME);
						if (pom.exists()) {
							MavenProjectManager projectManager = MavenPluginActivator
									.getDefault().getMavenProjectManager();
							IMavenProjectFacade facade = projectManager.create(
									pom, true, null);
							if (facade != null) {
								MavenProject mavenProject = facade
										.getMavenProject(null);
								if (mavenProject != null) {
									String version = getSeamVersion(mavenProject);
									if (version != null) {
										return refProject;
									}
								}
							}
						}

					}
				}
			}
		}
		return null;
	}

	private String getSeamVersion(MavenProject mavenProject) {
		List<Artifact> artifacts = new ArrayList<Artifact>();
		ArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_TEST);
		for (Artifact artifact : mavenProject.getArtifacts()) {
			if (filter.include(artifact)) {
				artifacts.add(artifact);
			}
		}
		Artifact seamArtifact = null;
		for (Artifact artifact:artifacts) {
	    	String groupId = artifact.getGroupId();
    		if (ORG_JBOSS_SEAM_GROUP_ID.equals(groupId)) {
    			String artifactId = artifact.getArtifactId();
    			if (artifactId != null && JBOSS_SEAM_ARTIFACT_ID.equals(artifactId)) {
	    			return artifact.getVersion();
	    		} else if (artifactId != null && artifactId.startsWith(JBOSS_SEAM_ARTIFACT_PREFIX)) {
	    			seamArtifact = artifact;
	    		}
	    	}
	    }
	    if (seamArtifact != null) {
	    	return seamArtifact.getVersion();
	    }
	    return null;
	}

	private IDataModel createSeamDataModel(String deployType, String seamVersion, IProject project) {
		IDataModel config = (IDataModel) new SeamFacetInstallDataModelProvider().create();
		String seamRuntimeName = getSeamRuntimeName(seamVersion);
		if (seamRuntimeName != null) {
			config.setStringProperty(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME, seamRuntimeName);
		}
		config.setBooleanProperty(ISeamFacetDataModelProperties.DB_ALREADY_EXISTS, true);
		config.setBooleanProperty(ISeamFacetDataModelProperties.RECREATE_TABLES_AND_DATA_ON_DEPLOY, false);
		config.setStringProperty(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS, deployType);
		config.setBooleanProperty(ISeamFacetDataModelProperties.CONFIGURE_DEFAULT_SEAM_RUNTIME, false);
		config.setBooleanProperty(ISeamFacetDataModelProperties.CONFIGURE_WAR_PROJECT, false);
		config.setBooleanProperty(ISeamFacetDataModelProperties.PROJECT_ALREADY_EXISTS, true);
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeamArtifacts = store.getBoolean(Activator.CONFIGURE_SEAM_ARTIFACTS);
		if (!configureSeamArtifacts) {
			return config;
		}
		String viewFolder = getViewFolder(project);
		if (viewFolder != null) {
			config.setStringProperty(ISeamFacetDataModelProperties.WEB_CONTENTS_FOLDER, viewFolder);
		}
		IJavaProject javaProject = JavaCore.create(project);
		List<IPath> sourcePaths = new ArrayList<IPath>();
		if (javaProject != null && javaProject.isOpen()) {
			try {
				IClasspathEntry[] entries = javaProject.getRawClasspath();
				for (int i = 0; i < entries.length; i++) {
					IClasspathEntry entry = entries[i];
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						sourcePaths.add(entry.getPath());
					}
				}
			} catch (JavaModelException e) {
				MavenSeamActivator.log(e);
			}
			if (sourcePaths.size() > 0) {
				IPath actionSourceFolder = null;
				IPath modelSourceFolder = null;
				for (IPath sourcePath:sourcePaths) {
					if (sourcePath.toString().contains("hot")) { //$NON-NLS-1$
						actionSourceFolder = sourcePath;
					} else {
						modelSourceFolder = sourcePath;
					}
				}
				if (actionSourceFolder == null) {
					actionSourceFolder = modelSourceFolder;
				}
				if (modelSourceFolder == null) {
					modelSourceFolder = actionSourceFolder;
				}
				String modelSourceFolderStr = modelSourceFolder.toString();
				config.setStringProperty(ISeamFacetDataModelProperties.ENTITY_BEAN_SOURCE_FOLDER, modelSourceFolderStr);
				String actionSourceFolderStr = actionSourceFolder.toString();
				config.setStringProperty(ISeamFacetDataModelProperties.SESSION_BEAN_SOURCE_FOLDER, actionSourceFolderStr);
			}
			try {
				IPackageFragment[] packageFragments = javaProject.getPackageFragments();
				for (int i = 0; i < packageFragments.length; i++) {
					IPackageFragment pf = packageFragments[i];
					if (pf != null && pf.getKind() == IPackageFragmentRoot.K_SOURCE && !pf.isDefaultPackage()) {
						String packageName = pf.getElementName();
						config.setStringProperty(ISeamFacetDataModelProperties.SESSION_BEAN_PACKAGE_NAME, packageName);
						config.setStringProperty(ISeamFacetDataModelProperties.ENTITY_BEAN_PACKAGE_NAME, packageName);
					}
				}
			} catch (JavaModelException e) {
				MavenSeamActivator.log(e);
			}
		}
		//config.setStringProperty(ISeamFacetDataModelProperties.TEST_CASES_PACKAGE_NAME, "org.test.beans");
		//config.setStringProperty(ISeamFacetDataModelProperties.SEAM_CONNECTION_PROFILE, "noop-connection");
		//config.setProperty(ISeamFacetDataModelProperties.JDBC_DRIVER_JAR_PATH, new String[] { "noop-driver.jar" });
		return config;
	}

	private String getSeamRuntimeName(String seamVersionStr) {
		if (seamVersionStr == null) {
			return null;
		}
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeamRuntime = store.getBoolean(Activator.CONFIGURE_SEAM_RUNTIME);
		if (!configureSeamRuntime) {
			return null;
		}
		String seamRuntime5 = null;
		String seamRuntime3 = null;
		SeamRuntime[] seamRuntimes = SeamRuntimeManager.getInstance().getRuntimes();
		for (int i = 0; i < seamRuntimes.length; i++) {
			SeamRuntime seamRuntime = seamRuntimes[i];
			String seamHomeDir = seamRuntime.getHomeDir();
			if (seamHomeDir == null) {
				continue;
			}
			if ( ! (new File(seamHomeDir).exists() )) {
				continue;
			}
			String fullVersion = SeamUtil.getSeamVersionFromManifest(seamRuntime.getHomeDir());
			if (fullVersion == null)  {
				continue;
			}
			if (fullVersion == seamVersionStr) {
				return seamRuntime.getName();
			}
			if (seamRuntime5 == null) {
				String fullVersion5 = fullVersion.substring(0,5);
				String seamVersion5 = seamVersionStr.substring(0,5);
				if (seamVersion5.equals(fullVersion5)) {
					seamRuntime5 = seamRuntime.getName();
				}
			}
			if (seamRuntime5 == null && seamRuntime3 == null) {
				String fullVersion3 = fullVersion.substring(0,3);
				String seamVersion3 = seamVersionStr.substring(0,3);
				if (seamVersion3.equals(fullVersion3)) {
					seamRuntime3 = seamRuntime.getName();
				}
			}
		}
		if (seamRuntime5 != null) {
			return seamRuntime5;
		}
		SeamVersion seamVersion = SeamVersion.parseFromString(seamVersionStr.substring(0,3));
		SeamRuntime defaultRuntime = SeamRuntimeManager.getInstance().getDefaultRuntime(seamVersion);
		if (defaultRuntime != null) {
			return defaultRuntime.getName();
		}
		return seamRuntime3;
	}
	
	private static class RemoveComponentFromEnterpriseApplicationOperationEx extends RemoveComponentFromEnterpriseApplicationOperation {

		public RemoveComponentFromEnterpriseApplicationOperationEx(
				IDataModel model) {
			super(model);
		}

		@Override
		protected void updateEARDD(IProgressMonitor monitor) {
			//super.updateEARDD(monitor);
		}

		
	}
}
