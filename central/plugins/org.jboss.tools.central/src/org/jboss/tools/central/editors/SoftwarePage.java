/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
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
import java.util.Dictionary;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.commons.core.DelegatingProgressMonitor;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.part.PageBook;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;
import org.jboss.tools.central.jobs.RefreshDiscoveryJob;

/**
 * 
 * @author snjeza
 *
 */
public class SoftwarePage extends AbstractJBossCentralPage implements IRunnableContext {

	public static final String ID = ID_PREFIX + "SoftwarePage";

	private Dictionary<Object, Object> environment;
	private ScrolledForm form;
	private IProgressMonitor monitor;
	private PageBook pageBook;
	private Composite loadingComposite;
	private Composite featureComposite;
	private DiscoveryViewer discoveryViewer;
	private RefreshJobChangeListener refreshJobChangeListener;
	private InstallAction installAction;
	
	public SoftwarePage(FormEditor editor) {
		super(editor, ID, "Software/Update");
		monitor = new DelegatingProgressMonitor();
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		
		Composite body = form.getBody();
	    GridLayout gridLayout = new GridLayout(1, true);
	    gridLayout.horizontalSpacing = 7;
	    body.setLayout(gridLayout);
	    toolkit.paintBordersFor(body);
		
	    Composite left = createComposite(toolkit, body);
		createFeaturesSection(toolkit, left);
		toolkit.paintBordersFor(left);
	    
	    //Composite right = createComposite(toolkit, body);
	    //Section toInstall = createSection(toolkit, right, "To install", ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED);
		
	    //toolkit.paintBordersFor(right);
	    
	    super.createFormContent(managedForm);
		
	}

	protected void createFeaturesSection(FormToolkit toolkit, Composite parent) {
		final Section features = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED);
		features.setText("Features Available");
	    features.setLayout(new GridLayout());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    gd.widthHint = 350;
	    //gd.heightHint = 100;
	    features.setLayoutData(gd);
	    
	    createFeaturesToolbar(toolkit, features);
	    
	    featureComposite = toolkit.createComposite(features);
		gd =new GridData(SWT.FILL, SWT.FILL, true, true);
		featureComposite.setLayoutData(gd);
		featureComposite.setLayout(new GridLayout());
		
		pageBook = new PageBook(featureComposite, SWT.NONE);
		gd =new GridData(SWT.FILL, SWT.FILL, true, true);
	    pageBook.setLayoutData(gd);
	    
