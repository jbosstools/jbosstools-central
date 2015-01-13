/*************************************************************************************
 * Copyright (c) 2008-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/

package org.jboss.tools.central.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.jboss.tools.browsersim.browser.IBrowser;
import org.jboss.tools.browsersim.browser.IBrowserFunction;
import org.jboss.tools.browsersim.browser.WebKitBrowserFactory;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.internal.CentralHelper;
import org.jboss.tools.central.internal.JsonUtil;
import org.jboss.tools.central.internal.WizardSupport;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizard;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager.ProxyWizardManagerListener;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager.UpdateEvent;
import org.jboss.tools.central.jobs.RefreshBuzzJob;
import org.jboss.tools.central.model.FeedsEntry;
import org.jboss.tools.central.preferences.PreferenceKeys;
import org.jboss.tools.central.wizards.AbstractJBossCentralProjectWizard;
import org.jboss.tools.discovery.core.internal.connectors.DiscoveryUtil;
import org.jboss.tools.discovery.core.internal.connectors.JBossDiscoveryUi;
import org.jboss.tools.project.examples.IProjectExampleManager;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard2;
import org.osgi.framework.Bundle;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * 
 * @author Fred Bricon
 */
public class GettingStartedHtmlPage extends AbstractJBossCentralPage implements ProxyWizardManagerListener,
		IPropertyChangeListener {

	public static class RefreshQuickstartsJob extends Job {

		private Map<String, ProjectExample> examples;

		public RefreshQuickstartsJob() {
			super("Download quickstarts list");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IProjectExampleManager manager = ProjectExamplesActivator.getDefault().getProjectExampleManager();
			try {
				//long start = System.currentTimeMillis();
				Collection<ProjectExample> examplesList = manager.getExamples(monitor);
				examples = Maps.uniqueIndex(examplesList, new Function<ProjectExample, String>() {
					public String apply(ProjectExample example) {
						return example.getId();
					}
				});

				//long elapsed = System.currentTimeMillis() - start;
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == JBossCentralActivator.JBOSS_CENTRAL_FAMILY;
		}

		public Map<String, ProjectExample> getExamples() {
			return examples;
		}
	}

	public static final String ID = ID_PREFIX + "GettingStartedPage";

	private ScrolledForm form;
	private IBrowser browser;

	private Collection<ProxyWizard> allWizards;
	private Map<String, ProjectExample> examples;
	private Map<String, ProxyWizard> displayedWizardsMap;
	private RefreshBuzzJobChangeListener buzzlistener;

	public GettingStartedHtmlPage(FormEditor editor) {
		super(editor, ID, "Getting Started");
		ProxyWizardManager.INSTANCE.registerListener(this);
		JBossCentralActivator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		RefreshBuzzJob buzzjob = RefreshBuzzJob.INSTANCE;
		buzzlistener = new RefreshBuzzJobChangeListener();
		buzzjob.addJobChangeListener(buzzlistener);
		buzzjob.schedule();

		RefreshQuickstartsJob job = new RefreshQuickstartsJob();
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				examples = ((RefreshQuickstartsJob) event.getJob()).getExamples();
				syncBrowserExec(getLoadQuickstartsScript());
			}

		});
		job.schedule();

		super.createFormContent(managedForm);

		form = managedForm.getForm();
		Composite body = form.getBody();
		GridLayoutFactory.fillDefaults().applyTo(body);
		createBrowserSection(body);
		form.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				form.removeDisposeListener(this);
			}
		});
	}

	private void createBrowserSection(final Composite parent) {
		WebKitBrowserFactory factory = new WebKitBrowserFactory();
		browser = factory.createBrowser(parent, SWT.NONE, false);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 1;
		layoutData.verticalSpan = 1;
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		browser.registerBrowserFunction("openInIDE", new IBrowserFunction() {
			@Override
			public Object function(Object[] browserArgs) {
				String function = browserArgs[0].toString();
				String arg =  browserArgs[1].toString();
				switch (function) {
				case "quickstart":
					openQuickstart(parent.getShell(), arg);
					break;
				case "wizard":
					openProxyWizard(parent.getShell(), arg);
					break;
				case "openlink":
					JBossCentralActivator.openUrl(arg, parent.getShell());
					break;
				case "openpage":
					getEditor().setActivePage(arg);
				default:
					break;

				}
				return null;
			}
		});

		browser.registerBrowserFunction("initialize", new IBrowserFunction() {
			@Override
			public Object function(Object[] browserArgs) {
				browser.execute(getBuzzScript());
				browser.execute(getProxyWizardsScript());
				browser.execute(getLoadQuickstartsScript());
				browser.execute(getToggleEarlyAccessScript());
				return null;
			}
		});

		Job centralJob = new Job("Extract Central page") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final String url;
				try {
					url = CentralHelper.getCentralUrl(monitor);
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							browser.setUrl(url);
						}
					});
				} catch (CoreException e) {
					return e.getStatus();
				}

				Job job = new Job("Update project wizard list") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						ProxyWizardManager proxyWizardManager = ProxyWizardManager.INSTANCE; // FIXME lookup global instance.
						List<ProxyWizard> wizards = proxyWizardManager.getProxyWizards(true, monitor);
						resetWizards(wizards);
						return Status.OK_STATUS;
					}

					@Override
					public boolean belongsTo(Object family) {
						return family == JBossCentralActivator.JBOSS_CENTRAL_FAMILY;
					}
				};
				job.schedule();
				
				return Status.OK_STATUS;
			}
		};
		centralJob.schedule();
	}

	protected void openQuickstart(Shell shell, String quickstartId) {
		final ProjectExample pe = examples.get(quickstartId);
		if (pe == null) {
			System.err.println(quickstartId + " is not a valid quickstart");
			return;
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IWizard wizard = new NewProjectExamplesWizard2(pe);
				WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
				dialog.open();
			}
		});
	}

	protected void openProxyWizard(Shell shell, String proxyWizardId) {
		try {
			ProxyWizard proxyWizard = displayedWizardsMap.get(proxyWizardId);
			IConfigurationElement element = findWizard(proxyWizard);
			if (element == null) {
				// Wizard not installed/completely available
				installMissingWizard(proxyWizard.getRequiredComponentIds());
			} else {
				WizardSupport.openWizard(element);
			}
		} catch (CoreException e1) {
			JBossCentralActivator.log(e1);
		} catch (InvocationTargetException e1) {
			JBossCentralActivator.log(e1);
		} catch (InterruptedException e1) {
			JBossCentralActivator.log(e1);
		}
		System.err.println("Opening " + proxyWizardId);
	}

	private IConfigurationElement findWizard(ProxyWizard proxyWizard) {
		IConfigurationElement element = WizardSupport.getInstalledWizards().get(proxyWizard.getWizardId());
		if (element == null) {
			return null;
		}
		List<String> pluginIds = proxyWizard.getRequiredPluginIds();
		if (pluginIds != null) {
			for (String id : pluginIds) {
				if (Platform.getBundle(id) == null) {
					// wizard is installed but required plugin is missing
					return null;
				}
			}
		}
		return element;
	}

	@SuppressWarnings("restriction")
	protected void installMissingWizard(final Collection<String> connectorIds) throws InvocationTargetException,
			InterruptedException {

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (!MessageDialog.openQuestion(shell, "Information",
				"The required features to use this wizard need to be installed. Do you want to proceed?")) {
			return;
		}
		;

		final IStatus[] results = new IStatus[1];
		final ConnectorDiscovery[] connectorDiscoveries = new ConnectorDiscovery[1];

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				connectorDiscoveries[0] = DiscoveryUtil.createConnectorDiscovery();
				connectorDiscoveries[0].setEnvironment(JBossCentralActivator.getEnvironment());
				results[0] = connectorDiscoveries[0].performDiscovery(monitor);
				if (monitor.isCanceled()) {
					results[0] = Status.CANCEL_STATUS;
				}
			}
		};

		IRunnableContext context = new ProgressMonitorDialog(shell);
		context.run(true, true, runnable);
		if (results[0] == null) {
			return;
		}
		if (results[0].isOK()) {
			List<DiscoveryConnector> connectors = connectorDiscoveries[0].getConnectors();
			List<ConnectorDescriptor> installableConnectors = new ArrayList<>();
			for (DiscoveryConnector connector : connectors) {
				if (connectorIds.contains(connector.getId())) {
					installableConnectors.add(connector);
				}
			}
			JBossDiscoveryUi.install(installableConnectors, context);
		} else {
			String message = results[0].toString();
			switch (results[0].getSeverity()) {
			case IStatus.ERROR:
				MessageDialog.openError(shell, "Error", message);
				break;
			case IStatus.WARNING:
				MessageDialog.openWarning(shell, "Warning", message);
				break;
			case IStatus.INFO:
				MessageDialog.openInformation(shell, "Information", message);
				break;
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (PreferenceKeys.ENABLE_EARLY_ACCESS.equals(event.getProperty())) {
			syncBrowserExec(getToggleEarlyAccessScript());
			resetWizards(allWizards);
		}
	}

	private void resetWizards(Collection<ProxyWizard> proxyWizards) {
		allWizards = proxyWizards;
		if (proxyWizards == null) {
			displayedWizardsMap = Collections.emptyMap();
			return;
		}
		boolean earlyAccessEnabled = JBossCentralActivator.getDefault().getPreferences()
				.getBoolean(PreferenceKeys.ENABLE_EARLY_ACCESS, PreferenceKeys.ENABLE_EARLY_ACCESS_DEFAULT_VALUE);
		Map<String, ProxyWizard> newWizards = new LinkedHashMap<>(proxyWizards.size());
		for (ProxyWizard proxyWizard : proxyWizards) {
			if (earlyAccessEnabled || !proxyWizard.hasTag("earlyaccess") ) {
				newWizards.put(proxyWizard.getId(), proxyWizard);
			}
		}
		displayedWizardsMap = newWizards;
		
		syncBrowserExec(getProxyWizardsScript());
	}
	
	@Override
	public void onProxyWizardUpdate(UpdateEvent event) throws CoreException {
		resetWizards(event.getProxyWizards());
	}

	private class RefreshBuzzJobChangeListener extends JobChangeAdapter {
		@Override
		public void done(IJobChangeEvent event) {
			syncBrowserExec(getBuzzScript());
		}
	}
	
	private void syncBrowserExec(final String script) {
		if (browser != null && !browser.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (!browser.isDisposed()) {
						browser.execute(script);
					}
				}
			});
		}
	}

	//Javascript providers
	private String getBuzzScript() {
		List<FeedsEntry> buzz = RefreshBuzzJob.INSTANCE.getEntries();
		buzz = buzz.size() > 5 ? buzz.subList(0, 5) : buzz;
		String json = JsonUtil.jsonifyBuzz(buzz);
		String script = "loadBuzz(" + json + ");";
		return script;
	}
	
	private String getToggleEarlyAccessScript() {
		boolean earlyAccessEnabled = JBossCentralActivator.getDefault().getPreferences()
				.getBoolean(PreferenceKeys.ENABLE_EARLY_ACCESS, PreferenceKeys.ENABLE_EARLY_ACCESS_DEFAULT_VALUE);
		String script = "toggleEarlyAccess(" + earlyAccessEnabled + ");";
		System.err.println(script);
		return script;
	}
	
	private String getProxyWizardsScript() {
		final Collection<ProxyWizard> proxyWizards = displayedWizardsMap.values();
		String wizardsJson = JsonUtil.jsonifyWizards(proxyWizards);
		System.err.println(wizardsJson);
		String script = "loadWizards(" + wizardsJson + ");";
		return script;
	}
	
	private String getLoadQuickstartsScript() {
		String json = JsonUtil.jsonifyExamples(examples==null?Collections.<ProjectExample>emptyList():examples.values());
		String script = "loadQuickstarts(" + json + ");";
		return script;
	}
	
	@Override
	public void dispose() {
		JBossCentralActivator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		ProxyWizardManager.INSTANCE.unRegisterListener(this);
		if (buzzlistener != null) {
			RefreshBuzzJob.INSTANCE.removeJobChangeListener(buzzlistener);
		}
		if (browser != null) {
			browser.dispose();
			browser = null;
		}
		super.dispose();
	}
	
}
