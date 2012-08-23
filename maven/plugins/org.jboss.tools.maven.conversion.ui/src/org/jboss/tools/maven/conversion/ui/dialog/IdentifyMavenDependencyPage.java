/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jboss.tools.maven.conversion.ui.handlers.IdentifyJarJob;
import org.jboss.tools.maven.conversion.ui.internal.CellListener;
import org.jboss.tools.maven.conversion.ui.internal.MavenDependencyConversionActivator;
import org.jboss.tools.maven.sourcelookup.identification.IFileIdentificationManager;
import org.jboss.tools.maven.sourcelookup.identification.IdentificationUtil;
import org.jboss.tools.maven.sourcelookup.internal.identification.FileIdentificationManager;

public class IdentifyMavenDependencyPage extends WizardPage {

	private static final String SOURCE_PROPERTY = "SOURCE_PROPERTY";

	private static final String DEPENDENCY_PROPERTY = "DEPENDENCY_PROPERTY";

	private static final int DEPENDENCY_COLUMN = 2;

	private Map<IClasspathEntry, Dependency> dependencyMap;

	private Map<IClasspathEntry, IdentifyJarJob> identificationJobs;

	private Set<IClasspathEntry> initialEntries;
	
	private IProject project;
	
	private Image jarImage;
	private Image projectImage;
	private Image okImage;
	private Image failedImage;
	private Image loadingImage;
	
	private CheckboxTableViewer dependenciesViewer;
	
	public IdentifyMavenDependencyPage(IProject project, Set<IClasspathEntry> entries) {
		super("");
		this.project = project;
		initialEntries = Collections.unmodifiableSet(entries);
		initDependencyMap();
	}

