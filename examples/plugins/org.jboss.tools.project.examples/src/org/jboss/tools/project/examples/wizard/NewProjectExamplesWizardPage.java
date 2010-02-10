/*************************************************************************************
 * Copyright (c) 2008-2009 JBoss by Red Hat and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.part.PageBook;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.dialog.FixDialog;
import org.jboss.tools.project.examples.fixes.PluginFix;
import org.jboss.tools.project.examples.fixes.SeamRuntimeFix;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.model.Category;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.model.ProjectUtil;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.osgi.framework.Bundle;

/**
 * @author snjeza
 * 
 */
public class NewProjectExamplesWizardPage extends WizardPage {

	private IStructuredSelection selection;
	private Button showQuickFixButton;
	private Combo siteCombo;
	private Text noteText;
	private Button details;
	private PageBook notesPageBook;
	private Composite noteEmptyComposite;
	private Composite noteComposite;
	
	public NewProjectExamplesWizardPage() {
		super("org.jboss.tools.project.examples"); //$NON-NLS-1$
        setTitle( Messages.NewProjectExamplesWizardPage_Project_Example );
        setDescription( Messages.NewProjectExamplesWizardPage_Import_Project_Example );
        setImageDescriptor( ProjectExamplesActivator.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID, "icons/new_wiz.gif")); //$NON-NLS-1$
		
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		
		composite.setLayoutData(gd);
		
		Composite siteComposite = new Composite(composite,SWT.NONE);
		GridLayout gridLayout = new GridLayout(2,false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		siteComposite.setLayout(gridLayout);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		siteComposite.setLayoutData(gd);
		
		final Button button = new Button(siteComposite,SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		button.setLayoutData(gd);
		button.setText(Messages.ProjectExamplesPreferencePage_Show_experimental_sites);
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		button.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES));
		
		new Label(siteComposite,SWT.NONE).setText(Messages.NewProjectExamplesWizardPage_Site);
		siteCombo = new Combo(siteComposite,SWT.READ_ONLY);
		siteCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		String[] items = getItems();
		siteCombo.setItems(items);
		siteCombo.setText(ProjectExamplesActivator.ALL_SITES);
		
		new Label(composite,SWT.NONE).setText(Messages.NewProjectExamplesWizardPage_Projects);
		
		final ProjectExamplesPatternFilter filter = new ProjectExamplesPatternFilter();
		
		int styleBits = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		final FilteredTree filteredTree = new FilteredTree(composite, styleBits, filter,true);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		final TreeViewer viewer = filteredTree.getViewer();
		Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(parent.getFont());
		
		viewer.setLabelProvider(new ProjectLabelProvider());
		viewer.setContentProvider(new ProjectContentProvider());
		
		refresh(viewer);
		final SiteFilter siteFilter = new SiteFilter();
		viewer.addFilter(siteFilter);
		
