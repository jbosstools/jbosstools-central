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
package org.jboss.tools.project.examples.preferences;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.project.examples.model.SiteCategory;

/**
 * 
 * @author snjeza
 *
 */
public class ProjectExamplesPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static final String ID = "org.jboss.tools.project.examples.preferences.projectExamplesPreferencePage"; //$NON-NLS-1$
	private Button showExperimentalSites;
	private Sites sites;
	private TreeViewer viewer;
	private ProjectExampleSite selectedSite;
	private Button showInvalidSites;
	private Text outputDirectoryText;
	private Button isWorkspace;
	private Button showProjectReadyWizard;
	private Button showReadme;
	private Button showQuickFix;
	
	@Override
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		
		Group outputDirectoryGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout(2, false);
		outputDirectoryGroup.setLayout(layout);
		outputDirectoryGroup.setText(Messages.ProjectExamplesPreferencePage_Output_directory);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		outputDirectoryGroup.setLayoutData(gd);
		
		isWorkspace = new Button(outputDirectoryGroup, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		isWorkspace.setLayoutData(gd);
		isWorkspace.setText(Messages.ProjectExamplesPreferencePage_Use_default_workspace_location);
		isWorkspace.setSelection(ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT));
		
		outputDirectoryText = new Text(outputDirectoryGroup, SWT.SINGLE|SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.verticalAlignment = SWT.CENTER;
		outputDirectoryText.setLayoutData(gd);
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		String outputDirectoryValue = store.getString(ProjectExamplesActivator.PROJECT_EXAMPLES_OUTPUT_DIRECTORY);
		outputDirectoryText.setText(outputDirectoryValue == null ? "" : outputDirectoryValue); //$NON-NLS-1$
		final Button outputDirectoryBrowse = new Button(outputDirectoryGroup, SWT.PUSH);
		outputDirectoryBrowse.setText(Messages.Browse);
		outputDirectoryBrowse.addSelectionListener(new SelectionAdapter(){
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SINGLE);
				String value = outputDirectoryText.getText();
				if (value.trim().length() == 0) {
					value = Platform.getLocation().toOSString();
				}
				dialog.setFilterPath(value);
			
				String result = dialog.open();
				if (result == null || result.trim().length() == 0) {
					return;
				}
				outputDirectoryText.setText(result);
				
			}
		
		});
		enableControls(outputDirectoryBrowse);
		
		isWorkspace.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableControls(outputDirectoryBrowse);
			}
			
		});
		
		showProjectReadyWizard = new Button(composite,SWT.CHECK);
		showProjectReadyWizard.setText("Show Project Ready wizard");
		showProjectReadyWizard.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_PROJECT_READY_WIZARD));
		
		showReadme = new Button(composite,SWT.CHECK);
		showReadme.setText("Show readme/cheatsheet file");
		showReadme.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_README));
		
		showQuickFix = new Button(composite,SWT.CHECK);
		showQuickFix.setText("Show Quick Fix dialog");
		showQuickFix.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_QUICK_FIX));
		
		showExperimentalSites = new Button(composite,SWT.CHECK);
		showExperimentalSites.setText(Messages.ProjectExamplesPreferencePage_Show_experimental_sites);
		showExperimentalSites.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES));
		
		showInvalidSites = new Button(composite,SWT.CHECK);
		showInvalidSites.setText(Messages.ProjectExamplesPreferencePage_Show_invalid_sites);
		showInvalidSites.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_INVALID_SITES));
		
		
		
		Group sitesGroup = new Group(composite,SWT.NONE);
		sitesGroup.setText(Messages.ProjectExamplesPreferencePage_Sites);
		GridLayout gl = new GridLayout(2,false);
		sitesGroup.setLayout(gl);
		sitesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer = new TreeViewer(sitesGroup,SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new SitesContentProvider());
		viewer.setLabelProvider(new SitesLabelProvider());
		sites = new Sites();
		viewer.setInput(sites);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.expandAll();
		
		Composite buttonComposite = new Composite(sitesGroup, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1,false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText(Messages.ProjectExamplesPreferencePage_Add);
		addButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				SiteDialog dialog = new SiteDialog(getShell(),null,sites);
				int ok = dialog.open();
				if (ok == Window.OK) {
					String name = dialog.getName();
					if (name != null) {
						URL url = dialog.getURL();
						ProjectExampleSite site = new ProjectExampleSite();
						site.setUrl(url);
						site.setName(name);
						site.setEditable(true);
						sites.add(site);
						viewer.refresh();
					}
				}
			}	
		});
		final Button editButton = new Button(buttonComposite, SWT.PUSH);
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.setText(Messages.ProjectExamplesPreferencePage_Edit);
		editButton.setEnabled(false);
		editButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				if (selectedSite == null) {
					return;
				}
				SiteDialog dialog = new SiteDialog(getShell(),selectedSite,sites);
				int ok = dialog.open();
				if (ok == Window.OK) {
					String name = dialog.getName();
					if (name != null) {
						URL url = dialog.getURL();
						ProjectExampleSite site = selectedSite;
						site.setUrl(url);
						site.setName(name);
						site.setEditable(true);
						viewer.refresh();
					}
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		final Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setText(Messages.ProjectExamplesPreferencePage_Remove);
		removeButton.setEnabled(false);
		
		removeButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				if (selectedSite != null) {
					sites.remove(selectedSite);
					viewer.refresh();
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			
			public void selectionChanged(SelectionChangedEvent event) {
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
				selectedSite = null;
				ISelection selection = event.getSelection();
				if (selection instanceof ITreeSelection) {
					ITreeSelection treeSelection = (ITreeSelection) selection;
					Object object = treeSelection.getFirstElement();
					if (object instanceof ProjectExampleSite) {
						selectedSite = (ProjectExampleSite) object;
						boolean editable = ((IProjectExampleSite) object).isEditable();
						editButton.setEnabled(editable);
						removeButton.setEnabled(editable);
					}
				}
			}
		});
		
		return composite;
	}

	protected void enableControls(Button outputDirectoryBrowse) {
		outputDirectoryText.setEnabled(!isWorkspace.getSelection());
		outputDirectoryBrowse.setEnabled(!isWorkspace.getSelection());
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		showProjectReadyWizard.setSelection(ProjectExamplesActivator.SHOW_PROJECT_READY_WIZARD_VALUE);
		showReadme.setSelection(ProjectExamplesActivator.SHOW_README_VALUE);
		showQuickFix.setSelection(ProjectExamplesActivator.SHOW_QUICK_FIX_VALUE);
		
		showExperimentalSites.setSelection(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES_VALUE);
		showInvalidSites.setSelection(ProjectExamplesActivator.SHOW_INVALID_SITES_VALUE);
		isWorkspace.setSelection(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT_VALUE);
		outputDirectoryText.setText(""); //$NON-NLS-1$
		sites.getUserSites().clear();
		storePreferences();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		storePreferences(); 
		return super.performOk();
	}

	private void storePreferences() {
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		store.setValue(ProjectExamplesActivator.SHOW_PROJECT_READY_WIZARD, showProjectReadyWizard.getSelection());
		store.setValue(ProjectExamplesActivator.SHOW_README, showReadme.getSelection());
		store.setValue(ProjectExamplesActivator.SHOW_QUICK_FIX, showQuickFix.getSelection());
		
		store.setValue(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES, showExperimentalSites.getSelection());
		store.setValue(ProjectExamplesActivator.SHOW_INVALID_SITES, showInvalidSites.getSelection());
		store.setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT, isWorkspace.getSelection());
		String value = outputDirectoryText.getText();
		if (!value.isEmpty()) {
			store.setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_OUTPUT_DIRECTORY, value);
		}
		try {
			String userSites = ProjectExampleUtil.getAsXML(sites.getUserSites());
			store.setValue(ProjectExamplesActivator.USER_SITES, userSites);
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		}
	}

	class SitesContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Sites) {
				return ((Sites)parentElement).getSiteCategories();
			}
			if (parentElement instanceof SiteCategory) {
				return ((SiteCategory) parentElement).getSites().toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return element instanceof Sites || element instanceof SiteCategory;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}
	
	class SitesLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof IProjectExampleSite) {
				return ((IProjectExampleSite) element).getName();
			}
			return super.getText(element);
		}
		
	}
	
}
