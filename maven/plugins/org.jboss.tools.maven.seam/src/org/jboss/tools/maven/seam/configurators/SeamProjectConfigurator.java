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
import org.jboss.tools.maven.seam.Messages;
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
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeam = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM);
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
	    		installWarFacets(fproj, model, seamVersion, monitor);
	    	} else if ("ear".equals(packaging)) { //$NON-NLS-1$
	    		configureApplicationXml(project, monitor);
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
		    			boolean configureSeamArtifacts = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS);
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

	private IProjectFacetVersion getSeamFacetVersion(String seamVersion) {
		String version = seamVersion.substring(0, 3);
		return seamFacet.getVersion(version);
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
		installJSFFacet(fproj, monitor);
		installM2Facet(fproj, monitor);
		if (!fproj.hasProjectFacet(seamFacet)) {
			IProjectFacetVersion seamFacetVersion = getSeamFacetVersion(seamVersion);
			fproj.installProjectFacet(seamFacetVersion, model, monitor);
		} else {
			String deploying = model.getStringProperty(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS);
			if (deploying != null && deploying.equals(ISeamFacetDataModelProperties.DEPLOY_AS_WAR)) {
				IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
				boolean configureSeamArtifacts = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS);
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
		boolean configureSeamPortlet = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAMPORTLET);
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
			IDataModel model = MavenSeamActivator.getDefault().createJSFDataModel(fproj,jsfVersion);
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
			if (!project.hasNature(ISeamProject.NATURE_ID)) {
				EclipseResourceUtil.addNatureToProject(project,	ISeamProject.NATURE_ID);
			}
			if(!project.hasNature(IKbProject.NATURE_ID)) {
				EclipseResourceUtil.addNatureToProject(project, IKbProject.NATURE_ID);
			}
			for (int i = 0; i < earProjects.length; i++) {
				IEclipsePreferences prefs = SeamCorePlugin
						.getSeamPreferences(project);
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
							MavenProjectManager projectManager = MavenPlugin.getDefault().getMavenProjectManager();
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
				IVirtualComponent refComponent = references[i]
						.getReferencedComponent();
				IProject refProject = refComponent.getProject();
				if (JavaEEProjectUtilities.isEJBProject(refProject)) {
					if (refProject.hasNature(IMavenConstants.NATURE_ID)) {
						IFile pom = refProject
								.getFile(IMavenConstants.POM_FILE_NAME);
						if (pom.exists()) {
							MavenProjectManager projectManager = MavenPlugin
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
		List<Dependency> dependencies = mavenProject.getDependencies();
		Dependency seamDependency = null;
	    for (Dependency dependency:dependencies) {
	    	String groupId = dependency.getGroupId();
    		if (groupId != null && ORG_JBOSS_SEAM_GROUP_ID.equals(groupId)) {
    			String artifactId = dependency.getArtifactId();
    			if (artifactId != null && JBOSS_SEAM_ARTIFACT_ID.equals(artifactId)) {
	    			return dependency.getVersion();
	    		} else if (artifactId != null && artifactId.startsWith(JBOSS_SEAM_ARTIFACT_PREFIX)) {
	    			seamDependency = dependency;
	    		}
	    	}
	    }
	    if (seamDependency != null) {
	    	return seamDependency.getVersion();
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
		IPreferenceStore store = MavenSeamActivator.getDefault().getPreferenceStore();
		boolean configureSeamArtifacts = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM_ARTIFACTS);
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
		boolean configureSeamRuntime = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM_RUNTIME);
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
	
	private void configureApplicationXml(IProject project, IProgressMonitor monitor) {
		EARArtifactEdit earArtifactEdit = null;
		try {
			earArtifactEdit = EARArtifactEdit.getEARArtifactEditForWrite(project);
			if(earArtifactEdit!=null) {
				Application application = earArtifactEdit.getApplication();
				if (application == null) {
					return;
				}
				EList modules = application.getModules();
				for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if (object instanceof Module) {
						Module module = (Module) object;
						String uri = module.getUri();
						if (uri != null && (uri.startsWith("mvel14") || uri.startsWith("mvel2"))) { //$NON-NLS-1$ //$NON-NLS-2$
							iterator.remove();
						}
					}
				}				
				earArtifactEdit.saveIfNecessary(monitor);
			}
		} finally {
			if(earArtifactEdit!=null) {
				earArtifactEdit.dispose();
			}
		}
	}
}
