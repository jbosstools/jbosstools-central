/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jboss.tools.maven.conversion.core.ProjectDependency;
import org.jboss.tools.maven.conversion.core.ProjectDependency.DependencyKind;
import org.jboss.tools.maven.conversion.ui.dialog.xpl.ConversionUtils;
import org.jboss.tools.maven.conversion.ui.dialog.xpl.EditDependencyDialog;
import org.jboss.tools.maven.conversion.ui.internal.CellListener;
import org.jboss.tools.maven.conversion.ui.internal.MavenDependencyConversionActivator;
import org.jboss.tools.maven.conversion.ui.internal.jobs.DependencyResolutionJob;
import org.jboss.tools.maven.conversion.ui.internal.jobs.IdentificationJob;
import org.jboss.tools.maven.conversion.ui.internal.jobs.IdentificationJob.Task;
import org.jboss.tools.maven.conversion.ui.internal.jobs.IdentifyJarJob;
import org.jboss.tools.maven.conversion.ui.internal.jobs.IdentifyProjectJob;
import org.jboss.tools.maven.core.identification.IFileIdentificationManager;
import org.jboss.tools.maven.core.identification.IdentificationUtil;
import org.jboss.tools.maven.core.internal.identification.FileIdentificationManager;
import org.jboss.tools.maven.ui.wizard.ConfigureMavenRepositoriesWizard;

public class IdentifyMavenDependencyPage extends WizardPage {

	/*
	private static final String SOURCE_PROPERTY = "SOURCE_PROPERTY";

	private static final String DEPENDENCY_PROPERTY = "DEPENDENCY_PROPERTY";
    */
	
	private static final int DEPENDENCY_COLUMN = 2;

	private Map<ProjectDependency, Dependency> dependencyMap;

	private Map<ProjectDependency, IdentificationJob> identificationJobs;

	private List<ProjectDependency> initialEntries;

	private Map<Dependency, Boolean> dependencyResolution = new ConcurrentHashMap<Dependency, Boolean>();

	private IProject project;
	
	private Image jarImage;
	private Image projectImage;
	private Image okImage;
	private Image failedImage;
	private Image loadingImage;
	private Image unresolvedImage;
	
	private CheckboxTableViewer dependenciesViewer;
	
	private Button deleteJarsBtn;
	
	private boolean deleteJars;

	private Button startIdentification;

	private Button stopButton;

	private Label warningImg;

	private Link warningLink;

	private static String MESSAGE = "Identify existing project references as Maven dependencies. Double-click on a Maven dependency to edit its details";


	public IdentifyMavenDependencyPage(IProject project, List<ProjectDependency> entries) {
		super("");
		this.project = project;
		initialEntries = Collections.unmodifiableList(entries);
		initDependencyMap();
	}

	private void initDependencyMap() {
		dependencyMap = new LinkedHashMap<ProjectDependency, Dependency>(initialEntries.size());
		for (ProjectDependency entry : initialEntries) {
				dependencyMap.put(entry, null);
		}
	}

	public void dispose() {
		if (jarImage != null) jarImage.dispose();
		if (okImage != null) okImage.dispose();
		if (projectImage != null) projectImage.dispose();
		if (failedImage != null) failedImage.dispose();
		if (loadingImage != null) loadingImage.dispose();
		if (unresolvedImage != null) unresolvedImage.dispose();
	}
	
	
	private void initImages() {
		jarImage = MavenDependencyConversionActivator.getJarIcon();
		projectImage = MavenDependencyConversionActivator.getProjectIcon();
		okImage = MavenDependencyConversionActivator.getOkIcon();
		unresolvedImage = MavenDependencyConversionActivator.getWarningIcon();
		failedImage = MavenDependencyConversionActivator.getFailedIcon();
		loadingImage = MavenDependencyConversionActivator.getLoadingIcon();
	}

	private void createWarning(Composite container) {
		warningImg = new Label(container,  SWT.CENTER); 
		warningLink = new Link(container, SWT.NONE);
		warningLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(warningImg);
		warningImg.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
		warningLink.setText("Some selected dependencies can not be resolved. Click <a>here</a> to configure repositories in your settings.xml.");
		warningLink.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
	          openSettingsRepositoriesWizard();
	        }

