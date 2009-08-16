package org.jboss.tools.maven.seam.configurators;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
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
		boolean configureSeam = store.getBoolean(MavenSeamActivator.CONFIGURE_SEAM);
		if (!configureSeam) {
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
	    	IDataModel model = createSeamDataModel(deploying, seamVersion);
    		final IFacetedProject fproj = ProjectFacetsManager.create(project);
	    	if ("war".equals(packaging)) { //$NON-NLS-1$
	    		installWarFacets(fproj,model,seamVersion, monitor);
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
		}
	}

	private void installJSFFacet(IFacetedProject fproj, IProgressMonitor monitor)
			throws CoreException {
		if (!fproj.hasProjectFacet(jsfFacet)) {
			fproj.installProjectFacet(jsfVersion, null, monitor);
		}
	}

	private void storeSettings(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences prefs = projectScope.getNode(SeamCorePlugin.PLUGIN_ID);
		prefs.put(ISeamFacetDataModelProperties.SEAM_SETTINGS_VERSION, 
				ISeamFacetDataModelProperties.SEAM_SETTINGS_VERSION_1_1);
		
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

	private IDataModel createSeamDataModel(String deployType, String seamVersion) {
		IDataModel config = (IDataModel) new SeamFacetInstallDataModelProvider().create();
		String seamRuntimeName = getSeamRuntimeName(seamVersion);
		if (seamRuntimeName != null) {
			config.setStringProperty(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME, seamRuntimeName);
		}
		config.setBooleanProperty(ISeamFacetDataModelProperties.DB_ALREADY_EXISTS, true);
		config.setBooleanProperty(ISeamFacetDataModelProperties.RECREATE_TABLES_AND_DATA_ON_DEPLOY, false);
		config.setStringProperty(ISeamFacetDataModelProperties.JBOSS_AS_DEPLOY_AS, deployType);
		//config.setStringProperty(ISeamFacetDataModelProperties.SESSION_BEAN_PACKAGE_NAME, "org.session.beans");
		//config.setStringProperty(ISeamFacetDataModelProperties.ENTITY_BEAN_PACKAGE_NAME, "org.entity.beans");
		//config.setStringProperty(ISeamFacetDataModelProperties.TEST_CASES_PACKAGE_NAME, "org.test.beans");
		config.setBooleanProperty(ISeamFacetDataModelProperties.CONFIGURE_DEFAULT_SEAM_RUNTIME, false);
		config.setBooleanProperty(ISeamFacetDataModelProperties.CONFIGURE_WAR_PROJECT, false);
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
}