	    discoveryViewer = new DiscoveryViewer(getSite(), this);
	    discoveryViewer.setShowConnectorDescriptorKindFilter(false);
	    discoveryViewer.setShowInstalledFilterEnabled(true);
		discoveryViewer.setDirectoryUrl(JBossCentralActivator.getJBossDiscoveryDirectory());
		discoveryViewer.createControl(pageBook);
		discoveryViewer.setEnvironment(getEnvironment());
		discoveryViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
		        DiscoveryConnector connector = (DiscoveryConnector)element;
		        //System.out.println(connector.getId());
		        if (connector.getId().equals("org.eclipse.mylyn.discovery.tests.connectorDescriptor1") ||
		        		connector.getId().equals("org.eclipse.mylyn.discovery.test1") ||
		        		connector.getId().equals("org.eclipse.mylyn.discovery.2tests") ||
		        		connector.getId().equals("org.eclipse.mylyn.trac") ) {
		        	//System.out.println("filtered " + connector.getId());
		          return false;
		        }
				return true;
			}
		});
		
		Control discoveryControl = discoveryViewer.getControl();
		adapt(toolkit, discoveryControl);
		if (discoveryControl instanceof Composite) {
			((Composite) discoveryControl).setLayout(new GridLayout());
		}
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		discoveryControl.setLayoutData(gd);
		
		discoveryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				installAction.setEnabled(discoveryViewer.getInstallableConnectors().size() > 0);
			}
		});
	    
	    loadingComposite = createLoadingComposite(toolkit, pageBook);	    
		
	    form.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				GridData gridData = (GridData) featureComposite.getLayoutData();
				Point size = form.getSize();
				gridData.heightHint = size.y - 25;
				gridData.widthHint = size.x - 25;
				gridData.grabExcessVerticalSpace = true;

				gridData = (GridData) features.getLayoutData();
				gridData.heightHint = size.y - 20;
				gridData.widthHint = size.x - 20;
				gridData.grabExcessVerticalSpace = false;
				form.reflow(true);
				form.redraw();
			}
	    });

	    
		features.setClient(featureComposite);
		showLoading();
		pageBook.pack(true);
		
		RefreshDiscoveryJob refreshDiscoveryJob = RefreshDiscoveryJob.INSTANCE;
		refreshJobChangeListener = new RefreshJobChangeListener();
		refreshDiscoveryJob.addJobChangeListener(refreshJobChangeListener);
		refreshDiscoveryJob.schedule();
				
	}

	private Dictionary<Object, Object> getEnvironment() {
		if (environment == null) {
			environment = JBossCentralActivator.getEnvironment();
		}
		return environment;
	}

	private void createFeaturesToolbar(FormToolkit toolkit, Section section) {
		Composite headerComposite = toolkit.createComposite(section, SWT.NONE);
	    RowLayout rowLayout = new RowLayout();
	    rowLayout.marginTop = 0;
	    rowLayout.marginBottom = 0;
	    headerComposite.setLayout(rowLayout);
	    headerComposite.setBackground(null);
	    
	    ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		toolBarManager.createControl(headerComposite);

		installAction = new InstallAction();
		installAction.setEnabled(false);
		toolBarManager.add(installAction);
		
		toolBarManager.add(new CheckForUpdatesAction());
		
		CommandContributionItem item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.refreshDiscovery");
		toolBarManager.add(item);

	    toolBarManager.update(true);
	    
		section.setTextClient(headerComposite);
	}

	
	private void adapt(FormToolkit toolkit, Control control) {
		toolkit.adapt(control, true, true);
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (Control c:children) {
				adapt(toolkit, c);
			}
		}
	}

	@Override
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		ModalContext.run(runnable, fork, monitor, getDisplay());
	}
	
	public boolean showLoading() {
		if (pageBook.isDisposed()) {
			return false;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				pageBook.showPage(loadingComposite);
				form.reflow(true);
				form.redraw();
			}
		});
		
		return true;
	}

	public boolean refresh() {
		if (pageBook == null || pageBook.isDisposed() || discoveryViewer == null || discoveryViewer.getControl() == null) {
			return false;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				pageBook.showPage(discoveryViewer.getControl());
				form.reflow(true);
				form.redraw();
			}
		});
		
		return true;
	}
	
	@Override
	public void dispose() {
		if (refreshJobChangeListener != null) {
			RefreshDiscoveryJob.INSTANCE.removeJobChangeListener(refreshJobChangeListener);
			refreshJobChangeListener = null;
		}
		super.dispose();
	}

	public DiscoveryViewer getDiscoveryViewer() {
		return discoveryViewer;
	}

	private class RefreshJobChangeListener implements IJobChangeListener {

		@Override
		public void aboutToRun(IJobChangeEvent event) {
			
		}

		@Override
		public void awake(IJobChangeEvent event) {
			
		}

		@Override
		public void done(IJobChangeEvent event) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					refresh();
				}
			});
			
		}

		@Override
		public void running(IJobChangeEvent event) {
			
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			showLoading();
		}

		@Override
		public void sleeping(IJobChangeEvent event) {
			
		}
		
	}
	
	private class InstallAction extends Action {

		public InstallAction() {
			super("Install", JBossCentralActivator.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, "/icons/repository-submit.gif"));
		}

		@Override
		public void run() {
			DiscoveryUi.install(discoveryViewer.getInstallableConnectors(), SoftwarePage.this);
		}
		
	}

	private class CheckForUpdatesAction extends Action {

		public CheckForUpdatesAction() {
			super("Check for Updates", JBossCentralActivator.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, "/icons/update.gif"));
		}

		@Override
		public void run() {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
	        try {
	          handlerService.executeCommand("org.eclipse.equinox.p2.ui.sdk.update", new Event());
	        }
	        catch (Exception e) {
	        	JBossCentralActivator.log(e);
	        }
		}
		
	}

}
