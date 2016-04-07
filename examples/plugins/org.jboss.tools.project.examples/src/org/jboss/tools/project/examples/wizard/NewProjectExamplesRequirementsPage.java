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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.project.examples.fixes.IProjectExamplesFix;
import org.jboss.tools.project.examples.fixes.ProjectFixManager;
import org.jboss.tools.project.examples.fixes.UIHandler;
import org.jboss.tools.project.examples.internal.Messages;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;

@SuppressWarnings("nls")
public class NewProjectExamplesRequirementsPage extends WizardPage implements IProjectExamplesWizardPage {

	private static final String PAGE_NAME = "org.jboss.tools.project.examples.requirements"; //$NON-NLS-1$
	protected ProjectExampleWorkingCopy projectExample;
	protected Text descriptionText;
	protected Label projectSizeLabel;
	protected Text projectSize;
	protected WizardContext wizardContext;
	protected TableViewer tableViewer;
	protected List<IProjectExamplesFix> fixes = new ArrayList<>();
	private Image fulfilledRequirement;
	private Image missingRecommendation;
	private Image missingRequirement;
	
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
	protected ProjectFixManager fixManager;
	private Composite sizeComposite;
	
	public NewProjectExamplesRequirementsPage(ProjectExampleWorkingCopy projectExample) {
		this(PAGE_NAME, projectExample);
	}

	public NewProjectExamplesRequirementsPage(String pageName, ProjectExampleWorkingCopy projectExample) {
		super(pageName);
		this.projectExample = projectExample;
		setTitleAndDescription(projectExample);
		fulfilledRequirement =  ProjectExamplesActivator.getImageDescriptor("icons/ok.png").createImage();
		missingRecommendation =  ProjectExamplesActivator.getImageDescriptor("icons/warning.gif").createImage();
		missingRequirement = ProjectExamplesActivator.getImageDescriptor("icons/error.gif").createImage();
		fixManager = ProjectExamplesActivator.getDefault().getProjectFixManager();
	}

	public NewProjectExamplesRequirementsPage() {
		this(null);
	}

