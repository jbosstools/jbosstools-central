/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.foundation.ui.xpl.taskwizard.TaskWizardDialog;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.fixes.WTPRuntimeFix;
import org.jboss.tools.project.examples.internal.discovery.DiscoveryUtil;
import org.jboss.tools.project.examples.internal.discovery.JBossDiscoveryUi;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.ui.RuntimeSharedImages;
import org.jboss.tools.runtime.ui.RuntimeUIActivator;
import org.jboss.tools.runtime.ui.wizard.DownloadRuntimesTaskWizard;

public class NewProjectExamplesRequirementsPage extends WizardPage implements IProjectExamplesWizardPage {

	private static final String PAGE_NAME = "org.jboss.tools.project.examples.requirements"; //$NON-NLS-1$
	protected ProjectExampleWorkingCopy projectExample;
	protected Text descriptionText;
	protected Label projectSizeLabel;
	protected Text projectSize;
	protected WizardContext wizardContext;
	protected TableViewer tableViewer;
	protected List<ProjectFix> fixes = new ArrayList<ProjectFix>();
	protected ArrayList<ProjectFix> unsatisfiedFixes = new ArrayList<ProjectFix>();
	private Image checkboxOn;
	private Image checkboxOff;
	private Link link;
	private IServerLifecycleListener serverListener = new IServerLifecycleListener() {

		private void refreshInUIThread() {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					refreshFixes();
				}
			});
		}

		@Override
		public void serverAdded(IServer server) {
			refreshInUIThread();
		}

		@Override
		public void serverChanged(IServer server) {
			refreshInUIThread();
		}

		@Override
		public void serverRemoved(IServer server) {
			refreshInUIThread();
		}

	};
	
	public NewProjectExamplesRequirementsPage(ProjectExampleWorkingCopy projectExample) {
		this(PAGE_NAME, projectExample);
	}

	public NewProjectExamplesRequirementsPage(String pageName, ProjectExampleWorkingCopy projectExample) {
		super(pageName);
		this.projectExample = projectExample;
		setTitleAndDescription(projectExample);
		checkboxOn =  RuntimeUIActivator.sharedImages().image(RuntimeSharedImages.CHECKBOX_ON_KEY);
		checkboxOff = RuntimeUIActivator.sharedImages().image(RuntimeSharedImages.CHECKBOX_OFF_KEY);
	}

	public NewProjectExamplesRequirementsPage() {
		this(null);
	}

	protected void setTitleAndDescription(ProjectExample projectExample) {
		setTitle( "Requirements" );
        setDescription( "Project Example Requirements" );
		if (projectExample != null) {
			if (projectExample.getShortDescription() != null) {
				setTitle(projectExample.getShortDescription());
			}
			if (projectExample.getHeadLine() != null) {
				setDescription(ProjectExamplesActivator.getShortDescription(projectExample.getHeadLine()));
			}
			if (descriptionText != null) {
				if (projectExample.getDescription() != null) {
					descriptionText.setText(projectExample.getDescription());
				}
				if (projectExample.getSizeAsText() != null && projectSize != null) {
					projectSize.setText(projectExample.getSizeAsText());
				}
			}
		} else {
			setTitle( "Requirements" );
	        setDescription( "Project Example Requirements" );
	        if (descriptionText != null) {
	        	descriptionText.setText(""); //$NON-NLS-1$
	        	if (projectSize != null) {
	        		projectSize.setText(""); //$NON-NLS-1$
	        	}
	        }
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2,false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(gd);
		Dialog.applyDialogFont(composite);

		//Set description
		setDescriptionArea(composite);
		
		//Set project size label or runtime/archetype selection
		setSelectionArea(composite);
				
		Group fixesGroup = new Group(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		fixesGroup.setLayoutData(gd);
		GridLayout layout = new GridLayout(2, false);
		fixesGroup.setLayout(layout);
		fixesGroup.setText("Requirements");
		
		tableViewer = new TableViewer(fixesGroup, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		Table table = tableViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnNames = new String[] { "Type", "Description", "Found?"};
		int[] columnWidths = new int[] { 100, 300, 50};
		
		for (int i = 0; i < columnNames.length; i++) {
			TableViewerColumn tc = new TableViewerColumn(tableViewer, SWT.LEFT);
			tc.getColumn().setText(columnNames[i]);
			tc.getColumn().setWidth(columnWidths[i]);
			tc.setLabelProvider(new FixLabelProvider(i));
		}

		tableViewer.setContentProvider(new FixContentProvider(fixes));
		
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		
		createButtons(fixesGroup, tableViewer);
		
		link = new Link(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan=2;
		link.setLayoutData(gd);
		
		setAdditionalControls(composite);
		
		setPageComplete(true);
		setControl(composite);
		if (projectExample != null) {
			setProjectExample(projectExample);
		}
		ServerCore.addServerLifecycleListener(serverListener);
	}

	@Override
	public void dispose() {
		ServerCore.removeServerLifecycleListener(serverListener);
		super.dispose();
	}

	protected void setDescriptionArea(Composite composite) {
		Label descriptionLabel = new Label(composite,SWT.NONE);
		descriptionLabel.setText(Messages.NewProjectExamplesWizardPage_Description);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		gd.horizontalSpan = 2;
		descriptionLabel.setLayoutData(gd);
		descriptionText = new Text(composite, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		GC gc = new GC(composite.getParent());
		gd.heightHint = Dialog.convertHeightInCharsToPixels(gc
				.getFontMetrics(), 6);
		gc.dispose();
		gd.horizontalSpan = 2;
		gd.widthHint = 250;
		descriptionText.setLayoutData(gd);
	}
	
	protected void setSelectionArea(Composite composite) {
		projectSizeLabel = new Label(composite,SWT.NULL);
		projectSizeLabel.setText(Messages.NewProjectExamplesWizardPage_Project_size);
		projectSize = new Text(composite,SWT.READ_ONLY);
		projectSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
	}
	
	protected void setAdditionalControls(Composite composite) {
	}

	private void createButtons(Composite parent, final TableViewer viewer) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1,false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		
		final Button install = new Button(buttonComposite, SWT.PUSH);
		install.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		install.setText("Install...");
		install.setEnabled(false);
		install.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				ProjectFix fix = getSelectedProjectFix();
				if (ProjectFix.WTP_RUNTIME.equals(fix.getType())
						|| ProjectFix.SEAM_RUNTIME.equals(fix.getType())) {
					String preferenceId = "org.jboss.tools.runtime.preferences.RuntimePreferencePage"; //$NON-NLS-1$
					PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(getShell(), preferenceId, null, null);
					preferenceDialog.open();
					refreshFixes();
				} else if (ProjectFix.PLUGIN_TYPE.equals(fix.getType())) {
					String connectorId = fix.getProperties().get(ProjectFix.CONNECTOR_ID);
					Set<String> connectorIds = new HashSet<String>();
					if (connectorId != null) {
						String[] ids = connectorId.split(","); //$NON-NLS-1$
						for (String id:ids) {
							if (id != null && !id.trim().isEmpty()) {
								connectorIds.add(id.trim());
							}
						}
					}
					if (connectorIds.size() > 0) {
						try {
							install(connectorIds);
						} catch (Exception e1) {
							ProjectExamplesActivator.log(e1);
						}
					}
					refreshFixes();
				}
				
			}
		
		});
		
//		final Button p2install = new Button(buttonComposite, SWT.PUSH);
//		p2install.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		p2install.setText("Install New Software...");
//		p2install.setToolTipText("P2 Install New Software");
//		p2install.addSelectionListener(new SelectionListener(){
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				
//			}
//
//			public void widgetSelected(SelectionEvent e) {
//				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
//		        try {
//		          handlerService.executeCommand("org.eclipse.equinox.p2.ui.sdk.install", new Event()); //$NON-NLS-1$
//		        }
//		        catch (Exception e1) {
//		        	ProjectExamplesActivator.log(e1);
//		        }
//				refreshFixes();
//			}
//		
//		});
		
		final Button downloadAndInstall = new Button(buttonComposite, SWT.PUSH);
		downloadAndInstall.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		downloadAndInstall.setText("Download and Install...");
		downloadAndInstall.setEnabled(false);
		
		downloadAndInstall.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				ProjectFix fix = getSelectedProjectFix();
				if (fix != null) {
					List<DownloadRuntime> runtimes = getDownloadRuntimes(fix);
					WizardDialog dialog = new TaskWizardDialog(getShell(), new DownloadRuntimesTaskWizard(runtimes));
					dialog.open();
					refreshFixes();
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
				
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = viewer.getSelection();
				install.setEnabled(false);
				downloadAndInstall.setEnabled(false);
				downloadAndInstall.setToolTipText("");
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof ProjectFix) {
						ProjectFix fix = (ProjectFix) object;
						String fixType = fix.getType();
						if (!unsatisfiedFixes.contains(fix) && !(ProjectFix.WTP_RUNTIME.equals(fixType)
								|| ProjectFix.SEAM_RUNTIME.equals(fixType))) {
							return;
						}
						if (ProjectFix.WTP_RUNTIME.equals(fixType)
								|| ProjectFix.SEAM_RUNTIME.equals(fixType)) {
							List<DownloadRuntime> downloadRuntimes = getDownloadRuntimes(fix);
							if (downloadRuntimes != null) {
								for (Iterator iterator = downloadRuntimes.iterator(); iterator.hasNext();) {
									DownloadRuntime downloadRuntime = (DownloadRuntime) iterator.next();
									if (downloadRuntime == null) {
										iterator.remove();
									}
								}
							}
							if (downloadRuntimes != null && !downloadRuntimes.isEmpty()) {
								StringBuilder tooltip = new StringBuilder("Download and install ");
								if (downloadRuntimes.size() > 1 && downloadRuntimes.get(0) != null && downloadRuntimes.get(0).getName() != null) {
									tooltip.append("a runtime");
								} else {
									tooltip.append(downloadRuntimes.get(0).getName());
								}
								downloadAndInstall.setToolTipText(tooltip.toString());
								downloadAndInstall.setEnabled(true);
							} else {
								downloadAndInstall.setEnabled(false);
							}
							install.setEnabled(true);
							install.setToolTipText("JBoss Runtime Detection");
						} else if (ProjectFix.PLUGIN_TYPE.equals(fixType)) {
							install.setEnabled(fix.getProperties().get(ProjectFix.CONNECTOR_ID) != null);
							install.setToolTipText("Install required feature(s)");
						}
							
					}
				} 
			}
		});	
		
	}

	protected ProjectFix getSelectedProjectFix() {
		ISelection sel = tableViewer.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Object object = selection.getFirstElement();
			if (object instanceof ProjectFix) {
				return (ProjectFix) object;
			}
		}
		return null;
	}

	protected List<DownloadRuntime> getDownloadRuntimes(ProjectFix fix) {
		if (ProjectFix.WTP_RUNTIME.equals(fix.getType())) {
			IProgressMonitor monitor = new NullProgressMonitor();
			String allDownloadRuntimeIds = fix.getProperties().get(ProjectFix.ALLOWED_TYPES);
			if (allDownloadRuntimeIds != null && !ProjectFix.ANY.equals(allDownloadRuntimeIds)) {
				DownloadRuntime[] downloadableRuntimes = WTPRuntimeFix.getDownloadRuntimesFromPattern(allDownloadRuntimeIds, monitor);
				if (downloadableRuntimes != null && downloadableRuntimes.length > 0) {
					return Arrays.asList(downloadableRuntimes);
				}
			}
		}
		
        //Fall back on legacy stuff		
		final String downloadId = fix.getProperties().get(ProjectFix.DOWNLOAD_ID);
		if (downloadId != null) {
			DownloadRuntime dr = RuntimeCoreActivator.getDefault().getDownloadRuntimes().get(downloadId);
			if (dr != null) {
				return Collections.singletonList(dr);
			}
		}
		return null;
	}
	
	public ProjectExample getProjectExample() {
		return projectExample;
	}

	public void setProjectExample(final ProjectExampleWorkingCopy projectExample) {
		this.projectExample = projectExample;
		setTitleAndDescription(projectExample);
		refreshFixes();
		
		if (link == null) {
			return;
		}
		if (projectExample != null && projectExample.getSourceLocation() != null && !projectExample.getSourceLocation().isEmpty()) {
			link.setVisible(true);
			link.setText("Found a bug? Or have improvements to this example? Help us develop it, source can be found at <a>Project Example Source</a>");
			link.getParent().pack(true);
			link.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					String text = e.text;
					if ("Project Example Source".equals(text)) {
						IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
								.getBrowserSupport();
						try {
							URL url = new URL(projectExample.getSourceLocation());
							support.getExternalBrowser().openURL(url);
						} catch (Exception e1) {
							ProjectExamplesActivator.log(e1);
						}
					}
				}
			} );
		} else {
			link.setVisible(false);
			link.setText(""); //$NON-NLS-1$
			link.getParent().pack(true);
		}
	}
	
	protected void refreshFixes() {
		if (getControl() == null || getControl().isDisposed()) {
			return;
		}
		fixes = new ArrayList<ProjectFix>();
		unsatisfiedFixes = new ArrayList<ProjectFix>();
		if (projectExample == null) {
			return;
		}
		
		fixes = projectExample.getFixes();
		projectExample.setUnsatisfiedFixes(unsatisfiedFixes);
		for (ProjectFix fix:fixes) {
			if (!ProjectExamplesActivator.canFix(projectExample, fix)) {
				unsatisfiedFixes.add(fix);
			}
		}
		tableViewer.setInput(fixes);
	}

	private String getProjectFixDescription(ProjectFix projectFix) {
		
		return projectFix.getProperties().get(ProjectFix.DESCRIPTION);
	}
	
	protected void install(final Set<String> connectorIds) throws InvocationTargetException, InterruptedException {
		final IStatus[] results = new IStatus[1];
		final ConnectorDiscovery[] connectorDiscoveries = new ConnectorDiscovery[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ConnectorDiscovery connectorDiscovery = DiscoveryUtil.createConnectorDiscovery();
				connectorDiscoveries[0] = connectorDiscovery;
				results[0] = connectorDiscoveries[0].performDiscovery(monitor);
				if (monitor.isCanceled()) {
					results[0] = Status.CANCEL_STATUS;
				}
			}
		};
		getWizard().getContainer().run(true, true, runnable);
		if (results[0] == null) {
			return;
		}
		if (results[0].isOK()) {
			List<DiscoveryConnector> connectors = connectorDiscoveries[0].getConnectors();
			List<ConnectorDescriptor> installableConnectors = new ArrayList<ConnectorDescriptor>();
			for (DiscoveryConnector connector:connectors) {
				if (connectorIds.contains(connector.getId())) {
					installableConnectors.add(connector);
				}
			}
			JBossDiscoveryUi.install(installableConnectors, getWizard().getContainer());
		} else {
			String message = results[0].toString();
			switch (results[0].getSeverity()) {
			case IStatus.ERROR:	
				MessageDialog.openError(getShell(), "Error", message);
				break;
			case IStatus.WARNING:
				MessageDialog.openWarning(getShell(), "Warning", message);
				break;
			case IStatus.INFO:
				MessageDialog.openInformation(getShell(), "Information", message);
				break;
			}
		}
	}
	
	private class FixLabelProvider extends ColumnLabelProvider {

		private int columnIndex;

		public FixLabelProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public boolean useNativeToolTip(Object object) {
			return super.useNativeToolTip(object);
		}

		@Override
		public Image getImage(Object element) {
			if (columnIndex == 2 && element instanceof ProjectFix) {
				ProjectFix fix = (ProjectFix) element;
				if (!unsatisfiedFixes.contains(fix)) {
					return checkboxOn;
				} else {
					return checkboxOff;
				}
				
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ProjectFix) {
				ProjectFix fix = (ProjectFix) element;
				if (columnIndex == 0) {
					if (ProjectFix.WTP_RUNTIME.equals(fix.getType())) {
						return "server/runtime";
					}
					return fix.getType();
				}
				if (columnIndex == 1) {
					return getProjectFixDescription(fix);
				}
				
			}
			return null;
		}
		
		@Override
		public String getToolTipText(Object element) {
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				return null;
			}
			if (element instanceof ProjectFix && columnIndex == 1) {
				return getProjectFixDescription((ProjectFix) element);
			}
			return null;
		}
	}

	private class FixContentProvider implements IStructuredContentProvider {

		private List<ProjectFix> fixes;

		public FixContentProvider(List<ProjectFix> fixes) {
			this.fixes = fixes;
		}

		public Object[] getElements(Object inputElement) {
			return fixes.toArray();
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			fixes = (List<ProjectFix>) newInput;
		}

	}


	public void setDescriptionText(String longDescription) {
		if (descriptionText != null) {
			if (longDescription == null) {
				longDescription = "";
			}
			if (!longDescription.equals(descriptionText.getText())) {
				//only change text if necessary to avoid flickering
				descriptionText.setText(longDescription);
			}
		}
	}
	
	@Override
	public IWizardPage getNextPage() {
		// FIXME
		if (projectExample != null) {
			IWizard wizard = getWizard();
			if (wizard instanceof NewProjectExamplesWizard2) {
				NewProjectExamplesWizard2 exampleWizard = ((NewProjectExamplesWizard2)wizard); 
				ProjectExample projectExample = exampleWizard.getSelectedProjectExample();
				if (projectExample != null && projectExample.getImportType() != null) {
					for (IProjectExamplesWizardPage page: exampleWizard.getContributedPages("extra")) {
						if (projectExample.getImportType().equals(page.getProjectExampleType())) {
							return page;
						}
					}
				}
				return ((NewProjectExamplesWizard2)wizard).getLocationsPage();
			}
		}
		return super.getNextPage();
	}

	@Override
	public void onWizardContextChange(String key, Object value) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean finishPage() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getProjectExampleType() {
		return ProjectExample.IMPORT_TYPE_ZIP;
	}

	@Override
	public Map<String, Object> getPropertiesMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWizardContext(WizardContext context) {
		this.wizardContext = context;
	}

	@Override
	public String getPageType() {
		return "requirement";
	}

}