	        private void openSettingsRepositoriesWizard() {
				ConfigureMavenRepositoriesWizard wizard = new ConfigureMavenRepositoriesWizard();
				WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
				dialog.create();
				dialog.open(); 
			}

			public void widgetDefaultSelected(SelectionEvent e) {
	        	widgetSelected(e);
	        }
	      });
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

		setMessage(MESSAGE);

		createWarning(container);
		
		displayDependenciesTable(container);

		Link remoteRepoPrefsLink = new Link(container, SWT.NONE);
		remoteRepoPrefsLink.setText("Manage <a>remote repositories</a> used to identify dependencies.");
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		remoteRepoPrefsLink.setLayoutData(gd);
		remoteRepoPrefsLink.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
	          openRemoteRepoPrefs();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) {
	        	widgetSelected(e);
	        }
	      });

		deleteJarsBtn = addCheckButton(container, "Delete original references from project", deleteJars);
		deleteJarsBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteJars = deleteJarsBtn.getSelection();
			}
		});

		runIdentificationJobs(null);
	}

	private Button addCheckButton(Composite container, String label,
			boolean selected) {
		Button checkBtn = new Button(container, SWT.CHECK);
		checkBtn.setText(label);
		checkBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		checkBtn.setSelection(selected);
		return checkBtn;
	}

    private void openRemoteRepoPrefs() {
	      String id = "org.jboss.tools.maven.ui.preferences.RemoteRepositoriesPreferencePage";
	      PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] {id}, null).open();
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
		sourceColumn.getColumn().setText("Project Reference");
		sourceColumn.getColumn().setWidth(270);
		sourceColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			@SuppressWarnings("unchecked")
			public String getText(Object element) {
				ProjectDependency projectDependency = (ProjectDependency) element;
				return projectDependency.getPath().lastSegment();
			}
			
			@Override
			public String getToolTipText(Object element) {
				ProjectDependency projectDependency = (ProjectDependency) element;
				if (projectDependency.getDependencyKind() == DependencyKind.Project) {
					return "";
				}
				try {
					return "SHA1 Checksum : "+IdentificationUtil.getSHA1(ConversionUtils.getFile(projectDependency.getPath()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "Unable to compute SHA1 Checksum";
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public Image getImage(Object element) {
				ProjectDependency projectDependency = (ProjectDependency) element;
				Image img;
				if (projectDependency.getDependencyKind() == ProjectDependency.DependencyKind.Archive) {
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
		dependencyColumn.setLabelProvider(new DependencyLabelProvider());

		dependenciesViewer.setContentProvider(ArrayContentProvider.getInstance());
		dependenciesViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				refresh();
			}
		});
		dependenciesViewer.setInput(dependencyMap.keySet());
		dependenciesViewer.setAllChecked(true);

		dependenciesViewer.getTable().addListener(SWT.MouseDoubleClick, new CellListener(dependenciesViewer.getTable()) {
			
			@Override
			protected void handle(int columnIndex, TableItem item) {
				if (columnIndex == DEPENDENCY_COLUMN) {
					ProjectDependency projectDep = (ProjectDependency) item.getData();
					
					IdentificationJob job = identificationJobs.get(projectDep);
					if (Job.RUNNING == job.getState()) {
						return;
					}
					
					Dependency d= dependencyMap.get(projectDep);
					EditDependencyDialog editDependencyDialog = new EditDependencyDialog(getShell());
					editDependencyDialog.setDependency(d);
					if(editDependencyDialog.open() == Window.OK) {
						Dependency newDep = editDependencyDialog.getDependency();
						dependencyMap.put(projectDep,newDep);
						if (!eq(newDep,d)) {
							resolve(projectDep, newDep);
						}
					}
				}
			}

			private boolean eq(Dependency newDep, Dependency d) {
				if (newDep == d) {
					return true;
				}
				if (d == null) {
					return false;
				}
				return newDep.toString().equals(d.toString());
			}
		});
		
		
		addSelectionButton(container, "Select All", true);
		addSelectionButton(container, "Deselect All", false);
		addIdentifyButton(container, "Identify dependencies");
		addStopButton(container, "Stop identification");
	}


	private void resolve(ProjectDependency projectDependency, Dependency d) {
		if (d != null) {
			IdentificationJob job = identificationJobs.get(projectDependency);
			job.setDependency(d);
			job.setRequestedProcess(Task.RESOLUTION_ONLY);
			job.schedule();
		}
	}
	
	public boolean hasNoRunningJobs() {
		for (IdentificationJob job : identificationJobs.values()) {
			if (job.getState() == Job.RUNNING || job.getState() == Job.WAITING){
				return false;
			}
		}
		return true;
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

	private void addStopButton(Composite container, String label) {
		stopButton = new Button(container, SWT.NONE);
		stopButton.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
		stopButton.setText(label);
		stopButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				cancel();
				refresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

	
	private void addIdentifyButton(Composite container, String label) {
		startIdentification = new Button(container, SWT.NONE);
		startIdentification.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
		startIdentification.setText(label);
		startIdentification.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				runIdentificationJobs(null);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}
	
	protected void runIdentificationJobs(IProgressMonitor monitor) {
		
		initJobs();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		for (Map.Entry<ProjectDependency, Dependency> entry : dependencyMap.entrySet()) {
			if (entry.getValue() != null) {
				//don't need to run identification
				//continue;
			}
			IdentificationJob job = identificationJobs.get(entry.getKey());
			if (job != null) {
				job.setProgressGroup(monitor, 1);
				int jobState = job.getState();
				if (jobState == Job.NONE) {
					job.setRequestedProcess(Task.ALL);
					job.schedule();
				}
			}
		}
		refresh();
	}

	private synchronized void refresh() {
		enableIdentificationButtons();
		displayWarning();
		setPageComplete(hasNoRunningJobs());
		//setMessage(MESSAGE);
	}

	private void displayWarning() {
		for (Dependency d : getDependencies()) {
			if (Boolean.FALSE.equals(dependencyResolution.get(d))) {
				setVisible(warningImg, true);
				setVisible(warningLink, true);
				return;
			}
		} 
		setVisible(warningImg, false);
		setVisible(warningLink, false);

	}

	private void setVisible(Control control, boolean visible) {
		if (control != null && !control.isDisposed()) {
			control.setVisible(visible);
		}
	}

	private void enableIdentificationButtons() {
		boolean hasNoRunningJobs = hasNoRunningJobs();
		if (startIdentification != null && !startIdentification.isDisposed()) {
			startIdentification.setEnabled(hasNoRunningJobs);
		}
		if (stopButton != null && !stopButton.isDisposed()) {
			stopButton.setEnabled(!hasNoRunningJobs);
		}
		
		if (dependenciesViewer != null && !dependenciesViewer.getTable().isDisposed()) {
			dependenciesViewer.refresh();
		}
	}
	
    private static String toString(Dependency d) {
		if (d == null) {
			return "Unidentified dependency";
		}
		StringBuilder text = new StringBuilder(d.getGroupId())
		.append(":")
		.append(d.getArtifactId())
		.append(":")
		.append(d.getVersion());
		return text.toString();
	}
    
    private void initJobs() {
    	if (identificationJobs == null) {
    		identificationJobs = new HashMap<ProjectDependency, IdentificationJob>(dependencyMap.size());
    		
    		Table t = dependenciesViewer.getTable();
    		IFileIdentificationManager fileIdentificationManager = new FileIdentificationManager();
    		
    		for (final TableItem item : t.getItems()) {
    			final ProjectDependency projectDep = (ProjectDependency)item.getData();
    			Dependency mavenDep = dependencyMap.get(projectDep);
    			if (mavenDep != null) {
    				//already identified
    				continue;
    			}
    			File jar;
				try {
					final IdentificationJob job;
					if (projectDep.getDependencyKind() == ProjectDependency.DependencyKind.Project) {
						job = new IdentifyProjectJob("Search the Maven coordinates for "+projectDep.getPath(), projectDep.getPath());
					} else if (projectDep.getDependencyKind() == ProjectDependency.DependencyKind.Archive) {
						jar = ConversionUtils.getFile(projectDep.getPath());
						job = new IdentifyJarJob("Search the Maven coordinates for "+jar.getAbsolutePath(), fileIdentificationManager, jar);
					} else {
						job = new DependencyResolutionJob("Resolve the Maven dependency for "+projectDep.getPath());
					}
					
					job.addJobChangeListener(new IJobChangeListener() {
						
						@Override
						public void sleeping(IJobChangeEvent event) {
							//refreshUI();
						}
						
						@Override
						public void scheduled(IJobChangeEvent event) {
							//refreshUI();
						}
						
						@Override
						public void running(IJobChangeEvent event) {
							refreshUI();
						}
						
						@Override
						public void done(IJobChangeEvent event) {
							Dependency d = job.getDependency();
							dependencyMap.put(projectDep, d);
							if (d != null) {
								dependencyResolution.put(d, job.isResolvable());
							}
							refreshUI();
						}
						
						@Override
						public void awake(IJobChangeEvent event) {
							//refreshUI();							
						}
						
						@Override
						public void aboutToRun(IJobChangeEvent event) {
							//refreshUI();
						}
						
						private void refreshUI() {
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									refresh(projectDep);
								}
							});
						}
					});
					identificationJobs.put(projectDep, job);
				} catch (CoreException e) {
					e.printStackTrace();
				}
    		}    		
    	}
    }

	private synchronized void refresh(ProjectDependency key) {
		if (dependenciesViewer == null || dependenciesViewer.getTable().isDisposed()) {
			return;
		}
		//dependenciesViewer.refresh();
		try {
			for (TableItem item : dependenciesViewer.getTable().getItems()) {
				@SuppressWarnings("unchecked")
				final ProjectDependency projectDep = (ProjectDependency)item.getData();
				if (projectDep.equals(key)) {
					dependenciesViewer.refresh(projectDep, false);
					//Don't force check when there's an existing dependency, only uncheck if they're is not.
					if (dependencyMap.get(projectDep) == null) {
						Job job = identificationJobs.get(projectDep);
						if (job != null && job.getState() == Job.NONE) {
							dependenciesViewer.setChecked(projectDep, false);
						}
					}
					setPageComplete(hasNoRunningJobs());
					return;
				}
			}
		} finally {
			displayWarning();
			enableIdentificationButtons();
		}
	}

	public List<Dependency> getDependencies() {
		if (dependenciesViewer == null || dependenciesViewer.getTable().isDisposed()) {
			return Collections.emptyList();
		}
		Object[] selection = dependenciesViewer.getCheckedElements();
		List<Dependency> dependencies = new ArrayList<Dependency>(selection.length);
		for (Object o : selection) {
			ProjectDependency projectDep = (ProjectDependency) o;
			Dependency d = dependencyMap.get(projectDep);
			if (d != null) {
				dependencies.add(d);
			}
		}
		return dependencies;
	}

	public boolean isDeleteJars() {
		return deleteJars;
	}

	private boolean isResolved(Dependency d) {
		if (d == null) {
			return false;
		}
		Boolean resolved = dependencyResolution.get(d);
		return resolved == null? false:resolved.booleanValue();
	}
	
	private class DependencyLabelProvider extends ColumnLabelProvider {
			@Override
			@SuppressWarnings("unchecked")
			public String getText(Object element) {
				ProjectDependency projectDep = (ProjectDependency) element;
				IdentificationJob job = identificationJobs ==null? null:identificationJobs.get(projectDep);
				if (job != null) {
					int jobState = job.getState();
					if (jobState == Job.RUNNING || jobState == Job.WAITING) {
						return "Identification in progress...";
					}
				}
				Dependency d = dependencyMap.get(projectDep);
				return IdentifyMavenDependencyPage.toString(d);
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public Image getImage(Object element) {
				ProjectDependency projectDep = (ProjectDependency) element;
				IdentificationJob job = identificationJobs ==null? null:identificationJobs.get(projectDep);
				if (job != null) {
					int jobState = job.getState();
					if (jobState == Job.RUNNING || jobState == Job.WAITING) {
						return loadingImage;
					}
				}
				
				Dependency d = dependencyMap.get(projectDep);
				
				if (d == null) {
					return failedImage;
				} else {
					Image img;
					if (isResolved(d)) {
						img = okImage;
					} else {
						img = unresolvedImage;
					}
					return img;
				}
			}
	}

	public void cancel() {
		for (IdentificationJob job : identificationJobs.values()) {
			job.cancel();
		}
	}
}