	private void initDependencyMap() {
		dependencyMap = new LinkedHashMap<IClasspathEntry, Dependency>(initialEntries.size());
		IJavaProject javaProject = JavaCore.create(project);
		try {
			
			for (IClasspathEntry entry : initialEntries) {
				if ((entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY && entry.getPath() != null)
						|| (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)) {
					dependencyMap.put(entry, null);
				} else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), javaProject );
					if (container != null) {
						for (IClasspathEntry cpe: container.getClasspathEntries()) {
							dependencyMap.put(cpe, null);
						}
					}
				}
			}
			
		} catch(Exception e) {
			setMessage(e.getLocalizedMessage());
		}
	}

	private void initImages() {
		jarImage = MavenDependencyConversionActivator.getJarIcon();
		projectImage = MavenDependencyConversionActivator.getProjectIcon();
		okImage = MavenDependencyConversionActivator.getOkIcon();
		failedImage = MavenDependencyConversionActivator.getFailedIcon();
		loadingImage = MavenDependencyConversionActivator.getLoadingIcon();
	}

	@Override
	public void createControl(Composite parent) {

		setTitle("Identify Maven dependencies");

		initImages();

		Composite container = new Composite(parent, SWT.NONE);
		container.setEnabled(true);
		setControl(container);
		
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 12;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		String message = "Identify existing classpath entries as Maven dependencies";
		setMessage(message);

		displayDependenciesTable(container);

		runIdentificationJobs();
	}
	

	private void displayDependenciesTable(Composite container) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4);
		gd.heightHint = 500;
		gd.widthHint = 545;

		dependenciesViewer = CheckboxTableViewer.newCheckList(container,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );
		Table table = dependenciesViewer.getTable();
		table.setFocus();
		table.setLayoutData(gd);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn emptyColumn = new TableColumn(table, SWT.NONE);
		emptyColumn.setWidth(20);

		TableViewerColumn sourceColumn = new TableViewerColumn(dependenciesViewer, SWT.NONE);
		sourceColumn.getColumn().setText("Classpath Entry ");
		sourceColumn.getColumn().setWidth(270);
		sourceColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			@SuppressWarnings("unchecked")
			public String getText(Object element) {
				Map.Entry<IClasspathEntry, Dependency> entry = (Map.Entry<IClasspathEntry, Dependency>) element;
				return entry.getKey().getPath().lastSegment();
			}
			
			@Override
			public String getToolTipText(Object element) {
				try {
					return "SHA1 Checksum : "+IdentificationUtil.getSHA1(ConversionUtils.getFile(((Map.Entry<IClasspathEntry, Dependency>) element).getKey()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "Unable to compute SHA1 Checksum";
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public Image getImage(Object element) {
				Map.Entry<IClasspathEntry, String> entry = (Map.Entry<IClasspathEntry, String>) element;
				Image img;
				if (entry.getKey().getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					img = jarImage;
				} else {
					img = projectImage;
				}
				return img;
			}
		});

		TableViewerColumn dependencyColumn = new TableViewerColumn(dependenciesViewer, SWT.NONE);
		dependencyColumn.getColumn().setText("Maven Dependency");
		dependencyColumn.getColumn().setWidth(270);
		dependencyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public String getText(Object element) {
				Map.Entry<IClasspathEntry, Dependency> entry = (Map.Entry<IClasspathEntry, Dependency>) element;
				IdentifyJarJob job = identificationJobs ==null? null:identificationJobs.get(entry.getKey());
				if (job != null) {
					int jobState = job.getState();
					if (jobState == Job.RUNNING || jobState == Job.WAITING) {
						return "Identification in progress...";
					}
				}
				return IdentifyMavenDependencyPage.toString(entry.getValue());
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public Image getImage(Object element) {
				Map.Entry<IClasspathEntry, String> entry = (Map.Entry<IClasspathEntry, String>) element;
				IdentifyJarJob job = identificationJobs ==null? null:identificationJobs.get(entry.getKey());
				if (job != null) {
					int jobState = job.getState();
					if (jobState == Job.RUNNING || jobState == Job.WAITING) {
						return loadingImage;
					}
				}
				if (entry.getValue() == null) {
					return failedImage;
				} else {
					return okImage;
				}
			}
		});

		dependenciesViewer.setContentProvider(ArrayContentProvider.getInstance());
		dependenciesViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				refresh();
			}
		});
		dependenciesViewer.setInput(dependencyMap.entrySet());
		dependenciesViewer.setAllChecked(true);

		dependenciesViewer.getTable().addListener(SWT.MouseDoubleClick, new CellListener(dependenciesViewer.getTable()) {
			
			@Override
			protected void handle(int columnIndex, TableItem item) {
				if (columnIndex == DEPENDENCY_COLUMN) {
					Entry<IClasspathEntry, Dependency> entry = (Map.Entry<IClasspathEntry, Dependency>) item.getData();
					Dependency d= entry.getValue();
					EditDependencyDialog editDependencyDialog = new EditDependencyDialog(getShell());
					editDependencyDialog.setDependency(d);
					if(editDependencyDialog.open() == Window.OK) {
						entry.setValue(editDependencyDialog.getDependency());
					}
				}
			}
		});
		
		
		addSelectionButton(container, "Select All", true);
		addSelectionButton(container, "Deselect All", false);
		//addIdentifyButton(container, "Identify dependencies");
		addResetButton(container, "Reset");

		addCellEditors();
	}

	
	@Override
	public boolean isPageComplete() {
		return true;
	} 
	
	protected void addCellEditors() {
		dependenciesViewer.setColumnProperties(
				new String[] { "EMPTY",	SOURCE_PROPERTY, DEPENDENCY_PROPERTY });

		DependencyCellEditor dce = new DependencyCellEditor(dependenciesViewer.getTable());
		CellEditor[] editors = new CellEditor[] { null, null, dce};
		dependenciesViewer.setCellEditors(editors);
		dependenciesViewer.setCellModifier(new DependencyCellModifier());
	}

	private class DependencyCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			return DEPENDENCY_PROPERTY.equals(property);
		}

		public Object getValue(Object element, String property) {
			Map.Entry<IClasspathEntry, Dependency> entry = (Map.Entry<IClasspathEntry, Dependency>) element;
			if (property.equals(SOURCE_PROPERTY)) {
				return entry.getKey().getPath().toOSString();
			} else if (property.equals(DEPENDENCY_PROPERTY)) {
				return IdentifyMavenDependencyPage.toString(entry.getValue());
			}
			return ""; //$NON-NLS-1$
		}

		public void modify(Object element, String property, Object value) {
			if (property.equals(DEPENDENCY_PROPERTY)) {
				TableItem item = (TableItem) element;
				Map.Entry<IClasspathEntry, Dependency> entry = (Map.Entry<IClasspathEntry, Dependency>) item.getData();
				if (value instanceof Dependency) {
					entry.setValue((Dependency)value);
					refresh();
				}
			}
		}
	}

	private class DependencyCellEditor extends DialogCellEditor {

		DependencyCellEditor (Composite parent) {
			super(parent);
	    }
		
	    @Override
		protected Object openDialogBox(Control cellEditorWindow) {
			Table table = (Table)cellEditorWindow.getParent(); 
			int idx = table.getSelectionIndex();
			Dependency d= ((Map.Entry<IClasspathEntry, Dependency>) table.getItem(idx).getData()).getValue();
			EditDependencyDialog editDependencyDialog = new EditDependencyDialog(cellEditorWindow.getShell());
			editDependencyDialog.setDependency(d);
			if(editDependencyDialog.open() == Window.OK) {
				return editDependencyDialog.getDependency();
			}
			return d;
		}
		
	}


	private Button addSelectionButton(Composite container, String label,
			final boolean ischecked) {
		Button button = new Button(container, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
		button.setText(label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				dependenciesViewer.setAllChecked(ischecked);
				refresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return button;
	}

	private Button addResetButton(Composite container, String label) {
		Button button = new Button(container, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
		button.setText(label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				initDependencyMap( );
				dependenciesViewer.setInput(dependencyMap.entrySet());
				dependenciesViewer.setAllChecked(true);
				refresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return button;
	}

	
	private Button addIdentifyButton(Composite container, String label) {
		Button button = new Button(container, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
		button.setText(label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				runIdentificationJobs();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return button;
	}
	
	protected void runIdentificationJobs() {
		
		initJobs();
		for (Map.Entry<IClasspathEntry, Dependency> entry : dependencyMap.entrySet()) {
			if (entry.getValue() != null) {
				//don't need to run identification
				continue;
			}
			IdentifyJarJob job = identificationJobs.get(entry.getKey());
			if (job != null) {
				int jobState = job.getState();
				if (jobState == Job.NONE) {
					job.schedule();
				}
			}
		}
		
	}

	protected void refresh() {
		if (dependenciesViewer != null && !dependenciesViewer.getTable().isDisposed()) {
			dependenciesViewer.refresh();
		}
	}
	

    static String toString(Dependency d) {
		if (d == null) {
			return "   Unidentified dependency";
		}
		StringBuilder text = new StringBuilder("   ");
		text.append(d.getGroupId())
		.append(":")
		.append(d.getArtifactId())
		.append(":")
		.append(d.getVersion());
		return text.toString();
	}
    
    void initJobs() {
    	if (identificationJobs == null) {
    		identificationJobs = new HashMap<IClasspathEntry, IdentifyJarJob>(dependencyMap.size());
    		
    		Table t = dependenciesViewer.getTable();
    		IFileIdentificationManager fileIdentificationManager = new FileIdentificationManager();
    		
    		for (final TableItem item : t.getItems()) {
    			final Map.Entry<IClasspathEntry, Dependency> entry = (Map.Entry<IClasspathEntry, Dependency>)item.getData();
    			if (entry.getValue() != null) {
    				//already identified
    				continue;
    			}
    			File jar;
				try {
					jar = ConversionUtils.getFile(entry.getKey());
					
					final IdentifyJarJob job = new IdentifyJarJob("Search the Maven coordinates for "+jar.getAbsolutePath(), fileIdentificationManager, jar);
					job.addJobChangeListener(new IJobChangeListener() {
						
						@Override
						public void sleeping(IJobChangeEvent event) {
						}
						
						@Override
						public void scheduled(IJobChangeEvent event) {
							item.setImage(DEPENDENCY_COLUMN, loadingImage);
							item.setText(DEPENDENCY_COLUMN, "Identification in progress...");
						}
						
						@Override
						public void running(IJobChangeEvent event) {
						}
						
						@Override
						public void done(IJobChangeEvent event) {
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									Dependency d = job.getDependency();
									dependencyMap.put(entry.getKey(), d);
									refresh();
								}
							});
						}
						
						@Override
						public void awake(IJobChangeEvent event) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void aboutToRun(IJobChangeEvent event) {
							// TODO Auto-generated method stub
							
						}
					});
					identificationJobs.put(entry.getKey(), job);
				} catch (CoreException e) {
					e.printStackTrace();
				}
    		}    		
    	}
    	
    }

	public List<Dependency> getDependencies() {
		
		Object[] selection = dependenciesViewer.getCheckedElements();
		List<Dependency> dependencies = new ArrayList<Dependency>(selection.length);
		for (Object o : selection) {
			Map.Entry<IClasspathEntry, Dependency> entry = (Map.Entry<IClasspathEntry, Dependency>) o;
			Dependency d = entry.getValue();
			if (d != null) {
				dependencies.add(d);
			}
		}
		return dependencies;
	}
}
