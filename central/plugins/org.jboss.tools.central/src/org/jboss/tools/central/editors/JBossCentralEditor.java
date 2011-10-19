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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.CommandContributionItem;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.actions.OpenJBossBlogsHandler;
import org.jboss.tools.central.editors.xpl.TextSearchControl;
import org.jboss.tools.central.jobs.RefreshBlogsJob;

/**
 * 
 * @author snjeza
 *
 */
public class JBossCentralEditor extends SharedHeaderFormEditor {

	private static final String UTF_8_ENCODING = "UTF-8";

	private static final String JBOSS_CENTRAL = "JBoss Central";

	public static final String ID = "org.jboss.tools.central.editors.JBossCentralEditor";

	private AbstractJBossCentralPage gettingStartedPage;
	
	private SoftwarePage softwarePage;
	
	private Image headerImage;
	private Image gettingStartedImage;
	private Image softwareImage;
	
	public JBossCentralEditor() {
		super();
	}
	
	public void dispose() {
		if (headerImage != null) {
			headerImage.dispose();
			headerImage = null;
		}
		if (gettingStartedImage != null) {
			gettingStartedImage.dispose();
			gettingStartedImage = null;
		}
		if (softwareImage != null) {
			softwareImage.dispose();
			softwareImage = null;
		}
		RefreshBlogsJob.INSTANCE.cancel();
		super.dispose();
	}
	
	public void doSave(IProgressMonitor monitor) {
		
	}
	
