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
package org.jboss.tools.project.examples.wizard;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.runtimes.RuntimeUtils;

/**
 * @author snjeza
 * 
 */
public class NewProjectExamplesMainPage extends WizardPage {

	//private static final int DEFAULT_HEIGHT = 430;
	private static final int DEFAULT_WIDTH = 600;
	private IStructuredSelection selection;
	private Combo siteCombo;
	private List<ProjectExampleCategory> categories;
	private Text descriptionText;
	private ProjectExample selectedProject;
	private Combo targetRuntimeTypesCombo;
	
	public NewProjectExamplesMainPage() {
		super("org.jboss.tools.project.examples.main"); //$NON-NLS-1$
        setTitle( Messages.NewProjectExamplesWizardPage_Project_Example );
        setDescription( Messages.NewProjectExamplesWizardPage_Import_Project_Example );
    }

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(gd);
		
		Composite siteComposite = new Composite(composite,SWT.NONE);
		GridLayout gridLayout = new GridLayout(2,false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		siteComposite.setLayout(gridLayout);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		siteComposite.setLayoutData(gd);
		
		final Button button = new Button(siteComposite,SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		gd.horizontalSpan = 2;
		button.setLayoutData(gd);
		button.setText(Messages.ProjectExamplesPreferencePage_Show_experimental_sites);
		final IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		button.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES));
		
