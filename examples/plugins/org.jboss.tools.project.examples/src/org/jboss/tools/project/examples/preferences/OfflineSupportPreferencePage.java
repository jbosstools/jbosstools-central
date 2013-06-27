/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.preferences;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.internal.offline.ExtractScriptJob;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleUtil;
import org.jboss.tools.project.examples.offline.OfflineUtil;

/**
 * Offline support Preference page
 * 
 * @author Fred Bricon
 *
 */
public class OfflineSupportPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static boolean VISIBLE;
	
	public static final String ID = "org.jboss.tools.project.examples.preferences.offlineSupportPreferencePage"; //$NON-NLS-1$
	private Text offlineDirectoryText;
	private Button enableOfflineSupport;
	private Button offlineDirectoryBrowse;
	
	
	public OfflineSupportPreferencePage() {
		//We need to ensure the maven example plugin, if present, is active, 
		//so its propertylistener can be registered and listen to offline status changes
		//FIXME Oooooh that's baaaaad
		try {
			ProjectExamplesActivator.getDefault().getImportProjectExample(null);
		} catch (Exception pleaseIgnoreThatHorribleHorribleHack) {
			//Yeah I know
		}
	}
	
	@Override
	protected Control createContents(Composite parent) {

		ExtractScriptJob extractScriptJob = new ExtractScriptJob(); 
		extractScriptJob.schedule();
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
				
		createEnableOfflineGroup(composite);
		
		createGoOfflineGroup(composite);
		
		return composite;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		VISIBLE = visible;
	}
	
	private void enableControls() {
		offlineDirectoryText.setEnabled(enableOfflineSupport.getSelection());
		offlineDirectoryBrowse.setEnabled(enableOfflineSupport.getSelection());
		checkOfflineDirectory();
	}
	
	private void checkOfflineDirectory() {
		if (enableOfflineSupport.getSelection()) {
			File offlineDir = getOfflineDir();
			if (offlineDir == null) {
				setErrorMessage("You must set an offline directory"); //$NON-NLS-1$
			} else if (!offlineDir.exists()) {
				setMessage("The offline directory is empty, make sure you run the offline script", WARNING); //$NON-NLS-1$
			} else if (offlineDir.listFiles().length == 0) {
				setMessage("The offline directory does not exist, make sure you run the offline script", WARNING); //$NON-NLS-1$
			}
		} else {
			setMessage(null);
			setErrorMessage(null);
		}
	}
	
	private void createEnableOfflineGroup(Composite composite) {
		Composite enableOfflineSupportGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		enableOfflineSupportGroup.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		enableOfflineSupportGroup.setLayoutData(gd);
		
		enableOfflineSupport = new Button(enableOfflineSupportGroup, SWT.CHECK);
		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd2.horizontalSpan = 2;
		enableOfflineSupport.setLayoutData(gd2);
		enableOfflineSupport.setText("Enable offline mode for project examples"); //$NON-NLS-1$
		enableOfflineSupport.setToolTipText("In offline mode, examples will be fetched from the offline directory"); //$NON-NLS-1$
		enableOfflineSupport.setSelection(ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_ENABLED));

		Label offlineDirectoryLabel = new Label(enableOfflineSupportGroup, SWT.NONE);
		offlineDirectoryLabel.setText("Offline directory :"); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(2, 1).applyTo(offlineDirectoryLabel);
		
		
		offlineDirectoryText = new Text(enableOfflineSupportGroup, SWT.SINGLE|SWT.BORDER);
		GridData gd3 = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd3.verticalAlignment = SWT.CENTER;
		offlineDirectoryText.setLayoutData(gd3);
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		String offlineDirectoryValue = store.getString(ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_DIRECTORY);
		
		offlineDirectoryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				checkOfflineDirectory();
			}
		});
		
		
		offlineDirectoryText.setText(offlineDirectoryValue == null ? "" : offlineDirectoryValue); //$NON-NLS-1$
		offlineDirectoryBrowse = new Button(enableOfflineSupportGroup, SWT.PUSH);
		offlineDirectoryBrowse.setText(Messages.Browse);
		offlineDirectoryBrowse.addSelectionListener(new SelectionAdapter(){
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SINGLE);
				String value = offlineDirectoryText.getText();
				if (value.trim().length() == 0) {
					value = Platform.getLocation().toOSString();
				}
				dialog.setFilterPath(value);
			
				String result = dialog.open();
				if (result == null || result.trim().length() == 0) {
					return;
				}
				offlineDirectoryText.setText(result);
			}
		
		});

		enableOfflineSupport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableControls();
			}
		});

		enableControls();
	}

	@SuppressWarnings("nls")
	private void createGoOfflineGroup(Composite composite) {
		Group goOfflineGroup = new Group(composite, SWT.NONE);
		GridLayout layout2 = new GridLayout(2, false);
		goOfflineGroup.setLayout(layout2);
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
		goOfflineGroup.setLayoutData(gd2);
		goOfflineGroup.setText("Prepare offline data");	 //$NON-NLS-1$

		Label description = new Label(goOfflineGroup, SWT.WRAP);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(description);
		description.setText("The following command generates an offline cache usable by the project examples.\n Pre-requisites :\n");
		
		Link groovylink = new Link(goOfflineGroup, SWT.WRAP);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(groovylink);
		groovylink.setText(" - <a>Groovy 2.1.x</a> must be set in your path");
		groovylink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openUrl("http://groovy.codehaus.org/Installing+Groovy");
			}
		});
		
		Link reposlink = new Link(goOfflineGroup, SWT.WRAP);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(reposlink);
		reposlink.setText(" - the JBoss Public and Red Hat Maven repositories must be <a>configured in your settings.xml</a>"); //$NON-NLS-1$
		reposlink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openUrl("http://community.jboss.org/wiki/SettingUpTheJBossEnterpriseRepositories");
			}
		});
				
		final StyledText text = new StyledText(goOfflineGroup, SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, false).hint(90, 250).applyTo(text);
		text.setEditable(false);
		
		Collection<String> descriptors = getDescriptors(ProjectExampleUtil.getPluginSites());
		Collection<String> categories = getAsStringCollection(ProjectExampleUtil.getCategoryURLs());
		StringBuilder command = new StringBuilder("groovy \"") //$NON-NLS-1$
		                        .append(OfflineUtil.getGoOfflineScript().getAbsolutePath())
		                        .append("\" ") //$NON-NLS-1$
		                        .append(StringUtils.join(categories, " ")) //$NON-NLS-1$
		                        .append(" ") //$NON-NLS-1$
		                        .append(StringUtils.join(descriptors, " "))//$NON-NLS-1$
		                        .append(" -q -e"); //$NON-NLS-1$
		 
		text.setText(command.toString());
		
	    final Clipboard cb = new Clipboard(Display.getCurrent());

	    Button copy = new Button(goOfflineGroup, SWT.PUSH);
	    GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(copy);
		
	    copy.setText("Copy to clipboard"); //$NON-NLS-1$
	    copy.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    	   String textData = text.getText();
	           TextTransfer textTransfer = TextTransfer.getInstance();
	           cb.setContents(new Object[] { textData }, new Transfer[] {textTransfer});
	      }
	    });
	    
	    Label instructions = new Label(goOfflineGroup, SWT.WRAP);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(instructions);
	    instructions.setText("The script will create an offline/ directory under the current directory.\n"
	    		+ "It will attempt to download and build all project examples, populating a clean local maven repository.\n\n"
	    		+ "- Make sure you copy the contents of offline/.jbosstools/cache to the final offline directory\n"
	    		+ "- Copy the contents offline/.m2/repository to your local maven repository location.");
	}
	
	private Collection<String> getDescriptors(Collection<IProjectExampleSite> pluginSites) {
		List<String> descriptorUrls = new ArrayList<String>(pluginSites.size());
		for (IProjectExampleSite site : pluginSites) {
			if (!site.isExperimental()) {
				descriptorUrls.add(site.getUrl().toString());
			}
		}
		return descriptorUrls;
	}

	private Collection<String> getAsStringCollection(Collection<URL> urls) {
		List<String> sUrls = new ArrayList<String>(urls.size());
		for (URL u : urls) {
		   sUrls.add(u.toString());
		}
		return sUrls;
	}
	
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		offlineDirectoryText.setText(ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_DIRECTORY_VALUE); //$NON-NLS-1$
		storePreferences();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (getErrorMessage() != null) {
			return false;
		}
		storePreferences(); 
		return super.performOk();
	}

	private void storePreferences() {
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		File offlineDir = getOfflineDir();
		Boolean isOfflineEnabled = enableOfflineSupport.getSelection();
		if (offlineDir != null) {
			store.setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_DIRECTORY, offlineDir.getAbsolutePath());
		}
		store.setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_OFFLINE_ENABLED, isOfflineEnabled.toString());
		
	}

	private File getOfflineDir() {
		String offlineDir = offlineDirectoryText.getText().trim();
		if (!offlineDir.isEmpty()) {
			return new File(offlineDir);
		}
		return null;
	}
	
	//FIXME duplicated code from JBossCentralActivator. Surely there must be some built-in stuff doing that alread somewhere.

	public static void openUrl(String location) {
		URL url = null;
		boolean asExternal = true;
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		try {
			if (location != null) {
				url = new URL(location);
			}

			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL
					|| asExternal) {
				IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
						.getBrowserSupport();
				support.getExternalBrowser().openURL(url);
			} else {
				IWebBrowser browser = null;
				int flags;
				if (WorkbenchBrowserSupport.getInstance()
						.isInternalWebBrowserAvailable()) {
					flags = IWorkbenchBrowserSupport.AS_EDITOR
							| IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				} else {
					flags = IWorkbenchBrowserSupport.AS_EXTERNAL
							| IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = ProjectExamplesActivator.PLUGIN_ID
						+ System.currentTimeMillis();
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(
						flags, generatedId, null, null);
				browser.openURL(url);
			}
		} catch (PartInitException e) {
			Status status = new Status(IStatus.ERROR,
					ProjectExamplesActivator.PLUGIN_ID,
					"Browser initialization failed");
			ProjectExamplesActivator.getDefault().getLog().log(status);
			MessageDialog
					.openError(shell, "Open Location", status.getMessage());
		} catch (MalformedURLException e) {
			Status status = new Status(IStatus.ERROR,
					ProjectExamplesActivator.PLUGIN_ID, "Invalid URL");
			ProjectExamplesActivator.getDefault().getLog().log(status);
			MessageDialog
					.openError(shell, "Open Location", status.getMessage());
		}
	}

}