	public void doSaveAs() {
		
	}
	
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof JBossCentralEditorInput))
			throw new PartInitException("Invalid Input: Must be JBossCentralEditorInput");
		super.init(site, editorInput);
		setPartName(JBOSS_CENTRAL);
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	protected void addPages() {
		try {
			gettingStartedPage = new GettingStartedPage(this);
			int index = addPage(gettingStartedPage);
			if (gettingStartedImage == null) {
				gettingStartedImage = JBossCentralActivator.getImageDescriptor("/icons/gettingStarted.png").createImage();
			}
			setPageImage(index, gettingStartedImage);
			
			softwarePage = new SoftwarePage(this);
			index = addPage(softwarePage);
			if (softwareImage == null) {
				softwareImage = JBossCentralActivator.getImageDescriptor("/icons/software.png").createImage();
			}
			setPageImage(index, softwareImage);
			
		} catch (PartInitException e) {
			JBossCentralActivator.log(e, "Error adding page");
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	protected void createHeaderContents(IManagedForm headerForm) {
		ScrolledForm form = headerForm.getForm();
		form.setText(JBOSS_CENTRAL);
		form.setToolTipText(JBOSS_CENTRAL);
		
		form.setImage(getHeaderImage());
		getToolkit().decorateFormHeading(form.getForm());
		
		Composite headerComposite = getToolkit().createComposite(form.getForm().getHead(), SWT.NONE);
		headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		headerComposite.setLayout(new GridLayout(2, false));
		headerComposite.setBackground(null);
		
//		Button showOnStartup = getToolkit().createButton(headerComposite, "Show on Startup", SWT.CHECK);
//		showOnStartup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		showOnStartup.setBackground(null);
//		showOnStartup.setSelection(JBossCentralActivator.getDefault().showJBossCentralOnStartup());
//		showOnStartup.addSelectionListener(new SelectionAdapter() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				IEclipsePreferences preferences = JBossCentralActivator.getDefault().getPreferences();
//				boolean showOnStartup = preferences.getBoolean(JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP, JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP_DEFAULT_VALUE);
//				preferences.putBoolean(JBossCentralActivator.SHOW_JBOSS_CENTRAL_ON_STARTUP, !showOnStartup);
//				JBossCentralActivator.getDefault().savePreferences();
//			}
//		
//		});

		Composite searchComposite = getToolkit().createComposite(headerComposite);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, true);
		gd.widthHint = 200;
		searchComposite.setLayoutData(gd);
		searchComposite.setBackground(null);
		searchComposite.setLayout(new GridLayout(2, false));
		ImageHyperlink menuLink = getToolkit().createImageHyperlink(searchComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		menuLink.setLayoutData(gd);
		menuLink.setBackground(null);
		menuLink.setImage(CommonImages.getImage(CommonImages.TOOLBAR_ARROW_DOWN));
		menuLink.setToolTipText("Search Menu");
		final TextSearchControl searchControl = new TextSearchControl(searchComposite, false);
		gd = new GridData(SWT.END, SWT.FILL, true, true);
		gd.widthHint = 200;
		searchControl.setLayoutData(gd);
		searchControl.setBackground(null);
		getToolkit().adapt(searchControl);
		searchControl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					searchControl.getTextControl().setText("");
					searchControl.setInitialMessage(searchControl.getInitialMessage());
				} else {
					try {
						StringBuffer url = new StringBuffer();
						String initialMessage = searchControl.getInitialMessage();
						if (JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL.equals(initialMessage)) {
							url.append("https://access.redhat.com/knowledge/searchResults?col=avalon_portal&topSearchForm=topSearchForm&language=en&quickSearch=");
							url.append(URLEncoder.encode(searchControl.getText(), UTF_8_ENCODING));
						} else {
							url.append("http://community.jboss.org/search.jspa?searchArea=");
							url.append(URLEncoder.encode(initialMessage, UTF_8_ENCODING));
							url.append("&as_sitesearch=jboss.org&q=");
							url.append(URLEncoder.encode(searchControl.getText(), UTF_8_ENCODING));
						}
						final String location = url.toString();
						AbstractHandler handler = new OpenJBossBlogsHandler() {

							@Override
							public String getLocation() {
								return location;
							}
							
						};
						handler.execute(new ExecutionEvent());
					} catch (UnsupportedEncodingException e1) {
						JBossCentralActivator.log(e1);
					} catch (ExecutionException e1) {
						JBossCentralActivator.log(e1);
					}
				}
			}
		
		});
		
		final Menu menu = new Menu(menuLink);
		final MenuItem searchCommunityPortal = new MenuItem(menu, SWT.CHECK);
		searchCommunityPortal.setText(JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL);
		final MenuItem searchCommunity = new MenuItem(menu, SWT.CHECK);
		searchCommunity.setText(JBossCentralActivator.SEARCH_THE_COMMUNITY);
		
		String initialMessage = searchControl.getInitialMessage();
		if (JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL.equals(initialMessage)) {
			searchCommunityPortal.setSelection(true);
		} else {
			searchCommunity.setSelection(true);
		}
		searchCommunity.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchCommunity.setSelection(true);
				searchCommunityPortal.setSelection(false);
				searchControl.setInitialMessage(JBossCentralActivator.SEARCH_THE_COMMUNITY);
			}
		
		});
		
		searchCommunityPortal.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				searchCommunity.setSelection(false);
				searchCommunityPortal.setSelection(true);
				searchControl.setInitialMessage(JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL);
			}
		
		});

		menuLink.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				menu.setVisible(false);
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				menu.setVisible(true);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				
			}
		});
		
		form.getForm().setHeadClient(headerComposite);
		//form.getForm().setToolBarVerticalAlignment(SWT.BOTTOM);
		
		IToolBarManager toolbar = form.getToolBarManager();
		CommandContributionItem item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.openJBossToolsHome");
		toolbar.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.favoriteAtEclipseMarketplace");
		toolbar.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.preferences");
		toolbar.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.openJBossToolsTwitter");
		toolbar.add(item);
		
		toolbar.update(true);
	}

	private Image getHeaderImage() {
		if (headerImage == null) {
			headerImage = JBossCentralActivator.getImageDescriptor("/icons/jboss.gif").createImage();
		}
		return headerImage;
	}

	public AbstractJBossCentralPage getGettingStartedPage() {
		return gettingStartedPage;
	}

	public SoftwarePage getSoftwarePage() {
		return softwarePage;
	}
	
	
}
