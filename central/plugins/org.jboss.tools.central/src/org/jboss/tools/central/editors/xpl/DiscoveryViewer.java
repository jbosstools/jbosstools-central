/*******************************************************************************
 * Copyright (c) 2010, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - Added support for update
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.GradientCanvas;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptorKind;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDiscovery;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryCategory;
import org.eclipse.mylyn.internal.discovery.core.model.DiscoveryConnector;
import org.eclipse.mylyn.internal.discovery.core.model.Icon;
import org.eclipse.mylyn.internal.discovery.core.model.Overview;
import org.eclipse.mylyn.internal.discovery.core.util.DiscoveryCategoryComparator;
import org.eclipse.mylyn.internal.discovery.core.util.DiscoveryConnectorComparator;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryImages;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.mylyn.internal.discovery.ui.wizards.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.views.markers.internal.CategoryComparator;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.editors.xpl.filters.FilterEntry;
import org.jboss.tools.central.editors.xpl.filters.FiltersSelectionDialog;
import org.jboss.tools.project.examples.internal.discovery.DiscoveryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * The main wizard page that allows users to select connectors that they wish to install.
 * 
 * @author David Green
 * @author Steffen Pingel
 */
public class DiscoveryViewer extends Viewer {

	private static final int MINIMUM_HEIGHT = 100;

	private static boolean useNativeSearchField(Composite composite) {
		if (useNativeSearchField == null) {
			useNativeSearchField = Boolean.FALSE;
			Text testText = null;
			try {
				testText = new Text(composite, SWT.SEARCH | SWT.ICON_CANCEL);
				useNativeSearchField = new Boolean((testText.getStyle() & SWT.ICON_CANCEL) != 0);
			} finally {
				if (testText != null) {
					testText.dispose();
				}
			}

		}
		return useNativeSearchField;
	}

	private boolean showConnectorDescriptorTextFilter;

	private static final String COLOR_WHITE = "white"; //$NON-NLS-1$

	private static Boolean useNativeSearchField;

	private final Set<ConnectorDescriptor> installableConnectors = new HashSet<ConnectorDescriptor>();
	private final Set<ConnectorDescriptor> installedConnectors = new HashSet<ConnectorDescriptor>();
	private final Set<ConnectorDescriptor> updatableConnectors = new HashSet<ConnectorDescriptor>();

	private Composite parent;
	private Composite topLevelControl;
	private Composite body;
	private ScrolledComposite bodyScrolledComposite;
	private Composite scrolledContents;

	private final List<Resource> disposables;

	private Font h1Font;
	private Font h2Font;

	private Color colorWhite;
	private Color colorCategoryGradientStart;
	private Color colorCategoryGradientEnd;
	
	private Image infoImage;
	private Cursor handCursor;
	

	private String previousFilterText = ""; //$NON-NLS-1$
	private Pattern filterPattern;
	private Text filterText;
	private Label clearFilterTextControl;
	
	private WorkbenchJob refreshJob;

	private Set<String> installedFeatures;

	private boolean verifyUpdateSiteAvailability;

	private Dictionary<Object, Object> environment;

	private final IRunnableContext context;

	private Set<String> directoryUrls;
	private volatile HashMap<String, ConnectorDiscovery> discoveries = new LinkedHashMap<String, ConnectorDiscovery>();
	private List<DiscoveryConnector> allConnectors;
	private Map<DiscoveryConnector, ConnectorDescriptorItemUi> itemsUi = new HashMap<DiscoveryConnector, ConnectorDescriptorItemUi>();
	private Map<String, Control> categories = new HashMap<String, Control>();

	private int minimumHeight;

	private final List<FilterEntry> userFilters = new ArrayList<FilterEntry>();


	public DiscoveryViewer(Composite parent, IRunnableContext context) {
		this.parent = parent;
		this.context = context;
		this.allConnectors = Collections.emptyList();
		this.disposables = new ArrayList<Resource>();
		setShowConnectorDescriptorTextFilter(true);
		setMinimumHeight(MINIMUM_HEIGHT);
		createEnvironment();
	}

	public void selectAll() {
		for(ConnectorDescriptorItemUi itemUi:itemsUi.values()) {
			itemUi.select(true);
		}
	}
	
	public void deselectAll() {
		for(ConnectorDescriptorItemUi itemUi:itemsUi.values()) {
			itemUi.select(false);
		}
	}

	private void clearDisposables() {
		disposables.clear();
		h1Font = null;
		h2Font = null;
		infoImage = null;
		handCursor = null;
		colorCategoryGradientStart = null;
		colorCategoryGradientEnd = null;
	}

	private void clearFilterText() {
		filterText.setText(""); //$NON-NLS-1$
		filterTextChanged();
	}

	static Image computeIconImage(AbstractDiscoverySource discoverySource, Icon icon, int dimension, boolean fallback) {
		String imagePath;
		switch (dimension) {
		case 64:
			imagePath = icon.getImage64();
			if (imagePath != null || !fallback) {
				break;
			}
		case 48:
			imagePath = icon.getImage48();
			if (imagePath != null || !fallback) {
				break;
			}
		case 32:
			imagePath = icon.getImage32();
			break;
		default:
			throw new IllegalArgumentException();
		}
		if (imagePath != null && imagePath.length() > 0) {
			URL resource = discoverySource.getResource(imagePath);
			if (resource != null) {
				ImageDescriptor descriptor = ImageDescriptor.createFromURL(resource);
				Image image = descriptor.createImage();
				if (image != null) {
					return image;
				}
			}
		}
		return null;
	}

