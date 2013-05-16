/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/

package org.jboss.tools.central.editors;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringEscapeUtils;
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
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
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
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.internal.discovery.ExpressionBasedBundleDiscoveryStrategy;
import org.jboss.tools.central.internal.discovery.ExpressionBasedRemoteBundleDiscoveryStrategy;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizard;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager.ProxyWizardManagerListener;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager.UpdateEvent;
import org.jboss.tools.central.jobs.RefreshBuzzJob;
import org.jboss.tools.central.jobs.RefreshTutorialsJob;
import org.jboss.tools.central.model.FeedsEntry;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.jboss.tools.project.examples.wizard.NewProjectExamplesWizard2;
import org.osgi.framework.Bundle;

/**
 * 
 * @author snjeza
 * @author Fred Bricon
 *
 */
public class GettingStartedPage extends AbstractJBossCentralPage implements ProxyWizardManagerListener {

	private static final String BUZZ_WARNING_ID = "org.jboss.tools.central.buzzWarning";
	
	private static final String CLASS_ATTRIBUTE = "class";
	public static final String ID = ID_PREFIX + "GettingStartedPage";
	
	protected static final long TIME_DELAY = 2000L;
	private IWorkbenchAction newWizardDropDownAction;
	private ScrolledForm form;
	private PageBook buzzPageBook;
	private ScrolledComposite buzzScrollComposite;
	private RefreshBuzzJobChangeListener RefreshBuzzJobChangeListener;
	private FormText buzzNoteText;
	private FormText tutorialsNoteText;
	private Composite buzzLoadingComposite;
	private Composite tutorialsLoadingComposite;
	private FormText buzzExceptionText;
	private FormText tutorialsExceptionText;
	private Composite buzzComposite;
	private Composite tutorialsComposite;
	private Composite comprehensiveTutorialComposite;
	private FormToolkit toolkit;
	private ScrolledComposite tutorialScrollComposite;
	private PageBook tutorialPageBook;
	private RefreshTutorialsJobChangeListener refreshTutorialsJobChangeListener;
	private Section buzzSection;
	private Section tutorialsSection;
	private Section documentationSection;
	private Section projectsSection;
	private Section comprehensiveTutorialSection;
	private Composite projectsComposite;
	private Composite documentationComposite;
	
	private Point oldSize;
	private ToolBarManager buzzToolBarManager;
	private Action buzzWarning;
	private ToolBarManager tutorialsToolBarManager;

	HyperlinkSettings hyperlinkSettings;
	private ScrolledComposite descriptionCompositeScroller;
	private Label descriptionLabel;

	protected ImageHyperlink highlightedLink;

	private Composite descriptionComposite;

	public GettingStartedPage(FormEditor editor) {
		super(editor, ID, "Getting Started");
		
		ProxyWizardManager.INSTANCE.registerListener(this);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		hyperlinkSettings = new HyperlinkSettings(getDisplay());
		hyperlinkSettings.setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_NEVER);
		
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		
		Composite body = form.getBody();
	    body.setLayout(new GridLayout(1,false));
	    
	    //Project section
		Composite top = createComposite(toolkit, body);
		createProjectsSection(toolkit, top);
		
		//Quickstarts + tutorial   
	    Composite middle = createComposite(toolkit, body);
	    ((GridLayout)middle.getLayout()).makeColumnsEqualWidth = false;
	    ((GridLayout)middle.getLayout()).numColumns = 2;
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(middle);
	    createSamplesSection(toolkit, middle);
		createComprehensiveTutorial(toolkit, middle);

