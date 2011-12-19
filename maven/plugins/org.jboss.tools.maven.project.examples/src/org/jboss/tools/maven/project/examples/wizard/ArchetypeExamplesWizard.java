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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.Messages;
import org.eclipse.m2e.core.ui.internal.actions.SelectionUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;

/**
 * 
 * @author snjeza
 *
 */
public class ArchetypeExamplesWizard extends Wizard implements INewWizard {

	private Project projectDescription;
	private ProjectImportConfiguration configuration;
	private ArchetypeExamplesWizardFirstPage simplePage;
	private ArchetypeExamplesWizardPage wizardPage;
	protected List<IWorkingSet> workingSets = new ArrayList<IWorkingSet>();
	private String projectName;
	private String artifactId;

	public ArchetypeExamplesWizard(File location, Project projectDescription) {
		super();
		setWindowTitle("New JBoss Project");
		setDefaultPageImageDescriptor(MavenProjectExamplesActivator.getNewWizardImageDescriptor());
		setNeedsProgressMonitor(true);
		this.projectDescription = projectDescription;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	   IWorkingSet workingSet = SelectionUtil.getSelectedWorkingSet(selection);
	    if(workingSet != null) {
	      this.workingSets.add(workingSet);
	    }
	}

	@Override
	public boolean performFinish() {
		final Model model = wizardPage.getModel();
		final String groupId = model.getGroupId();
		artifactId = model.getArtifactId();
		final String version = model.getVersion();
		final String javaPackage = wizardPage.getJavaPackage();
		final Properties properties = wizardPage.getProperties();
		final Archetype archetype = wizardPage.getArchetype();
		projectName = configuration.getProjectName(model);
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		final IPath location = simplePage.getLocationPath();

		
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    
	    boolean pomExists = location.append(projectName).append(IMavenConstants.POM_FILE_NAME).toFile().exists();
	    if ( pomExists ) {
	      MessageDialog.openError(getShell(), NLS.bind(Messages.wizardProjectJobFailed, projectName), Messages.wizardProjectErrorPomAlreadyExists);
	      return false;
	    }		
		
		final IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			
			public void run(final IProgressMonitor monitor)
					throws CoreException {
				
				MavenPlugin.getProjectConfigurationManager().createArchetypeProject(
						project, location, archetype,
						groupId, artifactId, version, javaPackage, properties,
						configuration, monitor);
			}
		};
		
		final IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
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

	
	private Throwable getRootCause(Throwable ex) {
		if (ex == null) return null;
		Throwable rootCause = getRootCause(ex.getCause());
		if (rootCause == null) {
			rootCause = ex;
		}
		return rootCause;
	}

	public void addPages() {
	    configuration = new ProjectImportConfiguration();
	    String profiles = projectDescription.getDefaultProfiles();
	    if (profiles != null && profiles.trim().length() > 0) {
	    	configuration.getResolverConfiguration().setActiveProfiles(profiles);
	    }
	    simplePage = new ArchetypeExamplesWizardFirstPage(configuration, projectDescription, workingSets);
	    addPage(simplePage);  
	    String location = ProjectExamplesActivator.getDefault().getPreferenceStore().getString(ProjectExamplesActivator.PROJECT_EXAMPLES_OUTPUT_DIRECTORY);
	    if (location != null && location.trim().length() > 0) {
	    	simplePage.setLocationPath(new Path(location));
	    }
	    wizardPage = new ArchetypeExamplesWizardPage(configuration, projectDescription);
	    wizardPage.setPageComplete(true);//We want to enable the finish button early
	    addPage(wizardPage);
	    
	    simplePage.setProjectNameModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				wizardPage.setArtifactId(simplePage.getProjectName());
				ArchetypeExamplesWizard.this.getContainer().updateButtons();
			}
		});

	    simplePage.setPackageNameModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent e) {
				String packageName = ((Combo)e.getSource()).getText();
				wizardPage.setPackageName(packageName);
				ArchetypeExamplesWizard.this.getContainer().updateButtons();
			}
		});

	    
	    simplePage.setPropertyModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				wizardPage.updateArchetypeProperty("enterprise", Boolean.toString(simplePage.isEnterpriseTargetRuntime()));
			}
		});
	    
	}
	
	public String getProjectName() {
		return projectName;
	}

	public String getArtifactId() {
		return artifactId;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
	    simplePage.setUseDefaultWorkspaceLocation(ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT));
	}
}
