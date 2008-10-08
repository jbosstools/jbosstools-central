/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;


import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Category;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectUtil;

/**
 * @author snjeza
 * 
 */
public class NewProjectExamplesWizardPage extends WizardPage {

	private Project selectedProject;
	
	public NewProjectExamplesWizardPage() {
		super("org.jboss.tools.project.examples");
        setTitle( "Project Example" );
        setDescription( "Import Project Example" );
        setImageDescriptor( ProjectExamplesActivator.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID, "icons/new_wiz.gif"));
		
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent,SWT.NULL);
		composite.setLayout(new GridLayout(1,false));
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint= 225;
		composite.setLayoutData(gd);
		
		TreeViewer viewer = new TreeViewer(composite,SWT.SINGLE);
		Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(parent.getFont());
		
		viewer.setLabelProvider(new ProjectLabelProvider());
		viewer.setContentProvider(new ProjectContentProvider());
		viewer.setInput(ProjectUtil.getProjects());
		
		Label descriptionLabel = new Label(composite,SWT.NULL);
		descriptionLabel.setText("Description:");
		final Text text = new Text(composite,SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.heightHint=75;
		text.setLayoutData(gd);
		
		Composite internal = new Composite(composite, SWT.NULL);
		internal.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_BOTH);
		internal.setLayoutData(gd);
		
		Label projectNameLabel = new Label(internal,SWT.NULL);
		projectNameLabel.setText("Project name:");
		final Text projectName = new Text(internal,SWT.BORDER | SWT.READ_ONLY);
		projectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label projectSizeLabel = new Label(internal,SWT.NULL);
		projectSizeLabel.setText("Project size:");
		final Text projectSize = new Text(internal,SWT.BORDER | SWT.READ_ONLY);
		projectSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label projectURLLabel = new Label(internal,SWT.NULL);
		projectURLLabel.setText("URL:");
		final Text projectURL = new Text(internal,SWT.BORDER | SWT.READ_ONLY);
		projectURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selected = selection.getFirstElement();
				if (selected instanceof Project) {
					selectedProject = (Project) selected;
					text.setText(selectedProject.getDescription());
					setPageComplete(true);
					projectName.setText(selectedProject.getName());
					projectURL.setText(selectedProject.getUrl());
					projectSize.setText(selectedProject.getSizeAsText());
				} else {
					selectedProject=null;
					text.setText("");
					projectName.setText("");
					projectURL.setText("");
					projectSize.setText("");
					setPageComplete(false);
				}
			}
			
		});
		setPageComplete(false);
		
		setControl(composite);
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
			if (parentElement instanceof List) {
				List children = (List) parentElement;
				return children.toArray();
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

	public Project getSelectedProject() {
		return selectedProject;
	}
}
