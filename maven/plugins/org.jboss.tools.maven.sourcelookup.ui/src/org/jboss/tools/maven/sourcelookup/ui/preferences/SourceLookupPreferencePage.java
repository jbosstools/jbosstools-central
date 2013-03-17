/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.ui.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.jws.soap.SOAPBinding;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.internal.util.SourceLookupUtil;
import org.jboss.tools.maven.sourcelookup.ui.SourceLookupUIActivator;

/**
 * 
 * @author snjeza
 * 
 */
public class SourceLookupPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button autoAddButton;
	
	private Button searchButton;

	private IProject searchProject;

	private Text includePatternText;

	private Text excludePatternText;

	private CheckboxTableViewer serversViewer;

	private List<IServer> servers;

	private Button selectAllButton;

	private Button deselectAllButton;

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
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 1;
		autoAddButton.setLayoutData(gd);
		autoAddButton.setSelection(SourceLookupActivator.getDefault()
				.isAutoAddSourceContainer());
		autoAddButton.setText("Automatically add the JBoss Maven source container to all JBoss AS launch configurations");

		Label includePatternLabel = new Label(composite, SWT.NONE);
		includePatternLabel.setText("Include pattern:");
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		includePatternLabel.setLayoutData(gd);
		
		includePatternText = new Text(composite, SWT.MULTI | SWT.WRAP| SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL,true,false);
		gd.heightHint = 50;
		includePatternText.setLayoutData(gd);
		includePatternText.setText(SourceLookupActivator.getDefault().getIncludePattern());
		
		Label excludePatternLabel = new Label(composite, SWT.NONE);
		excludePatternLabel.setText("Exclude pattern:");
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		excludePatternLabel.setLayoutData(gd);
		
		excludePatternText = new Text(composite, SWT.MULTI | SWT.WRAP| SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL,true,false);
		gd.heightHint = 50;
		excludePatternText.setLayoutData(gd);
		excludePatternText.setText(SourceLookupActivator.getDefault().getExcludePattern());
		
		includePatternText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		includePatternText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		searchButton = new Button(composite, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 1;
		searchButton.setLayoutData(gd);
		try {
			searchProject = SourceLookupUtil.getProject(new NullProgressMonitor());
			searchButton.setSelection(searchProject.exists());
		} catch (CoreException e) {
			SourceLookupUIActivator.log(e);
			searchButton.setEnabled(false);
		}
		searchButton.setText("Include the following JBoss Servers in the Java Search");
		
		final Group serversGroup = new Group(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		serversGroup.setLayoutData(gd);
		layout = new GridLayout(1, false);
		serversGroup.setLayout(layout);
		serversGroup.setText("Servers");
		serversViewer = CheckboxTableViewer
				.newCheckList(serversGroup, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		Table table = serversViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnHeaders = new String[] { "Name", "Location" };
		
		ColumnLayoutData[] layouts = {
				new ColumnWeightData(200,200),
				new ColumnWeightData(200,200),
			};

		TableLayout tableLayout = new AutoResizeTableLayout(table);
		for (int i = 0; i < layouts.length; i++) {
			tableLayout.addColumnData(layouts[i]);
		}
		
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn column = new TableViewerColumn(serversViewer, SWT.NONE);
			column.setLabelProvider(new ServersLabelProvider(i));
			column.getColumn().setText(columnHeaders[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			
		}
		
		serversViewer.setContentProvider(new ServersContentProvider());

		serversViewer.setInput(getServers());
		
		createServerButtons(serversGroup);
		
		enableServers();
		searchButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableServers();
			}
		
		});
		List<IServer> enabledServers = SourceLookupUtil.getServers();
		
		for (IServer server : servers) {
			serversViewer.setChecked(server, enabledServers.contains(server));
		}
		return composite;
	}

	public void enableServers() {
		boolean enabled = searchButton.getSelection();
		serversViewer.getTable().setEnabled(enabled);
		selectAllButton.setEnabled(enabled);
		deselectAllButton.setEnabled(enabled);
	}
	
	private void createServerButtons(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		buttonComposite.setLayoutData(gd);
		
		selectAllButton = new Button(buttonComposite, SWT.PUSH);
		gd = new GridData(SWT.END, SWT.TOP, false, false);
		selectAllButton.setLayoutData(gd);
		selectAllButton.setText("Select All");
		selectAllButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				serversViewer.setAllChecked(true);
			}
		});
		
		deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		gd = new GridData(SWT.END, SWT.TOP, false, false);
		deselectAllButton.setLayoutData(gd);
		deselectAllButton.setText("Deselect All");
		deselectAllButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				serversViewer.setAllChecked(true);
			}
		});
	}
	
	private Object getServers() {
		IServer[] servers = ServerCore.getServers();
		List<IServer> jbossServers = new ArrayList<IServer>();
		for (IServer server:servers) {
			if (server != null) {
				try {
					IJBossServer jbossServer = ServerConverter
							.checkedGetJBossServer(server);
					if (jbossServer != null) {
						jbossServers.add(server);
					}
				} catch (CoreException e) {
					SourceLookupActivator.log(e);
				}
			}
		}
		this.servers = jbossServers;
		return jbossServers;
	}

	protected void validate() {
		setErrorMessage(null);
		try {
			if (!includePatternText.getText().isEmpty()) {
				Pattern.compile(includePatternText.getText());
			}
			if (!excludePatternText.getText().isEmpty()) {
				Pattern.compile(excludePatternText.getText());
			}
		} catch (Exception e) {
			setErrorMessage(e.getMessage());
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void performApply() {
		IEclipsePreferences preferences = SourceLookupActivator.getPreferences();
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				autoAddButton.getSelection());
		preferences.put(SourceLookupActivator.INCLUDE_PATTERN, includePatternText.getText());
		preferences.put(SourceLookupActivator.EXCLUDE_PATTERN, excludePatternText.getText());
		
		boolean needsUpdate = false;
		if (searchButton.getSelection()) {
			String serverPreferences = SourceLookupActivator.getDefault()
					.getSearchServers();
			List<IServer> enabledServers = new ArrayList<IServer>();
			for (IServer server : servers) {
				if (serversViewer.getChecked(server)) {
					enabledServers.add(server);
				}
			}
			String serversIds = SourceLookupUtil.getServerIds(enabledServers
					.toArray(new IServer[0]));
			if (serverPreferences == null
					|| !serverPreferences.equals(serversIds)) {
				preferences.put(SourceLookupActivator.SEARCH_SERVERS,
						serversIds);
				needsUpdate = true;
			}
		}
		SourceLookupActivator.getDefault().savePreferences();
		
		WorkspaceJob job = null;
		
		if (searchProject.exists() && !searchButton.getSelection()) {
			job = getDeleteProjectJob();
		}
		if (!searchProject.exists() && searchButton.getSelection()) {
			job = new WorkspaceJob("Creating the '" + SourceLookupUtil.SEARCH_PROJECT_NAME + "' project ...") {
				
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					searchProject = SourceLookupUtil.createProject(monitor);
					return Status.OK_STATUS;
				}
			};
		}
		if (job == null && needsUpdate) {
			job = new WorkspaceJob("Updating the classpath ...") {
				
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					SourceLookupUtil.updateClasspath();
					return Status.OK_STATUS;
				}
			};
		}
		if (job != null) {
			job.setUser(true);
			job.schedule();
		}
		
	}

	public WorkspaceJob getDeleteProjectJob() {
		WorkspaceJob job;
		job = new WorkspaceJob("Deleting '" + SourceLookupUtil.SEARCH_PROJECT_NAME + "' project...") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				SourceLookupUtil.deleteProject(searchProject, monitor);
				return Status.OK_STATUS;
			}
		};
		return job;
	}

	@Override
	protected void performDefaults() {
		IEclipsePreferences preferences = SourceLookupActivator.getPreferences();
		autoAddButton
				.setSelection(SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
		preferences.putBoolean(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER,
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_CONTAINER_DEFAULT);
		preferences.put(SourceLookupActivator.INCLUDE_PATTERN, SourceLookupActivator.INCLUDE_PATTERN_DEFAULT);
		includePatternText.setText(SourceLookupActivator.INCLUDE_PATTERN_DEFAULT);
		preferences.put(SourceLookupActivator.EXCLUDE_PATTERN, SourceLookupActivator.EXCLUDE_PATTERN_DEFAULT);
		excludePatternText.setText(SourceLookupActivator.EXCLUDE_PATTERN_DEFAULT);
		try {
			if (SourceLookupUtil.getProject(new NullProgressMonitor()).exists()) {
				WorkspaceJob job = getDeleteProjectJob();
				job.setUser(true);
				job.schedule();
			}
		} catch (CoreException e) {
			SourceLookupUIActivator.log(e);
		}
		searchButton.setSelection(false);
		serversViewer.setAllChecked(false);
		preferences.put(SourceLookupActivator.SEARCH_SERVERS, SourceLookupActivator.SEARCH_SERVERS_VALUE);
		enableServers();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
	
	private class ServersContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return servers.toArray(new IServer[0]);
		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

	}

	private class ServersLabelProvider extends ColumnLabelProvider {

		private int columnIndex;

		public ServersLabelProvider(int columnIndex) {
			super();
			this.columnIndex = columnIndex;
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			if (!(element instanceof IServer)) {
				return null;
			}
			IServer server = (IServer) element;
			switch (columnIndex) {
			case 0:
				return server.getName();
			
			case 1:
				IRuntime rt = server.getRuntime();
				if( rt != null ) {
					return rt.getLocation().toOSString();
				}
				return ""; //$NON-NLS-1$
			
			default:
				break;
			}
			return null;
		}
		
	}
}