		Label descriptionLabel = new Label(composite,SWT.NULL);
		descriptionLabel.setText(Messages.NewProjectExamplesWizardPage_Description);
		final Text text = new Text(composite,SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.heightHint=75;
		text.setLayoutData(gd);
		
		Composite internal = new Composite(composite, SWT.NULL);
		internal.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_BOTH);
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
				if (selected instanceof Project && selection.size() == 1) {
					Project selectedProject = (Project) selected;
					text.setText(selectedProject.getDescription());
					projectName.setText(selectedProject.getName());
					projectURL.setText(selectedProject.getUrl());
					projectSize.setText(selectedProject.getSizeAsText());
				} else {
					//Project selectedProject=null;
					text.setText(""); //$NON-NLS-1$
					projectName.setText(""); //$NON-NLS-1$
					projectURL.setText(""); //$NON-NLS-1$
					projectSize.setText(""); //$NON-NLS-1$
				}
				boolean canFinish = refresh(false);
				setPageComplete(canFinish);
			}
			
		});
		
		notesPageBook = new PageBook( internal , SWT.NONE );
        notesPageBook.setLayout(new GridLayout(1,false));
        gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		notesPageBook.setLayoutData( gd );
        
        noteEmptyComposite = new Composite( notesPageBook, SWT.NONE );
        noteEmptyComposite.setLayout( new GridLayout(1, false));
        //notesEmptyComposite.setVisible( false );
        gd=new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		noteEmptyComposite.setLayoutData(gd);
		
		noteComposite = new Composite(notesPageBook, SWT.NONE);
		noteComposite.setLayout(new GridLayout(2,false));
		gd=new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		noteComposite.setLayoutData(gd);
		noteComposite.setVisible(false);
		
		notesPageBook.showPage(noteEmptyComposite);
		
		Composite messageComposite = new Composite(noteComposite, SWT.BORDER);
		messageComposite.setLayout(new GridLayout(2, false));
		gd=new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		
		messageComposite.setLayoutData(gd);
		
		Label noteLabel = new Label(messageComposite,SWT.NONE);
		gd=new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		noteLabel.setLayoutData(gd);
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		image.setBackground(noteLabel.getBackground());
		noteLabel.setImage(image);
		
		noteText = new Text(messageComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		noteText.setText(""); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.heightHint=50;
		gd.widthHint = 400;
		noteText.setLayoutData(gd);
		noteText.setText("This example has some requirements that could not be automatically configured. When importing the example you might see some errors which would need fixing manually or via Quick Fixes. Click \"Details\" to see more.");
		
		details = new Button(noteComposite, SWT.PUSH);
		details.setText("Details...");
		details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Dialog dialog = new FixDialog(getShell(), NewProjectExamplesWizardPage.this);
				dialog.open();
			}
		});
		
		showQuickFixButton = new Button(internal,SWT.CHECK);
		showQuickFixButton.setText(Messages.NewProjectExamplesWizardPage_Show_the_Quick_Fix_dialog);
		showQuickFixButton.setSelection(true);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		showQuickFixButton.setLayoutData(gd);
		
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
				refresh(viewer);
				if (siteCombo != null) {
					String[] items = getItems();
					int index = siteCombo.getSelectionIndex();
					siteCombo.setItems(items);
					if (items.length > 0 && (index < 0 || index > items.length) ) {
						siteCombo.select(0);
					} else {
						siteCombo.select(index);
					}
				}
				siteFilter.setSite(siteCombo.getText());
				viewer.refresh();
			}
			
		});
		setPageComplete(false);
		
		setControl(composite);
	}

	private boolean canFix(Project project,ProjectFix fix) {
		String type = fix.getType();
		if (ProjectFix.PLUGIN_TYPE.equals(type)) {
			return new PluginFix().canFix(project, fix);
		}
		
		if (ProjectFix.WTP_RUNTIME.equals(type)) {
			return new WTPRuntimeFix().canFix(project, fix);
		}
		
		if (ProjectFix.SEAM_RUNTIME.equals(type)) {
			return new SeamRuntimeFix().canFix(project, fix);
		}
		ProjectExamplesActivator.log("Invalid fix in " + project.getName() + ".");
		return true;
	}
	
	private void refresh(final TreeViewer viewer) {
		AdaptableList input = new AdaptableList(getCategories());
		viewer.setInput(input);
		viewer.refresh();
	}

	private List<Category> getCategories() {
		return ProjectUtil.getProjects();
	}
	
	private String[] getItems() {
		List<Category> categories = getCategories();
		Set<String> sites = new TreeSet<String>();
		sites.add(ProjectExamplesActivator.ALL_SITES);
		for (Category category:categories) {
			List<Project> projects = category.getProjects();
			for (Project project:projects) {
				sites.add(project.getSite());
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
			if (element instanceof Category) {
				Category category = (Category) element;
				return category.getName();
			}
			if (element instanceof Project) {
				Project project = (Project) element;
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
			if (parentElement instanceof Category) {
				Category category = (Category) parentElement;
				return category.getProjects().toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof Project) {
				return ((Project)element).getCategory();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return element instanceof Category;
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
	
	public boolean showQuickFix() {
		if (showQuickFixButton != null) {
			return showQuickFixButton.getSelection();
		}
		return false;
	}

	public boolean refresh(boolean force) {
		boolean canFinish = false;
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof Project) {
				canFinish=true;
				Project project = (Project) object;
				if (force || project.getUnsatisfiedFixes() == null) {
					List<ProjectFix> fixes = project.getFixes();
					List<ProjectFix> unsatisfiedFixes = new ArrayList<ProjectFix>();
					project.setUnsatisfiedFixes(unsatisfiedFixes);
					for (ProjectFix fix:fixes) {
						if (!canFix(project, fix)) {
							unsatisfiedFixes.add(fix);
						}
					}
				}
				if (project.getUnsatisfiedFixes().size() > 0) {
					notesPageBook.showPage(noteComposite);
					noteComposite.setVisible(true);
					noteEmptyComposite.setVisible(false);
				} else {
					notesPageBook.showPage(noteEmptyComposite);
					noteComposite.setVisible(false);
					noteEmptyComposite.setVisible(true);
				}

			} else {
				canFinish=false;
				break;
			}
		}
		return canFinish;
	}
}
