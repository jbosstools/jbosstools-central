package org.jboss.tools.central.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.dialogs.PreferencesUtil;
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
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.forms.widgets.FormFonts;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.jobs.RefreshNewsJob;
import org.jboss.tools.central.model.NewsEntry;
import org.osgi.framework.Bundle;

public class GettingStartedPage extends AbstractJBossCentralPage {

	private static final String CLASS_ATTRIBUTE = "class";
	public static final String ID = ID_PREFIX + "GettingStartedPage";
	
	protected static final long TIME_DELAY = 2000L;
	private IWorkbenchAction newWizardDropDownAction;
	private ScrolledForm form;
	private PageBook newsPageBook;
	private Image loaderImage;
	private Image newsImage;
	private ScrolledComposite scrollComposite;
	private static Font authorFont;
	private Font linkFont;
	private RefreshJobChangeListener refreshJobChangeListener;
	private FormText newsNoteText;
	private FormText newsLoadingText;
	private FormText newsExceptionText;
	private Composite newsComposite;
	private FormToolkit toolkit;
	private ScrolledComposite tutorialScrollComposite;
	private PageBook tutorialPageBook;
	
	public GettingStartedPage(FormEditor editor) {
		super(editor, ID, "Getting Started");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		
		Composite body = form.getBody();
	    GridLayout gridLayout = new GridLayout(2, true);
	    gridLayout.horizontalSpacing = 7;
	    body.setLayout(gridLayout);
	    toolkit.paintBordersFor(body);
		
		Composite left = createComposite(toolkit, body);
		createTutorialsSection(toolkit, left);
		createProjectsSection(toolkit, left);
		createDocumentationSection(toolkit, left);
		toolkit.paintBordersFor(left);
		
		Composite right = createComposite(toolkit, body);
	    createNewsSection(toolkit, right);
		toolkit.paintBordersFor(right);
		
	    super.createFormContent(managedForm);
	    form.redraw();
	    form.reflow(true);
	}

	private void createNewsSection(FormToolkit toolkit, Composite parent) {
		final Section news = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED);
		news.setText("News");
	    news.setLayout(new GridLayout());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
	    gd.widthHint = 350;
	    gd.heightHint = 100;
	    news.setLayoutData(gd);
	    linkFont = news.getFont();
		createNewsToolbar(toolkit, news);
		
		scrollComposite = new ScrolledComposite(news, SWT.V_SCROLL);
		gd =new GridData(SWT.FILL, SWT.FILL, false, false);
		scrollComposite.setLayoutData(gd);
		scrollComposite.setLayout(new GridLayout());
		
		newsPageBook = new PageBook(scrollComposite, SWT.WRAP);
		gd =new GridData(SWT.FILL, SWT.FILL, false, false);
	    newsPageBook.setLayoutData(gd);
        
        scrollComposite.setContent(newsPageBook);
    	scrollComposite.setExpandVertical(true);
    	scrollComposite.setExpandHorizontal(true);
    	scrollComposite.addControlListener(new ControlAdapter() {
    		public void controlResized(ControlEvent e) {
    			recomputeScrollComposite(scrollComposite, newsPageBook);
    		}
    	});

    	newsNoteText = createNewsNoteText(toolkit);
	    newsLoadingText = createLoadingText(toolkit);	    
	    newsExceptionText = createExceptionText(toolkit);

	    form.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				GridData gridData = (GridData) scrollComposite.getLayoutData();
				Point size = form.getSize();
				gridData.heightHint = size.y - 55;
				gridData.widthHint = size.x/2 - 10;
				gridData.grabExcessVerticalSpace = true;

