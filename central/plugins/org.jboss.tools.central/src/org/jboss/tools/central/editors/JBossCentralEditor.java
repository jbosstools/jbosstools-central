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
import java.lang.reflect.Field;
import java.net.URLEncoder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.forms.widgets.BusyIndicator;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.eclipse.ui.internal.forms.widgets.TitleRegion;
import org.eclipse.ui.menus.CommandContributionItem;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.actions.OpenJBossBlogsHandler;
import org.jboss.tools.central.editors.xpl.TextSearchControl;
import org.jboss.tools.project.examples.ProjectExamplesActivator;

/**
 * 
 * @author snjeza
 * 
 */
public class JBossCentralEditor extends SharedHeaderFormEditor {

	private static final String COMMANDS_GROUP = "commands";

	private static final String UTF_8_ENCODING = "UTF-8";

	public static final String JBOSS_CENTRAL = "JBoss Central";

	public static final String ID = "org.jboss.tools.central.editors.JBossCentralEditor";

	private AbstractJBossCentralPage gettingStartedPage;

	private SoftwarePage softwarePage;

	private Image headerImage;
	private Image gettingStartedImage;
	private Image softwareImage;

	private Composite toolbarComposite;

	private Composite searchComposite;

	public JBossCentralEditor() {
		super();
	}

	public void dispose() {
		gettingStartedPage = null;
		softwarePage = null;
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
		Job.getJobManager().cancel(JBossCentralActivator.JBOSS_CENTRAL_FAMILY); 
		try {
			Job.getJobManager().join(JBossCentralActivator.JBOSS_CENTRAL_FAMILY, new NullProgressMonitor());
		} catch (OperationCanceledException e) {
			// ignore
		} catch (InterruptedException e) {
			// ignore
		}
		
		getSite().setSelectionProvider(null);
		
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
			editorInput = JBossCentralEditorInput.INSTANCE;
		super.init(site, editorInput);
		setPartName(JBOSS_CENTRAL);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
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
				gettingStartedImage = JBossCentralActivator.getImageDescriptor(
						"/icons/gettingStarted.png").createImage();
			}
			setPageImage(index, gettingStartedImage);

			if (ProjectExamplesActivator.getDefault().getConfigurator()
					.getJBossDiscoveryDirectory() != null) {
				softwarePage = new SoftwarePage(this);
				index = addPage(softwarePage);
				if (softwareImage == null) {
					softwareImage = JBossCentralActivator.getImageDescriptor(
							"/icons/software.png").createImage();
				}
				setPageImage(index, softwareImage);
			}
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
		final ScrolledForm form = headerForm.getForm();
		//form.setText(JBOSS_CENTRAL);
		new HeaderText(form);
		form.setToolTipText(JBOSS_CENTRAL);
		form.setImage(getHeaderImage());
		getToolkit().decorateFormHeading(form.getForm());
		
		final IToolBarManager toolbar = form.getToolBarManager();
		ControlContribution searchControl = new ControlContribution("Search") {
			@Override
			protected Control createControl(Composite parent) {
				return createSearchControl(parent);
			}
		};
		toolbar.add(searchControl);
		toolbar.add(new GroupMarker(COMMANDS_GROUP));
		String[] commandIds = ProjectExamplesActivator.getDefault()
				.getConfigurator().getMainToolbarCommandIds();
		for (String commandId : commandIds) {
			CommandContributionItem item = JBossCentralActivator
					.createContributionItem(getSite(), commandId);
			toolbar.appendToGroup(COMMANDS_GROUP, item);
		}

