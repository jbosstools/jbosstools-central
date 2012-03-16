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
package org.jboss.tools.project.examples.dialog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.progress.IProgressService;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.filetransfer.ECFExamplesTransport;
import org.jboss.tools.project.examples.runtimes.DownloadRuntime;
import org.jboss.tools.runtime.core.JBossRuntimeLocator;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.IRuntimeDetector;
import org.jboss.tools.runtime.core.model.RuntimeDefinition;
import org.jboss.tools.runtime.core.model.RuntimePath;
import org.jboss.tools.runtime.ui.RuntimeUIActivator;

/**
 * @author snjeza
 * 
 */
public class DownloadRuntimeDialog extends Dialog {

	private static final String FOLDER_IS_REQUIRED = "This folder is required";
	private static final String FOLDER_IS_NOT_WRITABLE = "This folder does not exist or is not writable";
	private static final String DELETE_ON_EXIT = "deleteOnExit";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String USER_HOME = "user.home";
	private static final String DEFAULT_DIALOG_PATH = "defaultDialogPath";
	private static final String DEFAULT_DESTINATION_PATH = "defaultDestinationPath";
	private IDialogSettings dialogSettings;
	private Button deleteOnExit;
	private Text destinationPathText;
	private Text pathText;
	private DownloadRuntime downloadRuntime;
	private String delete;
	private ControlDecoration decPathError;
	private ControlDecoration decPathReq;
	private ControlDecoration destinationPathError;
	private ControlDecoration destinationPathReq;
	