				gridData = (GridData) news.getLayoutData();
				gridData.heightHint = size.y - 40;
				gridData.widthHint = size.x/2 - 5;
				gridData.grabExcessVerticalSpace = false;
				form.reflow(true);
				form.redraw();
				recomputeScrollComposite(scrollComposite, newsPageBook);
			}
	    });
		        
		news.setClient(scrollComposite);
		showLoading();
		newsPageBook.pack(true);
		RefreshNewsJob refreshNewsJob = RefreshNewsJob.INSTANCE;
		refreshJobChangeListener = new RefreshJobChangeListener();
		refreshNewsJob.addJobChangeListener(refreshJobChangeListener);
		refreshNewsJob.schedule();
	}

	private FormText createExceptionText(FormToolkit toolkit) {
		FormText formText = toolkit.createFormText(newsPageBook, true);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false);
	    formText.setLayoutData(gd);
		return formText;
	}
	
	private FormText createLoadingText(FormToolkit toolkit) {
		FormText formText = toolkit.createFormText(newsPageBook, true);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false);
	    formText.setLayoutData(gd);
		String text = JBossCentralActivator.FORM_START_TAG +
				"<img href=\"image\"/> <b>Refreshing...</b>" +
				JBossCentralActivator.FORM_END_TAG;
		formText.setText(text, true, false);
		Image image = getLoaderImage();
		formText.setImage("image", image);
		return formText;
	}

	private FormText createNewsNoteText(FormToolkit toolkit) {
		FormText formText = toolkit.createFormText(newsPageBook, true);
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
		final Section tutorials = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED);
		tutorials.setText("Project Examples");
		tutorials.setLayout(new GridLayout());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
	    gd.widthHint = 350;
	    gd.heightHint = 200;
	    tutorials.setLayoutData(gd);
	    
	    createTutorialsToolbar(toolkit, tutorials);
		
	    tutorialScrollComposite = new ScrolledComposite(tutorials, SWT.V_SCROLL);
		gd =new GridData(SWT.FILL, SWT.FILL, false, false);
		tutorialScrollComposite.setLayoutData(gd);
		tutorialScrollComposite.setLayout(new GridLayout());
		toolkit.adapt(tutorialScrollComposite);
		
		tutorialPageBook = new PageBook(tutorialScrollComposite, SWT.WRAP);
		gd =new GridData(SWT.FILL, SWT.FILL, false, false);
	    tutorialPageBook.setLayoutData(gd);
        
        tutorialScrollComposite.setContent(tutorialPageBook);
    	tutorialScrollComposite.setExpandVertical(true);
    	tutorialScrollComposite.setExpandHorizontal(true);
    	tutorialScrollComposite.addControlListener(new ControlAdapter() {
    		public void controlResized(ControlEvent e) {
    			recomputeScrollComposite(tutorialScrollComposite, tutorialPageBook);
    		}
    	});

		Composite tutorialComposite = toolkit.createComposite(tutorialPageBook, SWT.NONE);	    
		tutorialComposite.setLayout(new GridLayout());
		gd =new GridData(SWT.FILL, SWT.FILL, true, true);
	    tutorialComposite.setLayoutData(gd);
	    
