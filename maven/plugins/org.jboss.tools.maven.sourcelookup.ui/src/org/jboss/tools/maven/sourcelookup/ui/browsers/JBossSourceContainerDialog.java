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
package org.jboss.tools.maven.sourcelookup.ui.browsers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.maven.sourcelookup.ui.SourceLookupUIActivator;

/**
 * 
 * @author snjeza
 * 
 */
public class JBossSourceContainerDialog extends TitleAreaDialog {

	private TableViewer viewer;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
	private String homePath;
	private Image image;

	public JBossSourceContainerDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		image = SourceLookupUIActivator.imageDescriptorFromPlugin(SourceLookupUIActivator.PLUGIN_ID, "icons/jboss.gif").createImage();
	}

	public List<IJBossServerRuntime> getRuntimes() {
		IServer[] servers = ServerCore.getServers();
		List<IJBossServerRuntime> runtimes = new ArrayList<IJBossServerRuntime>();
		if (servers != null) {
			for (IServer server : servers) {
				IJBossServer jbossServer = null;
				try {
					jbossServer = ServerConverter.checkedGetJBossServer(server);
				} catch (CoreException e) {
					SourceLookupUIActivator.log(e);
				}
				if (jbossServer != null) {
					IJBossServerRuntime runtime = jbossServer.getRuntime();
					if (runtime != null) {
						runtimes.add(runtime);
					}
				}
			}
		}
		return runtimes;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText("Runtime Selection");
		super.configureShell(newShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		setMessage("Select Server Runtime");
		setTitle("Add a container to the source lookup path");
		setTitleImage(DebugPluginImages
				.getImage(IInternalDebugUIConstants.IMG_ADD_SRC_LOC_WIZ));
		initializeDialogUnits(composite);

		viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		viewer.getTable().setLayoutData(data);

		viewer.setLabelProvider(new RuntimeLabelProvider());
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		List<IJBossServerRuntime> runtimes = getRuntimes();
		viewer.setInput(runtimes);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Object object = structuredSelection.getFirstElement();
					if (object instanceof IJBossServerRuntime) {
						IJBossServerRuntime runtime = (IJBossServerRuntime) object;
						setHomePath(runtime);
					}
				}
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		
		if (runtimes.size() > 0) {
			viewer.getTable().select(0);
			setHomePath(runtimes.get(0));
		}

		Dialog.applyDialogFont(composite);

		return composite;

	}

	protected void setHomePath(IJBossServerRuntime runtime) {
		if (runtime != null) {
			IPath location = runtime.getRuntime().getLocation();
			this.homePath = location.toOSString();
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button chooseHomeButton = createButton(parent, 2, "Choose Home...",
				false);
		chooseHomeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Choose Home:");
				String path = dialog.open();
				if (path != null) {
					setHomePath(path);
					okPressed();
				}
			}

		});
		super.createButtonsForButtonBar(parent);
	}

	private class RuntimeLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof IJBossServerRuntime) {
				IJBossServerRuntime runtime = (IJBossServerRuntime) element;
				if (runtime.getRuntime() != null) {
					return runtime.getRuntime().getName();
				}
			}
			return super.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			return image;
		}

	}

	public String getHomePath() {
		return homePath;
	}

	public void setHomePath(String homePath) {
		this.homePath = homePath;
	}

	@Override
	public boolean close() {
		image.dispose();
		return super.close();
	}

}
