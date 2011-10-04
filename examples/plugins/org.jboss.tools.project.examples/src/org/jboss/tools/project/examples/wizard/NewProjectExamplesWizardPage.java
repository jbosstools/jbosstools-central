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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.part.PageBook;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.dialog.FixDialog;
import org.jboss.tools.project.examples.model.Category;
import org.jboss.tools.project.examples.model.IImportProjectExample;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.project.examples.model.ProjectUtil;

/**
 * @author snjeza
 * 
 */
public class NewProjectExamplesWizardPage extends WizardPage {

	private static final int DEFAULT_HEIGHT = 400;
	private static final int DEFAULT_WIDTH = 600;
	private IStructuredSelection selection;
	private Button showQuickFixButton;
	private Combo siteCombo;
	private Text noteText;
	private Button details;
	private PageBook notesPageBook;
	private Composite noteEmptyComposite;
	private Composite noteComposite;
	private List<Category> categories;
	private Text descriptionText;
	
	public NewProjectExamplesWizardPage() {
		super("org.jboss.tools.project.examples"); //$NON-NLS-1$
        setTitle( Messages.NewProjectExamplesWizardPage_Project_Example );
        setDescription( Messages.NewProjectExamplesWizardPage_Import_Project_Example );
        setImageDescriptor( ProjectExamplesActivator.imageDescriptorFromPlugin(ProjectExamplesActivator.PLUGIN_ID, "icons/new_wiz.gif")); //$NON-NLS-1$
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
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		button.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES));
		
		new Label(siteComposite,SWT.NONE).setText(Messages.NewProjectExamplesWizardPage_Site);
		siteCombo = new Combo(siteComposite,SWT.READ_ONLY);
		siteCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		new Label(composite,SWT.NONE).setText(Messages.NewProjectExamplesWizardPage_Projects);
		
		final ProjectExamplesPatternFilter filter = new ProjectExamplesPatternFilter();
		
		int styleBits = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP;
		final FilteredTree filteredTree = new FilteredTree(composite, styleBits, filter,true);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		final TreeViewer viewer = filteredTree.getViewer();
		Tree tree = viewer.getTree();
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GC gc = new GC(parent);
		gd.heightHint = Dialog.convertHeightInCharsToPixels(gc
				.getFontMetrics(), 7);
		gc.dispose(); 
		tree.setLayoutData(gd);
		tree.setFont(parent.getFont());
		
		viewer.setLabelProvider(new ProjectLabelProvider());
		viewer.setContentProvider(new ProjectContentProvider());
		
		final SiteFilter siteFilter = new SiteFilter();
		viewer.addFilter(siteFilter);
		
		Label descriptionLabel = new Label(composite,SWT.NONE);
		descriptionLabel.setText(Messages.NewProjectExamplesWizardPage_Description);
		descriptionText = new Text(composite,SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gc = new GC(parent);
		gd.heightHint = Dialog.convertHeightInCharsToPixels(gc
				.getFontMetrics(), 8);
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
				if (selected instanceof Project && selection.size() == 1) {
					Project selectedProject = (Project) selected;
					descriptionText.setText(selectedProject.getDescription());
					projectName.setText(selectedProject.getName());
					projectURL.setText(selectedProject.getUrl());
					projectSize.setText(selectedProject.getSizeAsText());
				} else {
					//Project selectedProject=null;
					descriptionText.setText(""); //$NON-NLS-1$
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
        gd=new GridData(GridData.FILL, GridData.FILL, true, false);
        gc = new GC(parent);
		gd.heightHint = Dialog.convertHeightInCharsToPixels(gc
				.getFontMetrics(), 6);
		gc.dispose(); 
		gd.horizontalSpan=2;
		notesPageBook.setLayoutData( gd );
        
        noteEmptyComposite = new Composite( notesPageBook, SWT.NONE );
        noteEmptyComposite.setLayout( new GridLayout(1, false));
        //notesEmptyComposite.setVisible( false );
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		noteEmptyComposite.setLayoutData(gd);
		
		noteComposite = new Composite(notesPageBook, SWT.NONE);
		noteComposite.setLayout(new GridLayout(2,false));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		noteComposite.setLayoutData(gd);
		noteComposite.setVisible(false);
		
		notesPageBook.showPage(noteEmptyComposite);
		
		Composite messageComposite = new Composite(noteComposite, SWT.BORDER);
		messageComposite.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		messageComposite.setLayoutData(gd);
		
		Label noteLabel = new Label(messageComposite,SWT.NONE);
		gd=new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		noteLabel.setLayoutData(gd);
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		image.setBackground(noteLabel.getBackground());
		noteLabel.setImage(image);
		
		noteText = new Text(messageComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gc = new GC(parent);
		gd.heightHint = Dialog.convertHeightInCharsToPixels(gc
				.getFontMetrics(), 3);
		gc.dispose(); 
		noteText.setLayoutData(gd);
		
		details = new Button(noteComposite, SWT.PUSH);
		details.setText(Messages.NewProjectExamplesWizardPage_Details);
		gd=new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		details.setLayoutData(gd);
		details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Dialog dialog = new FixDialog(getShell(), NewProjectExamplesWizardPage.this);
				dialog.open();
			}
		});
		setDefaultNote();
		showQuickFixButton = new Button(composite,SWT.CHECK);
		showQuickFixButton.setText(Messages.NewProjectExamplesWizardPage_Show_the_Quick_Fix_dialog);
		showQuickFixButton.setSelection(true);
		gd=new GridData(SWT.BEGINNING, SWT.BOTTOM, false, false);
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
				refresh(viewer, true);
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
		
		configureSizeAndLocation();
		refresh(viewer, true);
		siteCombo.setText(ProjectExamplesActivator.ALL_SITES);
	}

	private void configureSizeAndLocation() {
		Shell shell = getContainer().getShell();
		Point size = new Point(DEFAULT_WIDTH, getHeight());
		shell.setSize(size);
		Point location = getInitialLocation(size, shell);
		shell.setBounds(getConstrainedShellBounds(new Rectangle(location.x,
				location.y, size.x, size.y)));
	}
	
	private int getHeight() {
		GC gc = new GC(getControl());
		int height = Dialog.convertVerticalDLUsToPixels(gc
				.getFontMetrics(), DEFAULT_HEIGHT);
		gc.dispose(); 
		return height;
	}

	private Rectangle getConstrainedShellBounds(Rectangle preferredSize) {
		Rectangle result = new Rectangle(preferredSize.x, preferredSize.y,
				preferredSize.width, preferredSize.height);

		Monitor mon = getClosestMonitor(getShell().getDisplay(), Geometry
				.centerPoint(result));

		Rectangle bounds = mon.getClientArea();

		if (result.height > bounds.height) {
			result.height = bounds.height;
		}

		if (result.width > bounds.width) {
			result.width = bounds.width;
		}

		result.x = Math.max(bounds.x, Math.min(result.x, bounds.x
				+ bounds.width - result.width));
		result.y = Math.max(bounds.y, Math.min(result.y, bounds.y
				+ bounds.height - result.height));

		return result;
	}

	private static Monitor getClosestMonitor(Display toSearch, Point toFind) {
		int closest = Integer.MAX_VALUE;

		Monitor[] monitors = toSearch.getMonitors();
		Monitor result = monitors[0];

		for (int idx = 0; idx < monitors.length; idx++) {
			Monitor current = monitors[idx];

			Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(toFind)) {
				return current;
			}

			int distance = Geometry.distanceSquared(Geometry
					.centerPoint(clientArea), toFind);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}


	private Point getInitialLocation(Point initialSize, Shell shell) {
		Composite parent = shell.getParent();

		Monitor monitor = shell.getDisplay().getPrimaryMonitor();
		if (parent != null) {
			monitor = parent.getMonitor();
		}

		Rectangle monitorBounds = monitor.getClientArea();
		Point centerPoint;
		if (parent != null) {
			centerPoint = Geometry.centerPoint(parent.getBounds());
		} else {
			centerPoint = Geometry.centerPoint(monitorBounds);
		}

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
				monitorBounds.y, Math.min(centerPoint.y
						- (initialSize.y * 2 / 3), monitorBounds.y
						+ monitorBounds.height - initialSize.y)));
	}

	private void refresh(final TreeViewer viewer, boolean show) {
		AdaptableList input = new AdaptableList(getCategories(show));
		viewer.setInput(input);
		viewer.refresh();
		String[] items = getItems();
		siteCombo.setItems(items);		
	}

	private List<Category> getCategories(boolean show) {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			
			public void run(IProgressMonitor monitor) {
				categories = ProjectUtil.getProjects(monitor);
			}
		};
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, op);
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		}
		HashSet<IProjectExampleSite> invalidSites = ProjectUtil.getInvalidSites();
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
		for (Category category:categories) {
			List<Project> projects = category.getProjects();
			for (Project project:projects) {
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
		notesPageBook.showPage(noteEmptyComposite);
		noteComposite.setVisible(false);
		noteEmptyComposite.setVisible(true);
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof Project) {
				canFinish=true;
				Project project = (Project) object;
				String importType = project.getImportType();
				if (importType != null && importType.length() > 0) {
					IImportProjectExample importProjectExample = ProjectExamplesActivator.getDefault().getImportProjectExample(importType);
					if (importProjectExample == null) {
						notesPageBook.showPage(noteComposite);
						noteComposite.setVisible(true);
						noteEmptyComposite.setVisible(false);
						noteText.setText(project.getImportTypeDescription());
						details.setEnabled(false);
						canFinish = false;
						break;
					} else {
						setDefaultNote();
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

	private void setDefaultNote() {
		noteText.setText(Messages.NewProjectExamplesWizardPage_Note);
		details.setEnabled(true);
	}
}