	private IStatus computeStatus(InvocationTargetException e, String message) {
		Throwable cause = e.getCause();
		IStatus statusCause;
		if (cause instanceof CoreException) {
			statusCause = ((CoreException) cause).getStatus();
		} else {
			statusCause = new Status(IStatus.ERROR, DiscoveryUi.ID_PLUGIN, cause.getMessage(), cause);
		}
		if (statusCause.getMessage() != null) {
			message = NLS.bind(Messages.ConnectorDiscoveryWizardMainPage_message_with_cause, message,
					statusCause.getMessage());
		}
		IStatus status = new MultiStatus(DiscoveryUi.ID_PLUGIN, 0, new IStatus[] { statusCause }, message, cause);
		return status;
	}

	void configureLook(Control control, Color background) {
		control.setBackground(background);
	}

	public void createBodyContents() {
		// remove any existing contents
		if (body == null || body.isDisposed()) {
			return;
		}
		for (Control child : body.getChildren()) {
			child.dispose();
		}
		clearDisposables();
		allConnectors = new ArrayList<DiscoveryConnector>();
		this.itemsUi.clear();
		initializeCursors();
		initializeImages();
		initializeFonts();
		initializeColors();

		GridLayoutFactory.fillDefaults().applyTo(body);

		bodyScrolledComposite = new ScrolledComposite(body, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		configureLook(bodyScrolledComposite, colorWhite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(bodyScrolledComposite);

		this.scrolledContents = new Composite(bodyScrolledComposite, SWT.NONE);
		configureLook(scrolledContents, colorWhite);
		scrolledContents.setRedraw(false);
		try {
			createDiscoveryContents(scrolledContents);
			updateFilters();
		} finally {
			scrolledContents.layout(true);
			scrolledContents.setRedraw(true);
		}
		Point size = scrolledContents.computeSize(body.getSize().x, SWT.DEFAULT, true);
		scrolledContents.setSize(size);

		bodyScrolledComposite.setExpandHorizontal(true);
		bodyScrolledComposite.setMinWidth(100);
		bodyScrolledComposite.setExpandVertical(true);
		bodyScrolledComposite.setMinHeight(1);

		bodyScrolledComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				// XXX small offset in case list has a scroll bar
				Point size = scrolledContents.computeSize(body.getSize().x - 20, SWT.DEFAULT, true);
				scrolledContents.setSize(size);
				bodyScrolledComposite.setMinHeight(size.y);
			}
		});

		bodyScrolledComposite.setContent(scrolledContents);

