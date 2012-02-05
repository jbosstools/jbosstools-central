/*************************************************************************************
 * Copyright (c) 2010-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.mylyn.internal.discovery.core.model.BundleDiscoveryStrategy;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.core.model.RemoteBundleDiscoveryStrategy;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.actions.JBossRuntimeDetectionPreferencesHandler;
import org.jboss.tools.central.editors.DescriptionToolTip;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.ui.actions.DownloadRuntimeAction;

/**
 * @author snjeza
 * 
 */
public class ProjectExamplesDialog extends FormDialog implements IRunnableContext {
	
	private ProjectExample tutorial;
	private FormToolkit toolkit;
	private ScrolledForm form;
	private Composite fixesComposite;
	private Section reqSection;
	private ProgressMonitorPart fProgressMonitorPart;
	private Control fLastControl;
	private Set<Button> controls = new HashSet<Button>();
	
	public ProjectExamplesDialog(Shell parentShell, ProjectExample project) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
				| SWT.RESIZE | getDefaultOrientation());
		this.tutorial = project;
		//setHelpAvailable(false);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		mform.getForm().setText(tutorial.getDescription());
		form = mform.getForm();
		toolkit = mform.getToolkit();
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		form.getBody().setLayout(new GridLayout());
		form.setText(tutorial.getShortDescription());
		Section descSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR|ExpandableComposite.EXPANDED);
		descSection.setText("Description");
	    descSection.setLayout(new GridLayout());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    descSection.setLayoutData(gd);
		Text text = new Text(descSection, SWT.READ_ONLY | SWT.WRAP);
		text.setText(JBossCentralActivator.getDefault().getDescription(tutorial));
		toolkit.adapt(text, false, false);
		gd = new GridData(GridData.FILL, GridData.FILL, false, false);
	    text.setLayoutData(gd);
		descSection.setClient(text);
		
		reqSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED);
		reqSection.setText("Requirements");
	    reqSection.setLayout(new GridLayout());
	    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    reqSection.setLayoutData(gd);
		
		fixesComposite = toolkit.createComposite(reqSection);
		fixesComposite.setLayout(new GridLayout(3, false));
		fixesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		reqSection.setClient(fixesComposite);
		
		refreshFixes();
		
		mform.getForm().setBackgroundImage(null);
		mform.getToolkit().decorateFormHeading(mform.getForm().getForm());
		mform.getForm().setImage(
				JBossCentralActivator.getDefault().getImage(
						"/icons/examples_wiz.gif"));
		getShell().setText("Project Example");
		form.getBody().pack();
	}

	protected void refreshFixes() {
		controls.clear();
		addButtons();
		
		List<ProjectFix> fixes = tutorial.getFixes();
		List<ProjectFix> unsatisfiedFixes = new ArrayList<ProjectFix>();
		tutorial.setUnsatisfiedFixes(unsatisfiedFixes);
		for (ProjectFix fix:fixes) {
			if (!ProjectExamplesActivator.canFix(tutorial, fix)) {
				unsatisfiedFixes.add(fix);
			}
		}
		fixes = tutorial.getUnsatisfiedFixes();
		disposeChildren(fixesComposite);
		reqSection.setVisible(fixes.size() > 0);
		if (fixes.size() > 0) {
			for (ProjectFix projectFix : fixes) {
				if (ProjectFix.WTP_RUNTIME.equals(projectFix.getType())
						|| ProjectFix.SEAM_RUNTIME.equals(projectFix.getType())) {
					addDescription(projectFix);
					addRuntimeFixActions(projectFix);
				}
			}
			Set<String> connectorIds = new HashSet<String>();
			boolean havePluginFix = false;
			for (ProjectFix projectFix : fixes) {
				if (ProjectFix.PLUGIN_TYPE.equals(projectFix.getType())) {
					if (havePluginFix) {
						new Label(fixesComposite, SWT.NONE);
						new Label(fixesComposite, SWT.NONE);
					}
					addDescription(projectFix);
					havePluginFix = true;
					String connectorId = projectFix.getProperties().get(ProjectFix.CONNECTOR_ID);
					if (connectorId != null) {
						String[] ids = connectorId.split(",");
						for (String id:ids) {
							if (id != null && !id.trim().isEmpty()) {
								connectorIds.add(id.trim());
							}
						}
					}
				}
			}
			if (havePluginFix) {
				addPluginFixActions(connectorIds);
			} else {
				new Label(fixesComposite, SWT.NONE);
				new Label(fixesComposite, SWT.NONE);
			}
		}
	}

	protected void addButtons() {
		Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
		if (getButton(IDialogConstants.CANCEL_ID) != null) {
			controls.add(cancelButton);
			controls.add(getButton(IDialogConstants.OK_ID));
		}
	}

	private void disposeChildren(Composite composite) {
		Control[] children = composite.getChildren();
		for (Control child : children) {
			if (child instanceof Composite) {
				disposeChildren((Composite) child);
				child.dispose();
			} else {
				child.dispose();
			}
		}
	}

	private void addDescription(ProjectFix projectFix) {
		FormText fixDescriptionText = toolkit.createFormText(fixesComposite, true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		fixDescriptionText.setLayoutData(gd);
		StringBuffer buffer = new StringBuffer();
		buffer.append(JBossCentralActivator.FORM_START_TAG);
		buffer.append("<img href=\"image\"/> ");
		String description = projectFix.getProperties().get(ProjectFix.DESCRIPTION);
		description = StringEscapeUtils.escapeHtml(description);
		buffer.append(description);
		buffer.append(JBossCentralActivator.FORM_END_TAG);
		fixDescriptionText.setText(buffer.toString(), true, false);
		if (ProjectFix.WTP_RUNTIME.equals(projectFix.getType())) {
			fixDescriptionText.setImage("image", JBossCentralActivator.getDefault().getImage("/icons/wtp_server.gif"));
		} else if (ProjectFix.SEAM_RUNTIME.equals(projectFix.getType())) {
			fixDescriptionText.setImage("image", JBossCentralActivator.getDefault().getImage("/icons/seam16.png"));
		} if (ProjectFix.PLUGIN_TYPE.equals(projectFix.getType())) {
			fixDescriptionText.setImage("image", JBossCentralActivator.getDefault().getImage("/icons/software.png"));
		}
	}

	protected void addRuntimeFixActions(ProjectFix projectFix) {
		Button install = toolkit.createButton(fixesComposite, "Install", SWT.PUSH);
		GridData gd = new GridData(SWT.FILL, SWT.END, false, false);
		install.setLayoutData(gd);
		ToolTip tooltip = new DescriptionToolTip(install, "JBoss Runtime Detection");
		tooltip.activate();
		//install.setImage(JBossCentralActivator.getDefault().getImage("/icons/search_local.png"));
		install.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					new JBossRuntimeDetectionPreferencesHandler().execute(new ExecutionEvent());
				} catch (ExecutionException e1) {
					JBossCentralActivator.log(e1);
				}
				refresh();
			}
		});
		controls.add(install);
		final String downloadId = projectFix.getProperties().get(ProjectFix.DOWNLOAD_ID);
		boolean haveDownloadId = false;
		if (downloadId != null) {
			DownloadRuntime downloadRuntime = RuntimeCoreActivator.getDefault().getDownloadRuntimes().get(downloadId);
			if (downloadRuntime != null) {
				haveDownloadId = true;
				Button download = toolkit.createButton(fixesComposite, "Download and Install...", SWT.PUSH);
				gd = new GridData(SWT.FILL, SWT.END, false, false);
				download.setLayoutData(gd);
				ToolTip tip = new DescriptionToolTip(download, "Download and install " + downloadRuntime.getName());
				tip.activate();
				//download.setImage(JBossCentralActivator.getDefault().getImage("/icons/repository-submit.gif"));
				controls.add(download);
				download.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						new DownloadRuntimeAction(downloadId).run();
						refresh();
					}
				});

			}
		}
		if (!haveDownloadId) {
			new Label(fixesComposite, SWT.NONE);
		}
	}

	protected void addPluginFixActions(final Set<String> connectorIds) {
		if (connectorIds.size() > 0) {
			Button install = toolkit.createButton(fixesComposite, "Install", SWT.PUSH);
			ToolTip tooltip = new DescriptionToolTip(install, "Install required feature(s)");
			tooltip.activate();
			//install.setImage(JBossCentralActivator.getDefault().getImage("/icons/software.png"));
			install.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						install(connectorIds);
					} catch (Exception e1) {
						// FIXME
						JBossCentralActivator.log(e1);
					}
					refresh();
				}
			});
			controls.add(install);
		}
		Button p2install = toolkit.createButton(fixesComposite, "Install New Software", SWT.PUSH);
		ToolTip tip = new DescriptionToolTip(p2install, "P2 Install New Software");
		tip.activate();
		//p2install.setImage(JBossCentralActivator.getDefault().getImage("/icons/update.gif"));
		
		p2install.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
		        try {
		          handlerService.executeCommand("org.eclipse.equinox.p2.ui.sdk.install", new Event());
		        }
		        catch (Exception e1) {
		        	JBossCentralActivator.log(e1);
		        }
				refresh();
			}
		});
		controls.add(p2install);
		if (connectorIds.size() == 0) {
			new Label(fixesComposite, SWT.NONE);
		}
	}

	protected void install(final Set<String> connectorIds) throws InvocationTargetException, InterruptedException {
		final IStatus[] results = new IStatus[1];
		final ConnectorDiscovery[] connectorDiscoveries = new ConnectorDiscovery[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				connectorDiscoveries[0] = new ConnectorDiscovery();

				// look for descriptors from installed bundles
				connectorDiscoveries[0].getDiscoveryStrategies().add(new BundleDiscoveryStrategy());

				RemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new RemoteBundleDiscoveryStrategy();
				remoteDiscoveryStrategy.setDirectoryUrl(JBossCentralActivator.getDefault().getConfigurator().getJBossDiscoveryDirectory());
				connectorDiscoveries[0].getDiscoveryStrategies().add(remoteDiscoveryStrategy);

				connectorDiscoveries[0].setEnvironment(JBossCentralActivator.getEnvironment());
				connectorDiscoveries[0].setVerifyUpdateSiteAvailability(true);
				results[0] = connectorDiscoveries[0].performDiscovery(monitor);
				if (monitor.isCanceled()) {
					results[0] = Status.CANCEL_STATUS;
				}
			}
		};
		run(true, true, runnable);
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
			DiscoveryUi.install(installableConnectors, ProjectExamplesDialog.this);
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

	@Override
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		//ModalContext.run(runnable, cancelable, monitor, getDisplay());
		if (getShell() != null && getShell().isVisible()) {
			// Save focus control
			fLastControl = getShell().getDisplay().getFocusControl();
			if (fLastControl != null && fLastControl.getShell() != getShell()) {
				fLastControl = null;
			}
			// Attach the progress monitor part to the cancel button
			fProgressMonitorPart.attachToCancelComponent(null);
			fProgressMonitorPart.getParent().setVisible(true);
			
			try {
				updateControls(false);
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			}
			finally {
				updateControls(true);
				if (getShell() != null) {
					fProgressMonitorPart.getParent().setVisible(false);
					fProgressMonitorPart.removeFromCancelComponent(null);
					if (fLastControl != null) {
						fLastControl.setFocus();
					}
				}
			}
		}
		else {
			PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
		}
	}
	
	private void updateControls(boolean enabled) {
		for (Button control:controls) {
			control.setEnabled(enabled);
		}
	}

	protected Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(font);
		Label sep = new Label(composite, SWT.HORIZONTAL|SWT.SEPARATOR);
		sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite buttonComposite = new Composite(composite, SWT.NULL);
		layout = new GridLayout();
		if (isHelpAvailable()) {
			layout.numColumns = 3;
		} else {
			layout.numColumns = 2;
		}
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonComposite.setFont(font);
		
        if (isHelpAvailable()) {
        	createHelpControl(buttonComposite);
        }
		Composite monitorComposite = new Composite(buttonComposite, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		monitorComposite.setLayout(layout);
		monitorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout pmLayout = new GridLayout();
		fProgressMonitorPart= new ProgressMonitorPart(monitorComposite, pmLayout, true);
		fProgressMonitorPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fProgressMonitorPart.setFont(font);
		monitorComposite.setVisible(false);

		boolean helpAvailable = isHelpAvailable();
		setHelpAvailable(false);
		super.createButtonBar(buttonComposite);
		Control[] children = buttonComposite.getChildren();
		for (Control child:children) {
			if (child instanceof Label) {
				child.setVisible(false);
				child.dispose();
			}
		}
		setHelpAvailable(helpAvailable);
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Start",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		addButtons();
	}

	protected void refresh() {
		if (form == null || form.isDisposed()) {
			return;
		}
		refreshFixes();
		fixesComposite.pack();
		form.reflow(true);
		form.redraw();
		form.layout(true, true);
		getShell().pack();
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		ProjectExamplesActivator.importProjectExamples(Arrays.asList(tutorial), true);
	}	
	
}