//		ExpandableComposite seam2Category = toolkit.createExpandableComposite(tutorialComposite, ExpandableComposite.TITLE_BAR|ExpandableComposite.CLIENT_INDENT|ExpandableComposite.TWISTIE);
//		seam2Category.setText("Seam 2");
//		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
//		seam2Category.setLayoutData(gd);
//		seam2Category.setLayout(new GridLayout());
		
		//newsLoadingText = createLoadingText(toolkit);

	    form.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				GridData gridData = (GridData) tutorialScrollComposite.getLayoutData();
				Point size = form.getSize();
				//gridData.heightHint = size.y - 55;
				gridData.widthHint = size.x/2 - 10;
				gridData.grabExcessVerticalSpace = true;

				gridData = (GridData) tutorials.getLayoutData();
				//gridData.heightHint = size.y - 40;
				gridData.widthHint = size.x/2 - 5;
				gridData.grabExcessVerticalSpace = false;
				form.reflow(true);
				form.redraw();
				recomputeScrollComposite(tutorialScrollComposite, tutorialPageBook);
			}
	    });
		        
		tutorials.setClient(tutorialScrollComposite);
		tutorialPageBook.showPage(tutorialComposite);
		form.reflow(true);
		form.redraw();
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
		
		item = JBossCentralActivator.createContributionItem(getSite(), "org.jboss.tools.central.downloadJBossAs701Handler");
		toolBarManager.add(item);

	    toolBarManager.update(true);
	    
		section.setTextClient(headerComposite);
	}
	
	public void createProjectsSection(FormToolkit toolkit, Composite parent) {
		Section projects = createSection(toolkit, parent, "Create Projects", ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE);
	    
	    Composite headerComposite = toolkit.createComposite(projects, SWT.NONE);
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
	    
		projects.setTextClient(headerComposite);
		
		Composite composite = toolkit.createComposite(projects);
	    GridLayout layout = new GridLayout(2, true);
	    layout.horizontalSpacing = 10;
	    composite.setLayout(layout);
	    GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

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
					createProjectLink(toolkit, composite, element);
					createdIDs.add(id);
				}
			}
		}
		projects.setClient(composite);
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
		Section documentation = createSection(toolkit, parent, "Documentation", ExpandableComposite.TITLE_BAR|ExpandableComposite.TWISTIE);
		
		Composite composite = toolkit.createComposite(documentation);
	    GridLayout layout = new GridLayout(2, true);
	    layout.horizontalSpacing = 30;
	    composite.setLayout(layout);
	    GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
	    
		addHyperlink(toolkit, composite, "New and Noteworthy", "http://docs.jboss.org/tools/whatsnew/");
		addHyperlink(toolkit, composite, "User Forum", "http://community.jboss.org/en/tools?view=discussions");
		
		addHyperlink(toolkit, composite, "Reference", "http://docs.jboss.org/tools/latest/");
		addHyperlink(toolkit, composite, "Developer Forum", "http://community.jboss.org/en/tools/dev?view=discussions");
		
		addHyperlink(toolkit, composite, "FAQ", "http://www.jboss.org/tools/docs/faq");
		addHyperlink(toolkit, composite, "Wiki", "http://community.jboss.org/wiki/JBossTools");
		
		addHyperlink(toolkit, composite, "Screencasts", "http://docs.jboss.org/tools/movies/");
		addHyperlink(toolkit, composite, "Issue Tracker", "https://issues.jboss.org/browse/JBIDE");
		
		
		documentation.setClient(composite);
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
		if (loaderImage != null) {
			loaderImage.dispose();
			loaderImage = null;
		}
		if (newsImage != null) {
			newsImage.dispose();
			newsImage = null;
		}
		if (refreshJobChangeListener != null) {
			RefreshNewsJob.INSTANCE.removeJobChangeListener(refreshJobChangeListener);
			refreshJobChangeListener = null;
		}
		super.dispose();
	}

	public boolean showLoading() {
		if (newsPageBook.isDisposed()) {
			return false;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				newsPageBook.showPage(newsLoadingText);
				form.reflow(true);
				form.redraw();
				recomputeScrollComposite(scrollComposite, newsPageBook);
			}
		});
		
		return true;
	}

	private Image getLoaderImage() {
		if (loaderImage == null) {
			loaderImage = JBossCentralActivator.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, "/icons/loader.gif").createImage();
		}
		return loaderImage;
	}

	private Image getNewsImage() {
		if (newsImage == null) {
			newsImage = JBossCentralActivator.imageDescriptorFromPlugin(JBossCentralActivator.PLUGIN_ID, "/icons/newsLink.gif").createImage();
		}
		return newsImage;
	}
	
	private void recomputeScrollComposite(ScrolledComposite composite, PageBook pageBook) {
		Rectangle r = composite.getClientArea();
		composite.setMinSize(pageBook.computeSize(r.width, SWT.DEFAULT));
	}

	public void showNote() {
		if (newsPageBook.isDisposed()) {
			return;
		}
		Display display = getDisplay();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				newsPageBook.showPage(newsNoteText);
				form.reflow(true);
				form.redraw();
				recomputeScrollComposite(scrollComposite, newsPageBook);
			}
		});
	}

	private Font getAuthorFont(Display display) {
		if (authorFont == null) {
			Font font = JFaceResources.getDefaultFont();
			authorFont = FormFonts.getInstance().getBoldFont(display, font);
		}
		return authorFont;
	}
	
	private Font getLinkFont(Display display) {
		if (linkFont == null) {
			linkFont = JFaceResources.getDefaultFont();
		}
		return linkFont;
	}
	
	
	private void showException(Exception e) {
		JBossCentralActivator.log(e);
		String text = JBossCentralActivator.FORM_START_TAG +
				"<img href=\"image\"/> " + 
				e.getMessage() + 
				JBossCentralActivator.FORM_END_TAG;
		newsExceptionText.setText(text, true, false);
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		newsExceptionText.setImage("image", image);
		newsPageBook.showPage(newsExceptionText);
	}

	public void refresh() {
		RefreshNewsJob job = RefreshNewsJob.INSTANCE;
		if (job.getException() != null) {
			showException(job.getException());
			return;
		}
		List<NewsEntry> entries = job.getEntries();
		if (entries == null || entries.size() == 0) {
			showNote();
			return;
		}
		showNews(entries);
	}

	private void showNews(List<NewsEntry> entries) {
		int i = 0;
		if (newsComposite != null && !newsComposite.isDisposed()) {
			newsComposite.dispose();
		}
		newsComposite = toolkit.createComposite(newsPageBook, SWT.NONE);	    
		newsComposite.setLayout(new GridLayout());
		GridData gd =new GridData(SWT.FILL, SWT.FILL, true, true);
	    newsComposite.setLayoutData(gd);

		for (final NewsEntry entry:entries) {
			if (i++ > JBossCentralActivator.MAX_FEEDS) {
				return;
			}
			String text = entry.getFormString();
			final FormText formText = toolkit.createFormText(newsComposite, true);
			formText.setText(text, true, true);
			Display display = Display.getCurrent();
			formText.setFont(getLinkFont(display));
			formText.setFont("default", JFaceResources.getDefaultFont());
			Font boldFont = getAuthorFont(display);
			formText.setFont("author", boldFont);
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
			RefreshNewsJob.INSTANCE.setException(null);
			showLoading();
		}

		@Override
		public void sleeping(IJobChangeEvent event) {
			
		}
		
	}
	
}
