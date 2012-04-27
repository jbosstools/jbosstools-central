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
package org.jboss.tools.maven.sourcelookup.ui.preferences;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.maven.sourcelookup.NexusRepository;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.ui.browsers.EditNexusRepositoryDialog;

/**
 * 
 * @author snjeza
 * 
 */
public class SourceLookupPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button autoAddButton;
	private CheckboxTableViewer tableViewer;
	private Set<NexusRepository> nexusRepositories;

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(layout);

		autoAddButton = new Button(composite, SWT.CHECK);
		autoAddButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		autoAddButton.setSelection(SourceLookupActivator.getDefault()
				.isAutoAddSourceContainer());
		autoAddButton
				.setText("Automatically add the JBoss Maven source container to all JBoss AS launch configurations");

		// Nexus Repositories
		Group group = new Group(composite, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		group.setLayoutData(gd);
		layout = new GridLayout(2, false);
		group.setLayout(layout);
		group.setText("Nexus Index Repositories");
		tableViewer = CheckboxTableViewer
				.newCheckList(group, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		Table table = tableViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 300;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnNames = new String[] { "Name", "URL" };
		int[] columnWidths = new int[] { 200, 200 };

		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setText(columnNames[i]);
			tc.setWidth(columnWidths[i]);
		}
		
		ColumnLayoutData[] layouts = {
				new ColumnWeightData(200,200),
				new ColumnWeightData(200,200)
			};

		TableLayout tableLayout = new AutoResizeTableLayout(table);
		for (int i = 0; i < layouts.length; i++) {
			tableLayout.addColumnData(layouts[i]);
		}
		
		nexusRepositories = SourceLookupActivator
				.getNexusRepositories();
		tableViewer.setLabelProvider(new NexusRepositoryLabelProvider());
		tableViewer.setContentProvider(new NexusRepositoryContentProvider(
				nexusRepositories));

		tableViewer.setInput(nexusRepositories);
		for (NexusRepository nexusRepository : nexusRepositories) {
			tableViewer.setChecked(nexusRepository,
					nexusRepository.isEnabled());
		}
		tableViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				NexusRepository repository = (NexusRepository) event
						.getElement();
				repository.setEnabled(!repository.isEnabled());
			}
		});
		
		createButtons(group, tableViewer);
		return composite;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private void createButtons(Composite parent, final TableViewer viewer) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1,false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText("Add...");
		addButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				EditNexusRepositoryDialog dialog = new EditNexusRepositoryDialog(getShell(), null);
				int ok = dialog.open();
				if (ok == Window.OK) {
					NexusRepository repository = dialog.getNexusRepository();
					nexusRepositories.add(repository);
					viewer.refresh();
					tableViewer.setChecked(repository,
							repository.isEnabled());
				}
				viewer.refresh();
			}
		
		});
		
		final Button editButton = new Button(buttonComposite, SWT.PUSH);
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.setText("Edit...");
		editButton.setEnabled(false);
		
		editButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof NexusRepository) {
						NexusRepository repository = (NexusRepository) object;
						NexusRepository edit = new NexusRepository(repository.getName(), repository.getUrl(), repository.isEnabled());
						EditNexusRepositoryDialog dialog = new EditNexusRepositoryDialog(getShell(), edit);
						int ok = dialog.open();
						if (ok == Window.OK) {
							repository.setName(edit.getName());
							repository.setUrl(edit.getUrl());
							repository.setEnabled(edit.isEnabled());
							viewer.refresh();
							tableViewer.setChecked(repository,
									repository.isEnabled());
						}
					}
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		final Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setText("Remove");
		removeButton.setEnabled(false);
		
		removeButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof NexusRepository) {
						nexusRepositories.remove(object); 
						viewer.refresh();
					}
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		final Button upButton = new Button(buttonComposite, SWT.PUSH);
		upButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		upButton.setText("Up");
		upButton.setEnabled(false);
		
		final Button downButton = new Button(buttonComposite, SWT.PUSH);
		downButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		downButton.setText("Down");
		downButton.setEnabled(false);
		
		upButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof NexusRepository) {
						NexusRepository selected = (NexusRepository) object;
						NexusRepository[] reps = nexusRepositories.toArray(new NexusRepository[0]);
						int selectedIndex = -1;
						for (int i = 0; i < reps.length; i++) {
							NexusRepository rep = reps[i];
							if (selected.equals(rep)) {
								selectedIndex = i;
								break;
							}
						}
						if (selectedIndex > 0) {
							NexusRepository temp = reps[selectedIndex-1];
							reps[selectedIndex - 1] = selected;
							reps[selectedIndex] = temp;
							nexusRepositories.clear();
							for (NexusRepository repository:reps) {
								nexusRepositories.add(repository);
							}
							viewer.refresh();
							int newIndex = selectedIndex - 1;
							downButton.setEnabled( newIndex < (reps.length - 1));
							upButton.setEnabled(newIndex > 0);
							
						}
					}
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		downButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof NexusRepository) {
						NexusRepository selected = (NexusRepository) object;
						NexusRepository[] reps = nexusRepositories.toArray(new NexusRepository[0]);
						int selectedIndex = -1;
						for (int i = 0; i < reps.length; i++) {
							NexusRepository rep = reps[i];
							if (selected.equals(rep)) {
								selectedIndex = i;
								break;
							}
						}
						if (selectedIndex >= 0 && selectedIndex < reps.length) {
							NexusRepository temp = reps[selectedIndex + 1];
							reps[selectedIndex + 1] = selected;
							reps[selectedIndex] = temp;
							nexusRepositories.clear();
							for (NexusRepository repository:reps) {
								nexusRepositories.add(repository);
							}
							viewer.refresh();
							int newIndex = selectedIndex + 1;
							downButton.setEnabled( newIndex < (reps.length - 1));
							upButton.setEnabled(newIndex > 0);
						}
					}
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					editButton.setEnabled(object instanceof NexusRepository);
					removeButton.setEnabled(object instanceof NexusRepository);
					upButton.setEnabled(false);
					downButton.setEnabled(false);
					if (object instanceof NexusRepository && nexusRepositories.size() > 1) {
						NexusRepository repository = (NexusRepository) object;
						Iterator<NexusRepository> iterator = nexusRepositories.iterator();
						NexusRepository first = null;
						if (iterator.hasNext()) {
							first = iterator.next();
						}
						NexusRepository last = null;
						while (iterator.hasNext()) {
							last = iterator.next();
						}
						if (repository.equals(last)) {
							upButton.setEnabled(true);
						} else if (repository.equals(first)) {
							downButton.setEnabled(true);
						} else {
							upButton.setEnabled(true);
							downButton.setEnabled(true);
						}
						
					}
				} else {
					editButton.setEnabled(false);
					removeButton.setEnabled(false);
					upButton.setEnabled(false);
					downButton.setEnabled(false);
				}
			}
		});	
	}

	@Override
	protected void performApply() {
		IEclipsePreferences preferences = SourceLookupActivator.getDefault()
				.getPreferences();
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				autoAddButton.getSelection());
		SourceLookupActivator.setNexusRepositories(nexusRepositories);
		SourceLookupActivator.saveNexusRepositories();
		SourceLookupActivator.getDefault().savePreferences();
		tableViewer.setInput(nexusRepositories);
		for (NexusRepository nexusRepository : nexusRepositories) {
			tableViewer.setChecked(nexusRepository,
					nexusRepository.isEnabled());
		}
	}

	@Override
	protected void performDefaults() {
		IEclipsePreferences preferences = SourceLookupActivator.getPreferences();

		autoAddButton
				.setSelection(SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
		SourceLookupActivator.setNexusRepositories(null);
		nexusRepositories = SourceLookupActivator.getDefaultRepositories();
		SourceLookupActivator.saveNexusRepositories();
		SourceLookupActivator.getDefault().savePreferences();
		tableViewer.setInput(nexusRepositories);
		for (NexusRepository nexusRepository : nexusRepositories) {
			tableViewer.setChecked(nexusRepository,
					nexusRepository.isEnabled());
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}

	private class NexusRepositoryLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof NexusRepository) {
				NexusRepository nr = (NexusRepository) element;
				if (columnIndex == 0) {
					return nr.getName();
				}
				if (columnIndex == 1) {
					return nr.getUrl();
				}
			}
			return null;
		}
	}

	private class NexusRepositoryContentProvider implements
			IStructuredContentProvider {

		private Set<NexusRepository> repositories;

		public NexusRepositoryContentProvider(Set<NexusRepository> repositories) {
			this.repositories = repositories;
		}

		public Object[] getElements(Object inputElement) {
			return repositories.toArray();
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			repositories = (Set<NexusRepository>) newInput;
		}
	}

	@Override
	public boolean performCancel() {
		SourceLookupActivator.setNexusRepositories(null);
		return super.performCancel();
	}
}