		final Button serverButton = new Button(siteComposite,SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		gd.horizontalSpan = 2;
		serverButton.setLayoutData(gd);
		serverButton.setText(Messages.ProjectExamplesPreferencePage_Show_runtime_sites);
		serverButton.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_RUNTIME_SITES));
		
		new Label(siteComposite,SWT.NONE).setText(Messages.NewProjectExamplesWizardPage_Site);
		siteCombo = new Combo(siteComposite,SWT.READ_ONLY);
		siteCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		new Label(siteComposite,SWT.NONE).setText(Messages.NewProjectExamplesMainPage_TargetedRuntime);
		targetRuntimeTypesCombo = new Combo(siteComposite, SWT.READ_ONLY);
		targetRuntimeTypesCombo .setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		new Label(composite,SWT.NONE).setText(Messages.NewProjectExamplesWizardPage_Projects);
		
		final ProjectExamplesPatternFilter filter = new ProjectExamplesPatternFilter();
		
		int styleBits = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP;
		final FilteredTree filteredTree = new FilteredTree(composite, styleBits, filter, true);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		final TreeViewer viewer = filteredTree.getViewer();
		Tree tree = viewer.getTree();
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GC gc = new GC(parent);
		gd.heightHint = Dialog.convertHeightInCharsToPixels(gc
				.getFontMetrics(), 9);
		gc.dispose(); 
		tree.setLayoutData(gd);
		tree.setFont(parent.getFont());
		
		viewer.setLabelProvider(new ProjectLabelProvider());
		viewer.setContentProvider(new ProjectContentProvider());
		
		
		final SiteFilter siteFilter = new SiteFilter();
		final RuntimeTypeFilter serverFilter = new RuntimeTypeFilter();
		
		viewer.addFilter(siteFilter);
		viewer.addFilter(serverFilter);
		
		Label descriptionLabel = new Label(composite,SWT.NONE);
		descriptionLabel.setText(Messages.NewProjectExamplesWizardPage_Description);
		descriptionText = new Text(composite,SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gc = new GC(parent);
		gd.heightHint = Dialog.convertHeightInCharsToPixels(gc
				.getFontMetrics(), 6);
		gc.dispose();
		descriptionText.setLayoutData(gd);
		
		Composite internal = new Composite(composite, SWT.NULL);
		internal.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL, GridData.FILL, true, false);
		gd.widthHint = DEFAULT_WIDTH;
		internal.setLayoutData(gd);
		
		Label projectNameLabel = new Label(internal,SWT.NULL);
		projectNameLabel.setText(Messages.NewProjectExamplesWizardPage_Project_name);
		final Text projectName = new Text(internal,SWT.BORDER | SWT.READ_ONLY);
		projectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label projectSizeLabel = new Label(internal,SWT.NULL);
		projectSizeLabel.setText(Messages.NewProjectExamplesWizardPage_Project_size);
		final Text projectSize = new Text(internal,SWT.BORDER | SWT.READ_ONLY);
		projectSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label projectURLLabel = new Label(internal,SWT.NULL);
		projectURLLabel.setText(Messages.NewProjectExamplesWizardPage_URL);
		final Text projectURL = new Text(internal,SWT.BORDER | SWT.READ_ONLY);
		projectURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				selection = (IStructuredSelection) event.getSelection();
				Object selected = selection.getFirstElement();
				String projectType = null;
				if (selected instanceof ProjectExample && selection.size() == 1) {
					selectedProject = (ProjectExample) selected;
					descriptionText.setText(selectedProject.getDescription());
					projectName.setText(selectedProject.getName());
					projectURL.setText(selectedProject.getUrl());
					projectSize.setText(selectedProject.getSizeAsText());
					//readyPage.setProjectExample(selectedProject);
					projectType = selectedProject.getImportType();
				} else {
					selectedProject = null;
					String description = ""; //$NON-NLS-1$
					if (selected instanceof ProjectExampleCategory) {
						ProjectExampleCategory category = (ProjectExampleCategory) selected;
						if (category.getDescription() != null) {
							description = category.getDescription();
						}
					}
					descriptionText.setText(description);
					projectName.setText(""); //$NON-NLS-1$
					projectURL.setText(""); //$NON-NLS-1$
					projectSize.setText(""); //$NON-NLS-1$
				}
				
				for (IWizardPage page : getWizard().getPages()) {
					if (page instanceof IProjectExamplesWizardPage) {
						IProjectExamplesWizardPage pewp = (IProjectExamplesWizardPage) page;
						if (projectType != null && projectType.equals(pewp.getProjectExampleType())) {
							pewp.setProjectExample(selectedProject);
						} else {
							pewp.setProjectExample(null);
						}
					}
				}
				
				boolean canFinish = refresh(false);
				setPageComplete(canFinish);
			}
			
		});
		
		siteCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				siteFilter.setSite(siteCombo.getText());
				viewer.refresh();
			}
			
		});
		
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
				store.setValue(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES, button.getSelection());
				
				//Store current combo selections
				String selectedRuntime = targetRuntimeTypesCombo.getText();
				String selectedSite = siteCombo.getText();

				//Rebuild the combo lists
				refresh(viewer, true);

				//Restore the combo selections with initial values if possible
				restoreCombo(targetRuntimeTypesCombo, selectedRuntime);
				restoreCombo(siteCombo, selectedSite);
				
				siteFilter.setSite(siteCombo.getText());
				viewer.refresh();
			}
			
		});

		serverButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
				store.setValue(ProjectExamplesActivator.SHOW_RUNTIME_SITES, serverButton.getSelection());
				
				//Store current combo selections
				String selectedRuntime = targetRuntimeTypesCombo.getText();
				String selectedSite = siteCombo.getText();

				//Rebuild the combo lists
				refresh(viewer, true);

				//Restore the combo selections with initial values if possible
				restoreCombo(targetRuntimeTypesCombo, selectedRuntime);
				restoreCombo(siteCombo, selectedSite);
				
				siteFilter.setSite(siteCombo.getText());
				viewer.refresh();
			}
			
		});

		targetRuntimeTypesCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				serverFilter.setRuntimeType(targetRuntimeTypesCombo.getText());
				viewer.refresh();
			}
		});

		
		setPageComplete(false);
		
		setControl(composite);

		refresh(viewer, true);
		siteCombo.setText(ProjectExamplesActivator.ALL_SITES);
		
		targetRuntimeTypesCombo.setText(ProjectExamplesActivator.ALL_RUNTIMES);
		
		

	}

	private void loadRuntimeTypes() {
		if (targetRuntimeTypesCombo == null) {
			return;
		}
		targetRuntimeTypesCombo.removeAll();
		targetRuntimeTypesCombo.add(ProjectExamplesActivator.ALL_RUNTIMES);
		
		Set<IRuntimeType> installedRuntimeTypes = RuntimeUtils.getInstalledRuntimeTypes();
		List<IRuntimeType> sortedTypes = new ArrayList<IRuntimeType>();
		
		for (ProjectExampleCategory category : categories) {
			for (ProjectExample project : category.getProjects()) {
				for (IRuntimeType type : WTPRuntimeFix.getTargetedServerRuntimes(project)) {
					if (!sortedTypes.contains(type)) {
						//If runtime types have a server instance, display them first
						if (installedRuntimeTypes.contains(type)) {
							sortedTypes.add(0, type);
						} else {
							sortedTypes.add(type);
						}
					}
				}
			}
		}
		
		for (IRuntimeType type : sortedTypes) {
			targetRuntimeTypesCombo.add(type.getName());
		}
	}
	
	private void refresh(final TreeViewer viewer, boolean show) {
		AdaptableList input = new AdaptableList(getCategories(show));
		viewer.setInput(input);
		viewer.refresh();
		String[] items = getItems();
		siteCombo.setItems(items);		
		loadRuntimeTypes();
	}

	private List<ProjectExampleCategory> getCategories(boolean show) {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			
			public void run(IProgressMonitor monitor) {
				categories = ProjectExampleUtil.getProjects(monitor);
			}
		};
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, op);
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		}
		HashSet<IProjectExampleSite> invalidSites = ProjectExampleUtil.getInvalidSites();
		boolean showInvalidSites = ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.SHOW_INVALID_SITES);
		if (invalidSites.size() > 0 && showInvalidSites && show) {
			String message = Messages.NewProjectExamplesWizardPage_Cannot_access_the_following_sites;
			for (IProjectExampleSite site:invalidSites) {
				message = message + site.getName() + "\n"; //$NON-NLS-1$
				ProjectExamplesActivator.log(NLS.bind(Messages.InvalideSite, new Object[] {site.getName(), site.getUrl()} ));
			}
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(getShell(), Messages.NewProjectExamplesWizardPage_Invalid_Sites, message, Messages.NewProjectExamplesWizardPage_Show_this_dialog_next_time, true, ProjectExamplesActivator.getDefault().getPreferenceStore(), ProjectExamplesActivator.SHOW_INVALID_SITES);
			boolean toggleState = dialog.getToggleState();
			ProjectExamplesActivator.getDefault().getPreferenceStore().setValue(ProjectExamplesActivator.SHOW_INVALID_SITES, toggleState);
		}
		return categories;
	}
	
	private String[] getItems() {
		//List<Category> categories = getCategories(true);
		Set<String> sites = new TreeSet<String>();
		sites.add(ProjectExamplesActivator.ALL_SITES);
		for (ProjectExampleCategory category:categories) {
			List<ProjectExample> projects = category.getProjects();
			for (ProjectExample project:projects) {
				String name = project.getSite() == null ? ProjectExamplesActivator.ALL_SITES : project.getSite().getName();
				sites.add(name);
			}
		}
		String[] items = sites.toArray(new String[0]);
		return items;
	}
	
	private class ProjectLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ProjectExampleCategory) {
				ProjectExampleCategory category = (ProjectExampleCategory) element;
				return category.getName();
			}
			if (element instanceof ProjectExample) {
				ProjectExample project = (ProjectExample) element;
				return project.getShortDescription();
			}
			return super.getText(element);
		}
	}
	
	private class ProjectContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof AdaptableList) {
				Object[] childCollections = ((AdaptableList)parentElement).getChildren();
				//List children = (List) parentElement;
				//return children.toArray();
				return childCollections;
			}
			if (parentElement instanceof ProjectExampleCategory) {
				ProjectExampleCategory category = (ProjectExampleCategory) parentElement;
				return category.getProjects().toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof ProjectExample) {
				return ((ProjectExample)element).getCategory();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return element instanceof ProjectExampleCategory;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}

	public IStructuredSelection getSelection() {
		return selection;
	}

	public boolean refresh(boolean force) {
		boolean canFinish = false;
		
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof ProjectExample) {
				// FIXME
				canFinish=true;
				ProjectExample project = (ProjectExample) object;
				String importType = project.getImportType();
				if (!ProjectExample.IMPORT_TYPE_ZIP.equals(importType) && importType != null && importType.length() > 0) {
					IImportProjectExample importProjectExample = ProjectExamplesActivator.getDefault().getImportProjectExample(importType);
					if (importProjectExample == null) {
						// FIXME
						canFinish = false;
						break;
					} else {
						//setDefaultNote();
					}
				}
				if (force || project.getUnsatisfiedFixes() == null) {
					List<ProjectFix> fixes = project.getFixes();
					List<ProjectFix> unsatisfiedFixes = new ArrayList<ProjectFix>();
					project.setUnsatisfiedFixes(unsatisfiedFixes);
					for (ProjectFix fix:fixes) {
						if (!ProjectExamplesActivator.canFix(project, fix)) {
							unsatisfiedFixes.add(fix);
						}
					}
				}
				if (project.getUnsatisfiedFixes().size() > 0) {
					// FIXME
				} else {
					
				}

			} else {
				canFinish=false;
				break;
			}
		}
		return canFinish;
	}

	public ProjectExample getSelectedProject() {
		return selectedProject;
	}

	
	private static void restoreCombo(Combo combo, String initialValue) {
		//Look position of initial value
		int selectedIdx = combo.indexOf(initialValue);
		if (selectedIdx  < 0) {
			//If initial value not found, reset to first item
			selectedIdx = 0;
		}
		//Reset position of combo to the appropriate item index
		combo.select(selectedIdx); 
	}
	
	@Override
	public IWizardPage getNextPage() {
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
			//return ((NewProjectExamplesWizard2)wizard).getReadyPage();
		}
		return super.getNextPage();
	}
}