		Dialog.applyDialogFont(body);
		// we've changed it so it needs to know
		body.layout(true);
	}

	private Label createClearFilterTextControl(Composite filterContainer, final Text filterText) {
		final Image inactiveImage = CommonImages.FIND_CLEAR_DISABLED.createImage();
		final Image activeImage = CommonImages.FIND_CLEAR.createImage();
		final Image pressedImage = new Image(filterContainer.getDisplay(), activeImage, SWT.IMAGE_GRAY);

		final Label clearButton = new Label(filterContainer, SWT.NONE);
		clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		clearButton.setImage(inactiveImage);
		clearButton.setToolTipText(Messages.ConnectorDiscoveryWizardMainPage_clearButton_toolTip);
		clearButton.setBackground(filterContainer.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		clearButton.addMouseListener(new MouseAdapter() {
			private MouseMoveListener fMoveListener;

			private boolean isMouseInButton(MouseEvent e) {
				Point buttonSize = clearButton.getSize();
				return 0 <= e.x && e.x < buttonSize.x && 0 <= e.y && e.y < buttonSize.y;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				clearButton.setImage(pressedImage);
				fMoveListener = new MouseMoveListener() {
					private boolean fMouseInButton = true;

					public void mouseMove(MouseEvent e) {
						boolean mouseInButton = isMouseInButton(e);
						if (mouseInButton != fMouseInButton) {
							fMouseInButton = mouseInButton;
							clearButton.setImage(mouseInButton ? pressedImage : inactiveImage);
						}
					}
				};
				clearButton.addMouseMoveListener(fMoveListener);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (fMoveListener != null) {
					clearButton.removeMouseMoveListener(fMoveListener);
					fMoveListener = null;
					boolean mouseInButton = isMouseInButton(e);
					clearButton.setImage(mouseInButton ? activeImage : inactiveImage);
					if (mouseInButton) {
						clearFilterText();
						filterText.setFocus();
					}
				}
			}
		});
		clearButton.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				clearButton.setImage(activeImage);
			}

			public void mouseExit(MouseEvent e) {
				clearButton.setImage(inactiveImage);
			}

			public void mouseHover(MouseEvent e) {
			}
		});
		clearButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				inactiveImage.dispose();
				activeImage.dispose();
				pressedImage.dispose();
			}
		});
		clearButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = Messages.ConnectorDiscoveryWizardMainPage_clearButton_accessibleListener;
			}
		});
		clearButton.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_PUSHBUTTON;
			}
		});
		return clearButton;
	}

	public void createControl() {
		createRefreshJob();

		Composite container = new Composite(parent, SWT.NULL);
		container.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				refreshJob.cancel();
				if (disposables != null) {
					for (Resource resource : disposables) {
						resource.dispose();
					}
					clearDisposables();
				}
				if (DiscoveryViewer.this.discoveries != null) {
					for (ConnectorDiscovery discovery : DiscoveryViewer.this.discoveries.values()) {
						discovery.dispose();
					}
					for (ConnectorDescriptorItemUi item : itemsUi.values()) {
						item.dispose();
					}
				}
			}
		});
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		//		
		{ // header
			Composite header = new Composite(container, SWT.NULL);
			GridLayoutFactory.fillDefaults().applyTo(header);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(header);

			// TODO: refresh button?
			if (isShowConnectorDescriptorTextFilter()) {
				Composite filterContainer = new Composite(header, SWT.NULL);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(filterContainer);

				Label label = new Label(filterContainer, SWT.NULL);
				label.setText(Messages.ConnectorDiscoveryWizardMainPage_filterLabel);

				if (isShowConnectorDescriptorTextFilter()) {
					Composite textFilterContainer;
					boolean nativeSearch = useNativeSearchField(header);
					if (nativeSearch) {
						textFilterContainer = new Composite(filterContainer, SWT.NULL);
					} else {
						textFilterContainer = new Composite(filterContainer, SWT.BORDER);
						textFilterContainer.setBackground(header.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
					}
					GridDataFactory.fillDefaults().grab(true, false).applyTo(textFilterContainer);
					GridLayoutFactory.fillDefaults().numColumns(2).applyTo(textFilterContainer);

					if (nativeSearch) {
						filterText = new Text(textFilterContainer, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
					} else {
						filterText = new Text(textFilterContainer, SWT.SINGLE);
					}

					filterText.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
							filterTextChanged();
						}
					});
					if (nativeSearch) {
						filterText.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
								if (e.detail == SWT.ICON_CANCEL) {
									clearFilterText();
								}
							}
						});
						GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(filterText);
					} else {
						GridDataFactory.fillDefaults().grab(true, false).applyTo(filterText);
						clearFilterTextControl = createClearFilterTextControl(textFilterContainer, filterText);
						clearFilterTextControl.setVisible(false);
					}
				}

				if (this.userFilters.size() == 1) {
					final FilterEntry theFilter = this.userFilters.get(0);
					final Button checkbox = new Button(filterContainer, SWT.CHECK);
					checkbox.setSelection(theFilter.isEnabled());
					checkbox.setText(theFilter.getLabel());
					checkbox.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {
							widgetSelected(e);
						}

						public void widgetSelected(SelectionEvent e) {
							theFilter.setEnabled(checkbox.getSelection());
							// refresh UI
							updateFilters();
						}
					});
				} else if (this.userFilters.size() > 1) {
					Link link = new Link(filterContainer, SWT.NONE);
					link.setText("<a>" + org.jboss.tools.central.Messages.DiscoveryViewer_filtersLink + "</a>");
					link.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							FiltersSelectionDialog dialog = new FiltersSelectionDialog(parent.getShell(), DiscoveryViewer.this.userFilters);
							if (dialog.open() == Dialog.OK) {
								if (!dialog.getToggledFilters().isEmpty()) {
									for (FilterEntry entry : dialog.getToggledFilters()) {
										entry.setEnabled(!entry.isEnabled());
									}
									updateFilters();
								}
							}
						}
					});
				}

				GridLayoutFactory.fillDefaults()
						.numColumns(filterContainer.getChildren().length)
						.applyTo(filterContainer);
			}

		}
		{ // container
			body = new Composite(container, SWT.NULL);
			GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, minimumHeight).applyTo(body);
		}
		Dialog.applyDialogFont(container);
		this.topLevelControl = container;
	}
	
	private void updateFilters() {
		for (ConnectorDescriptorItemUi item : this.itemsUi.values()) {
			item.setVisible(true);
		}
		// First apply text filter
		for (ConnectorDescriptorItemUi item : this.itemsUi.values()) {
			ConnectorDescriptor descriptor = item.getConnector();
			if (previousFilterText != null && !previousFilterText.isEmpty()) {
				if (!(filterMatches(descriptor.getName()) || filterMatches(descriptor.getDescription())
						|| filterMatches(descriptor.getProvider()) || filterMatches(descriptor.getLicense()))) {
					item.setVisible(false);
				}
			}
		}
		// Then apply user filters SEQUENTIALLY (order of filters can have an impact)
		for (FilterEntry filter : this.userFilters) {
			if (filter.isEnabled()) {
				for (ConnectorDescriptorItemUi item : this.itemsUi.values()) {
					if (!filter.getFilter().select(this, null, item.getConnector())) {
						item.setVisible(false);
					}
				}
			}
		}
		
		boolean atLeastAConnectorVisible = false;
		for (ConnectorDescriptorItemUi item : this.itemsUi.values()) {
			if (item.isVisible()) {
				atLeastAConnectorVisible = true;
			}
		}
		if (!atLeastAConnectorVisible) {
			// TODO show help
		}
		
		this.scrolledContents.layout(true);
		this.scrolledContents.setRedraw(true);
	}
	
	public void setMinimumHeight(int minimumHeight) {
		this.minimumHeight = minimumHeight;
		if (body != null) {
			GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, minimumHeight).applyTo(body);
		}
	}

	public static int getMinimumHeight() {
		return MINIMUM_HEIGHT;
	}

	private void createDiscoveryContents(Composite container) {

		Color background = container.getBackground();

		/* TODO if (emptyDiscoveries) {
			GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(container);

			Control helpTextControl;
			if (filterPattern != null) {
				Link link = new Link(container, SWT.WRAP);

				link.setFont(container.getFont());
				link.setText("There are no matching plug-ins.  Please <a>clear the filter text</a> or try again later.");
				link.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						clearFilterText();
						filterText.setFocus();
					}
				});
				helpTextControl = link;
			} else {
				Label helpText = new Label(container, SWT.WRAP);
				helpText.setFont(container.getFont());
				helpText.setText("Sorry, all available plug-ins from JBoss Central are already installed. More gets added over time, try again later.");
				helpTextControl = helpText;
			}
			configureLook(helpTextControl, background);
			GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(helpTextControl);
		} else*/ {
			GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).applyTo(container);
			Map<String, LinkedHashSet<DiscoveryCategory>> categoriesById = new HashMap<String, LinkedHashSet<DiscoveryCategory>>();
			// necessary because categories from != discovery are not equal, whereas we want them unified in UI
			for (ConnectorDiscovery discovery : this.discoveries.values()) {
				for (DiscoveryCategory category : discovery.getCategories()) {
					if (categoriesById.get(category.getId()) == null) {
						categoriesById.put(category.getId(), new LinkedHashSet<DiscoveryCategory>());
					}
					categoriesById.get(category.getId()).add(category);
				}
			}
			
			// Sort by 1st category
			SortedSet<LinkedHashSet<DiscoveryCategory>> sortedCategories = new TreeSet<LinkedHashSet<DiscoveryCategory>>(new Comparator<LinkedHashSet<DiscoveryCategory>>() {
				private DiscoveryCategoryComparator comparator = new DiscoveryCategoryComparator();
				
				@Override
				public int compare(LinkedHashSet<DiscoveryCategory> o1, LinkedHashSet<DiscoveryCategory> o2) {
					if (o1 == o2) {
						return 0;
					}
					return this.comparator.compare(o1.iterator().next(), o2.iterator().next());
				}
			});
			sortedCategories.addAll(categoriesById.values());

			Composite categoryChildrenContainer = null;
			for (LinkedHashSet<DiscoveryCategory> categories : sortedCategories) {
				boolean isEmpty = true;
				DiscoveryCategory firstCategory = categories.iterator().next();
				categoryChildrenContainer = createCategoryHeaderAndContainer(container, firstCategory, background);
				this.categories.put(firstCategory.getId(), categoryChildrenContainer);
				// Populate category
				for (DiscoveryCategory category : categories) {
					List<DiscoveryConnector> connectors = new ArrayList<DiscoveryConnector>(category.getConnectors());
					Collections.sort(connectors, new DiscoveryConnectorComparator(category));
					for (final DiscoveryConnector connector : connectors) {
//						if (isFiltered(connector)) {
//							continue;
//						}
//	
//						// a separator between connector descriptors
//						Composite border = new Composite(categoryChildrenContainer, SWT.NULL);
//						GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 1).applyTo(border);
//						GridLayoutFactory.fillDefaults().applyTo(border);
//						border.setBackground(border.getDisplay().getSystemColor(SWT.COLOR_GRAY));
////						border.addPaintListener(new ConnectorBorderPaintListener());
	
						ConnectorDescriptorItemUi itemUi = new ConnectorDescriptorItemUi(this, connector,
								categoryChildrenContainer,
								background,
								h2Font,
								infoImage);
						itemsUi.put(connector, itemUi);
						itemUi.updateAvailability();
						allConnectors.add(connector);
					}
				}
			}
			// last one gets a border
			Composite border = new Composite(categoryChildrenContainer, SWT.NULL);
			GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 1).applyTo(border);
			GridLayoutFactory.fillDefaults().applyTo(border);
		}
		container.layout(true);
		container.redraw();
	}

	/**
	 * @param container
	 * @param category
	 * @param background
	 * @return
	 */
	private Composite createCategoryHeaderAndContainer(Composite container, DiscoveryCategory category, Color background) {
		Composite categoryChildrenContainer;
		{ // category header
			final GradientCanvas categoryHeaderContainer = new GradientCanvas(container, SWT.NONE);
			categoryHeaderContainer.setData(category);
			categoryHeaderContainer.setSeparatorVisible(true);
			categoryHeaderContainer.setSeparatorAlignment(SWT.TOP);
			categoryHeaderContainer.setBackgroundGradient(new Color[] { colorCategoryGradientStart,
					colorCategoryGradientEnd }, new int[] { 100 }, true);
			categoryHeaderContainer.putColor(IFormColors.H_BOTTOM_KEYLINE1, colorCategoryGradientStart);
			categoryHeaderContainer.putColor(IFormColors.H_BOTTOM_KEYLINE2, colorCategoryGradientEnd);

			GridDataFactory.fillDefaults().span(2, 1).applyTo(categoryHeaderContainer);
			GridLayoutFactory.fillDefaults()
					.numColumns(3)
					.margins(5, 5)
					.equalWidth(false)
					.applyTo(categoryHeaderContainer);

			Label iconLabel = new Label(categoryHeaderContainer, SWT.NULL);
			if (category.getIcon() != null) {
				Image image = computeIconImage(category.getSource(), category.getIcon(), 48, true);
				if (image != null) {
					iconLabel.setImage(image);
					this.disposables.add(image);
				}
			}
			iconLabel.setBackground(null);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).span(1, 2).applyTo(iconLabel);

			Label nameLabel = new Label(categoryHeaderContainer, SWT.NULL);
			nameLabel.setFont(h1Font);
			nameLabel.setText(category.getName());
			nameLabel.setBackground(null);

			GridDataFactory.fillDefaults().grab(true, false).applyTo(nameLabel);
			if (hasTooltip(category)) {
				ToolBar toolBar = new ToolBar(categoryHeaderContainer, SWT.FLAT);
				toolBar.setBackground(null);
				ToolItem infoButton = new ToolItem(toolBar, SWT.PUSH);
				infoButton.setImage(infoImage);
				infoButton.setToolTipText(Messages.ConnectorDiscoveryWizardMainPage_tooltip_showOverview);
				hookTooltip(toolBar, infoButton, categoryHeaderContainer, nameLabel, category.getSource(),
						category.getOverview(), null);
				GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(toolBar);
			} else {
				new Label(categoryHeaderContainer, SWT.NULL).setText(" "); //$NON-NLS-1$
			}
			Label description = new Label(categoryHeaderContainer, SWT.WRAP);
			GridDataFactory.fillDefaults()
					.grab(true, false)
					.span(2, 1)
					.hint(100, SWT.DEFAULT)
					.applyTo(description);
			description.setBackground(null);
			description.setText(category.getDescription());
		}

		categoryChildrenContainer = new Composite(container, SWT.NULL);
		configureLook(categoryChildrenContainer, background);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(categoryChildrenContainer);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(categoryChildrenContainer);
		return categoryChildrenContainer;
	}

	private void createEnvironment() {
		environment = new Hashtable<Object, Object>(System.getProperties());
		// add the installed Mylyn version to the environment so that we can
		// have
		// connectors that are filtered based on version of Mylyn
		Bundle bundle = Platform.getBundle("org.eclipse.mylyn.tasks.core"); //$NON-NLS-1$
		if (bundle == null) {
			bundle = Platform.getBundle("org.eclipse.mylyn.commons.core"); //$NON-NLS-1$
		}
		String versionString = (String) bundle.getHeaders().get("Bundle-Version"); //$NON-NLS-1$
		if (versionString != null) {
			Version version = new Version(versionString);
			environment.put("org.eclipse.mylyn.version", version.toString()); //$NON-NLS-1$
			environment.put("org.eclipse.mylyn.version.major", version.getMajor()); //$NON-NLS-1$
			environment.put("org.eclipse.mylyn.version.minor", version.getMinor()); //$NON-NLS-1$
			environment.put("org.eclipse.mylyn.version.micro", version.getMicro()); //$NON-NLS-1$
		}
	}

	protected Pattern createPattern(String filterText) {
		if (filterText == null || filterText.length() == 0) {
			return null;
		}
		String regex = filterText.replace("\\", "\\\\").replace("?", ".").replace("*", ".*?"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		try {
			return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		} catch (Exception e) {
			return null;
		}
	}

	private void createRefreshJob() {
		refreshJob = new WorkbenchJob("filter") { //$NON-NLS-1$

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (filterText.isDisposed()) {
					return Status.CANCEL_STATUS;
				}
				String text = filterText.getText();
				text = text.trim();

				if (!previousFilterText.equals(text)) {
					previousFilterText = text;
					filterPattern = createPattern(previousFilterText);
					if (clearFilterTextControl != null) {
						clearFilterTextControl.setVisible(!previousFilterText.isEmpty());
					}
					createBodyContents();
				}
				return Status.OK_STATUS;
			}
		};
		refreshJob.setSystem(true);
	}

	private void discoveryUpdated(final boolean wasCancelled) {
		if (body == null || body.isDisposed()) {
			return;
		}
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				createBodyContents();
				if (body == null || body.isDisposed()) {
					return;
				}
				DiscoveryViewer.this.installableConnectors.clear();
				DiscoveryViewer.this.updatableConnectors.clear();
				DiscoveryViewer.this.installedConnectors.clear();
				if (DiscoveryViewer.this.discoveries != null && !wasCancelled) {
					for (ConnectorDiscovery discovery : DiscoveryViewer.this.discoveries.values()) {
						for (DiscoveryCategory category : discovery.getCategories()) {
							if (! category.getConnectors().isEmpty()) {
								return;
							}
						}
					}
					// nothing was discovered: notify the user
					MessageDialog.openWarning(parent.getShell(), Messages.ConnectorDiscoveryWizardMainPage_noConnectorsFound,
							Messages.ConnectorDiscoveryWizardMainPage_noConnectorsFound_description);
				}
			}
		});
	}

	private boolean filterMatches(String text) {
		boolean match = previousFilterText == null || previousFilterText.isEmpty();
		if (text != null && !match && previousFilterText != null) {
			match = text.toLowerCase().contains(previousFilterText.toLowerCase());
			if (!match && filterPattern != null) {
				match = filterPattern.matcher(text).find();
			}
		}
		return text != null && match;
	}

	private void filterTextChanged() {
		refreshJob.cancel();
		refreshJob.schedule(200L);
	}

	public Control getControl() {
		return topLevelControl;
	}

	/**
	 * the environment in which discovery should be performed.
	 * 
	 * @see ConnectorDiscovery#getEnvironment()
	 */
	public Dictionary<Object, Object> getEnvironment() {
		return environment;
	}

	public Set<ConnectorDescriptor> getInstallableConnectors() {
		return installableConnectors;
	}
	
	public Set<ConnectorDescriptor> getInstalledConnectors() {
		return this.installedConnectors;
	}
	
	public Set<ConnectorDescriptor> getUpdatableConnectors() {
		return this.updatableConnectors;
	}
	
	/**
	 * Contains a list of {@link ConnectorDescriptorItemUi}
	 */
	@Override
	public IStructuredSelection getSelection() {
		List<ConnectorDescriptorItemUi> elements = new ArrayList<ConnectorDescriptorItemUi>();
		for (ConnectorDescriptorItemUi item : elements) {
			if (item.getConnector().isSelected()) {
				elements.add(item);
			}
		}
		return new StructuredSelection(elements);
	}

	public boolean getVerifyUpdateSiteAvailability() {
		return verifyUpdateSiteAvailability;
	}

	private boolean hasTooltip(final DiscoveryCategory category) {
		return category.getOverview() != null && category.getOverview().getSummary() != null
				&& category.getOverview().getSummary().length() > 0;
	}

	private static void hookRecursively(Control control, Listener listener) {
		control.addListener(SWT.Dispose, listener);
		control.addListener(SWT.MouseHover, listener);
		control.addListener(SWT.MouseMove, listener);
		control.addListener(SWT.MouseExit, listener);
		control.addListener(SWT.MouseDown, listener);
		control.addListener(SWT.MouseWheel, listener);
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				hookRecursively(child, listener);
			}
		}
	}

	static void hookTooltip(final Control parent, final Widget tipActivator, final Control exitControl,
			final Control titleControl, AbstractDiscoverySource source, Overview overview, Image image) {
		final OverviewToolTip toolTip = new OverviewToolTip(parent, source, overview, image);
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseHover:
					toolTip.show(titleControl);
					break;
				case SWT.Dispose:
				case SWT.MouseWheel:
					toolTip.hide();
					break;
				}

			}
		};
		tipActivator.addListener(SWT.Dispose, listener);
		tipActivator.addListener(SWT.MouseWheel, listener);
		if (image != null) {
			tipActivator.addListener(SWT.MouseHover, listener);
		}
		Listener selectionListener = new Listener() {
			public void handleEvent(Event event) {
				toolTip.show(titleControl);
			}
		};
		tipActivator.addListener(SWT.Selection, selectionListener);
		Listener exitListener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseWheel:
					toolTip.hide();
					break;
				case SWT.MouseExit:
					/*
					 * Check if the mouse exit happened because we move over the
					 * tooltip
					 */
					Rectangle containerBounds = exitControl.getBounds();
					Point displayLocation = exitControl.getParent().toDisplay(containerBounds.x, containerBounds.y);
					containerBounds.x = displayLocation.x;
					containerBounds.y = displayLocation.y;
					if (containerBounds.contains(Display.getCurrent().getCursorLocation())) {
						break;
					}
					toolTip.hide();
					break;
				}
			}
		};
		hookRecursively(exitControl, exitListener);
	}

	private void initializeColors() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		if (colorWhite == null) {
			ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
			if (!colorRegistry.hasValueFor(COLOR_WHITE)) {
				colorRegistry.put(COLOR_WHITE, new RGB(255, 255, 255));
			}
			colorWhite = colorRegistry.get(COLOR_WHITE);
		}
		if (colorCategoryGradientStart == null) {
			colorCategoryGradientStart = themeManager.getCurrentTheme()
					.getColorRegistry()
					.get(CommonThemes.COLOR_CATEGORY_GRADIENT_START);
			colorCategoryGradientEnd = themeManager.getCurrentTheme()
					.getColorRegistry()
					.get(CommonThemes.COLOR_CATEGORY_GRADIENT_END);
		}
	}

	private void initializeCursors() {
		if (handCursor == null) {
			handCursor = new Cursor(this.topLevelControl.getShell().getDisplay(), SWT.CURSOR_HAND);
			disposables.add(handCursor);
		}
	}

	private FontDescriptor createFontDescriptor(int style, float heightMultiplier) {
		Font baseFont = JFaceResources.getDialogFont();
		FontData[] fontData = baseFont.getFontData();
		FontData[] newFontData = new FontData[fontData.length];
		for (int i = 0; i < newFontData.length; i++) {
			newFontData[i] = new FontData(fontData[i].getName(), (int) (fontData[i].getHeight() * heightMultiplier),
					fontData[i].getStyle() | style);
		}
		return FontDescriptor.createFrom(newFontData);
	}

	private void initializeFonts() {
		// create a level-2 heading font
		if (h2Font == null) {
			h2Font = new Font(Display.getCurrent(), createFontDescriptor(SWT.BOLD, 1.25f).getFontData());
			disposables.add(h2Font);
		}
		// create a level-1 heading font
		if (h1Font == null) {
			h1Font = new Font(Display.getCurrent(), createFontDescriptor(SWT.BOLD, 1.35f).getFontData());
			disposables.add(h1Font);
		}
	}

	private void initializeImages() {
		if (infoImage == null) {
			infoImage = DiscoveryImages.MESSAGE_INFO.createImage();
			disposables.add(infoImage);
		}
	}

