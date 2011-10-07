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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.dialogs.ProjectExamplesDialog;
import org.jboss.tools.central.jobs.RefreshNewsJob;
import org.jboss.tools.central.jobs.RefreshTutorialsJob;
import org.jboss.tools.central.model.NewsEntry;
import org.jboss.tools.central.model.Tutorial;
import org.jboss.tools.central.model.TutorialCategory;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.osgi.framework.Bundle;

/**
 * 
 * @author snjeza
 *
 */
public class GettingStartedPage extends AbstractJBossCentralPage {

	private static final String CLASS_ATTRIBUTE = "class";
	public static final String ID = ID_PREFIX + "GettingStartedPage";
	
	protected static final long TIME_DELAY = 2000L;
	private IWorkbenchAction newWizardDropDownAction;
	private ScrolledForm form;
	private PageBook newsPageBook;
	private ScrolledComposite scrollComposite;
	private RefreshNewsJobChangeListener refreshNewsJobChangeListener;
	private FormText newsNoteText;
	private FormText tutorialsNoteText;
	private Composite newsLoadingComposite;
	private Composite tutorialsLoadingComposite;
	private FormText newsExceptionText;
	private FormText tutorialsExceptionText;
	private Composite newsComposite;
	private Composite tutorialsComposite;
	private FormToolkit toolkit;
	private ScrolledComposite tutorialScrollComposite;
	private PageBook tutorialPageBook;
	private RefreshTutorialsJobChangeListener refreshTutorialsJobChangeListener;
	private Section newsSection;
	private Section tutorialsSection;
	private Section documentationSection;
	private Section projectsSection;
	private Composite projectsComposite;
	private Composite documentationComposite;
	
	private Set<TutorialCategory> expandedCategories = new HashSet<TutorialCategory>();
	
