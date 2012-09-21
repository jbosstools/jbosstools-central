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

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
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
import org.eclipse.ui.IWorkingSet;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.maven.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ArchetypeModel;
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

	private static final String WORKING_SETS = "workingSets"; //$NON-NLS-1$
	private Label projectNameLabel;
	private Combo projectNameCombo;
	private Label packageLabel;
	private Combo packageCombo;
	private boolean initialized;
	private ProjectExample projectExample;
	private WizardContext context;
	
	private IRuntimeLifecycleListener listener;
	private Button isWorkspace;
	private Combo outputDirectoryCombo;
	private ArchetypeModel archetypeModel;
	
	public ArchetypeExamplesWizardFirstPage() {
		super(new ProjectImportConfiguration(), "", "",new ArrayList<IWorkingSet>());
	}
	
	public ArchetypeExamplesWizardFirstPage(
			ProjectImportConfiguration configuration,
			ProjectExample projectDescription, List<IWorkingSet> workingSet) {
		super(configuration, projectDescription.getShortDescription(),Messages.ArchetypeExamplesWizardFirstPage_Title, workingSet);
		this.projectExample = projectDescription;
		setPageComplete(false);
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		setUseDefaultWorkspaceLocation(ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT));
		setLocationCombo(ProjectExamplesActivator.getDefault().getPreferenceStore().getString(ProjectExamplesActivator.PROJECT_EXAMPLES_OUTPUT_DIRECTORY));
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

		//createServerTargetComposite(container);
		
		Label emptyLabel = new Label(container, SWT.NONE);
		emptyLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				3, 1));

		
	}
	
	protected void validate() {
		if (!initialized) {
			return;
		}
		//Need to be called first, or error message would be overwritten
		super.validate();
		
		if (!isPageComplete()) {
			return;
		}
		if (outputDirectoryCombo != null && isWorkspace != null) {
			if (!isWorkspace.getSelection()) {
				String location = outputDirectoryCombo.getText();
				if (!validateLocation(location)) {
					return;
				}
			}
		}
		String errorMessage = validateInputs();
		setErrorMessage(errorMessage);
		setMessage(null);
		setPageComplete(errorMessage == null);

		//validateEnterpriseRepo();
		
	}

	private String validateInputs() {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

	    final String name = getProjectName();
		
	    // check whether the project name field is empty
	    if(name.trim().length() == 0) {
	      return Messages.ArchetypeExamplesWizardFirstPage_ProjectName_Cant_Be_Empty;
	    }
	    
	    final String resolvedName = getImportConfiguration().getProjectName(getModel());
	    
	    // check whether the project name is valid
	    final IStatus nameStatus = workspace.validateName(resolvedName, IResource.PROJECT);
	    if(!nameStatus.isOK()) {
	      return nameStatus.getMessage();
	    }
		
	    // check whether project already exists
	    final IProject handle = workspace.getRoot().getProject(resolvedName);
	    if(handle.exists()) {
	      return NLS.bind(Messages.ArchetypeExamplesWizardFirstPage_Existing_Project, resolvedName);
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
		if (archetypeModel != null) {
			String projectName = archetypeModel.getArtifactId();
			if (StringUtils.isNotBlank(projectName)) {
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (!p.exists()) {
					projectNameCombo.setText(projectName);
				}
			}
			
			String packageName = archetypeModel.getJavaPackage();
			if (StringUtils.isBlank(packageName) && packageCombo.getItemCount() > 0) {
				packageName = packageCombo.getItem(0);
			}
			if (packageName != null) {
				packageCombo.setText(packageName);
			}
		}

		initialized = true;
		validate();
	}

	public void setUseDefaultWorkspaceLocation(boolean value) {
		try {
			Field field = this.getClass().getSuperclass().getDeclaredField("useDefaultWorkspaceLocationButton"); //$NON-NLS-1$
			field.setAccessible(true);
			Object useDefaultWorkspaceLocation = field.get(this);
			if (useDefaultWorkspaceLocation instanceof Button) {
				isWorkspace = (Button) useDefaultWorkspaceLocation;
				isWorkspace.setSelection(value);
				isWorkspace.notifyListeners(SWT.Selection, new Event());
				isWorkspace.addSelectionListener(new SelectionAdapter() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						validate();
						if (isPageComplete()) {
							ProjectExamplesActivator.getDefault().getPreferenceStore().setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT, isWorkspace.getSelection());
						}
					}
				
				});
			}
		} catch (Exception e) {
			MavenProjectExamplesActivator.log(e);
		} 
	}
	
	public void setLocationCombo(String defaultLocation) {
		try {
			Field field = this.getClass().getSuperclass().getDeclaredField("locationCombo"); //$NON-NLS-1$
			field.setAccessible(true);
			Object locationComboField = field.get(this);
			if (locationComboField instanceof Combo) {
				outputDirectoryCombo = (Combo) locationComboField;
				outputDirectoryCombo.setText(defaultLocation);
				outputDirectoryCombo.notifyListeners(SWT.Selection, new Event());
				outputDirectoryCombo.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						validate();
						if (isPageComplete()) {
							ProjectExamplesActivator.getDefault().getPreferenceStore().setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_OUTPUT_DIRECTORY, outputDirectoryCombo.getText());
						}
					}
				
				});
			}
		} catch (Exception e) {
			MavenProjectExamplesActivator.log(e);
		} 
	}
	
	private boolean validateLocation(String location) {
		IPath projectPath = Path.fromOSString(location);
		if (!projectPath.toFile().exists()) {
			if (!canCreate(projectPath.toFile())) {
				setErrorMessage("Cannot create project content at the given external location.");
				setPageComplete(false);
				return false;
			}
		}
		return true;
	}
	
	private boolean canCreate(File file) {
		while (!file.exists()) {
			file= file.getParentFile();
			if (file == null)
				return false;
		}
		return file.canWrite();
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
			archetypeModel = projectExample.getArchetypeModel();
			initDefaultValues();
		} 
	}
	
	@Override
	public IWizardPage getNextPage() {
		IWizard wizard = getWizard();
		if (wizard instanceof NewProjectExamplesWizard2) {
			ProjectExample projectExample = ((NewProjectExamplesWizard2)wizard).getSelectedProjectExample();
			if (projectExample != null && projectExample.getImportType() != null) {
				List<IProjectExamplesWizardPage> pages = ((NewProjectExamplesWizard2)wizard).getContributedPages("extra");
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
			ProjectExample projectExample = ((NewProjectExamplesWizard2)wizard).getSelectedProjectExample();
			if (projectExample != null && projectExample.getImportType() != null) {
				List<IProjectExamplesWizardPage> pages = ((NewProjectExamplesWizard2)wizard).getContributedPages("requirement");
				for (IProjectExamplesWizardPage page:pages) {
					if (projectExample.getImportType().equals(page.getProjectExampleType())) {
						return page;
					}
				}
			} 
		}
		return super.getPreviousPage();
	}

	@Override
	public Map<String, Object> getPropertiesMap() {
		try {
			Field field = this.getClass().getSuperclass().getDeclaredField(WORKING_SETS);
			field.setAccessible(true);
			Object object = field.get(this);
			if (object instanceof List<?>) {
				Map<String, Object> propertiesMap = new HashMap<String, Object>();
				propertiesMap.put(WORKING_SETS, object); 
				return propertiesMap;
			}
		} catch (Exception e) {
			MavenProjectExamplesActivator.log(e);
		} 
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

	@Override
	public ProjectImportConfiguration getImportConfiguration() {
		ProjectImportConfiguration importConfiguration = (ProjectImportConfiguration) context.getProperty(MavenProjectConstants.IMPORT_PROJECT_CONFIGURATION);
		return importConfiguration;
	}
	
	private Model getModel() {
		return (Model) context.getProperty(MavenProjectConstants.MAVEN_MODEL);
	}

	@Override
	public String getPageType() {
		return "extra";
	}
	
}
