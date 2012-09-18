/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

/**
 * @author snjeza
 * 
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;

public class NewProjectExamplesWizard2 extends Wizard implements INewWizard {

	private NewProjectExamplesMainPage mainPage;
	private NewProjectExamplesRequirementsPage requirementsPage;
	private NewProjectExamplesLocationPage locationPage;
	private IStructuredSelection fSelection;
	//private NewProjectExamplesReadyPage readyPage;
	List<IProjectExamplesWizardPage> contributedPages = new LinkedList<IProjectExamplesWizardPage>();
	private ProjectExample projectExample;

	private WizardContext wizardContext = new WizardContext();
	
	private boolean isCentral = false;
	//private QuickFixPage quickFixPage;

	public NewProjectExamplesWizard2() {
		super();
		setWindowTitle(Messages.NewProjectExamplesWizard_New_Project_Example);
		setNeedsProgressMonitor(true);
	}
	
	public NewProjectExamplesWizard2(ProjectExample projectExample) {
		super();
		initializeProjectExample(projectExample);
	}

	protected void initializeProjectExample(ProjectExample projectExample) {
		this.projectExample = projectExample;
		this.isCentral = true;
		setWindowTitle(Messages.NewProjectExamplesWizard_New_Project_Example);
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Creates an empty wizard for creating a new resource in the workspace.
	 */
	@Override
	public boolean performFinish() {
		final List<ProjectExample> selectedProjects = new ArrayList<ProjectExample>();
		IWorkingSet[] workingSets = new IWorkingSet[0];
		Map<String, Object> propertiesMap = new HashMap<String, Object>();
		if (mainPage != null) {
			if (mainPage.getSelection() == null || mainPage.getSelection().size() <= 0) {
				return false;
			}
			IStructuredSelection selection = mainPage.getSelection();
			Iterator iterator = selection.iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof ProjectExample) {
					ProjectExample project = (ProjectExample) object;
					selectedProjects.add(project);
				}
			}
		} else {
			if (projectExample == null) {
				return false;
			}
			selectedProjects.add(projectExample);
			
		}
		if (selectedProjects.size() > 0) {
			projectExample = selectedProjects.get(0);
		}
		if (projectExample != null) {
			if (!ProjectExamplesActivator.MAVEN_ARCHETYPE.equals(projectExample.getImportType())) {
				workingSets = locationPage.getWorkingSets();
			} else {
				// 
			}
			String type = projectExample.getImportType();
			for (IProjectExamplesWizardPage contributedPage:contributedPages) {
				if (type == null || !type.equals(contributedPage.getProjectExampleType())) {
					continue;
				}
				if (!contributedPage.finishPage()) {
					return false;
				}
				Map<String, Object> pMap = contributedPage.getPropertiesMap();
				if (pMap != null) {
					propertiesMap.putAll(pMap);
				}
			}
		}
		ProjectExamplesActivator.importProjectExamples(selectedProjects, workingSets, propertiesMap);
		return true;
	}

			
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		initializeDefaultPageImageDescriptor();
	}

	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = ProjectExamplesActivator
				.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID,
						"icons/new_wiz.gif"); //$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	@Override
	public void addPages() {
		createContributedPages();
		
		if (projectExample == null) {
			mainPage = new NewProjectExamplesMainPage();
			addPage(mainPage);
		} 
		for(IProjectExamplesWizardPage page: getContributedPages("requirement")) {
			if (projectExample == null || projectExample.getImportType().equals(page.getProjectExampleType())) {
				addPage(page);
			}
		}

		locationPage = new NewProjectExamplesLocationPage();
		addPage(locationPage);
		if (getSelection() != null) {
			locationPage.init(getSelection(), getActivePart());
		}
		
		// contributed page
		for(IProjectExamplesWizardPage page: getContributedPages("extra")) {
			if (projectExample == null || projectExample.getImportType().equals(page.getProjectExampleType())) {
				addPage(page);
			}
		}
	}

	
	
	
	protected void createContributedPages() {
		Map<String, List<ContributedPage>> extensionPages = ProjectExamplesActivator.getDefault().getContributedPages();
		Set<String> keySet = extensionPages.keySet();
		for (String key:keySet) {
			List<ContributedPage> contributions = extensionPages.get(key);
			for(ContributedPage page:contributions) {
				try {
					IProjectExamplesWizardPage contributedPage = (IProjectExamplesWizardPage) page.getConfigurationElement().createExecutableExtension(ProjectExamplesActivator.CLASS);
					contributedPages.add(contributedPage);
				} catch (CoreException e) {
					ProjectExamplesActivator.log(e);
				}
			}
		}
	}

	private IWorkbenchPart getActivePart() {
		IWorkbenchWindow activeWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow != null) {
			IWorkbenchPage activePage= activeWindow.getActivePage();
			if (activePage != null) {
				return activePage.getActivePart();
			}
		}
		return null;
	}
	
	private IStructuredSelection getSelection() {
		if (fSelection == null) {
			if (getActivePart() == null || getActivePart().getSite() == null 
					|| getActivePart().getSite().getSelectionProvider() == null) {
				return new StructuredSelection();
			}
			ISelection sel = getActivePart().getSite().getSelectionProvider()
					.getSelection();
			if (sel instanceof IStructuredSelection) {
				fSelection = (IStructuredSelection) sel;
			}
		}
		return fSelection;
	}
	
	public ProjectExample getSelectedProjectExample() {
		if (projectExample != null) {
			return projectExample;
		}
		return (mainPage == null)?null:mainPage.getSelectedProject();
	}

//	public IWizardPage getReadyPage() {
//		return readyPage;
//	}

	public List<IProjectExamplesWizardPage> getContributedPages() {
		return contributedPages;
	}

	public List<IProjectExamplesWizardPage> getContributedPages(String pageType) {
		if (contributedPages == null || contributedPages.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<IProjectExamplesWizardPage> filteredPages = new ArrayList<IProjectExamplesWizardPage>();
		
		for (IProjectExamplesWizardPage p : contributedPages) {
			if (pageType.equals(p.getPageType())) {
				filteredPages.add(p);
			}
		}
		
		return filteredPages;
	}
	
	public IWizardPage getLocationsPage() {
		return locationPage;
	}

	public IWizardPage getRequirementsPage() {
		return requirementsPage;
	}

	
	@Override
	public boolean canFinish() {
		ProjectExample example = getSelectedProjectExample();
		if (example == null) {
			return false;
		}
		IWizardPage[] pages = getPages();
        for (IWizardPage page:pages) {
            if (page instanceof IProjectExamplesWizardPage) {
            	String type = ((IProjectExamplesWizardPage) page).getProjectExampleType();
            	if (type != null && type.equals(example.getImportType())) {
            		if (!page.isPageComplete()) {
            			return false;
            		}
            	}
            } else if (!page.isPageComplete()) {
				return false;
			}
        }
        return true;
	}

	public ProjectExample getProjectExample() {
		return projectExample;
	}

	@Override
	public void addPage(IWizardPage page) {
		if (page instanceof IProjectExamplesWizardPage) {
			IProjectExamplesWizardPage ewp = (IProjectExamplesWizardPage)page;
			ewp.setWizardContext(wizardContext);
			wizardContext.addListener(ewp);
			if (projectExample != null && ewp.getProjectExampleType().equals(projectExample.getImportType())) {
				ewp.setProjectExample(projectExample);
			}

		}
		super.addPage(page);
	}
}