		toolbar.update(true);
		form.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (toolbar instanceof ToolBarManager) {
					((ToolBarManager)toolbar).dispose();
				} else {
					toolbar.removeAll();
				}
				form.removeDisposeListener(this);
			}
		});
		form.layout(true, true);
	}

	protected Control createSearchControl(Composite parent) {
		
		toolbarComposite = parent;

		searchComposite = getToolkit().createComposite(parent);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, true);
		gd.widthHint = 200;
		searchComposite.setLayoutData(gd);
		searchComposite.setBackground(null);

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginBottom = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 10;
		searchComposite.setLayout(gridLayout);
		ImageHyperlink menuLink = getToolkit().createImageHyperlink(
				searchComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		menuLink.setLayoutData(gd);
		menuLink.setBackground(null);
		menuLink.setImage(JBossCentralActivator.getDefault().getImage("/icons/toolbar-arrow-down.gif"));
		menuLink.setToolTipText("Search Menu");
		final TextSearchControl searchControl = new TextSearchControl(
				searchComposite, false);
		gd = new GridData(SWT.END, SWT.TOP, true, true);
		gd.widthHint = 200;
		searchControl.setLayoutData(gd);
		searchControl.setBackground(null);
		getToolkit().adapt(searchControl);
		final SelectionListener searchControlListener = new SelectionAdapter() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					searchControl.getTextControl().setText("");
					searchControl.setInitialMessage(searchControl
							.getInitialMessage());
				} else {
					try {
						StringBuffer url = new StringBuffer();
						String initialMessage = searchControl
								.getInitialMessage();
						if (JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL
								.equals(initialMessage)) {
							url.append("https://access.redhat.com/knowledge/searchResults?col=avalon_portal&topSearchForm=topSearchForm&language=en&quickSearch=");
							url.append(URLEncoder.encode(
									searchControl.getText(), UTF_8_ENCODING));
						} else {
							url.append("http://community.jboss.org/search.jspa?searchArea=");
							url.append(URLEncoder.encode(initialMessage,
									UTF_8_ENCODING));
							url.append("&as_sitesearch=jboss.org&q=");
							url.append(URLEncoder.encode(
									searchControl.getText(), UTF_8_ENCODING));
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

		};
		searchControl.addSelectionListener(searchControlListener);
		searchControl.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				searchControl.removeSelectionListener(searchControlListener);
				searchControl.removeDisposeListener(this);
			}
		});
		final Menu menu = new Menu(menuLink);
		final MenuItem searchCommunityPortal = new MenuItem(menu, SWT.CHECK);
		searchCommunityPortal
				.setText(JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL);
		final MenuItem searchCommunity = new MenuItem(menu, SWT.CHECK);
		searchCommunity.setText(JBossCentralActivator.SEARCH_THE_COMMUNITY);

		String initialMessage = searchControl.getInitialMessage();
		if (JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL
				.equals(initialMessage)) {
			searchCommunityPortal.setSelection(true);
		} else {
			searchCommunity.setSelection(true);
		}
		final SelectionListener searchCommunityListener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				searchCommunity.setSelection(true);
				searchCommunityPortal.setSelection(false);
				searchControl
						.setInitialMessage(JBossCentralActivator.SEARCH_THE_COMMUNITY);
			}

		};
		searchCommunity.addSelectionListener(searchCommunityListener);
		searchCommunity.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				searchCommunity.removeSelectionListener(searchCommunityListener);
				searchCommunity.removeDisposeListener(this);
			}
		});

		final SelectionListener searchCommunityPortalListener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				searchCommunity.setSelection(false);
				searchCommunityPortal.setSelection(true);
				searchControl
						.setInitialMessage(JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL);
			}

		};
		searchCommunityPortal.addSelectionListener(searchCommunityPortalListener);

		searchCommunityPortal.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				searchCommunityPortal.removeSelectionListener(searchCommunityPortalListener);
				searchCommunityPortal.removeDisposeListener(this);
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
		searchComposite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				menu.dispose();
				searchComposite.removeDisposeListener(this);
			}
		});
		return searchComposite;
	}

	private Image getHeaderImage() {
		return ProjectExamplesActivator.getDefault().getConfigurator()
				.getHeaderImage();
	}

	public AbstractJBossCentralPage getGettingStartedPage() {
		return gettingStartedPage;
	}

	public SoftwarePage getSoftwarePage() {
		return softwarePage;
	}

	private class HeaderText {

		private StyledText titleLabel;

		private BusyIndicator busyLabel;

		private TitleRegion titleRegion;

		private ScrolledForm form;

		public HeaderText(ScrolledForm form) {
			this.form = form;
			try {
				FormHeading heading = (FormHeading) form.getForm().getHead();
				heading.setBusy(true);
				heading.setBusy(false);

				Field field = FormHeading.class.getDeclaredField("titleRegion"); //$NON-NLS-1$
				field.setAccessible(true);
				titleRegion = (TitleRegion) field.get(heading);

				for (Control child : titleRegion.getChildren())
					if (child instanceof BusyIndicator) {
						busyLabel = (BusyIndicator) child;
						break;
					}
				if (busyLabel == null)
					throw new IllegalArgumentException();

				final TextViewer titleViewer = new TextViewer(titleRegion, SWT.READ_ONLY);
				titleViewer.setDocument(new Document());

				titleLabel = titleViewer.getTextWidget();
				titleLabel.setText(JBossCentralEditor.JBOSS_CENTRAL);
				titleLabel.setForeground(heading.getForeground());
				titleLabel.setFont(heading.getFont());
				titleLabel.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						titleLabel.setSelection(0);
						Event selectionEvent= new Event();
						selectionEvent.x = 0;
						selectionEvent.y = 0;
						titleLabel.notifyListeners(SWT.Selection, selectionEvent);
					}
				});
				
				Point size = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				
				final Image emptyImage = new Image(heading.getDisplay(), size.x, size.y);
				busyLabel.setImage(emptyImage);
				titleLabel.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						emptyImage.dispose();
					}
				});

				busyLabel.addControlListener(new ControlAdapter() {
					public void controlMoved(ControlEvent e) {
						updateSizeAndLocations();
					}
				});
				titleLabel.moveAbove(busyLabel);
				titleRegion.addControlListener(new ControlAdapter() {
					public void controlResized(ControlEvent e) {
						updateSizeAndLocations();
					}
				});
				updateSizeAndLocations();
			} catch (Exception e) {
				JBossCentralActivator.log(e);
			} 
		}

		private void updateSizeAndLocations() {
			if (busyLabel == null || busyLabel.isDisposed())
				return;
			if (titleLabel == null || titleLabel.isDisposed())
				return;
			Point size = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			int y = (titleLabel.getParent().getSize().y - size.y) / 2;
			titleLabel.setBounds(busyLabel.getLocation().x + 20, y, size.x, size.y);
			titleRegion.setBounds(5, 0, size.x + 40, size.y + 8 );
			if (toolbarComposite != null && !toolbarComposite.isDisposed()) {
				int formWidth = form.getSize().x;
				int width = size.x + 40 + toolbarComposite.getSize().x;
				if (width > formWidth) {
					searchComposite.setVisible(false);
				} else{
					searchComposite.setVisible(true);
				}
			}
		}
	}

}