	protected void setTitleAndDescription(ProjectExample projectExample) {
		setTitle( "Requirements and Recommendations" );
        setDescription( "Project Example Requirements" );
        boolean showSize = false;
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
				if(projectSize != null && projectExample.getSize() > 0) {
					showSize = true;
					projectSize.setText(projectExample.getSizeAsText());
				}
			}
		} else {
	        if (descriptionText != null) {
	        	descriptionText.setText(""); //$NON-NLS-1$
	        	if (projectSize != null) {
	        		projectSize.setText(""); //$NON-NLS-1$
	        	}
	        }
		}
		if(sizeComposite != null) {
			GridData data = (GridData) sizeComposite.getLayoutData();
			data.exclude = !showSize;
			sizeComposite.setVisible(showSize);
			sizeComposite.getParent().pack();
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
		fixesGroup.setText("Requirements and Recommendations");
		
		tableViewer = new TableViewer(fixesGroup, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		Table table = tableViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnNames = new String[] { "Type", "Description", "Status"};
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
		sizeComposite = new Composite(composite, SWT.NONE);
		sizeComposite.setLayout(new GridLayout(2, false));
		
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		sizeComposite.setLayoutData(data);
	
		projectSizeLabel = new Label(sizeComposite,SWT.NULL);
		projectSizeLabel.setText(Messages.NewProjectExamplesWizardPage_Project_size);
		projectSize = new Text(sizeComposite,SWT.READ_ONLY);
		projectSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filler = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 2).applyTo(filler);
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
		install.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IProjectExamplesFix fix = getSelectedProjectFix();
				if (fix != null) {
					UIHandler uiHandler = getUIHandler(fix);
					uiHandler.handleInstallRequest(getShell(), getContainer(), fix);
					refreshFixes();
				}
			}
		});
		
		final Button downloadAndInstall = new Button(buttonComposite, SWT.PUSH);
		downloadAndInstall.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		downloadAndInstall.setText("Download and Install...");
		downloadAndInstall.setEnabled(false);
		
		downloadAndInstall.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final IProjectExamplesFix fix = getSelectedProjectFix();
				if (fix == null) {
					return;
				}
				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						UIHandler uiHandler = fixManager.getUIHandler(fix);
						uiHandler.handleDownloadRequest(Display.getCurrent().getActiveShell(), getContainer(), fix);
						refreshFixes();
					}
			});
		}});
				
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				final IProjectExamplesFix fix = getSelectedProjectFix();
				if (fix == null) {
					return;
				}
				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						IProgressMonitor monitor = new NullProgressMonitor();
						UIHandler uiHandler = getUIHandler(fix);
						uiHandler.decorateInstallButton(install, fix, monitor);
						uiHandler.decorateDownloadButton(downloadAndInstall, fix, monitor);
					}
				});
			}
		});	
		
	}

	protected IProjectExamplesFix getSelectedProjectFix() {
		ISelection sel = tableViewer.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Object object = selection.getFirstElement();
			if (object instanceof IProjectExamplesFix) {
				return (IProjectExamplesFix) object;
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
		fixes.clear();
		if (projectExample != null) {
			fixes.addAll(projectExample.getFixes());
		}
		tableViewer.setInput(fixes);
		validate();
	}

	private class FixLabelProvider extends ColumnLabelProvider {

		private int columnIndex;

		public FixLabelProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public Image getImage(Object element) {
			Image image = null;
			if (columnIndex == 2 && element instanceof IProjectExamplesFix) {
				IProjectExamplesFix fix = (IProjectExamplesFix) element;
				if (fix.isSatisfied()) {
					image = fulfilledRequirement;
				} else {
					image = fix.isRequired()?missingRequirement:missingRecommendation;
				}
			}
			return image;
		}
		
		@Override
		public String getText(Object element) {
			if (element instanceof IProjectExamplesFix) {
				IProjectExamplesFix fix = (IProjectExamplesFix) element;
				if (columnIndex == 0) {
					return fix.getLabel();
				}
				if (columnIndex == 1) {
					return fix.getDescription();
				}
			}
			return null;
		}
		
		@Override
		public Color getForeground(Object element) {
			if (element instanceof IProjectExamplesFix) {
				IProjectExamplesFix fix = (IProjectExamplesFix) element;
				if (isBlocking(fix)) {
					return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
				}
			}
			return super.getForeground(element);
		}
		
		@Override
		public String getToolTipText(Object element) {
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				return null;
			}
			String tooltip = null;
			if (element instanceof IProjectExamplesFix) {
				IProjectExamplesFix fix = (IProjectExamplesFix) element;
				if (columnIndex == 1) {
					tooltip = fix.getDescription();
				} else if (columnIndex == 2 ) {
					String label = fix.getLabel();
					if (fix.isSatisfied()) {
						String type = fix.isRequired()?"required":"recommended";
						tooltip = NLS.bind("This {0} {1} is already installed.", type, label);
					} else {
						if (fix.isRequired()) {
							tooltip = NLS.bind("This {0} must be installed to in order to continue.", label);
						} else {
							tooltip = NLS.bind("Installing this {0} is recommended but not mandatory.", label);
						}
					}
				}
			}
			return tooltip;
		}
	}

	private class FixContentProvider implements IStructuredContentProvider {

		private List<IProjectExamplesFix> fixes;

		public FixContentProvider(List<IProjectExamplesFix> fixes) {
			this.fixes = fixes;
		}

		public Object[] getElements(Object inputElement) {
			return fixes.toArray();
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			fixes = (List<IProjectExamplesFix>) newInput;
		}

	}

	protected UIHandler getUIHandler(IProjectExamplesFix fix) {
		return fixManager.getUIHandler(fix);
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
	}

	@Override
	public boolean finishPage() {
		return true;
	}

	@Override
	public String getProjectExampleType() {
		return ProjectExample.IMPORT_TYPE_ZIP;
	}

	@Override
	public Map<String, Object> getPropertiesMap() {
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
	
	protected void validate() {
		if (fixes != null && !fixes.isEmpty()) {
			for (IProjectExamplesFix fix : fixes) {
				if (isBlocking(fix)) {
					setErrorMessage("Some requirements must be installed in order to proceed");
					setPageComplete(false);
					return;
				}
			}
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

	private boolean isBlocking(IProjectExamplesFix fix) {
		return fix != null && fix.isRequired() && !fix.isSatisfied();
	}
}
