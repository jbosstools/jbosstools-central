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
package org.jboss.tools.maven.project.examples.wizard;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardLocationPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.internal.facets.FacetUtil;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.maven.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.wizard.IProjectExamplesWizardPage;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard2;
import org.jboss.tools.project.examples.wizard.WizardContext;

/**
 * Simplified UI for the Maven Archetype Wizard, based off the original m2e MavenProjectWizardLocationPage. 
 * 
 * @author Fred Bricon
 * 
 */
public class ArchetypeExamplesWizardFirstPage extends MavenProjectWizardLocationPage implements IProjectExamplesWizardPage {

	private static final String TARGET_RUNTIME = "targetRuntime";
	private Label projectNameLabel;
	private Combo projectNameCombo;
	private Label packageLabel;
	private Combo packageCombo;
	private Combo serverTargetCombo;
	private Map<String, IRuntime> serverRuntimes;
	private Composite warningLink;
	private boolean initialized;
	private Boolean isEnterpriseRepoAvailable;
	private ProjectExample projectDescription;
	private ProjectExample projectExample;
	private WizardContext context;
	
	public ArchetypeExamplesWizardFirstPage() {
		super(new ProjectImportConfiguration(), "", "",new ArrayList<IWorkingSet>());
	}
	
	public ArchetypeExamplesWizardFirstPage(
			ProjectImportConfiguration configuration,
			ProjectExample projectDescription, List<IWorkingSet> workingSet) {
		super(configuration, projectDescription.getShortDescription(),Messages.ArchetypeExamplesWizardFirstPage_Title, workingSet);
		this.projectDescription = projectDescription;
		setPageComplete(false);
	}
	