	public GettingStartedPage(FormEditor editor) {
		super(editor, ID, "Getting Started");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		
		Composite body = form.getBody();
	    GridLayout gridLayout = new GridLayout(2, true);
	    gridLayout.horizontalSpacing = 7;
	    body.setLayout(gridLayout);
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    body.setLayoutData(gd);
	    toolkit.paintBordersFor(body);
	    
		Composite left = createComposite(toolkit, body);
		createProjectsSection(toolkit, left);
		createTutorialsSection(toolkit, left);
		createDocumentationSection(toolkit, left);
		toolkit.paintBordersFor(left);
		
		Composite right = createComposite(toolkit, body);
	    createNewsSection(toolkit, right);
		toolkit.paintBordersFor(right);
		
		final ControlAdapter controlAdapter = new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				resize();
			}
		
		};
		form.addControlListener(controlAdapter);
		form.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				form.removeControlListener(controlAdapter);
				form.removeDisposeListener(this);
			}
		});
		
		resize();
	    
	}

	private void createNewsSection(FormToolkit toolkit, Composite parent) {
		newsSection = createSection(toolkit, parent, "News", ExpandableComposite.TITLE_BAR|ExpandableComposite.EXPANDED);
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    //gd.widthHint = 350;
	    //gd.heightHint = 100;
	    newsSection.setLayoutData(gd);
		createNewsToolbar(toolkit, newsSection);
				
		scrollComposite = new ScrolledComposite(newsSection, SWT.V_SCROLL);
		gd =new GridData(SWT.FILL, SWT.FILL, true, false);
		scrollComposite.setLayoutData(gd);
		scrollComposite.setLayout(new GridLayout());
		
		newsPageBook = new PageBook(scrollComposite, SWT.WRAP);
		gd =new GridData(SWT.FILL, SWT.FILL, true, false);
	    newsPageBook.setLayoutData(gd);
        
        scrollComposite.setContent(newsPageBook);
    	scrollComposite.setExpandVertical(true);
    	scrollComposite.setExpandHorizontal(true);
    	scrollComposite.setAlwaysShowScrollBars(false);
//    	scrollComposite.addControlListener(new ControlAdapter() {
//    		public void controlResized(ControlEvent e) {
//    			recomputeScrollComposite(scrollComposite, newsPageBook);
//    		}
//    	});

    	newsNoteText = createNoteText(toolkit, newsPageBook);
	    newsLoadingComposite = createLoadingComposite(toolkit, newsPageBook);	    
	    newsExceptionText = createExceptionText(toolkit, newsPageBook);
		
	    newsComposite = toolkit.createComposite(newsPageBook, SWT.NONE);	    
		newsComposite.setLayout(new TableWrapLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(newsComposite);
	    //newsComposite.setLayoutData(gd);

		newsSection.setClient(scrollComposite);
		showLoading(newsPageBook, newsLoadingComposite, scrollComposite);
		newsPageBook.pack(true);
		RefreshNewsJob refreshNewsJob = RefreshNewsJob.INSTANCE;
		refreshNewsJobChangeListener = new RefreshNewsJobChangeListener();
		refreshNewsJob.addJobChangeListener(refreshNewsJobChangeListener);
		refreshNewsJob.schedule();
	}

	private FormText createExceptionText(FormToolkit toolkit, Composite parent) {
		FormText formText = toolkit.createFormText(parent, true);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false);
	    formText.setLayoutData(gd);
		return formText;
	}
	
	private FormText createNoteText(FormToolkit toolkit, Composite parent) {
		FormText formText = toolkit.createFormText(parent, true);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false);
	    formText.setLayoutData(gd);
		formText.setText("<form><p>" +
	    		"<img href=\"image\"/>" + 
	    		" No entries found." +
				"Check your internet connection and " +
				"<a href=\"networkConnections\">Window > Preferences > General > Network Connections</a> " +
				"preferences" +
				"</p></form>",
				true, false);
		
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);

		formText.setImage("image", image);
		formText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				if ("networkConnections".equals(e.data)) {
					PreferenceDialog dialog = PreferencesUtil
							.createPreferenceDialogOn(null,
									"org.eclipse.ui.net.NetPreferences", null,
									null);
					dialog.open();
				}
			}
		});
		return formText;
	}

	private void createNewsToolbar(FormToolkit toolkit, Section section) {
		Composite headerComposite = toolkit.createComposite(section, SWT.NONE);
	    RowLayout rowLayout = new RowLayout();
	    rowLayout.marginTop = 0;
	    rowLayout.marginBottom = 0;
	    headerComposite.setLayout(rowLayout);
	    headerComposite.setBackground(null);
	    
	    ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		toolBarManager.createControl(headerComposite);
		
		CommandContributionItem item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.openJBossNews");
		toolBarManager.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.refreshJBossNews");
		toolBarManager.add(item);

	    toolBarManager.update(true);
	    
		section.setTextClient(headerComposite);
	}
	
	private void createTutorialsSection(FormToolkit toolkit, Composite parent) {
		tutorialsSection = createSection(toolkit, parent, "Project Examples", ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE);
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
	    //gd.widthHint = 350;
	    //gd.heightHint = 100;
	    tutorialsSection.setLayoutData(gd);
	    
	    createTutorialsToolbar(toolkit, tutorialsSection);
		
	    tutorialScrollComposite = new ScrolledComposite(tutorialsSection, SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		tutorialScrollComposite.setLayoutData(gd);
		tutorialScrollComposite.setLayout(new GridLayout());
		toolkit.adapt(tutorialScrollComposite);
		
		tutorialPageBook = new PageBook(tutorialScrollComposite, SWT.WRAP);
		gd =new GridData(SWT.FILL, SWT.FILL, false, false);
	    tutorialPageBook.setLayoutData(gd);
        
        tutorialScrollComposite.setContent(tutorialPageBook);
    	tutorialScrollComposite.setExpandVertical(true);
    	tutorialScrollComposite.setExpandHorizontal(true);
    	tutorialScrollComposite.setAlwaysShowScrollBars(false);
//    	tutorialScrollComposite.addControlListener(new ControlAdapter() {
//    		public void controlResized(ControlEvent e) {
//    			recomputeScrollComposite(tutorialScrollComposite, tutorialPageBook);
//    		}
//    	});
	    		
	    tutorialsNoteText = createNoteText(toolkit, tutorialPageBook);
	    tutorialsLoadingComposite = createLoadingComposite(toolkit, tutorialPageBook);	    
	    tutorialsExceptionText = createExceptionText(toolkit, tutorialPageBook);

	    tutorialsComposite = toolkit.createComposite(tutorialPageBook, SWT.NONE);	    
		tutorialsComposite.setLayout(new GridLayout());
		gd =new GridData(SWT.FILL, SWT.FILL, false, false);
		tutorialsComposite.setLayoutData(gd);
				        
	    tutorialsSection.setClient(tutorialScrollComposite);
	    
	    tutorialsSection.addExpansionListener(new ExpansionAdapter() {
						
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				resize();
			}
		});
	    
		showLoading(tutorialPageBook, tutorialsLoadingComposite, tutorialScrollComposite);
		tutorialPageBook.pack(true);
		RefreshTutorialsJob refreshTutorialsJob = RefreshTutorialsJob.INSTANCE;
		refreshTutorialsJobChangeListener = new RefreshTutorialsJobChangeListener();
		refreshTutorialsJob.addJobChangeListener(refreshTutorialsJobChangeListener);
		refreshTutorialsJob.schedule();
	}

	private void createTutorialsToolbar(FormToolkit toolkit, Section section) {
		Composite headerComposite = toolkit.createComposite(section, SWT.NONE);
	    RowLayout rowLayout = new RowLayout();
	    rowLayout.marginTop = 0;
	    rowLayout.marginBottom = 0;
	    headerComposite.setLayout(rowLayout);
	    headerComposite.setBackground(null);
	    
	    ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		toolBarManager.createControl(headerComposite);
		
		CommandContributionItem item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.newProjectExamplesWizard");
		toolBarManager.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.runtime.preferences");
		toolBarManager.add(item);

		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.seam.runtime.preferences");
		toolBarManager.add(item);

		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.wtp.runtime.preferences");
		toolBarManager.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.refreshJBossTutorials");
		toolBarManager.add(item);

		//Action action = new DownloadRuntimeAction("Download and Install JBoss AS 7.0.1", JBossCentralActivator.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, "/icons/jbossas7.png"), "org.jboss.tools.runtime.core.as.701");
		//toolBarManager.add(action);

	    toolBarManager.update(true);
	    
		section.setTextClient(headerComposite);
	}
	
	public void createProjectsSection(FormToolkit toolkit, Composite parent) {
		projectsSection = createSection(toolkit, parent, "Create Projects", ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED);
		projectsSection.setText("Create Projects");
	    projectsSection.setLayout(new GridLayout());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
	    //gd.widthHint = 350;
	    //gd.heightHint = 100;
	    projectsSection.setLayoutData(gd);
	    
		
	    Composite headerComposite = toolkit.createComposite(projectsSection, SWT.NONE);
	    RowLayout rowLayout = new RowLayout();
	    rowLayout.marginTop = 0;
	    rowLayout.marginBottom = 0;
	    headerComposite.setLayout(rowLayout);
	    headerComposite.setBackground(null);
	    
	    ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		toolBarManager.createControl(headerComposite);
	
	    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		newWizardDropDownAction = IDEActionFactory.NEW_WIZARD_DROP_DOWN
                .create(window);
	    toolBarManager.add(newWizardDropDownAction);
	    toolBarManager.update(true);
	    
		projectsSection.setTextClient(headerComposite);
		
		projectsComposite = toolkit.createComposite(projectsSection);
	    GridLayout layout = new GridLayout(2, true);
	    layout.horizontalSpacing = 10;
	    projectsComposite.setLayout(layout);
	    GridDataFactory.fillDefaults().grab(true, true).applyTo(projectsComposite);

	    IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
	    IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.eclipse.ui.newWizards");
	    IExtension[] extensions = extensionPoint.getExtensions();
	    
	    List<String> wizardIDs = new ArrayList<String>();
	    wizardIDs.add("org.eclipse.jst.servlet.ui.project.facet.WebProjectWizard");
	    wizardIDs.add("org.jboss.tools.seam.ui.wizards.SeamProjectWizard");
	    wizardIDs.add("org.eclipse.m2e.core.wizards.Maven2ProjectWizard");
	    wizardIDs.add(JBossCentralActivator.NEW_PROJECT_EXAMPLES_WIZARD_ID);
	    
	    List<String> createdIDs = new ArrayList<String>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				String id = element.getAttribute("id");
				if (wizardIDs.contains(id) && !createdIDs.contains(id)) {
					createProjectLink(toolkit, projectsComposite, element);
					createdIDs.add(id);
				}
			}
		}
		projectsSection.setClient(projectsComposite);
	}

	private void createProjectLink(FormToolkit toolkit, Composite composite,
			final IConfigurationElement element) {
		if (element == null) {
			return;
		}
		String name = element.getAttribute("name");
		String id = element.getAttribute("id");
		if (name == null || id == null) {
			return;
		}
		String iconPath = element.getAttribute("icon");
		Image image = null;
		if (iconPath != null) {
			IContributor contributor = element.getContributor();
			String pluginId = contributor.getName();
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, iconPath);
			if (imageDescriptor != null) {
				image = JBossCentralActivator.getDefault().getImage(imageDescriptor);
			}
		}
		ImageHyperlink link = toolkit.createImageHyperlink(composite, SWT.NONE);
	    link.setText(name);
	    if (image != null) {
	    	link.setImage(image);
	    }
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(link);
	    link.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				try {
					Object object = createExtension(element);
					if (object instanceof INewWizard) {
				          INewWizard wizard = (INewWizard)object;
				          ISelection selection = getSite().getSelectionProvider().getSelection();
				          if (selection instanceof IStructuredSelection) {
				        	  wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) selection);
				          }
				          WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
				          dialog.open();
					}
				} catch (CoreException e1) {
					JBossCentralActivator.log(e1);
				}
			}
	    	
	    });
	}

	public static Object createExtension(final IConfigurationElement element) throws CoreException {
		if (element == null) {
			return null;
		}
		try {
			Bundle bundle = Platform.getBundle(element.getContributor().getName());
			if (isActive(bundle)) {
				return element.createExecutableExtension(CLASS_ATTRIBUTE);
			}
			final Object[] ret = new Object[1];
			final CoreException[] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable() {
			    public void run() {
			        try {
			            ret[0] = element
			                    .createExecutableExtension(CLASS_ATTRIBUTE);
			        } catch (CoreException e) {
			            exc[0] = e;
			        }
			    }
			});
			if (exc[0] != null) {
				throw exc[0];
			}
			return ret[0];
		} catch (InvalidRegistryObjectException e) {
			throw new CoreException(new Status(IStatus.ERROR, JBossCentralActivator.PLUGIN_ID,
                    IStatus.ERROR, "Cannot create extension" ,e));
		}
	}
	
	private static boolean isActive(Bundle bundle) {
		if (bundle == null) {
			return false;
		}
		return bundle.getState() == Bundle.ACTIVE;
	}
	
	public void createDocumentationSection(FormToolkit toolkit, Composite parent) {
		documentationSection = createSection(toolkit, parent, "Documentation", ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE);
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
	    //gd.widthHint = 350;
	    //gd.heightHint = 100;
	    documentationSection.setLayoutData(gd);
	    
		documentationComposite = toolkit.createComposite(documentationSection);
	    GridLayout layout = new GridLayout(2, true);
	    layout.horizontalSpacing = 30;
	    documentationComposite.setLayout(layout);
	    GridDataFactory.fillDefaults().grab(true, true).applyTo(documentationComposite);
	    
		addHyperlink(toolkit, documentationComposite, "New and Noteworthy", "http://docs.jboss.org/tools/whatsnew/");
		addHyperlink(toolkit, documentationComposite, "User Forum", "http://community.jboss.org/en/tools?view=discussions");
		
		addHyperlink(toolkit, documentationComposite, "Reference", "http://docs.jboss.org/tools/latest/");
		addHyperlink(toolkit, documentationComposite, "Developer Forum", "http://community.jboss.org/en/tools/dev?view=discussions");
		
		addHyperlink(toolkit, documentationComposite, "FAQ", "http://www.jboss.org/tools/docs/faq");
		addHyperlink(toolkit, documentationComposite, "Wiki", "http://community.jboss.org/wiki/JBossTools");
		
		addHyperlink(toolkit, documentationComposite, "Screencasts", "http://docs.jboss.org/tools/movies/");
		addHyperlink(toolkit, documentationComposite, "Issue Tracker", "https://issues.jboss.org/browse/JBIDE");
		
		
		documentationSection.setClient(documentationComposite);
	}

	private void addHyperlink(FormToolkit toolkit, Composite composite, String text, final String url) {
		Hyperlink link = toolkit.createHyperlink(composite,
				text, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				JBossCentralActivator.openUrl(url, getSite().getShell());
			}
		});
	}

	@Override
	public void dispose() {
		newWizardDropDownAction.dispose();
		newWizardDropDownAction = null;
		if (refreshNewsJobChangeListener != null) {
			RefreshNewsJob.INSTANCE.removeJobChangeListener(refreshNewsJobChangeListener);
			refreshNewsJobChangeListener = null;
		}
		if (refreshTutorialsJobChangeListener != null) {
			RefreshTutorialsJob.INSTANCE.removeJobChangeListener(refreshTutorialsJobChangeListener);
			refreshTutorialsJobChangeListener = null;
		}
		super.dispose();
	}

	public boolean showLoading(final PageBook pageBook, final Composite composite, final ScrolledComposite scrolledComposite) {
		if (pageBook.isDisposed()) {
			return false;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				pageBook.showPage(composite);
				form.reflow(true);
				form.redraw();
				recomputeScrollComposite(scrolledComposite, pageBook);
			}
		});
		
		return true;
	}

	private Image getNewsImage() {
		return JBossCentralActivator.getDefault().getImage("/icons/newsLink.gif");
	}
	
	private void recomputeScrollComposite(ScrolledComposite composite, PageBook pageBook) {
		Rectangle r = composite.getClientArea();
		composite.setMinSize(pageBook.computeSize(r.width, SWT.DEFAULT));
	}

	public void showNote(final PageBook pageBook, final FormText noteText, final ScrolledComposite scrolledComposite) {
		if (pageBook.isDisposed()) {
			return;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				pageBook.showPage(noteText);
				form.reflow(true);
				form.redraw();
				recomputeScrollComposite(scrolledComposite, pageBook);
			}
		});
	}
	
	private void showException(PageBook pageBook, FormText exceptionText, Exception e) {
		JBossCentralActivator.log(e);
		String message = StringEscapeUtils.escapeXml(e.getMessage());
		String text = JBossCentralActivator.FORM_START_TAG +
				"<img href=\"image\"/> " + 
				message +
				JBossCentralActivator.FORM_END_TAG;
		exceptionText.setText(text, true, false);
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		exceptionText.setImage("image", image);
		pageBook.showPage(exceptionText);
	}

	public void refreshNews() {
		RefreshNewsJob job = RefreshNewsJob.INSTANCE;
		if (job.getState() == Job.NONE) {
			if (job.getException() != null) {
				showException(newsPageBook, newsExceptionText,
						job.getException());
				return;
			}
			List<NewsEntry> entries = job.getEntries();
			if (entries == null || entries.size() == 0) {
				showNote(newsPageBook, newsNoteText, scrollComposite);
				return;
			}
			showNews(entries);
		}
	}
	
	public void refreshTutorials() {
		RefreshTutorialsJob job = RefreshTutorialsJob.INSTANCE;
		if (job.getException() != null) {
			showException(tutorialPageBook, tutorialsExceptionText, job.getException());
			return;
		}
		Map<String, TutorialCategory> categories = job.getTutorialCategories();
		if (categories == null || categories.size() == 0) {
			showNote(tutorialPageBook, tutorialsNoteText, tutorialScrollComposite);
			return;
		}
		showTutorials(categories);
	}

	private void showTutorials(Map<String, TutorialCategory> categories) {
		disposeChildren(tutorialsComposite);
		Collection<TutorialCategory> tempCategories = categories.values();
		List<TutorialCategory> sortedCategories = new ArrayList<TutorialCategory>();
		sortedCategories.addAll(tempCategories);
		Collections.sort(sortedCategories);
		for (final TutorialCategory category:sortedCategories) {
			int style = ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE;
			if (expandedCategories.contains(category)) {
				style|=ExpandableComposite.EXPANDED;
			}
			final ExpandableComposite categoryComposite = toolkit.createExpandableComposite(tutorialsComposite, 
					style);
			categoryComposite.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TB_TOGGLE));
			categoryComposite.setText(category.getName());
			GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
			categoryComposite.setLayoutData(gd);
			categoryComposite.setLayout(new GridLayout());
			final Composite composite = toolkit.createComposite(categoryComposite);
			gd = new GridData(SWT.FILL, SWT.FILL, false, false);
			composite.setLayoutData(gd);
			composite.setLayout(new GridLayout(1, false));
			
			categoryComposite.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					if (e.getState()) {
						expandedCategories.add(category);
					} else {
						expandedCategories.remove(category);
					}
					resize();
				}
			});

			for (final Tutorial tutorial:category.getTutorials()) {
				Project project = tutorial.getProjectExamples();
				if (project == null) {
					continue;
				}
				FormText tutorialText = toolkit.createFormText(composite, true);
				configureTutorialText(tutorialText, tutorial);
				hookTooltip(tutorialText, tutorial);
			}
			categoryComposite.setClient(composite);
		}
		
		tutorialPageBook.showPage(tutorialsComposite);
		form.reflow(true);
		form.redraw();
		resize();
		//recomputeScrollComposite(tutorialScrollComposite, tutorialPageBook);
	}

	private void hookTooltip(FormText tutorialText, Tutorial tutorial) {
		final String description = JBossCentralActivator.getDefault().getDescription(tutorial);
		if (description != null && !description.isEmpty()) {
			ToolTip toolTip = new DescriptionToolTip(tutorialText, description);
			toolTip.activate();
		}
	}

	protected void configureTutorialText(FormText tutorialText, final Tutorial tutorial) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(JBossCentralActivator.FORM_START_TAG);
		//boolean haveImage = tutorial.getIconPath() != null && JBossCentralActivator.getDefault().getImage(tutorial.getIconPath()) != null;
		//if (haveImage) {
		//	buffer.append("<img href=\"image\"/> ");
		//}
		//if (project.getUnsatisfiedFixes().size() > 0) {
		buffer.append("<img href=\"image\"/> ");
		//}
		buffer.append("<a href=\"link\">");
		buffer.append(tutorial.getName());
		buffer.append("</a> ");
		
		buffer.append(JBossCentralActivator.FORM_END_TAG);
		String text = buffer.toString();
		tutorialText.setText(text , true, false);
		Image image;
		Project project = tutorial.getProjectExamples();
		List<ProjectFix> fixes = project.getFixes();
		List<ProjectFix> unsatisfiedFixes = new ArrayList<ProjectFix>();
		project.setUnsatisfiedFixes(unsatisfiedFixes);
		for (ProjectFix fix:fixes) {
			if (!ProjectExamplesActivator.canFix(project, fix)) {
				unsatisfiedFixes.add(fix);
			}
		}
		if (project.getUnsatisfiedFixes().size() > 0) {
			image = JBossCentralActivator.getDefault().getImage("/icons/nwarning.gif");
		} else {
			image = JBossCentralActivator.getDefault().getImage(tutorial.getIconPath());
		}
		tutorialText.setImage("image", image);
		tutorialText.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				Object object = e.data;
				if (object instanceof String) {
					ProjectExamplesDialog dialog = new ProjectExamplesDialog(getSite().getShell(), tutorial);
					dialog.open();
					refreshTutorials();
				}
			}
			
		});

	}

	private void disposeChildren(Composite composite) {
		Control[] children = composite.getChildren();
		for (Control child:children) {
			if (child instanceof Composite) {
				disposeChildren((Composite) child);
				child.dispose();
			} else {
				child.dispose();
			}
		}
	}

	private void showNews(List<NewsEntry> entries) {
		int i = 0;
		disposeChildren(newsComposite);
		
		for (final NewsEntry entry:entries) {
			if (i++ > JBossCentralActivator.MAX_FEEDS) {
				return;
			}
			String text = entry.getFormString();
			final FormText formText = toolkit.createFormText(newsComposite, true);
			TableWrapData td = new TableWrapData();
			td.indent = 2;
			Point size = newsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			td.maxWidth = size.x - 2;
			//formText.setText(text, true, true);
			//Display display = Display.getCurrent();
			//formText.setFont(getLinkFont(display));
			formText.setFont("default", JFaceResources.getDefaultFont());
			//formText.setForeground(JFaceColors.getHyperlinkText(getDisplay()));
			formText.setFont("description", JFaceResources.getDefaultFont());
			//Font boldFont = getAuthorFont(display);
			//formText.setFont("author", boldFont);
			formText.setColor("author", JFaceColors.getHyperlinkText(getDisplay()));
			formText.setImage("image", getNewsImage());
			if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
				ToolTip toolTip = new NewsToolTip(formText, entry.getDescription());
				toolTip.activate();
			}
			formText.addHyperlinkListener(new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					Object link = e.data;
					if (link instanceof String) {
						Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
						JBossCentralActivator.openUrl((String) link, shell);
					}
				}
			});
			
		}
		newsPageBook.showPage(newsComposite);
		form.reflow(true);
		form.redraw();
		recomputeScrollComposite(scrollComposite, newsPageBook);
	}

	protected void resize() {
		Point size = form.getSize();
		GridData gd;
		int widthHint = size.x/2 - 40;
		
		gd = (GridData) newsSection.getLayoutData();
		gd.heightHint = size.y - 40;
		gd.widthHint = widthHint;
		gd.grabExcessVerticalSpace = false;
		Point computedSize = newsSection.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		newsSection.setSize(widthHint, computedSize.y);
		
		gd = (GridData) tutorialsSection.getLayoutData();
		//gridData.heightHint = size.y - 40;
		gd.widthHint = widthHint;
		gd.grabExcessVerticalSpace = false;
		tutorialPageBook.pack();
		computedSize = tutorialPageBook.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		tutorialsSection.setSize(widthHint, computedSize.y);
		
		gd = (GridData) documentationSection.getLayoutData();
		//gridData.heightHint = size.y - 40;
		gd.widthHint = widthHint;
		gd.grabExcessVerticalSpace = false;
		computedSize = documentationSection.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		documentationSection.setSize(widthHint, computedSize.y);
		
		gd = (GridData) projectsSection.getLayoutData();
		//gridData.heightHint = size.y - 40;
		gd.widthHint = widthHint;
		gd.grabExcessVerticalSpace = false;
		computedSize = projectsSection.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		projectsSection.setSize(widthHint, computedSize.y);
		
		form.reflow(true);
		form.redraw();
		scrollComposite.setMinSize(widthHint, size.y - 55);
		
		computedSize = tutorialPageBook.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int y = computedSize.y;
		if (y > 200) {
			y = 200;
		}
		tutorialScrollComposite.setMinSize(widthHint, y);
		refreshNews();
		form.layout(true, true);
	}

	private class RefreshNewsJobChangeListener implements IJobChangeListener {

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
					refreshNews();
				}
			});
			
		}

		@Override
		public void running(IJobChangeEvent event) {
			
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			RefreshNewsJob.INSTANCE.setException(null);
			showLoading(newsPageBook, newsLoadingComposite, scrollComposite);
		}

		@Override
		public void sleeping(IJobChangeEvent event) {
			
		}
		
	}
	
	private class RefreshTutorialsJobChangeListener implements IJobChangeListener {

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
					refreshTutorials();
				}
			});
			
		}

		@Override
		public void running(IJobChangeEvent event) {
			
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			RefreshTutorialsJob.INSTANCE.setException(null);
			showLoading(tutorialPageBook, tutorialsLoadingComposite, tutorialScrollComposite);
		}

		@Override
		public void sleeping(IJobChangeEvent event) {
			
		}
		
	}
	
}