		//JBoss Buzz + Doc
		Composite bottom = createComposite(toolkit, body);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		createBuzzSection(toolkit, bottom);
		createDocumentationSection(toolkit, bottom);

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
	}

	private void createComprehensiveTutorial(FormToolkit toolkit,	Composite parent) {
		
		comprehensiveTutorialSection = createSection(toolkit, parent, "Comprehensive Tutorial", ExpandableComposite.TITLE_BAR);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, true);
		comprehensiveTutorialSection.setLayoutData(gd);
		
		comprehensiveTutorialComposite = toolkit.createComposite(comprehensiveTutorialSection);
		GridLayout layout = new GridLayout();
	    layout.horizontalSpacing = 10;
	    comprehensiveTutorialComposite.setLayout(layout);
	    ImageHyperlink thumb = toolkit.createImageHyperlink(comprehensiveTutorialComposite, SWT.NONE);
	    Image ticketMonsterImage = JBossCentralActivator.getDefault().getImage("/icons/TicketMonster_thumbnail.png");;
		thumb.setImage(ticketMonsterImage);
		thumb.setHref("http://www.jboss.org/jdf/examples/get-started/");
		thumb.addHyperlinkListener(new HyperlinkOpener());
		thumb.setToolTipText("Open the Ticket Monster tutorial on the JBoss Developer Framework site");
		GridDataFactory.fillDefaults().grab(true, true).applyTo(comprehensiveTutorialComposite);
		comprehensiveTutorialSection.setClient(comprehensiveTutorialComposite);
	}
		

	private void createBuzzSection(FormToolkit toolkit, Composite parent) {
		buzzSection = createSection(toolkit, parent, "JBoss Buzz", ExpandableComposite.TITLE_BAR);
		buzzSection.setLayout(new GridLayout(1, false));
		buzzSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    
		createBuzzToolbar(toolkit, buzzSection);
				
		buzzScrollComposite = new ScrolledComposite(buzzSection, SWT.V_SCROLL);
		buzzScrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		buzzScrollComposite.setLayout(new GridLayout());
		toolkit.adapt(buzzScrollComposite);
		
		buzzPageBook = new PageBook(buzzScrollComposite, SWT.WRAP);
	    buzzPageBook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        
        buzzScrollComposite.setContent(buzzPageBook);
    	buzzScrollComposite.setExpandVertical(true);
    	buzzScrollComposite.setExpandHorizontal(true);
    	buzzScrollComposite.setAlwaysShowScrollBars(false);

    	buzzNoteText = createNoteText(toolkit, buzzPageBook);
	    buzzLoadingComposite = createLoadingComposite(toolkit, buzzPageBook);	    
	    buzzExceptionText = createExceptionText(toolkit, buzzPageBook);
		
	    buzzComposite = toolkit.createComposite(buzzPageBook, SWT.NONE);	    
		buzzComposite.setLayout(new TableWrapLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buzzComposite);
		buzzSection.setClient(buzzScrollComposite);
		
		showLoading(buzzPageBook, buzzLoadingComposite, buzzScrollComposite);
		buzzPageBook.pack(true);
		RefreshBuzzJob job = RefreshBuzzJob.INSTANCE;
		job.setException(null);
		if (job.getEntries().size() > 0) {
			job.setNeedsRefresh(true);
			refreshBuzz();
			job.setNeedsRefresh(false);
		}
		RefreshBuzzJobChangeListener = new RefreshBuzzJobChangeListener();
		job.addJobChangeListener(RefreshBuzzJobChangeListener);
		job.schedule();
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
				"</p></form>",
				true, false);
		
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);

		formText.setImage("image", image);
		return formText;
	}

	private void createBuzzToolbar(FormToolkit toolkit, Section section) {
		Composite headerComposite = toolkit.createComposite(section, SWT.NONE);
	    RowLayout rowLayout = new RowLayout();
	    rowLayout.marginTop = 0;
	    rowLayout.marginBottom = 0;
	    headerComposite.setLayout(rowLayout);
	    headerComposite.setBackground(null);
	    
	    
	    buzzToolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		buzzToolBarManager.createControl(headerComposite);
		
		buzzWarning = new Action("Warning", JBossCentralActivator.getImageDescriptor("/icons/nwarning.gif")) {};
		buzzWarning.setId(BUZZ_WARNING_ID);
		buzzWarning.setActionDefinitionId(BUZZ_WARNING_ID);

		buzzToolBarManager.add(buzzWarning);
		
		setItemVisible(buzzToolBarManager, BUZZ_WARNING_ID, false);
		
		CommandContributionItem item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.openJBossBuzz");
		buzzToolBarManager.add(item);

		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.openJBossNews");
		buzzToolBarManager.add(item);

		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.openJBossToolsTwitter");
		buzzToolBarManager.add(item);
				
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.refreshJBossBuzz");
		buzzToolBarManager.add(item);

	    buzzToolBarManager.update(true);
	    
		section.setTextClient(headerComposite);
	}
	
	protected void setItemVisible(ToolBarManager toolBarManager, String id, boolean value) {
		IContributionItem[] items = toolBarManager.getItems();
		for (IContributionItem item:items)  {
			if (id.equals(item.getId())) {
				item.setVisible(value);
			}
		}
	}
	
	private void createSamplesSection(FormToolkit toolkit, Composite parent) {
		tutorialsSection = createSection(toolkit, parent, "Start from a sample", ExpandableComposite.TITLE_BAR);
	    GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
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
	    		
	    tutorialsNoteText = createNoteText(toolkit, tutorialPageBook);
	    tutorialsLoadingComposite = createLoadingComposite(toolkit, tutorialPageBook);	    
	    tutorialsExceptionText = createExceptionText(toolkit, tutorialPageBook);

	    tutorialsComposite = toolkit.createComposite(tutorialPageBook, SWT.NONE);	    
		tutorialsComposite.setLayout(new GridLayout());
		gd =new GridData(SWT.FILL, SWT.FILL, true, false);
		tutorialsComposite.setLayoutData(gd);
	    tutorialsSection.setClient(tutorialScrollComposite);

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
	    
	    tutorialsToolBarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		tutorialsToolBarManager.createControl(headerComposite);
		
		CommandContributionItem item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.newProjectExamplesWizard");
		tutorialsToolBarManager.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.runtime.preferences");
		tutorialsToolBarManager.add(item);

		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.wtp.runtime.preferences");
		tutorialsToolBarManager.add(item);
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.refreshJBossTutorials");
		tutorialsToolBarManager.add(item);

	    tutorialsToolBarManager.update(true);
	    
		section.setTextClient(headerComposite);
	}
	
	public void createProjectsSection(FormToolkit toolkit, Composite parent) {
		projectsSection = createSection(toolkit, parent, "Start from scratch", ExpandableComposite.TITLE_BAR);
		projectsSection.setText("Start from scratch");
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(projectsSection);

	    //Create header / toolbar composite
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
 
	    //Create 3 col container composite
    	Composite scratchComposite = toolkit.createComposite(projectsSection, SWT.NONE);
	    GridLayoutFactory.fillDefaults().numColumns(3).spacing(10, SWT.DEFAULT).applyTo(scratchComposite);
	    
	    //Create icons / wizard composite
		projectsComposite = toolkit.createComposite(scratchComposite);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).applyTo(projectsComposite);
	    GridLayout layout = new GridLayout(3, true);
	    layout.horizontalSpacing = 10;
	    layout.marginBottom = 10;
	    projectsComposite.setLayout(layout);


	  
	  //Create vertical separator composite
	  Composite filler = createComposite(toolkit, scratchComposite);
      Color grey = toolkit.getColors().createColor("grey", 194, 194, 194);
      filler.setBackground(grey);
      GridDataFactory.fillDefaults().grab(false, true).hint(1, SWT.DEFAULT).applyTo(filler);
	    
	  //Create project wizard description composite
      descriptionCompositeScroller = new ScrolledComposite(scratchComposite, SWT.V_SCROLL);
      descriptionCompositeScroller.setLayout(new FillLayout());
      descriptionCompositeScroller.setAlwaysShowScrollBars(false);
      descriptionComposite = createComposite(toolkit, descriptionCompositeScroller);
      descriptionLabel = toolkit.createLabel(descriptionComposite, "", SWT.WRAP);
      descriptionCompositeScroller.setExpandVertical(true);
      descriptionCompositeScroller.setExpandHorizontal(true);
      descriptionCompositeScroller.setContent(descriptionComposite);
	  projectsSection.setClient(scratchComposite);
	  resizedescriptionCompositeScroller();
	  
      List<ProxyWizard> proxyWizards = getProxyWizards();
      addProxyWizardLinks(proxyWizards);
	}

	private void addProxyWizardLinks(List<ProxyWizard> proxyWizards) {
		Map<String, IConfigurationElement> installedWizardIds = getInstalledWizardIds();
		disposeChildren(projectsComposite);
		for (ProxyWizard proxyWizard : proxyWizards){
			createProjectLink(toolkit, projectsComposite, proxyWizard, installedWizardIds);
		}
	}

	private Map<String, IConfigurationElement> getInstalledWizardIds() {
	    IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
	    IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.eclipse.ui.newWizards");
	    IExtension[] extensions = extensionPoint.getExtensions();
	    Map<String, IConfigurationElement> installedWizards = new HashMap<String, IConfigurationElement>(extensions.length);
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				boolean isProjectWizard=Boolean.parseBoolean(element.getAttribute("project"));
				if (isProjectWizard) {
					String id = element.getAttribute("id");
					installedWizards.put(id, element);
				}
			}
		}
		return installedWizards;
	}

	private List<ProxyWizard> getProxyWizards() {
		ProxyWizardManager proxyWizardManager = ProxyWizardManager.INSTANCE; //FIXME lookup global instance.
		return proxyWizardManager.getProxyWizards(true, new NullProgressMonitor());
	}

	private void setDescriptionLabel(String text) {
		if (!text.equals(descriptionLabel.getText())) {
			descriptionLabel.setText(text);
			resizedescriptionCompositeScroller();
		}
	}
	
	private void resizedescriptionCompositeScroller() {
		  if (projectsComposite == null || descriptionCompositeScroller==null) return;
	      int total = form.getClientArea().width;
	      int projectsSize = projectsComposite.getClientArea().width + 60;
	      int width = total - projectsSize;

	      GridDataFactory.fillDefaults().grab(true, true).hint(width, descriptionCompositeScroller.getClientArea().height).applyTo(descriptionCompositeScroller);
 		  GridDataFactory.fillDefaults().hint(descriptionCompositeScroller.getClientArea().width, SWT.DEFAULT).applyTo(descriptionLabel);
		  recomputeScrollComposite(descriptionCompositeScroller, descriptionComposite);
		  descriptionCompositeScroller.layout(true, true);
	}
	
	private void  createProjectLink(FormToolkit toolkit,
			Composite composite, final ProxyWizard proxyWizard,
			final Map<String, IConfigurationElement> installedWizardIds) {
		
		final URL iconUrl = proxyWizard.getIconUrl();
		Image image = null;
		if (iconUrl != null) {
			image = createImageFromUrl(iconUrl);
		}
		final ImageHyperlink link = toolkit.createImageHyperlink(composite, SWT.NONE);
		link.setUnderlined(false);
	    link.setText(proxyWizard.getLabel());
	    if (image != null) {
	    	link.setImage(image);
	    	link.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					if (link.getImage() != null) {
						link.getImage().dispose();
						link.setImage(null);
					}
				}
			});
	    }
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(link);
	    link.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				try {
					IConfigurationElement element = installedWizardIds.get(proxyWizard.getWizardId());
					if (element == null) {
						//Wizard not installed
						installMissingWizard(proxyWizard.getRequiredComponentIds());
					} else {
						openWizard(element);
					}
				} catch (CoreException e1) {
					JBossCentralActivator.log(e1);
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	    	
	    });
	    
	    final Color originalColor = composite.getBackground();
	    final Color blueish = toolkit.getColors().createColor("blue", 217, 230, 248);

	    link.addListener(SWT.MouseEnter, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				if (highlightedLink != null && !highlightedLink.isDisposed()) {
					highlightedLink.setBackground(originalColor);
				}
				setDescriptionLabel(proxyWizard.getDescription());
				highlightedLink = link;
				highlightedLink.setBackground(blueish);
			}
		});
	}

	private Image createImageFromUrl(URL iconUrl) {

		if (!iconUrl.getProtocol().equals("jar")) {
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(iconUrl);
			return descriptor.createImage();
		}
		
		//Load from jar:
		Image image = null;
		try {
			String fileName = iconUrl.getFile();
			if (fileName.contains("!")) {
				String[] location = fileName.split("!");
				fileName = location[0];
				String imageName = URLDecoder.decode(location[1].substring(1), "utf-8");
				File file = new File(new URI(fileName));
				JarFile jarFile = null;
				InputStream inputStream = null; 
				try {
					jarFile = new JarFile(file);
					ZipEntry imageEntry = jarFile.getEntry(imageName);
					if (imageEntry != null) {
						inputStream = jarFile.getInputStream(imageEntry);
						image = new Image(getDisplay(), inputStream);
					} 
				}finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}	
					} catch (Exception e) {
						//ignore
					}
					try {
						if (jarFile != null) {
							jarFile.close();
						}
					} catch (Exception e) {
						//ignore
				    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return image;
	}

	private void openWizard(IConfigurationElement element) throws CoreException {
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
	}

	@SuppressWarnings("restriction")
	protected void installMissingWizard(final Collection<String> connectorIds) throws InvocationTargetException, InterruptedException {
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (!MessageDialog.openQuestion(shell, "Information", "The required features to use this wizard need to be installed. Do you want to proceed?")) {
			return;
		};

		
		final IStatus[] results = new IStatus[1];
		final ConnectorDiscovery[] connectorDiscoveries = new ConnectorDiscovery[1];
		
		//TODO Refactor the connector installation b/w the software page & project examples
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				connectorDiscoveries[0] = new ConnectorDiscovery();
				// look for descriptors from installed bundles
				connectorDiscoveries[0].getDiscoveryStrategies().add(new ExpressionBasedBundleDiscoveryStrategy());
				ExpressionBasedRemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new ExpressionBasedRemoteBundleDiscoveryStrategy();
				remoteDiscoveryStrategy.setDirectoryUrl(ProjectExamplesActivator.getDefault().getConfigurator().getJBossDiscoveryDirectory());
				connectorDiscoveries[0].getDiscoveryStrategies().add(remoteDiscoveryStrategy);

				connectorDiscoveries[0].setEnvironment(ProjectExamplesActivator.getEnvironment());
				connectorDiscoveries[0].setVerifyUpdateSiteAvailability(true);
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
			List<ConnectorDescriptor> installableConnectors = new ArrayList<ConnectorDescriptor>();
			for (DiscoveryConnector connector:connectors) {
				if (connectorIds.contains(connector.getId())) {
					installableConnectors.add(connector);
				}
			}
			DiscoveryUi.install(installableConnectors, context);
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

	private void displayTutorialLinks(final Collection<ProjectExample> tutorials, final Composite composite, boolean addTooltips) {
		for (final ProjectExample tutorial : tutorials) {
			FormText tutorialText = toolkit.createFormText(composite, true);
			configureTutorialText(tutorialText, tutorial);
			if (addTooltips) {
				hookTooltip(tutorialText, tutorial);
			}
		}
	}

	private static boolean isActive(Bundle bundle) {
		if (bundle == null) {
			return false;
		}
		return bundle.getState() == Bundle.ACTIVE;
	}
	
	public void createDocumentationSection(FormToolkit toolkit, Composite parent) {

		documentationSection = createSection(toolkit, parent, "Other resources", SWT.NONE);
		documentationSection.setLayout(new GridLayout(1, false));
		documentationSection.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		
		documentationComposite = toolkit.createComposite(documentationSection);
		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, true, true, 1, 1);
		documentationComposite.setLayoutData(gd);

		GridLayout layout = new GridLayout(1, true);
	    documentationComposite.setLayout(layout);
	    
		addHyperlink(toolkit, documentationComposite, "JBoss developer website", "http://docs.jboss.org/tools/");
		addHyperlink(toolkit, documentationComposite, "User Forum", "http://community.jboss.org/en/tools?view=discussions");		
		addHyperlink(toolkit, documentationComposite, "Developer Forum", "http://community.jboss.org/en/tools/dev?view=discussions");
		addHyperlink(toolkit, documentationComposite, "Product documentation", "http://docs.jboss.org/tools/latest/");
		addHyperlink(toolkit, documentationComposite, "Videos", "http://docs.jboss.org/tools/movies/");

		documentationSection.setClient(documentationComposite);
	}
	
	private void addHyperlink(FormToolkit toolkit, Composite composite, String text, final String url) {
		Hyperlink link = toolkit.createHyperlink(composite,
				text, SWT.NONE);
		link.setUnderlined(false);
		GridDataFactory.fillDefaults().applyTo(link);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				JBossCentralActivator.openUrl(url, getSite().getShell());
			}
		});
	}

	@Override
	public void dispose() {
		if (newWizardDropDownAction != null) {
			newWizardDropDownAction.dispose();
			newWizardDropDownAction = null;
		}
		if (buzzToolBarManager != null) {
			buzzToolBarManager.dispose();
			buzzToolBarManager = null;
		}
		if (tutorialsToolBarManager != null) {
			tutorialsToolBarManager.dispose();
			tutorialsToolBarManager = null;
		}
		if (RefreshBuzzJobChangeListener != null) {
			RefreshBuzzJob.INSTANCE.removeJobChangeListener(RefreshBuzzJobChangeListener);
			RefreshBuzzJobChangeListener = null;
		}
	
		if (refreshTutorialsJobChangeListener != null) {
			RefreshTutorialsJob.INSTANCE.removeJobChangeListener(refreshTutorialsJobChangeListener);
			refreshTutorialsJobChangeListener = null;
		}
		
		ProxyWizardManager.INSTANCE.unRegisterListener(this);
		
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
				setBusyIndicator(composite, true);
				form.reflow(true);
				form.redraw();
				if (pageBook == tutorialPageBook) {
					recomputeScrollComposite(scrolledComposite, pageBook);
				}
			}
		});
		return true;
	}

	private Image getFeedsImage(FeedsEntry entry) {
		if (FeedsEntry.Type.TWITTER.equals(entry.getType())) {
			return JBossCentralActivator.getDefault().getImage("/icons/twitter-feed.png");
		}
		return JBossCentralActivator.getDefault().getImage("/icons/feedsLink.gif");
	}
	
	private void recomputeScrollComposite(ScrolledComposite scrolledComposite, Composite content) {
		if (scrolledComposite != null && content != null) {
			Rectangle r = scrolledComposite.getClientArea();
			scrolledComposite.setMinSize(content.computeSize(r.width, SWT.DEFAULT));
		}
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
	
	private void showException(PageBook pageBook, FormText exceptionText, Throwable e) {
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

	public void refreshBuzz() {
		RefreshBuzzJob job = RefreshBuzzJob.INSTANCE;
		if (job.getState() == Job.NONE) {
			List<FeedsEntry> entries = job.getEntries();
			if (job.getException() != null) {
				if (entries != null && job.getEntries().isEmpty()) {
					showException(buzzPageBook, buzzExceptionText,	job.getException());
					return;
				}
			}
			if (entries == null || entries.isEmpty()) {
				showNote(buzzPageBook, buzzNoteText, buzzScrollComposite);
				return;
			}
			if (job.needsRefresh()) {
				showEntries(entries, buzzComposite, buzzPageBook, buzzScrollComposite);
			}
		}
	}
		
	public void refreshTutorials() {
		RefreshTutorialsJob job = RefreshTutorialsJob.INSTANCE;
		if (job.getException() != null) {
			showException(tutorialPageBook, tutorialsExceptionText,	job.getException());
			return;
		}
		Map<ProjectExampleCategory, List<ProjectExample>> categories = job.getTutorialCategories();
		if (categories == null || categories.isEmpty()) {
			showNote(tutorialPageBook, tutorialsNoteText, tutorialScrollComposite);
			return;
		}
		showTutorials(categories);
		resize(true);
	}

	private void showTutorials(Map<ProjectExampleCategory, List<ProjectExample>> categories) {
		disposeChildren(tutorialsComposite);
		List<ProjectExampleCategory> sortedCategories = new ArrayList<ProjectExampleCategory>(categories.keySet());
		Collections.sort(sortedCategories);
		
		Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		Font arial = new Font(getDisplay(),"Arial",10,SWT.BOLD); 

		GridLayout gl = (GridLayout)tutorialsComposite.getLayout();
		int maxCols = 4;
		gl.makeColumnsEqualWidth = false;
		gl.numColumns = sortedCategories.size() < maxCols ? sortedCategories.size() : maxCols; 
		
		int categoryHeight = 122;
		for (final ProjectExampleCategory category : sortedCategories) {
			//Create category composite
			final Composite categoryComposite = toolkit.createComposite(tutorialsComposite);
			GridDataFactory.fillDefaults().grab(false, false).hint(SWT.DEFAULT, categoryHeight).applyTo(categoryComposite);
			categoryComposite.setLayout(new GridLayout());
			
			//Add black title
			Label categoryName = toolkit.createLabel(categoryComposite, category.getName());
			categoryName.setFont(arial);
			if (!JBossCentralEditor.useFefaultColors) {
				categoryName.setForeground(black);
			}
			//Add tooltip
			String description = category.getDescription();
			if (description != null && !description.isEmpty()){
				final DescriptionToolTip toolTip = new DescriptionToolTip(categoryName, description);
				toolTip.activate();
			}
			
			//Create vertical scroller
			ScrolledComposite scroller = new ScrolledComposite(categoryComposite, SWT.V_SCROLL);
	        GridData d = new GridData(GridData.FILL_BOTH);
	        scroller.setLayoutData(d);
			scroller.setMinHeight(categoryComposite.getSize().y);
		    scroller.setExpandHorizontal(true);
		    scroller.setExpandVertical(false);
			
			//Create scroller content
			final Composite composite = toolkit.createComposite(scroller);
			GridData d2 = new GridData(SWT.FILL, SWT.FILL, true, true);
			composite.setLayoutData(d2);
			composite.setLayout(new GridLayout());
			
			//Fill content with links
			displayTutorialLinks(categories.get(category), composite, true);
			
			//Recompute size
			scroller.setContent(composite);
			
			Point size = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			scroller.setBackground(composite.getBackground());//Let's hide the scroller background 
			composite.setSize(size);
			
		}
		
		tutorialPageBook.showPage(tutorialsComposite);
		tutorialPageBook.layout(true, true);
		form.reflow(true);
	}

	private void hookTooltip(FormText tutorialText, ProjectExample tutorial) {
		final String description = JBossCentralActivator.getDefault().getDescription(tutorial);
		if (description != null && !description.isEmpty()) {
			DescriptionToolTip toolTip = new DescriptionToolTip(tutorialText, description);
			toolTip.activate();
		}
	}

	protected void configureTutorialText(FormText tutorialText, final ProjectExample tutorial) {
		StringBuilder buffer = new StringBuilder();
		tutorialText.setHyperlinkSettings(hyperlinkSettings);
		buffer.append(JBossCentralActivator.FORM_START_TAG);
		buffer.append("<a href=\"link\">");
		buffer.append(tutorial.getShortDescription());
		buffer.append("</a> ");
		buffer.append(JBossCentralActivator.FORM_END_TAG);

		tutorialText.setText(buffer.toString() , true, false);
		
		tutorialText.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				Object object = e.data;
				if (object instanceof String) {
					NewProjectExamplesWizard2 wizard = new NewProjectExamplesWizard2(tutorial);
					WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
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

	private void showEntries(List<FeedsEntry> entries, Composite composite, PageBook pageBook, ScrolledComposite scrollable) {
		int i = 0;
		disposeChildren(composite);
		pageBook.layout(true, true);
		int h = scrollable.getClientArea().height;
		for (final FeedsEntry entry:entries) {
			String text = entry.getShortString(false);
			FormText formText = toolkit.createFormText(composite, true);
			formText.setHyperlinkSettings(hyperlinkSettings);
			TableWrapData td = new TableWrapData();
			td.indent = 2;
			formText.setLayoutData(td);
			try { 
				formText.setText(text, true, true);
			} catch(IllegalArgumentException se) {
				try {
					//twitter truncates their tweet feed, leading to tweets containing unfinished escaped sequences
					//like "blabla &amp..."
					text = entry.getShortString(true);
					formText.setText(text, true, true);
				} catch (IllegalArgumentException se2) {
					formText.dispose();
					formText = toolkit.createFormText(composite, false);
					formText.setLayoutData(td);
					try {
						formText.setText("Problem rendering entry - " + StringEscapeUtils.unescapeHtml(se.getMessage()), false, false);
					} catch (Exception e1) {
						JBossCentralActivator.log(se);
					}
					continue;
				}
			}
			formText.setFont("default", JFaceResources.getDefaultFont());
			formText.setFont("date", JFaceResources.getDefaultFont());
			formText.setColor("date", JFaceColors.getHyperlinkText(getDisplay()));
			formText.setFont("description", JFaceResources.getDefaultFont());
			formText.setColor("author", JFaceColors.getHyperlinkText(getDisplay()));
			formText.setImage("image", getFeedsImage(entry));
			if (JBossCentralActivator.isInternalWebBrowserAvailable() && entry.getDescription() != null && !entry.getDescription().isEmpty()) {
				ToolTip toolTip = new FeedsToolTip(formText, entry.getDate() + " " + entry.getDescription());
				toolTip.activate();
			}
			formText.addHyperlinkListener(new HyperlinkOpener());
			
			
		}
		pageBook.showPage(composite);
		pageBook.layout(true, true);
		form.reflow(true);
		resize(true);
	}

	private void resize() {
		resize(false);
	}
	
	protected void resize(boolean force) {
		
		if (documentationSection == null) {
			return;
		}
		Point size;
		size = form.getSize();
		size.y = form.getBody().getSize().y;
		if (!force && size.equals(oldSize)) {
			return;
		}
		oldSize = size;
		
		resizedescriptionCompositeScroller();
		
		int height = form.getClientArea().height - (projectsSection.getSize().y + tutorialsSection.getSize().y);
		int width =  size.x - (documentationSection.getSize().x);
		GridData gd = (GridData) buzzSection.getLayoutData();
		gd.heightHint = height - 30;
		gd.widthHint = width - 45;
		gd.grabExcessVerticalSpace = false;

		Point computedSize = buzzPageBook.computeSize(gd.widthHint, SWT.DEFAULT);

		buzzScrollComposite.setMinSize(gd.widthHint, computedSize.y);

		recomputeScrollComposite(buzzScrollComposite, buzzPageBook);

		form.layout(true, true);
		form.reflow(true);
	}

	private class RefreshBuzzJobChangeListener implements IJobChangeListener {

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
					if (buzzLoadingComposite == null || buzzLoadingComposite.isDisposed()) {
						return;
					}
					setBusyIndicator(buzzLoadingComposite, false);
					refreshBuzz();
					RefreshBuzzJob job = RefreshBuzzJob.INSTANCE;
					setItemVisible(buzzToolBarManager, BUZZ_WARNING_ID, false);
					if (!job.getEntries().isEmpty() && job.getException() != null) {
						String tooltip = job.getException().getClass().getName() + ": " + job.getException().getLocalizedMessage();
						buzzWarning.setToolTipText(tooltip);
						setItemVisible(buzzToolBarManager, BUZZ_WARNING_ID, true);
					}
					buzzToolBarManager.update(true);
					buzzSection.layout(true, true);
				}
			});
			
		}

		@Override
		public void running(IJobChangeEvent event) {
			
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			if (RefreshBuzzJob.INSTANCE.getEntries().size() <= 0) {
				showLoading(buzzPageBook, buzzLoadingComposite, buzzScrollComposite);
			}
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
					if (tutorialsLoadingComposite == null || tutorialsLoadingComposite.isDisposed()) {
						return;
					}
					setBusyIndicator(tutorialsLoadingComposite, false);
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
	
	@Override
	protected Section createSection(FormToolkit toolkit, Composite parent,
			String name, int style) {
		final Section section = super.createSection(toolkit, parent, name, style);
		final FormToolkit tk = toolkit;
		if (!JBossCentralEditor.useFefaultColors) {
			Composite separator = new Composite(section, style);
		    separator.addListener(SWT.Paint, new Listener() {
				public void handleEvent(Event e) {
		           if (section.isDisposed()) {
		        	   return;
		           }
			        GC gc = e.gc;
			        Color grey = tk.getColors().createColor("grey", 194, 194, 194);
			        gc.setBackground(grey);
		        }
			});
		    section.setSeparatorControl(separator);
			Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
			section.setTitleBarForeground(black );
		} else {
			section.setSeparatorControl(toolkit.createCompositeSeparator(section));
		}
		section.descriptionVerticalSpacing = 20;
		section.setTitleBarBackground(section.getBackground());
		section.setTitleBarBorderColor(section.getBackground());
		Font font = new Font(getDisplay(),"Lucida Sans Unicode",12,SWT.NORMAL); 
		section.setFont(font);
		return section;
	}
	
	class HyperlinkOpener extends HyperlinkAdapter {

		@Override
		public void linkActivated(HyperlinkEvent e) {
			Object link = (e.getHref() == null)?e.data:e.getHref();
			if (link instanceof String) {
				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				JBossCentralActivator.openUrl((String) link, shell);
			}
		}
	}

	@Override
	public void onProxyWizardUpdate(final UpdateEvent event) throws CoreException {
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if (projectsComposite.isDisposed()) {
					return;
				}
				List<ProxyWizard> newWizards = event.getProxyWizards();
				addProxyWizardLinks(newWizards);
				projectsComposite.layout(true, true);
			}
		});
	}

}