	@Override
	protected void createAdditionalControls(Composite container) {

		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		projectNameLabel = new Label(container, SWT.NONE);
		projectNameLabel.setText(Messages.ArchetypeExamplesWizardFirstPage_ProjectName_Label);
		projectNameCombo = new Combo(container, SWT.BORDER);
		projectNameCombo.setLayoutData(gridData);
		addFieldWithHistory("projectNameCombo", projectNameCombo);//$NON-NLS-1$ 
		projectNameCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isCurrentPage()) {
					context.setProperty(MavenProjectConstants.PROJECT_NAME, getProjectName());
				}
				validate();
			}
		});

		packageLabel = new Label(container, SWT.NONE);
		packageLabel.setText(Messages.ArchetypeExamplesWizardFirstPage_Package_Label);
		packageCombo = new Combo(container, SWT.BORDER);
		packageCombo.setLayoutData(gridData);
		packageCombo.setData("name", "packageCombo"); //$NON-NLS-1$ //$NON-NLS-2$
		addFieldWithHistory("packageCombo", packageCombo);//$NON-NLS-1$ 
		packageCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isCurrentPage()) {
					context.setProperty(MavenProjectConstants.PACKAGE, packageCombo.getText());
				}
				validate();
			}
		});

		//TODO read facet version from project example metadata
		IProjectFacetVersion facetVersion;
		try {
			facetVersion = ProjectFacetsManager.getProjectFacet(
					IJ2EEFacetConstants.DYNAMIC_WEB).getLatestVersion();
			createServerTargetComposite(container, facetVersion);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		Label emptyLabel = new Label(container, SWT.NONE);
		emptyLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				3, 1));

		
		/*
		projectNameCombo.setText(projectDescription.getArchetypeModel().getArtifactId());
		packageCombo.setText(projectDescription.getArchetypeModel().getJavaPackage());
		*/
	}
	
	@Override
	protected void createAdvancedSettings(Composite composite, GridData gridData) {
		createMissingRepositoriesWarning(composite, gridData);
	}

	protected void createServerTargetComposite(Composite parent,
			IProjectFacetVersion facetVersion) {
		Label serverTargetLabel = new Label(parent, SWT.NONE);
		serverTargetLabel.setText(Messages.ArchetypeExamplesWizardFirstPage_Target_Runtime_Label);

		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		serverTargetCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		serverTargetCombo.setLayoutData(gridData);
		serverRuntimes = getServerRuntimes(facetVersion);
		serverTargetCombo.add(Messages.ArchetypeExamplesWizardFirstPage_No_TargetRuntime);
		int i =0, selectedRuntimeIdx = 0;
		String lastUsedRuntime = dialogSettings.get(TARGET_RUNTIME);

		for (Map.Entry<String, IRuntime> entry : serverRuntimes.entrySet()) {
			serverTargetCombo.add(entry.getKey());
			++i;
			IRuntime runtime = entry.getValue();
			if (lastUsedRuntime != null && lastUsedRuntime.equals(runtime.getId())) {
				selectedRuntimeIdx = i;
			}
		}
				
		serverTargetCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (isCurrentPage()) {
					context.setProperty(MavenProjectConstants.ENTERPRISE_TARGET, isEnterpriseTargetRuntime());
				}
				validateEnterpriseRepo();
			}
		});
		if (selectedRuntimeIdx > 0) {
			serverTargetCombo.select(selectedRuntimeIdx);
		}

	}

	protected void validate() {
		if (!initialized) {
			return;
		}
		//Need to be called first, or error message would be overwritten
		super.validate();

		String errorMessage = validateInputs();
		setErrorMessage(errorMessage);
		setMessage(null);
		setPageComplete(errorMessage == null);

		validateEnterpriseRepo();
	}

	private String validateInputs() {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

	    final String name = getProjectName();
		
	    // check whether the project name field is empty
	    if(name.trim().length() == 0) {
	      return Messages.ArchetypeExamplesWizardFirstPage_ProjectName_Cant_Be_Empty;
	    }
	    
	    // check whether the project name is valid
	    final IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
	    if(!nameStatus.isOK()) {
	      return nameStatus.getMessage();
	    }
		
	    // check whether project already exists
	    final IProject handle = workspace.getRoot().getProject(name);
	    if(handle.exists()) {
	      return Messages.ArchetypeExamplesWizardFirstPage_Existing_Project;
	    }
	    
	    //check if the package is valid
	    String packageName = packageCombo.getText();
	    if(packageName.trim().length() != 0) {
	      if(!Pattern.matches("[A-Za-z_$][A-Za-z_$\\d]*(?:\\.[A-Za-z_$][A-Za-z_$\\d]*)*", packageName)) { //$NON-NLS-1$
	        return Messages.ArchetypeExamplesWizardFirstPage_Error_Package;
	      }
	    }
	    
	    return null;
	}

	public String getProjectName() {
		return (projectNameCombo == null) ? null : projectNameCombo.getText();
	}

	protected void validateEnterpriseRepo() {
		
		boolean isWarningLinkVisible = (isEnterpriseTargetRuntime() && !assertEnterpriseRepoAccessible());
		if (warningLink != null) {
			warningLink.setVisible(isWarningLinkVisible);
		}
	}

	
	public boolean isEnterpriseTargetRuntime() {
		if (serverTargetCombo == null)
			return false;
		String serverId = serverTargetCombo.getText();
		IRuntime runtime = serverRuntimes.get(serverId);
		return (runtime != null && RuntimeUtils.isEAP(runtime));
	}

	
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible && !initialized) {
			//Set defaults values from history here as the history is loaded in super.visible()
			initDefaultValues();
		}
	}


	private void initDefaultValues() {
		//JBIDE-10411 : provide sensible defaults for project name and package
		if (projectExample == null || projectNameCombo == null) {
			return;
		}
		projectDescription = projectExample;
		String projectName = projectDescription.getArchetypeModel().getArtifactId();
		if (StringUtils.isNotBlank(projectName)) {
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (!p.exists()) {
				projectNameCombo.setText(projectName);
			}
		}
		
		String packageName = null;
		if (packageCombo.getItemCount() > 0) {
			packageName = packageCombo.getItem(0);
		} else {
			packageName = projectDescription.getArchetypeModel().getJavaPackage();
		}
		if (packageName != null) {
			packageCombo.setText(packageName);
		}
		
		//Force setting of enterprise value
		context.setProperty(MavenProjectConstants.ENTERPRISE_TARGET, isEnterpriseTargetRuntime());

		initialized = true;
		validate();
	}

	private void createMissingRepositoriesWarning(Composite parent,
			GridData gridData) {
		//TODO make that damn component align correctly
		//warningLink = new MissingRepositoryWarningComponent(parent);
		
		//TODO delete that code
		warningLink = new Composite(parent, SWT.NONE);
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).span(3, 1)
				.applyTo(warningLink);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(warningLink);

		Label warningImg = new Label(warningLink, SWT.CENTER | SWT.TOP);
		warningImg.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));

		Link link = new Link(warningLink, SWT.NONE);
		link.setText(NLS.bind(Messages.ArchetypeExamplesWizardFirstPage_Unresolved_Enterprise_Repo, MavenArtifactHelper.ENTERPRISE_JBOSS_SPEC));
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Open default external browser
					PlatformUI.getWorkbench().getBrowserSupport()
							.getExternalBrowser().openURL(new URL(e.text));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		warningLink.setVisible(false);
	}



	private boolean assertEnterpriseRepoAccessible() {
		if (isEnterpriseRepoAvailable == null) {
			isEnterpriseRepoAvailable = MavenArtifactHelper.isEnterpriseRepositoryAvailable();
		}
		return isEnterpriseRepoAvailable.booleanValue();
	}

	protected Map<String, IRuntime> getServerRuntimes(
			IProjectFacetVersion facetVersion) {
		Set<org.eclipse.wst.common.project.facet.core.runtime.IRuntime> runtimesSet;
		if (facetVersion == null) {
			runtimesSet =RuntimeManager.getRuntimes();
		} else {
			runtimesSet = RuntimeManager.getRuntimes(Collections.singleton(facetVersion));
		}
		
		Map<String, IRuntime> runtimesMap = new LinkedHashMap<String, IRuntime>();
		for (org.eclipse.wst.common.project.facet.core.runtime.IRuntime r : runtimesSet) {
			IRuntime serverRuntime = FacetUtil.getRuntime(r);
			if (serverRuntime != null) {
				runtimesMap.put(r.getLocalizedName(), serverRuntime);
			}
		}
		return runtimesMap;
	}


	public void setUseDefaultWorkspaceLocation(boolean value) {
		Class clazz = MavenProjectWizardLocationPage.class;
		try {
			Field field = clazz.getDeclaredField("useDefaultWorkspaceLocationButton"); //$NON-NLS-1$
			field.setAccessible(true);
			Object useDefaultWorkspaceLocation = field.get(this);
			if (useDefaultWorkspaceLocation instanceof Button) {
				Button useDefaultWorkspaceLocationButton = (Button) useDefaultWorkspaceLocation;
				useDefaultWorkspaceLocationButton.setSelection(value);
				useDefaultWorkspaceLocationButton.notifyListeners(SWT.Selection, new Event());
			}
		} catch (Exception e) {
			MavenProjectExamplesActivator.log(e);
		} 
	}
	
	@Override
	public void dispose() {
		if (dialogSettings != null && serverRuntimes != null && serverTargetCombo != null) {
			IRuntime lastUsedRuntime = serverRuntimes.get(serverTargetCombo.getText());
			if (lastUsedRuntime != null) {
				dialogSettings.put(TARGET_RUNTIME, lastUsedRuntime.getId());
			}
		}
		super.dispose();
	}


	@Override
	public boolean finishPage() {
		return true;
	}


	@Override
	public String getProjectExampleType() {
		return ProjectExamplesActivator.MAVEN_ARCHETYPE;
	}

	@Override
	public void setProjectExample(ProjectExample projectExample) {
		this.projectExample = projectExample;
		if (projectExample != null) {
			if (projectExample.getShortDescription() != null) {
				setTitle(projectExample.getShortDescription());
			}
			if (projectExample.getDescription() != null) {
				setDescription(ProjectExamplesActivator.getShortDescription(projectExample.getDescription()));
			}
			ProjectImportConfiguration configuration = getImportConfiguration();
			if (configuration != null) {
				String profiles = projectExample.getDefaultProfiles();
			    if (profiles != null && profiles.trim().length() > 0) {
			    	configuration.getResolverConfiguration().setActiveProfiles(profiles);
			    }
			}
			initDefaultValues();
		} 
	}
	
	@Override
	public IWizardPage getNextPage() {
		IWizard wizard = getWizard();
		if (wizard instanceof NewProjectExamplesWizard2) {
			ProjectExample projectExample = ((NewProjectExamplesWizard2)wizard).getSelectedProjectExample();
			if (projectExample != null && projectExample.getImportType() != null) {
				List<IProjectExamplesWizardPage> pages = ((NewProjectExamplesWizard2)wizard).getContributedPages();
				for (IProjectExamplesWizardPage page:pages) {
					if (page == this) {
						continue;
					}
					if (projectExample.getImportType().equals(page.getProjectExampleType())) {
						return page;
					}
				}
			} 
		}
		return super.getNextPage();
	}

	@Override
	public IWizardPage getPreviousPage() {
		IWizard wizard = getWizard();
		if (wizard instanceof NewProjectExamplesWizard2) {
			return ((NewProjectExamplesWizard2) wizard).getRequirementsPage();
		}
		return super.getPreviousPage();
	}

	@Override
	public Map<String, Object> getPropertiesMap() {
		return null;
	}

	@Override
	public void onWizardContextChange(String key, Object value) {
		if (MavenProjectConstants.PROJECT_NAME.equals(key)) {
			String artifactId = value == null?"":value.toString();
			setProjectName(artifactId);
		} else if (MavenProjectConstants.PACKAGE.equals(key)){
			String packageName = value == null?"":value.toString();
			setPackageName(packageName);
		}
	}

	
	public void setProjectName(String projectName) {
		if (projectNameCombo != null && !projectNameCombo.getText().equals(projectName)) {
			projectNameCombo.setText(projectName);
		}
	}

	public void setPackageName(String packageName) {
		if (packageCombo != null) {
			if (!packageCombo.getText().equals(packageName)){ 
				packageCombo.setText(packageName);
			}
		}
	}	
	
	
	@Override
	public void setWizardContext(WizardContext context) {
		this.context = context;
	}

	
}
