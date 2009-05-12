package org.jboss.tools.project.examples.preferences;

import java.net.URL;

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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectUtil;
import org.jboss.tools.project.examples.model.SiteCategory;

public class ProjectExamplesPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button button;
	private Sites sites;
	private TreeViewer viewer;
	private ProjectExampleSite selectedSite;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		
		button = new Button(composite,SWT.CHECK);
		button.setText(Messages.ProjectExamplesPreferencePage_Show_experimental_sites);
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		button.setSelection(store.getBoolean(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES));
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
						boolean editable = ((ProjectExampleSite) object).isEditable();
						editButton.setEnabled(editable);
						removeButton.setEnabled(editable);
					}
				}
			}
		});
		
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		button.setSelection(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES_VALUE);
		sites.getUserSites().clear();
		storeSites();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		storeSites(); 
		return super.performOk();
	}

	private void storeSites() {
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		store.setValue(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES, button.getSelection());
		try {
			String userSites = ProjectUtil.getAsXML(sites.getUserSites());
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
