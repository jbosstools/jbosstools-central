/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.project.ProjectConfigurationManager;
import org.eclipse.m2e.core.project.IArchetype;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator;
import org.eclipse.m2e.core.ui.internal.Messages;
import org.eclipse.m2e.core.ui.internal.util.ArchetypeUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.settings.MavenSettingsChangeListener;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.maven.project.examples.utils.MavenArtifactHelper;
import org.jboss.tools.maven.project.examples.wizard.xpl.MavenProjectWizardArchetypeParametersPage;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ArchetypeModel;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.project.examples.wizard.IProjectExamplesWizardPage;
import org.jboss.tools.project.examples.wizard.WizardContext;

/**
 * 
 * @author snjeza
 *
 */
@SuppressWarnings("restriction")
public class ArchetypeExamplesWizardPage extends MavenProjectWizardArchetypeParametersPage
		implements IProjectExamplesWizardPage, MavenSettingsChangeListener {

	private ProjectExample projectExample;
	private boolean initialized = false;
	private Map<String, Object> propertiesMap = new HashMap<>();
	private WizardContext context;
	private MissingRepositoryWarningComponent warningComponent;
	private IStatus enterpriseRepoStatus;

	private ArchetypeModel archetypeModel;

	public ArchetypeExamplesWizardPage() {
		super(new ProjectImportConfiguration());
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		packageCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isCurrentPage()) {
					context.setProperty(MavenProjectConstants.PACKAGE, packageCombo.getText());
				}
			}
		});
		artifactIdCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isCurrentPage()) {
					context.setProperty(MavenProjectConstants.PROJECT_NAME, artifactIdCombo.getText());
				}
				validate();
			}
		});

		if (projectExample != null && !initialized) {
			initializeArchetype();
		}

		MavenCoreActivator.getDefault().registerMavenSettingsChangeListener(this);
	}

	protected void initializeArchetype() {
		if (getContainer() == null || archetypeModel == null) {
			return;
		}
		// System.err.println("Initializing archetype data "+
		// archetypeModel.getArchetypeArtifactId() + "
		// "+archetypeModel.getArchetypeVersion());
		Archetype archetype = new Archetype();

		archetype.setGroupId(archetypeModel.getArchetypeGroupId());
		archetype.setArtifactId(archetypeModel.getArchetypeArtifactId());
		archetype.setVersion(archetypeModel.getArchetypeVersion());
		archetype.setRepository(archetypeModel.getArchetypeRepository());

		if (ArchetypeUtil.areEqual(archetype, this.archetype) && initialized) {
			return;
		}

		// Since we set archetypeChanged=false later on, we need to do the equivalent of
		// MavenProjectWizardArchetypeParametersPage.loadArchetypeDescriptor(),
		// as we don't have the guarantee archetypeModel holds ALL the required
		// properties
		// This really is an extra-safe guard, as I believe we'll probably always
		// redefine all required properties in project-examples-maven-xxx.xml
		Properties defaultRequiredProperties = getRequiredProperties(archetype,
				archetypeModel.getArchetypeRepository());

		Properties properties = new Properties();

		for (Object key : defaultRequiredProperties.keySet()) {
			properties.put(key, defaultRequiredProperties.get(key));
		}

		// Override default required properties with our specific JBoss Tools values
		for (Object key : archetypeModel.getArchetypeProperties().keySet()) {
			properties.put(key, archetypeModel.getArchetypeProperties().get(key));
		}

		archetype.setProperties(properties);
		setArchetype(archetype);

		// JBIDE-9823 : Hack to prevent the properties table to be loaded a 2nd time
		// when setVisible() is called in MavenProjectWizardArchetypeParametersPage.
		// It needs to be called AFTER setArchetype(archetype) !!!
		archetypeChanged = false;
		if (resolverConfigurationComponent != null) {
			resolverConfigurationComponent.setExpanded(
					!resolverConfigurationComponent.getResolverConfiguration().getActiveProfileList().isEmpty());
		}
		if (propertiesTable != null) {
			initialized = true;
		}

		context.setProperty(MavenProjectConstants.HAS_ENTERPRISE_PROPERTY,
				defaultRequiredProperties.containsKey(MavenProjectConstants.ENTERPRISE_TARGET));

		Object enterpriseValue = context.getProperty(MavenProjectConstants.ENTERPRISE_TARGET);
		Boolean enterprise = (enterpriseValue instanceof Boolean) ? (Boolean) enterpriseValue : Boolean.FALSE;
		updateArchetypeProperty(MavenProjectConstants.ENTERPRISE_TARGET, enterprise.toString());
	}

	@Override
	protected void createAdvancedSettings(Composite composite, GridData gridData) {
		super.createAdvancedSettings(composite, gridData);
		warningComponent = new MissingRepositoryWarningComponent(composite);
	}

	public Archetype getArchetype() {
		return archetype;
	}

	private Properties getRequiredProperties(Archetype archetype, final String archetypeRepositoryUrl) {
		final String groupId = archetype.getGroupId();
		final String artifactId = archetype.getArtifactId();
		final String version = archetype.getVersion();
		final String archetypeName = groupId + ":" + artifactId + ":" + version; //$NON-NLS-1$ //$NON-NLS-2$
		final Properties requiredProperties = new Properties();
		try {
			getContainer().run(false, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask(NLS.bind("Downloading Archetype {0}", archetypeName), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
					try {
						IMaven maven = MavenPlugin.getMaven();

						List<ArtifactRepository> repositories = maven.getArtifactRepositories();

						ArtifactRepository archetypeRepository = null;

						if (StringUtils.isNotEmpty(archetypeRepositoryUrl)) {
							archetypeRepository = maven.createArtifactRepository("archetypeRepo",
									archetypeRepositoryUrl);
						}

						// JBIDE-10018 : remote archetypes need to be downloaded 1st or resolution will
						// fail
						Artifact a = downloadArchetype(groupId, artifactId, version, archetypeRepository, repositories);
						List<RequiredProperty> properties = M2EUIPluginActivator.getDefault().getArchetypePlugin()
								.getRequiredProperties(new IArchetype() {

									@Override
									public String getVersion() {
										return a.getVersion();
									}

									@Override
									public String getGroupId() {
										return a.getGroupId();
									}

									@Override
									public String getArtifactId() {
										return a.getArtifactId();
									}
								}, monitor);
						if (properties != null) {
							for (Object o : properties) {
								if (o instanceof RequiredProperty) {
									RequiredProperty rp = (RequiredProperty) o;
									requiredProperties.put(rp.getKey(), rp.getDefaultValue());
								}
							}
						}
					} catch (CoreException ex) {
						// TODO don't swallow exceptions
						MavenProjectExamplesActivator.log(ex);
					} finally {
						monitor.done();
					}
				}

			});
		} catch (InterruptedException ex) {
			// ignore
		} catch (InvocationTargetException ex) {
			String msg = NLS.bind("Error downloading archetype {0}", archetypeName);//$NON-NLS-1$
			MavenProjectExamplesActivator.log(ex, msg);
			setErrorMessage(msg + "\n" + ex.toString()); //$NON-NLS-1$
		}
		return requiredProperties;
	}

	/**
	 * Apparently, Archetype#generateProjectFromArchetype 2.0-alpha-4 does not
	 * attempt to resolve archetype from configured remote repositories. To
	 * compensate, we populate local repo with archetype pom/jar. So we use the same
	 * hack as in m2e to force the download first.
	 * 
	 * @see http://git.eclipse.org/c/m2e/m2e-core.git/tree/org.eclipse.m2e.core/src/org/eclipse/m2e/core/internal/project/ProjectConfigurationManager.java#n596
	 */
	private Artifact downloadArchetype(String groupId, String artifactId, String version,
			ArtifactRepository archetypeRepository, List<ArtifactRepository> repositories) throws CoreException {
		IMaven maven = MavenPlugin.getMaven();
		ArrayList<ArtifactRepository> repos = new ArrayList<>();
		if (archetypeRepository != null) {
			repos.add(archetypeRepository);
		}
		repos.addAll(maven.getArtifactRepositories()); // see
														// org.apache.maven.archetype.downloader.DefaultDownloader#download
		IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
		maven.resolve(groupId, artifactId, version, "pom", null, repos, nullProgressMonitor); //$NON-NLS-1$
		Artifact a = maven.resolve(groupId, artifactId, version, "jar", null, repos, nullProgressMonitor); //$NON-NLS-1$
		return a;
	}

	public void updateArchetypeProperty(String key, String value) {
		if (propertiesTable == null)
			return;

		for (int i = 0; i < propertiesTable.getItemCount(); i++) {
			TableItem item = propertiesTable.getItem(i);
			if (item.getText(KEY_INDEX).equals(key) && !item.getText(VALUE_INDEX).equals(value)) {
				item.setText(VALUE_INDEX, value);
				// don't break, just to be sure
			}
		}
	}

	public void setArtifactId(String projectName) {
		if (artifactIdCombo != null && !artifactIdCombo.getText().equals(projectName)) {
			artifactIdCombo.setText(projectName);
		}
	}

	public void setPackageName(String packageName) {
		if (packageCombo != null) {
			if (!packageCombo.getText().equals(packageName)) {
				packageCombo.setText(packageName);
			}
			if (!isCurrentPage()) {
				if (!groupIdCombo.getText().equals(packageName)) {
					groupIdCombo.setText(packageName);
				}
			}
		}
	}

	@Override
	protected void validate() {
		if (getControl() != null && getControl().isVisible()) {
			checkEnterpriseProperty();
		}
		super.validate();
	}

	private void checkEnterpriseProperty() {
		if (warningComponent == null || warningComponent.isDisposed() || propertiesTable.isDisposed()) {
			// Not initialized yet
			return;
		}
		for (int i = 0; i < propertiesTable.getItemCount(); i++) {
			TableItem item = propertiesTable.getItem(i);
			String value = item.getText(VALUE_INDEX);
			if (item.getText(KEY_INDEX).equals("enterprise")) { //$NON-NLS-1$
				warningComponent.setLinkText(""); //$NON-NLS-1$
				if ((Boolean.TRUE.toString().equalsIgnoreCase(value) || "y".equalsIgnoreCase(value)
						|| "yes".equalsIgnoreCase(value))) {
					if (enterpriseRepoStatus == null) {
						enterpriseRepoStatus = MavenArtifactHelper.checkRequirementsAvailable(projectExample);
					}
					if (!enterpriseRepoStatus.isOK()) {
						warningComponent.setLinkText(enterpriseRepoStatus.getMessage());
					}
				}
				return;
			}
		}
	}

	private Throwable getRootCause(Throwable ex) {
		if (ex == null)
			return null;
		Throwable rootCause = getRootCause(ex.getCause());
		if (rootCause == null) {
			rootCause = ex;
		}
		return rootCause;
	}

	@Override
	public boolean finishPage() {
		final Model model = getModel();
		final String groupId = model.getGroupId();
		final String artifactId = model.getArtifactId();
		final String version = model.getVersion();
		final String javaPackage = getJavaPackage();
		final Properties properties = getProperties();
		final Archetype archetype = getArchetype();
		ArchetypeExamplesWizardFirstPage simplePage = getSimplePage();
		if (simplePage == null) {
			MavenProjectExamplesActivator.log("Cannot import maven archetype");
			return false;
		}
		final ProjectImportConfiguration configuration = getImportConfiguration();
		String projectName = ProjectConfigurationManager.getProjectName(configuration, model);
		propertiesMap.put(ProjectExamplesActivator.PROPERTY_PROJECT_NAME, projectName);
		final IPath location = simplePage.getLocationPath();
		propertiesMap.put(ProjectExamplesActivator.PROPERTY_LOCATION_PATH, location);
		propertiesMap.put(ProjectExamplesActivator.PROPERTY_ARTIFACT_ID, artifactId);

		boolean pomExists = location.append(projectName).append(IMavenConstants.POM_FILE_NAME).toFile().exists();
		if (pomExists) {
			MessageDialog.openError(getShell(), NLS.bind(Messages.wizardProjectJobFailed, projectName),
					Messages.wizardProjectErrorPomAlreadyExists);
			return false;
		}

		final IWorkspaceRunnable wr = new IWorkspaceRunnable() {

			public void run(final IProgressMonitor monitor) throws CoreException {
				MavenPlugin.getMaven().createExecutionContext().execute(new ICallable<Void>() {
					public Void call(IMavenExecutionContext context, IProgressMonitor monitor) throws CoreException {

						//ensureArchetyperCacheIsLoaded(archetype);

						M2EUIPluginActivator.getDefault().getArchetypePlugin().getGenerator()
								.createArchetypeProjects(location, new IArchetype() {

									@Override
									public String getVersion() {
										return archetype.getVersion();
									}

									@Override
									public String getGroupId() {
										return archetype.getGroupId();
									}

									@Override
									public String getArtifactId() {
										return archetype.getArtifactId();
									}
								}, groupId, artifactId, version, javaPackage, convert(properties), false, monitor);
						return null;
					}
				}, monitor);

			}

			// Need to call ArchetypeArtifactManager#exists(...) to populate internal
			// archetyper cache for offline use
			// This is absolutely f*** up but we're using an oooold version of that
			// archetyper
//			private void ensureArchetyperCacheIsLoaded(Archetype archetype) throws CoreException {
//				IMaven maven = MavenPlugin.getMaven();
//
//				ArrayList<ArtifactRepository> repos = new ArrayList<>();
//				repos.addAll(maven.getArtifactRepositories());
//
//				ArtifactRepository archetypeRepository = null;
//				if (StringUtils.isNotBlank(archetype.getRepository())) {
//					archetypeRepository = maven.createArtifactRepository(archetype.getArtifactId() + "-repo", //$NON-NLS-1$
//							archetype.getRepository().trim());
//					repos.add(0, archetypeRepository);// If the archetype doesn't exist locally, this will be the first
//														// remote repo to be searched.
//				}
//				
//				// Code below does not work, due to ArchetypeArtifactManager not accessible in runtime
//				
//				ArchetypeArtifactManager aam = maven.lookup(ArchetypeArtifactManager.class);
//				aam.exists(archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion(),
//						archetypeRepository, maven.getLocalRepository(), repos,
//						IMavenExecutionContext.getThreadContext().get().newProjectBuildingRequest());
//			}
		};

		final IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					final IWorkspace ws = ResourcesPlugin.getWorkspace();
					ws.run(wr, ws.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			ProjectExamplesActivator.log(e);
			return true;
		} catch (InvocationTargetException e) {
			ProjectExamplesActivator.log(e);
			Throwable ex = e.getTargetException();
			String message = ex.getMessage();
			Throwable rootCause = getRootCause(ex);
			if (rootCause != null) {
				message += "\nRoot cause : " + rootCause.getMessage();
			}
			MessageDialog.openError(getShell(), "Error", message);
			return true;
		}

		return true;
	}

	private ArchetypeExamplesWizardFirstPage getSimplePage() {
		IWizardPage[] pages = getWizard().getPages();
		for (IWizardPage page : pages) {
			if (page instanceof ArchetypeExamplesWizardFirstPage) {
				return (ArchetypeExamplesWizardFirstPage) page;
			}
		}
		return null;
	}

	@Override
	public String getProjectExampleType() {
		return ProjectExamplesActivator.MAVEN_ARCHETYPE;
	}

	@Override
	public void setProjectExample(ProjectExampleWorkingCopy projectExample) {
		this.projectExample = projectExample;
		if (projectExample != null) {
			if (projectExample.getShortDescription() != null) {
				setTitle(projectExample.getShortDescription());
			}
			if (projectExample.getHeadLine() != null) {
				setDescription(ProjectExamplesActivator.getShortDescription(projectExample.getHeadLine()));
			}
			ProjectImportConfiguration configuration = getImportConfiguration();
			if (configuration != null) {
				String profiles = projectExample.getDefaultProfiles();
				if (profiles != null && profiles.trim().length() > 0) {
					configuration.getResolverConfiguration().setSelectedProfiles(profiles);
				}
			}
			archetypeModel = (ArchetypeModel) context.getProperty(MavenProjectConstants.ARCHETYPE_MODEL);
			if (archetypeModel == null) {
				archetypeModel = projectExample.getArchetypeModel();
			}
			initializeArchetype();
		}
	}

	@Override
	public Map<String, Object> getPropertiesMap() {
		return propertiesMap;
	}

	@Override
	public void onWizardContextChange(String key, Object value) {
		if (MavenProjectConstants.PROJECT_NAME.equals(key)) {
			String artifactId = value == null ? "" : value.toString();
			setArtifactId(artifactId);
		} else if (MavenProjectConstants.PACKAGE.equals(key)) {
			String packageName = value == null ? "" : value.toString();
			setPackageName(packageName);
		} else if (MavenProjectConstants.ENTERPRISE_TARGET.equals(key)) {
			// Make sure it's a boolean :
			Boolean enterprise = Boolean.FALSE;
			if (value instanceof Boolean) {
				enterprise = (Boolean) value;
			}
			updateArchetypeProperty(MavenProjectConstants.ENTERPRISE_TARGET, enterprise.toString());
			checkEnterpriseProperty();
		} else if (MavenProjectConstants.ENTERPRISE_REPO_STATUS.equals(key)) {
			enterpriseRepoStatus = (IStatus) value;
			checkEnterpriseProperty();
		} else if (MavenProjectConstants.ARCHETYPE_MODEL.equals(key)) {
			if (value instanceof ArchetypeModel) {
				archetypeModel = (ArchetypeModel) value;
				if (getControl() != null) {
					// reset control contents with archetype data
					initializeArchetype();
				}
			}
		}
	}

	@Override
	public void setWizardContext(WizardContext context) {
		this.context = context;
		context.setProperty(MavenProjectConstants.IMPORT_PROJECT_CONFIGURATION, getImportConfiguration());
		context.setProperty(MavenProjectConstants.MAVEN_MODEL, getDynamicModel());
	}

	/** Returns a dynamic Model that can be shared across wizard pages. */
	private Model getDynamicModel() {
		Model model = new Model() {
			public String getModelVersion() {
				return "4.0.0"; //$NON-NLS-1$
			}

			public String getGroupId() {
				return groupIdCombo.getText();
			}

			public String getArtifactId() {
				return artifactIdCombo.getText();
			}

			public String getVersion() {
				return versionCombo.getText();
			}
		};
		return model;
	}

	@Override
	public String getPageType() {
		return "extra";
	}

	@Override
	public void dispose() {
		MavenCoreActivator.getDefault().unregisterMavenSettingsChangeListener(this);
		super.dispose();
	}

	@Override
	public void onSettingsChanged() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// Reset previous status
				enterpriseRepoStatus = null;
				checkEnterpriseProperty();
			}
		});
	}
	
	private Map<String, String> convert(Properties prop) {
		return prop.entrySet().stream().collect(
				Collectors.toMap(
					e -> String.valueOf(e.getKey()),
					e -> String.valueOf(e.getValue()),
					(prev, next) -> next,
					HashMap::new));
	}
}