	public DownloadRuntimeDialog(Shell parentShell, DownloadRuntime downloadRuntime) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
				| SWT.RESIZE | getDefaultOrientation());
		dialogSettings = ProjectExamplesActivator.getDefault().getDialogSettings();
		this.downloadRuntime = downloadRuntime;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Download Runtime '" + downloadRuntime.getName() + "'");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		contents.setLayoutData(gd);
		contents.setLayout(new GridLayout(1, false));
		applyDialogFont(contents);
		initializeDialogUnits(area);

		Composite pathComposite = new Composite(contents, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		pathComposite.setLayoutData(gd);
		pathComposite.setLayout(new GridLayout(3, false));
		
		if (downloadRuntime.isDisclaimer()) {
			Group disclaimerComposite = new Group(pathComposite, SWT.NONE);
			gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.horizontalSpan = 3;
			disclaimerComposite.setLayoutData(gd);
			disclaimerComposite.setLayout(new GridLayout(1, false));
			disclaimerComposite.setText("Warning");
			new Label(disclaimerComposite, SWT.NONE).setText("This is a community project and, as such is not supported with an SLA.");
			Link link = new Link(disclaimerComposite, SWT.NONE);
			link.setText("Any questions or problems can be raised through the <a>JBoss AS 7 User Forum</a> where the community will provide their best efforts to assist.");
			Link link1 = new Link(disclaimerComposite, SWT.NONE);
			link1.setText("This project is included in the JBoss Enterprise Application Platform for which a <a>support subscription</a> is available.");
					
			link.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					String text = e.text;
					if ("JBoss AS 7 User Forum".equals(text)) {
						IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
								.getBrowserSupport();
						try {
							URL url = new URL("http://community.jboss.org/en/jbossas/as7_users?view=discussions"); //$NON-NLS-1$
							support.getExternalBrowser().openURL(url);
						} catch (Exception e1) {
							ProjectExamplesActivator.log(e1);
						}
					}
					
				}
			} );
			link1.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					String text = e.text;
					if ("support subscription".equals(text)) {
						IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
								.getBrowserSupport();
						try {
							URL url = new URL("http://www.redhat.com/jboss/"); //$NON-NLS-1$
							support.getExternalBrowser().openURL(url);
						} catch (Exception e1) {
							ProjectExamplesActivator.log(e1);
						}
					}
					
				}
			} );
		}
		Label urlLabel = new Label(pathComposite, SWT.NONE);
		urlLabel.setText("URL:");
		Text urlText = new Text(pathComposite, SWT.READ_ONLY|SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan=2;
		urlText.setLayoutData(gd);
		urlText.setText(downloadRuntime.getUrl());
		Label pathLabel = new Label(pathComposite, SWT.NONE);
		pathLabel.setText("Install folder:");
		
		pathText = new Text(pathComposite, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		pathText.setLayoutData(gd);
		final String defaultPath = getDefaultPath();
		pathText.setText(defaultPath);
		decPathError = addDecoration(pathText, FieldDecorationRegistry.DEC_ERROR, FOLDER_IS_NOT_WRITABLE);
		decPathReq = addDecoration(pathText, FieldDecorationRegistry.DEC_REQUIRED, FOLDER_IS_REQUIRED);
		pathText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		Button browseButton = new Button(pathComposite, SWT.NONE);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Select install folder");
				dialog.setFilterPath(pathText.getText());
				final String path = dialog.open();
				if (path == null) {
					return;
				}
				pathText.setText(path);
			}
		
		});
		
		Label destinationLabel = new Label(pathComposite, SWT.NONE);
		destinationLabel.setText("Download folder:");
		
		destinationPathText = new Text(pathComposite, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		destinationPathText.setLayoutData(gd);
		String destinationPath = dialogSettings.get(DEFAULT_DESTINATION_PATH);
		destinationPathError = addDecoration(destinationPathText, FieldDecorationRegistry.DEC_ERROR, FOLDER_IS_NOT_WRITABLE);
		destinationPathReq = addDecoration(destinationPathText, FieldDecorationRegistry.DEC_REQUIRED, FOLDER_IS_REQUIRED);
		
		if (destinationPath == null || destinationPath.isEmpty()) {
			destinationPath=System.getProperty(JAVA_IO_TMPDIR);
		}
		destinationPathText.setText(destinationPath);
		Button browseDestinationButton = new Button(pathComposite, SWT.NONE);
		browseDestinationButton.setText("Browse...");
		browseDestinationButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Select download folder");
				dialog.setFilterPath(destinationPathText.getText());
				final String path = dialog.open();
				if (path == null) {
					return;
				}
				destinationPathText.setText(path);
			}
		
		});
		
		destinationPathText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		deleteOnExit = new Button(pathComposite, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan=3;
		deleteOnExit.setLayoutData(gd);
		deleteOnExit.setText("Delete archive after installing");
		
		delete = dialogSettings.get(DELETE_ON_EXIT);
		if (delete == null) {
			delete = "true";
		}
		deleteOnExit.setSelection(new Boolean(delete));
		deleteOnExit.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				delete = new Boolean(deleteOnExit.getSelection()).toString();
			}
			
		});
		
		return area;
	}

	private String getDefaultPath() {
		String defaultPath = dialogSettings.get(DEFAULT_DIALOG_PATH);
		if (defaultPath == null || defaultPath.isEmpty()) {
			defaultPath=System.getProperty(USER_HOME);
		}
		return defaultPath;
	}

	private void showDecorations() {
		String path = pathText.getText();
		String destination = destinationPathText.getText();
		decPathError.hide();
		decPathReq.hide();
		destinationPathError.hide();
		destinationPathReq.hide();
		if (path.isEmpty()) {
			decPathReq.show();
		}
		if (destination.isEmpty()) {
			destinationPathReq.show();
		}
		boolean pathExists = checkPath(path, decPathError);
		boolean destExists = checkPath(destination, destinationPathError);
		getButton(IDialogConstants.OK_ID).setEnabled(pathExists
			&& destExists
			&& !path.isEmpty() && !destination.isEmpty());
		decPathError.setShowHover(true);
	}

	private boolean checkPath(String path, ControlDecoration dec) {
		if (path.isEmpty()) {
			return true;
		}
		try {
			File file = File.createTempFile("temp", "txt", new File(path));
			file.deleteOnExit();
			file.delete();
		} catch (IOException e) {
			dec.show();
			return false;
		}
		return true;
	}

	protected ControlDecoration addDecoration(Control control, String id, String description) {
		final ControlDecoration decPath = new ControlDecoration(control, SWT.TOP
				| SWT.LEFT);
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		FieldDecoration fd = registry.getFieldDecoration(id);
		decPath.setImage(fd.getImage());
		fd.setDescription(description);
	
		decPath.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(
				id).getImage());

		decPath.setShowOnlyOnFocus(false);
		decPath.setShowHover(true);
		decPath.setDescriptionText(description);
		return decPath;
	}

	protected void validate() {
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		if (pathText.getText().isEmpty()) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		if (destinationPathText.getText().isEmpty()) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		showDecorations();
	}

	@Override
	protected void okPressed() {
		dialogSettings.put(DEFAULT_DESTINATION_PATH,
				destinationPathText.getText());
		dialogSettings.put(DEFAULT_DIALOG_PATH, pathText.getText());
		dialogSettings.put(DELETE_ON_EXIT, delete);
		String selectedDirectory = pathText.getText();
		String destinationDirectory = destinationPathText.getText();
		boolean del = deleteOnExit.getSelection();
		super.okPressed();
		downloadRuntime(selectedDirectory, destinationDirectory, del);
	}

	private void downloadRuntime(final String selectedDirectory,
			final String destinationDirectory, final boolean deleteOnExit) {		
		Job downloadJob = new Job("Download '" + downloadRuntime.getName()) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Download '" + downloadRuntime.getName() + "' ...", 100);
				downloadAndInstall(selectedDirectory,
						destinationDirectory, deleteOnExit, monitor);
				return Status.OK_STATUS;
			}
			
		};
		downloadJob.setUser(false);
		downloadJob.schedule();
		IProgressService progressService= PlatformUI.getWorkbench().getProgressService();
		progressService.showInDialog(getActiveShell(), downloadJob);
	}
	
	private IStatus downloadAndInstall(String selectedDirectory, String destinationDirectory, boolean deleteOnExit, IProgressMonitor monitor) {
		FileInputStream in = null;
		OutputStream out = null;
		File file = null;
		try {
			URL url = new URL(downloadRuntime.getUrl());
			String name = url.getPath();
			int slashIdx = name.lastIndexOf('/');
			if (slashIdx >= 0)
				name = name.substring(slashIdx + 1);
			
			File destination = new File(destinationDirectory);
			destination.mkdirs();
			file = new File (destination, name);
			int i = 1;
			while (file.exists()) {
				file = new File(destination, name + "(" + i++ + ")");
			}
			
			if (deleteOnExit) {
				file.deleteOnExit();
			}
			out = new BufferedOutputStream(
					new FileOutputStream(file));
			
			IStatus result = ECFExamplesTransport.getInstance().download(file.getName(),
					url.toExternalForm(), out, monitor);
			out.flush();
			out.close();
			if (monitor.isCanceled()) {
				file.deleteOnExit();
				file.delete();
				return Status.CANCEL_STATUS;
			}
			File directory = new File(selectedDirectory);
			directory.mkdirs();
			if (!directory.isDirectory()) {
				ProjectExamplesActivator.getDefault().getLog().log(result);
				MessageDialog.openError(getActiveShell(), "Error", "The '" + directory + "' is not a directory.");
				file.deleteOnExit();
				file.delete();
				return Status.CANCEL_STATUS;
			}
			ProjectExamplesActivator.extractZipFile(file, directory, monitor);
			if (!result.isOK()) {
				ProjectExamplesActivator.getDefault().getLog().log(result);
				String message;
				if (result.getException() != null) {
					message = result.getException().getMessage();
				} else {
					message = result.getMessage();
				}
				MessageDialog.openError(getActiveShell(), "Error", message);
				file.deleteOnExit();
				file.delete();
				return Status.CANCEL_STATUS;
			}
			createRuntimes(selectedDirectory, monitor);
		} catch (IOException e) {
			ProjectExamplesActivator.log(e);
			if (file != null && file.exists()) {
				file.deleteOnExit();
				file.delete();
			}
			MessageDialog.openError(getActiveShell(), "Error", e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	private Shell getActiveShell() {
		Display display = Display.getDefault();
		if (display != null) {
			return display.getActiveShell();
		}
		return null;
	}

	private static void createRuntimes(String directory, IProgressMonitor monitor) {
		JBossRuntimeLocator locator = new JBossRuntimeLocator();
		Set<RuntimePath> runtimePaths = RuntimeUIActivator.getDefault()
				.getRuntimePaths();
		RuntimePath newPath = new RuntimePath(directory);
		runtimePaths.add(newPath);
		for (RuntimePath runtimePath : runtimePaths) {
			List<RuntimeDefinition> serverDefinitions = locator
					.searchForRuntimes(runtimePath.getPath(),
							monitor);
			runtimePath.getServerDefinitions().clear();
			for (RuntimeDefinition serverDefinition : serverDefinitions) {
				serverDefinition.setRuntimePath(runtimePath);
			}
			runtimePath.getServerDefinitions().addAll(serverDefinitions);
			RuntimeUIActivator.getDefault().getRuntimePaths().add(runtimePath);
			RuntimeUIActivator.getDefault().saveRuntimePaths();
		}
		List<RuntimeDefinition> serverDefinitions = RuntimeUIActivator
				.getDefault().getServerDefinitions();
		Set<IRuntimeDetector> detectors = RuntimeCoreActivator
				.getRuntimeDetectors();
		for (IRuntimeDetector detector : detectors) {
			if (detector.isEnabled()) {
				detector.initializeRuntimes(serverDefinitions);
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		showDecorations();
	}

}