//	/**
//	 * indicate if there is nothing to display in the UI, given the current state of
//	 * {@link org.eclipse.mylyn.internal.discovery.ui.wizards.ConnectorDiscoveryWizard#isVisible(ConnectorDescriptorKind) filters}.
//	 */
//	private boolean isEmpty(ConnectorDiscovery discovery) {
//		for (DiscoveryCategory category : discovery.getCategories()) {
//			if (!isEmpty(category)) {
//				return false;
//			}
//		}
//		return true;
//	}

//	/**
//	 * indicate if the category has nothing to display in the UI, given the current state of
//	 * {@link org.eclipse.mylyn.internal.discovery.ui.wizards.ConnectorDiscoveryWizard#isVisible(ConnectorDescriptorKind) filters}.
//	 */
//	private boolean isEmpty(DiscoveryCategory category) {
//		if (category.getConnectors().isEmpty()) {
//			return true;
//		}
//		for (ConnectorDescriptor descriptor : category.getConnectors()) {
//			if (connector) {
//				return false;
//			}
//		}
//		return true;
//	}

	/**
	 * Adds an EXCLUSION filter to the viewer. When enabled, this filter
	 * will exclude all elements which have filter.select returning false
	 * @param filter
	 * @param label
	 * @param enableByDefault
	 */
	public void addFilter(ViewerFilter filter, String label, boolean enableByDefault) {
		userFilters.add(new FilterEntry(filter, label, enableByDefault));
	}

	/**
	 * indicate if a text field should be provided to allow the user to filter connector descriptors
	 */
	public boolean isShowConnectorDescriptorTextFilter() {
		return showConnectorDescriptorTextFilter;
	}

	void modifySelection(final ConnectorDescriptorItemUi item, boolean selected) {
		DiscoveryConnector connector = item.getConnector();
		connector.setSelected(selected);
		if (connector.isInstalled()) {
			if (!item.isUpToDate()) {
				if (selected) {
					this.updatableConnectors.add(connector);
				} else {
					this.updatableConnectors.remove(connector);
				}
			}
			if (selected) {
				this.installedConnectors.add(connector);
			} else {
				this.installedConnectors.remove(connector);
			}
		} else if (connector.isInstallable()) {
			if (selected) {
				installableConnectors.add(connector);
			} else {
				installableConnectors.remove(connector);
			}
		}
		fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
	}

	public void showConnectorControl(ConnectorDescriptorItemUi item) {
		this.bodyScrolledComposite.showControl(item.getControl());
	}
	
	/**
	 * Add an URL to the list of catalog.
	 * This won't have any effect until catalog gets recomputed, via
	 * {@link DiscoveryViewer#updateDiscovery()} or similar.
	 * 
	 * Ideally, use it only when initiating the viewer, before it gets
	 * actually displayed.
	 * If you want to make viewer more dynamic, you should consider dealing
	 * with filter enablement instead.
	 * 
	 * @param directoryUrl
	 */
	public void addDirectoryUrl(String directoryUrl) {
		if (this.directoryUrls == null) {
			this.directoryUrls = new HashSet<String>();
		}
		this.directoryUrls.add(directoryUrl);
	}
	
	/**
	 * Clears the list of URL loaded by catalog
	 * This won't have any effect until catalog gets recomputed, via
	 * {@link DiscoveryViewer#updateDiscovery()} or similar.
	 * 
	 * Ideally, use it only when initiating the viewer, before it gets
	 * actually displayed.
	 * If you want to make viewer more dynamic, you should consider dealing
	 * with filter enablement instead.
	 */
	public void resetDirectoryUrls() {
		this.directoryUrls.clear();
	}
	
	/**
	 * Add some URLs to the list of catalog.
	 * This won't have any effect until catalog gets recomputed, via
	 * {@link DiscoveryViewer#updateDiscovery()} or similar.
	 * 
	 * Ideally, use it only when initiating the viewer, before it gets
	 * actually displayed.
	 * If you want to make viewer more dynamic, you should consider dealing
	 * with filter enablement instead.
	 * 
	 * @param directoryUrl
	 */
	public Set<String> getDirectoryUrls() {
		return Collections.unmodifiableSet(this.directoryUrls);
	}
	
	public void addDirectoryUrls(Collection<String> urls) {
		if (this.directoryUrls == null) {
			this.directoryUrls = new HashSet<String>();
		}
		this.directoryUrls.addAll(urls);
	}

	/**
	 * the environment in which discovery should be performed.
	 * 
	 * @see ConnectorDiscovery#getEnvironment()
	 */
	public void setEnvironment(Dictionary<Object, Object> environment) {
		if (environment == null) {
			throw new IllegalArgumentException();
		}
		this.environment = environment;
	}

	/**
	 * indicate if a text field should be provided to allow the user to filter connector descriptors
	 */
	public void setShowConnectorDescriptorTextFilter(boolean showConnectorDescriptorTextFilter) {
		this.showConnectorDescriptorTextFilter = showConnectorDescriptorTextFilter;
	}

	public void setVerifyUpdateSiteAvailability(boolean verifyUpdateSiteAvailability) {
		this.verifyUpdateSiteAvailability = verifyUpdateSiteAvailability;
	}

	public void updateDiscovery() {
		final Dictionary<Object, Object> environment = getEnvironment();
		boolean wasCancelled = false;
		try {
			final IStatus[] result = new IStatus[1];
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (DiscoveryViewer.this.installedFeatures == null) {
						DiscoveryViewer.this.installedFeatures = getInstalledFeatures(monitor);
					}
					// TODO parallize this loop
					for (String directoryUrl : DiscoveryViewer.this.directoryUrls) {
						ConnectorDiscovery connectorDiscovery = DiscoveryUtil.createConnectorDiscovery(directoryUrl);
						connectorDiscovery.setEnvironment(environment);
						connectorDiscovery.setVerifyUpdateSiteAvailability(false);
						try {
							result[0] = connectorDiscovery.performDiscovery(monitor);
						} finally {
							if (monitor.isCanceled()) {
								return;
							}
							DiscoveryViewer.this.discoveries.put(directoryUrl, connectorDiscovery);
							postDiscovery(connectorDiscovery);
						}
					}
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			});

			if (result[0] != null && !result[0].isOK()) {
				StatusManager.getManager().handle(result[0],
						StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
			}
		} catch (InvocationTargetException e) {
			IStatus status = computeStatus(e, Messages.ConnectorDiscoveryWizardMainPage_unexpectedException);
			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
			return;
		} catch (InterruptedException e) {
			// cancelled by user so nothing to do here.
			wasCancelled = true;
			return;
		}
		if (this.discoveries != null && !this.discoveries.isEmpty()) {
			discoveryUpdated(wasCancelled);
			if (verifyUpdateSiteAvailability) {
				try {
					context.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							// TODO parallelize
							for (ConnectorDiscovery discovery : DiscoveryViewer.this.discoveries.values()) {
								if (!discovery.getConnectors().isEmpty()) {
									discovery.verifySiteAvailability(monitor);
								}
							}
						}
					});
				} catch (InvocationTargetException e) {
					IStatus status = computeStatus(e, Messages.ConnectorDiscoveryWizardMainPage_unexpectedException);
					StatusManager.getManager().handle(status,
							StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
				} catch (InterruptedException e) {
					// cancelled by user so nothing to do here.
					wasCancelled = true;
					return;
				}
			}
		}
		// help UI tests
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (body == null || body.isDisposed()) {
					return;
				}
				body.setData("discoveryComplete", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	protected void postDiscovery(ConnectorDiscovery connectorDiscovery) {
		for (DiscoveryConnector connector : connectorDiscovery.getConnectors()) {
			connector.setInstalled(installedFeatures != null
					&& installedFeatures.containsAll(connector.getInstallableUnits()));
		}
	}

	protected Set<String> getInstalledFeatures(IProgressMonitor monitor) throws InterruptedException {
		return DiscoveryUi.createInstallJob().getInstalledFeatures(monitor);
	}

	@Override
	public Object getInput() {
		return this.directoryUrls;
	}

	@Override
	public void refresh() {
		createBodyContents();
	}

	/**
	 * this method supports either single String or Collection of String.
	 * Those strings are expected to be discovery catalog URLs
	 */
	@Override
	public void setInput(Object arg0) {
		if (arg0 instanceof Collection) {
			Collection<?> input = (Collection<?>) arg0;
			for (Object o : input) {
				if (o instanceof String) {
					addDirectoryUrl((String)o);
				} else {
					JBossCentralActivator.getDefault().getLog().log(new Status(IStatus.WARNING,
							JBossCentralActivator.PLUGIN_ID,
							"Ignored input of type " + o.getClass().getName() + " in discovery viewer"));
				}
			}
		} else if (arg0 instanceof String) {
			addDirectoryUrl((String)arg0);
		} else {
			JBossCentralActivator.getDefault().getLog().log(new Status(IStatus.WARNING,
					JBossCentralActivator.PLUGIN_ID,
					"Ignored input of type " + arg0.getClass().getName() + " in discovery viewer"));
		}
	}

	@Override
	public void setSelection(ISelection arg0, boolean arg1) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
