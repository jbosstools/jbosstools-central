/*************************************************************************************
 * Copyright (c) 2010-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.cli.MavenCli;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Settings;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.jboss.tools.maven.ui.Activator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * 
 * @author snjeza
 *
 */
public class ConfigureMavenRepositoriesWizardPage extends WizardPage {

	private static final String ERROR_TITLE = "Error";

	private static final String SNAPSHOTS_ELEMENT = "snapshots"; //$NON-NLS-1$

	private static final String UPDATE_POLICY_ELEMENT = "updatePolicy"; //$NON-NLS-1$

	private static final String ENABLED_ELEMENT = "enabled"; //$NON-NLS-1$

	private static final String RELEASES_ELEMENT = "releases"; //$NON-NLS-1$

	private static final String LAYOUT_ELEMENT = "layout"; //$NON-NLS-1$

	private static final String POLICY_NEVER = "never"; //$NON-NLS-1$

	private static final String LAYOUT_DEFAULT = "default"; //$NON-NLS-1$

	private static final String NAME_ELEMENT = "name"; //$NON-NLS-1$

	private static final String URL_ELEMENT = "url"; //$NON-NLS-1$

	private static final String REPOSITORY_ELEMENT = "repository"; //$NON-NLS-1$

	private static final String LASTPATH = "lastPath"; //$NON-NLS-1$

	private static final String CONFIGURE_MAVEN_REPOSITORIES = "ConfigureMavenRepositories"; //$NON-NLS-1$

	private static final String JBOSS_EAP_MAVEN_REPOSITORY = "JBoss EAP Maven Repository"; //$NON-NLS-1$

	private static final String PLUGIN_REPOSITORIES_ELEMENT = "pluginRepositories"; //$NON-NLS-1$

	private static final String PLUGIN_REPOSITORY_ELEMENT = "pluginRepository"; //$NON-NLS-1$

	private static final String REPOSITORIES_ELEMENT = "repositories"; //$NON-NLS-1$

	private static final String ACTIVE_BY_DEFAULT_ELEMENT = "activeByDefault"; //$NON-NLS-1$

	private static final String ACTIVATION_ELEMENT = "activation"; //$NON-NLS-1$

	private static final String ID_ELEMENT = "id"; //$NON-NLS-1$

	private static final String PROFILE_ELEMENT = "profile"; //$NON-NLS-1$

	private static final String PROFILES_ELEMENT = "profiles"; //$NON-NLS-1$

	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	
	public static final String JBOSSTOOLS_MAVEN_PROFILE_ID = "jbosstools-maven-profile"; //$NON-NLS-1$

	private static final String JSF_IMPL = "com" + File.separator + "sun" + File.separator + "faces" + File.separator + "jsf-impl";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String WFK_BOMS = "com" + File.separator + "redhat" + File.separator + "jboss" + File.separator + "wfk" + File.separator + "boms";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	
	private static final String PAGE_NAME = "org.jboss.tools.maven.ui.wizard.page"; //$NON-NLS-1$
	private static final String ADD_ALL = " Add All>> ";
	private static final String ADD = " Add>> ";
	private static final String REMOVE_ALL = " <<Remove All ";
	private static final String REMOVE = " <Remove ";

	private Button removeButton;
	private Button removeAllButton;
	private Button addButton;
	private Button addAllButton;
	private IMavenConfiguration mavenConfiguration;
	private IMaven maven;
	private Image jbossImage;
	private TableViewer includedRepositoriesViewer;
	private Set<RepositoryWrapper> includedRepositories;
	private Set<RepositoryWrapper> availableRepositories;
	private Set<RepositoryWrapper> selectedIncludedRepositories = new HashSet<RepositoryWrapper>();
	private Set<RepositoryWrapper> selectedAvailableRepositories = new HashSet<RepositoryWrapper>();
	private TableViewer availableRepositoriesViewer;

	private String localRepository;

	private Document document;

	private CompareConfiguration compareConfiguration;

	private TextMergeViewer previewViewer;

	private String newSettings;

	private String oldSettings;

	private Element jbossMavenProfile;

	private Element repositoriesElement;

	private Element pluginRepositoriesElement;
	
	private IDialogSettings dialogSettings;
	
	public ConfigureMavenRepositoriesWizardPage() {
		super(PAGE_NAME);
		setTitle("Configure Maven Repositories");
		mavenConfiguration = MavenPlugin.getMavenConfiguration();
		maven = MavenPlugin.getMaven();
		try {
			maven.reloadSettings();
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private String getLocalRepository() {
		if (localRepository == null) {
			String userSettings = getUserSettings();
			String globalSettings = MavenPlugin.getMavenRuntimeManager()
					.getGlobalSettingsFile();
			try {
				Settings settings = maven.buildSettings(globalSettings,
						userSettings);
				localRepository = settings.getLocalRepository();
				if (localRepository == null) {
					localRepository = RepositorySystem.defaultUserLocalRepository
							.getAbsolutePath();
				}
			} catch (CoreException e) {
				Activator.log(e);
			} 
		}
		return localRepository;
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);
		Dialog.applyDialogFont(composite);
		setControl(composite);
		
		Label userSettingsLabel = new Label(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		userSettingsLabel.setLayoutData(gd);
		
		String userSettings = getUserSettings();
	    userSettingsLabel.setText("User settings: " + userSettings);
	    
	    File settingsFile = new File(userSettings);
		try {
			if (!settingsFile.exists()) {
				createDefaultSettings();
				maven.reloadSettings();
				oldSettings = ""; //$NON-NLS-1$
			} else {
				oldSettings = readFile(settingsFile);
				newSettings = oldSettings;
			}
		} catch (Exception e) {
			Activator.log(e);
			MessageDialog.openError(getShell(), ERROR_TITLE, e.getMessage());
			throw new RuntimeException(e);
		}
		DocumentBuilder builder;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Activator.log(e);
			MessageDialog.openError(getShell(), ERROR_TITLE, e.getMessage());
			throw new RuntimeException(e);
		}
		try {
			document = builder.parse(new InputSource(new StringReader(
					newSettings)));
		} catch (Exception e) {
			Activator.log(e);
			try {
				createDefaultSettings();
			} catch (Exception e1) {
				Activator.log(e1);
				MessageDialog.openError(getShell(), ERROR_TITLE, e1.getMessage());
				throw new RuntimeException(e1);
			}
			try {
				document = builder.parse(new InputSource(new StringReader(
						newSettings)));
			} catch (Exception e1) {
				Activator.log(e1);
				MessageDialog.openError(getShell(), ERROR_TITLE, e1.getMessage());
				throw new RuntimeException(e1);
			}
		}
		NodeList profilesList = document.getElementsByTagName(PROFILES_ELEMENT);
		Node profiles;
		if (profilesList.getLength() > 0) {
			profiles = profilesList.item(0);
			NodeList profileNodes = profiles.getChildNodes();
			int length = profileNodes.getLength();

			for (int i = 0; i < length; i++) {
				Node profile = profileNodes.item(i);
				if (profile.getNodeType() == Node.ELEMENT_NODE
						&& PROFILE_ELEMENT.equals(profile.getNodeName())) {
					NodeList profileElements = profile.getChildNodes();
					for (int j = 0; j < profileElements.getLength(); j++) {
						Node node = profileElements.item(j);
						if (node.getNodeType() == Node.ELEMENT_NODE
								&& ID_ELEMENT.equals(node.getNodeName())) {
							String id = node.getTextContent();
							if (id != null) {
								id = id.trim();
							}
							if (JBOSSTOOLS_MAVEN_PROFILE_ID.equals(id)) {
								jbossMavenProfile = (Element) profile;
								break;
							}
						}
					}
				}
				if (jbossMavenProfile != null) {
					break;
				}
			}
		} else {
			profiles = document.createElement(PROFILES_ELEMENT);
			document.getDocumentElement().appendChild(profiles);
		}

		if (jbossMavenProfile == null) {
			createJBossMavenProfile(profiles);
		} else {
			configureJBossMavenProfile();
		}
        
	    Group repositoriesGroup = new Group(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        GridLayout layout = new GridLayout(3, false);
        repositoriesGroup.setLayout(layout);
        repositoriesGroup.setLayoutData(gd);
        repositoriesGroup.setText("Repositories");
	
        Composite availableRepositoriesComposite = new Composite(repositoriesGroup, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        availableRepositoriesComposite.setLayoutData(gd);
        availableRepositoriesComposite.setLayout(new GridLayout());
        
        Label availableRepositoriesLabel = new Label(availableRepositoriesComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        availableRepositoriesLabel.setLayoutData(gd);
        availableRepositoriesLabel.setText("Available Repositories:");
        
        availableRepositoriesViewer = new TableViewer(availableRepositoriesComposite, SWT.BORDER | SWT.MULTI |SWT.H_SCROLL|SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 150;
        gd.widthHint = 350;
        availableRepositoriesViewer.getTable().setLayoutData(gd);
        availableRepositoriesViewer.getTable().setLinesVisible(false);
        availableRepositoriesViewer.getTable().setHeaderVisible(false);
        TableViewerColumn column = new TableViewerColumn(availableRepositoriesViewer, SWT.NONE);
        column.getColumn().setText("Repository");
        //column.getColumn().setWidth(350);
        column.getColumn().setResizable(true);
        ColumnLayoutData columnLayoutData = new ColumnWeightData(350,350);
        TableLayout availableLayout = new AutoResizeTableLayout(availableRepositoriesViewer.getTable());
        availableLayout.addColumnData(columnLayoutData);
        availableRepositoriesViewer.setContentProvider(new ArrayContentProvider());
        availableRepositoriesViewer.setLabelProvider(new RepositoryLabelProvider());
        ColumnViewerToolTipSupport.enableFor(availableRepositoriesViewer,ToolTip.NO_RECREATE);
        
        Composite buttonsComposite = new Composite(repositoriesGroup, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        buttonsComposite.setLayoutData(gd);
        buttonsComposite.setLayout(new GridLayout(1, false));
        
        Label buttonsLabel = new Label(buttonsComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        buttonsLabel.setLayoutData(gd);
        
        GC gc = new GC(buttonsComposite);
        int maxAddRemoveButtonsWidth = computeMaxAddRemoveButtonsWidth(gc);
        gc.dispose();
        
        Composite buttonsComp = new Composite(buttonsComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        buttonsComp.setLayoutData(gd);
        buttonsComp.setLayout(new GridLayout());
        
        removeButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, REMOVE);
        removeAllButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, REMOVE_ALL);
        addButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, ADD);
        addAllButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, ADD_ALL);
   
        Composite includedRepositoriesComposite = new Composite(repositoriesGroup, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        includedRepositoriesComposite.setLayoutData(gd);
        includedRepositoriesComposite.setLayout(new GridLayout(1, false));
        
        Label includedRepositoriesLabel = new Label(includedRepositoriesComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        includedRepositoriesLabel.setLayoutData(gd);
        includedRepositoriesLabel.setText("Included Repositories:");
	    
	    includedRepositoriesViewer = new TableViewer(includedRepositoriesComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL|SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.widthHint = 350;
        gd.heightHint = 150;
        includedRepositoriesViewer.getTable().setLayoutData(gd);
        includedRepositoriesViewer.getTable().setLinesVisible(false);
        includedRepositoriesViewer.getTable().setHeaderVisible(false);
        TableViewerColumn c = new TableViewerColumn(includedRepositoriesViewer, SWT.NONE);
        c.getColumn().setText("Repository");
        c.getColumn().setResizable(true);
        TableLayout includedLayout = new AutoResizeTableLayout(includedRepositoriesViewer.getTable());
        includedLayout.addColumnData(columnLayoutData);
        
        includedRepositoriesViewer.setContentProvider(new ArrayContentProvider());
        includedRepositoriesViewer.setLabelProvider(new RepositoryLabelProvider());
        ColumnViewerToolTipSupport.enableFor(availableRepositoriesViewer, ToolTip.NO_RECREATE);
        
        Button recognizeButton = new Button(composite, SWT.PUSH);
		recognizeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));
		recognizeButton.setText("Recognize JBoss Maven Enterprise Repositories...");
		recognizeButton.setImage(getJBossImage());
		
		recognizeButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setMessage("Select the directory in which to search for JBoss Maven Enterprise Repositories:");
				directoryDialog.setText("Search for JBoss Maven Enterprise Repositories");

				dialogSettings = Activator.getDefault().getDialogSettings();
				IDialogSettings configureMavenRepositories = dialogSettings.getSection(CONFIGURE_MAVEN_REPOSITORIES);
				if (configureMavenRepositories == null) {
					configureMavenRepositories = dialogSettings.addNewSection(CONFIGURE_MAVEN_REPOSITORIES);
				}
				String filterPath = configureMavenRepositories.get(LASTPATH);
				if (filterPath != null) {
					directoryDialog.setFilterPath(filterPath);
				}
				String pathStr = directoryDialog.open();
				if (pathStr == null)
					return;
				
				configureMavenRepositories.put(LASTPATH, pathStr);
				final IPath path = new Path(pathStr);
				
				final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				dialog.setBlockOnOpen(false);
				dialog.setCancelable(true);
				dialog.open();
				final IProgressMonitor monitor = dialog.getProgressMonitor();
				monitor.beginTask("Searching...", 110);
				final Set<RepositoryWrapper> repos = new HashSet<RepositoryWrapper>();
				
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor2) {
						searchForRepositories(path, repos, monitor2);
					}
				};
				try {
					dialog.run(true, true, runnable);
				} catch (Exception e1) {
					Activator.log(e1);
				} 
				
				if (monitor.isCanceled()) {
					return;
				}
				for (RepositoryWrapper wrapper:repos) {
					if (!includedRepositories.contains(wrapper)) {
						availableRepositories.add(wrapper);
					}
				}
				refreshRepositories();
			}
		});
		
		includedRepositories = getIncludedRepositories();
		availableRepositories = getAvailableRepositories();
		List<RepositoryWrapper> remove = new ArrayList<RepositoryWrapper>();
		for (RepositoryWrapper availableRepository:availableRepositories) {
			if (includedRepositories.contains(availableRepository)) {
				remove.add(availableRepository);
			}
		}
		for (RepositoryWrapper repository:remove) {
			availableRepositories.remove(repository);
		}
		
		availableRepositoriesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				selectedAvailableRepositories.clear();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Iterator iterator = selection.iterator();
					while (iterator.hasNext()) {
						Object object = iterator.next();
						if (object instanceof RepositoryWrapper) {
							selectedAvailableRepositories.add((RepositoryWrapper) object);
						}
					}
				}
				configureButtons();
			}
		});
		includedRepositoriesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				selectedIncludedRepositories.clear();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Iterator iterator = selection.iterator();
					while (iterator.hasNext()) {
						Object object = iterator.next();
						if (object instanceof RepositoryWrapper) {
							selectedIncludedRepositories.add((RepositoryWrapper) object);
						}
					}
				}
				configureButtons();
			}
		});
		removeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (RepositoryWrapper wrapper:selectedIncludedRepositories) {
					if (wrapper.isJBossRepository()) {
						includedRepositories.remove(wrapper);
						availableRepositories.add(wrapper);
						removeRepository(wrapper);
					}
				}
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		removeAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<RepositoryWrapper> list = new ArrayList<RepositoryWrapper>();
				for (RepositoryWrapper wrapper:includedRepositories) {
					if (wrapper.isJBossRepository()) {
						list.add(wrapper);
						removeRepository(wrapper);
					}
				}
				includedRepositories.removeAll(list);
				availableRepositories.addAll(list);
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (RepositoryWrapper wrapper:selectedAvailableRepositories) {
					if (wrapper.isJBossRepository()) {
						includedRepositories.add(wrapper);
						availableRepositories.remove(wrapper);
						addRepository(wrapper);
					}
				}
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		addAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<RepositoryWrapper> list = new ArrayList<RepositoryWrapper>();
				for (RepositoryWrapper wrapper:availableRepositories) {
					if (wrapper.isJBossRepository()) {
						list.add(wrapper);
						addRepository(wrapper);
					}
				}
				includedRepositories.addAll(list);
				availableRepositories.removeAll(list);
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		
		Label separator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setVisible(false);
		
		Label previewLabel= new Label(composite, SWT.NULL);
		previewLabel.setText("Preview:");
		
		createPreviewer(composite);
		
		refreshRepositories();
		setPageComplete(false);
	}

	protected void createDefaultSettings() throws CoreException,
			UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		maven.writeSettings(new Settings(), out);
		newSettings = new String(out.toByteArray(), UTF_8);
	}

	protected void addRepository(RepositoryWrapper wrapper) {
		addRepository(wrapper, repositoriesElement, false);
		addRepository(wrapper, pluginRepositoriesElement, true);
		
	}

	private void addRepository(RepositoryWrapper wrapper, Element repos, boolean isPluginRepository) {
		Element repository;
		if (isPluginRepository) {
			repository = document.createElement(PLUGIN_REPOSITORY_ELEMENT);
		} else {
			repository = document.createElement(REPOSITORY_ELEMENT);
		}
		repos.appendChild(repository);
		addElement(repository, ID_ELEMENT, wrapper.getRepository().getId());
		addElement(repository, NAME_ELEMENT, wrapper.getRepository().getName());
		addElement(repository, URL_ELEMENT, wrapper.getRepository().getUrl());
		addElement(repository, LAYOUT_ELEMENT, LAYOUT_DEFAULT);
		RepositoryPolicy policy = wrapper.getRepository().getReleases();
		if (policy != null) {
			Element releases = addElement(repository, RELEASES_ELEMENT, null);
			addElement(releases, ENABLED_ELEMENT, policy.isEnabled() ? "true" : "false");  //$NON-NLS-1$//$NON-NLS-2$
			addElement(releases, UPDATE_POLICY_ELEMENT, POLICY_NEVER);
			repository.appendChild(releases);
		}
		policy = wrapper.getRepository().getSnapshots();
		if (policy != null) {
			Element snapshots = addElement(repository, SNAPSHOTS_ELEMENT, null);
			addElement(snapshots, ENABLED_ELEMENT, policy.isEnabled() ? "true" : "false");  //$NON-NLS-1$//$NON-NLS-2$
			addElement(snapshots, UPDATE_POLICY_ELEMENT, POLICY_NEVER);
			repository.appendChild(snapshots);
		}
	}

	protected Element addElement(Element element, String name, String value) {
		Element child = document.createElement(name);
		if (value != null) {
			Text textNode = document.createTextNode(value);
			child.appendChild(textNode);
		}
		element.appendChild(child);
		return child;
	}

	protected void removeRepository(RepositoryWrapper wrapper) {
		String url = wrapper.getRepository().getUrl();
		removeRepository(url, repositoriesElement, false);
		removeRepository(url, pluginRepositoriesElement, true);
	}

	protected void removeRepository(String url, Element repos, boolean isPluginRepository) {
		NodeList repositoryNodeList = repos.getChildNodes();
		int len = repositoryNodeList.getLength();
		String name;
		if (isPluginRepository) {
			name = PLUGIN_REPOSITORY_ELEMENT;
		} else {
			name = REPOSITORY_ELEMENT;
		}
		Node repository = null;
		for (int i = 0; i < len; i++) {
			Node node = repositoryNodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())) {
				String urlNode = getRepositoryUrl(node);
				if (urlNode != null && urlNode.equals(url)) {
					repository = node;
					break;
				}
			}
		}
		if (repository != null) {
			repos.removeChild(repository);
		}
	}

	private String getRepositoryUrl(Node repository) {
		NodeList nodeList = repository.getChildNodes();
		int len = nodeList.getLength();
		for (int i = 0; i < len; i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && URL_ELEMENT.equals(node.getNodeName())) {
				String value = node.getTextContent();
				if (value != null) {
					value = value.trim();
					if (!value.endsWith(RepositoryWrapper.SEPARATOR)) {
						value = value + RepositoryWrapper.SEPARATOR;
					}
				}
				return value;
			}
		}
		return null;
	}

	private void configureJBossMavenProfile() {
		NodeList nodeList = jbossMavenProfile.getChildNodes();
		int len = nodeList.getLength();
		for (int i = 0; i < len; i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && REPOSITORIES_ELEMENT.equals(node.getNodeName())) {
				repositoriesElement = (Element) node;
			}
			if (node.getNodeType() == Node.ELEMENT_NODE && PLUGIN_REPOSITORIES_ELEMENT.equals(node.getNodeName())) {
				pluginRepositoriesElement = (Element) node;
			}
			if (repositoriesElement != null && pluginRepositoriesElement != null) {
				return;
			}
		}
		if (repositoriesElement == null) {
			repositoriesElement = document.createElement(REPOSITORIES_ELEMENT); 
			jbossMavenProfile.appendChild(repositoriesElement);
		}
		if (pluginRepositoriesElement == null) {
			pluginRepositoriesElement = document.createElement(PLUGIN_REPOSITORIES_ELEMENT);
			jbossMavenProfile.appendChild(pluginRepositoriesElement);
		}
	}
	
	private Element createJBossMavenProfile(Node profiles) {
		jbossMavenProfile = document.createElement(PROFILE_ELEMENT); 
		profiles.appendChild(jbossMavenProfile);
		Element id = document.createElement(ID_ELEMENT);
		id.setTextContent(JBOSSTOOLS_MAVEN_PROFILE_ID);
		jbossMavenProfile.appendChild(id);
		Element activation = document.createElement(ACTIVATION_ELEMENT);
		jbossMavenProfile.appendChild(activation);
		Element activeByDefault = document.createElement(ACTIVE_BY_DEFAULT_ELEMENT); 
		activeByDefault.setTextContent("true"); //$NON-NLS-1$
		activation.appendChild(activeByDefault);
		repositoriesElement = document.createElement(REPOSITORIES_ELEMENT); 
		jbossMavenProfile.appendChild(repositoriesElement);
		pluginRepositoriesElement = document.createElement(PLUGIN_REPOSITORIES_ELEMENT);
		jbossMavenProfile.appendChild(pluginRepositoriesElement);
		return jbossMavenProfile;
	}

	private void createPreviewer(Composite composite) {
		compareConfiguration= new CompareConfiguration();
		compareConfiguration.setAncestorLabel("Preview:");
		
		compareConfiguration.setLeftLabel("Old settings");
		compareConfiguration.setLeftEditable(false);
		
		compareConfiguration.setRightLabel("New settings");
		compareConfiguration.setRightEditable(false);
		
		previewViewer= new TextMergeViewer(composite, SWT.BORDER, compareConfiguration);
		
		previewViewer.setInput(
				new DiffNode(Differencer.CHANGE,
					null,
					new StringPreviewerInput(oldSettings),
					new PreviewerInput()
				)
			);
		
		Control control = previewViewer.getControl();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumHeight = 200;
		control.setLayoutData(gd);
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (compareConfiguration != null)
					compareConfiguration.dispose();
			}
		});

	}

	private String getNewSettings() {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute("indent-number", 2); //$NON-NLS-1$
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			DOMSource source = new DOMSource(document);
			transformer.transform(source, result);
			return writer.toString();
		} catch (Exception e) {
			Activator.log(e);
		} 
		return null;
	}

	private void searchForRepositories(IPath path,
			Set<RepositoryWrapper> repos, IProgressMonitor monitor) {
		File[] files = null;
		if (path != null) {
			File f = path.toFile();
			if (f.isDirectory()) {
				files = new File[1];
				files[0] = f;
			}
			else
				return;
		} else
			files = File.listRoots();

		if (files != null) {
			int size = files.length;
			int work = 100 / size;
			int workLeft = 100 - (work * size);
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled())
					return;
				if (files[i] != null && files[i].isDirectory()) {
					searchDir(repos, files[i], 4, monitor);
				}
				monitor.worked(work);
			}
			monitor.worked(workLeft);
		} else
			monitor.worked(100);
	}
	
	private void searchDir(Set<RepositoryWrapper> repos, File directory, int depth,
			IProgressMonitor monitor) {
		
		String localRepository = getLocalRepository();
		if (localRepository != null && localRepository.trim().equals(directory.getAbsolutePath())) {
			return;
		}
		monitor.setTaskName("Searching " + directory.getAbsolutePath());
		File comFile = new File(directory, "com"); //$NON-NLS-1$
		if (comFile.isDirectory()) {
			RepositoryWrapper repository = getRepositoryFromDir(directory, repos, monitor);
			if (repository != null) {
				repos.add(repository);
				return;
			}
		}
		
		if (depth == 0)
			return;
		
		File[] files = directory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		if (files != null) {
			int size = files.length;
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled())
					return;
				searchDir(repos, files[i], depth - 1, monitor);
			}
		}
	}

	private RepositoryWrapper getRepositoryFromDir(File directory, Set<RepositoryWrapper> repos, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return null;
		}
		
		File file = new File(directory, JSF_IMPL);
		if (file.isDirectory()) {
			File[] list = file.listFiles(new FileFilter() {
				
				public boolean accept(File pathname) {
					if (pathname != null && pathname.getName() != null && pathname.getName().contains("redhat")) {
						return true;
					}
					return false;
				}
			});
			if (list != null && list.length >= 1) {
				// JBoss EAP Maven Repository
				Repository repository = getDefaultRepository();
				String id = JBOSSTOOLS_MAVEN_PROFILE_ID;
				Set<RepositoryWrapper> allRepositories = new HashSet<RepositoryWrapper>();
				allRepositories.addAll(repos);
				allRepositories.addAll(includedRepositories);
				allRepositories.addAll(availableRepositories);
				repository.setId(getUniqueId(id, allRepositories));
				repository.setName(JBOSS_EAP_MAVEN_REPOSITORY);
				try {
					repository.setUrl(directory.toURI().toURL().toString());
				} catch (MalformedURLException e) {
					Activator.log(e);
				}
				RepositoryWrapper wrapper = new RepositoryWrapper(repository, JBOSSTOOLS_MAVEN_PROFILE_ID);
				return wrapper;
			}
		}
		file = new File(directory, WFK_BOMS);
		if (file.isDirectory()) {
			// JBoss WFK Maven Repository
			Repository repository = getDefaultRepository();
			repository.setId("jboss-wfk-maven-repository");
			repository.setName("JBoss WFK Maven Repository");
			try {
				repository.setUrl(directory.toURI().toURL().toString());
			} catch (MalformedURLException e) {
				Activator.log(e);
			}
			RepositoryWrapper wrapper = new RepositoryWrapper(repository, JBOSSTOOLS_MAVEN_PROFILE_ID);
			return wrapper;
		}
		return null;
	}

	private String getUniqueId(String id, Set<RepositoryWrapper> allRepositories) {
		int i = 0;
		String startId = id;
		while (true) {
			boolean found = false;
			for (RepositoryWrapper wrapper:allRepositories) {
				if (id.equals(wrapper.getRepository().getId())) {
					id = startId + "." + i++;
					found = true;
					break;
				}
			}
			if (!found) {
				return id;
			}
		}
	}

	private String readFile(File file) throws FileNotFoundException {
	    StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8");
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + NL);
	      }
	    }
	    finally{
	      scanner.close();
	    }
	    return text.toString();
	  }
	
	private String getUserSettings() {
		String userSettings = mavenConfiguration.getUserSettingsFile();
	    if(userSettings == null || userSettings.length() == 0) {
	    	userSettings = MavenCli.DEFAULT_USER_SETTINGS_FILE.getAbsolutePath();
	    }
		return userSettings;
	}
	
	private void configureButtons() {
		removeAllButton.setEnabled(false);
		removeButton.setEnabled(false);
		for (RepositoryWrapper wrapper:selectedIncludedRepositories) {
			if (JBOSSTOOLS_MAVEN_PROFILE_ID.equals(wrapper.getProfileId())) {
				removeButton.setEnabled(true);
				break;
			}
		}
		for (RepositoryWrapper wrapper:includedRepositories) {
			if (JBOSSTOOLS_MAVEN_PROFILE_ID.equals(wrapper.getProfileId())) {
				removeAllButton.setEnabled(true);
				break;
			}
		}
		addButton.setEnabled(selectedAvailableRepositories.size() > 0);
		addAllButton.setEnabled(availableRepositories.size() > 0);
	}

	private void refreshRepositories() {
		includedRepositoriesViewer.setInput(includedRepositories.toArray(new RepositoryWrapper[0]));
        availableRepositoriesViewer.setInput(availableRepositories.toArray(new RepositoryWrapper[0]));
		previewViewer.refresh();
        selectedIncludedRepositories.clear();
        selectedAvailableRepositories.clear();
        includedRepositoriesViewer.setSelection(new StructuredSelection(selectedIncludedRepositories.toArray(new RepositoryWrapper[0])));
        availableRepositoriesViewer.setSelection(new StructuredSelection(selectedAvailableRepositories.toArray(new RepositoryWrapper[0])));
		configureButtons();
	}

	private Set<RepositoryWrapper> getIncludedRepositories() {
		Set<RepositoryWrapper> repositories = new TreeSet<RepositoryWrapper>();
        try {
			List<Profile> activeProfiles = getActiveProfiles();
			for (Profile profile:activeProfiles) {
				List<Repository> repos = profile.getRepositories();
				for (Repository repository:repos) {
					String profileId = profile.getId() == null ? "" : profile.getId();  //$NON-NLS-1$
					repositories.add(new RepositoryWrapper(repository, profileId));
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
        return repositories;
	}
	
	private Set<RepositoryWrapper> getAvailableRepositories() {
		Set<RepositoryWrapper> repositories = new TreeSet<RepositoryWrapper>();
        
		Repository repository = getDefaultRepository();
		repository.setId("jboss-public-repository"); //$NON-NLS-1$
		repository.setName("JBoss Public"); //$NON-NLS-1$
		repository.setUrl("https://repository.jboss.org/nexus/content/groups/public-jboss/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repository, JBOSSTOOLS_MAVEN_PROFILE_ID));
		
		repository = getDefaultRepository();
        repository.setId("java-net-public"); //$NON-NLS-1$
		repository.setName("Java Net Public"); //$NON-NLS-1$
		repository.setUrl("https://maven.java.net/content/groups/public/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repository, JBOSSTOOLS_MAVEN_PROFILE_ID));
		
		repository = getDefaultRepository();
        repository.setId("com.springsource.repository.bundles.release"); //$NON-NLS-1$
		repository.setName("EBR Spring Release"); //$NON-NLS-1$
		repository.setUrl("http://repository.springsource.com/maven/bundles/release/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repository, JBOSSTOOLS_MAVEN_PROFILE_ID));
		
		repository = getDefaultRepository();
        repository.setId("com.springsource.repository.bundles.external"); //$NON-NLS-1$
		repository.setName("EBR External Release"); //$NON-NLS-1$
		repository.setUrl("http://repository.springsource.com/maven/bundles/external/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repository, JBOSSTOOLS_MAVEN_PROFILE_ID));

		repository = getDefaultRepository();
        repository.setId("repository.apache.org"); //$NON-NLS-1$
		repository.setName("Apache Repository"); //$NON-NLS-1$
		repository.setUrl("https://repository.apache.org/content/groups/public/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repository, JBOSSTOOLS_MAVEN_PROFILE_ID));
		
		return repositories;
	}
	
	private Repository getDefaultRepository() {
		Repository repository = new Repository();
		repository.setLayout(LAYOUT_DEFAULT);
		RepositoryPolicy releases = new RepositoryPolicy();
		releases.setEnabled(true);
		releases.setUpdatePolicy(POLICY_NEVER); //$NON-NLS-1$
		repository.setReleases(releases);
		RepositoryPolicy snapshots = new RepositoryPolicy();
		snapshots.setEnabled(false);
		snapshots.setUpdatePolicy(POLICY_NEVER); //$NON-NLS-1$
		repository.setSnapshots(snapshots);
		return repository;
	}
	
	private Image getJBossImage() {
		if (jbossImage == null) {
			ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"icons/jboss.png"); //$NON-NLS-1$
			jbossImage = desc.createImage();
		}
		return jbossImage;
	}

	private List<Profile> getActiveProfiles() throws CoreException {
		Settings settings = maven.getSettings();
		List<String> activeProfilesIds = settings.getActiveProfiles();
		List<Profile> activeProfiles = new ArrayList<Profile>();
		for (Profile profile : settings.getProfiles()) {
			if ((profile.getActivation() != null && profile.getActivation().isActiveByDefault())
					|| activeProfilesIds.contains(profile.getId())) {
				activeProfiles.add(profile);
			}
		}
		return activeProfiles;
	}
	
	private Button createButton(Composite buttonsComp,
			int maxAddRemoveButtonsWidth, String text) {
		GridData gd;
		Button button = new Button(buttonsComp, SWT.NONE | SWT.LEFT);
        gd = new GridData();
        gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
        gd.widthHint = maxAddRemoveButtonsWidth;
        button.setLayoutData(gd);
        button.setText(text);
        return button;
	}
	
	private int computeMaxAddRemoveButtonsWidth(GC gc) {
		int maxWidth = 0;

		maxWidth = getGreaterWidth(gc,REMOVE, maxWidth);
		maxWidth = getGreaterWidth(gc,REMOVE_ALL, maxWidth);
		maxWidth = getGreaterWidth(gc,ADD, maxWidth);
		maxWidth = getGreaterWidth(gc,ADD_ALL, maxWidth);
		
		return maxWidth;
	}
	
	private int getGreaterWidth(GC gc, String str, int compareWidth) {
		int greaterWidth = compareWidth;

		Point strExtentPoint = gc.stringExtent(str);
		int strWidth = strExtentPoint.x;
		if (strWidth > compareWidth) {
			greaterWidth = strWidth;
		}

		return greaterWidth + 5;
	}
	
	@Override
	public void dispose() {
		if (jbossImage != null) {
			jbossImage.dispose();
		}
		super.dispose();
	}

	class RepositoryLabelProvider extends CellLabelProvider {
		
		public String getToolTipText(Object element) {
			if (element instanceof RepositoryWrapper) {
				return ((RepositoryWrapper) element).getDisplayName();
			}
			return null;
		}

		public Point getToolTipShift(Object object) {
			return new Point(10, 10);
		}

		public int getToolTipDisplayDelayTime(Object object) {
			return 1000;
		}

		public int getToolTipTimeDisplayed(Object object) {
			return 5000;
		}

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if (element instanceof RepositoryWrapper) {
				return ((RepositoryWrapper) element).getDisplayName();
			}
			return null;
		}

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (element instanceof RepositoryWrapper) {
				cell.setText(((RepositoryWrapper) element).getDisplayName());
			}
		}
	}

	class StringPreviewerInput implements ITypedElement, IEncodedStreamContentAccessor {
		String fContent;
		
		StringPreviewerInput(String content) {
			if (content == null) {
				content="<null>"; //$NON-NLS-1$
			}
			fContent= content;
		}
		
		public Image getImage() {
			return null;
		}
		public String getName() {
			return "no name";	//$NON-NLS-1$
		}
		public String getType() {
			return "xml";	//$NON-NLS-1$
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(getBytes(fContent, UTF_8));
		}
		public String getCharset() {
			return UTF_8;
		}
	}
	
	class PreviewerInput implements ITypedElement, IEncodedStreamContentAccessor {
		
		PreviewerInput() {
			
		}
		
		public Image getImage() {
			return null;
		}
		public String getName() {
			return "no name";	//$NON-NLS-1$
		}
		public String getType() {
			return "xml";	//$NON-NLS-1$
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(getBytes(getNewSettings(), UTF_8));
		}
		public String getCharset() {
			return UTF_8;
		}
	}
	public static byte[] getBytes(String s, String encoding) {
		byte[] bytes= null;
		if (s != null) {
			try {
				bytes= s.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				bytes= s.getBytes();
			}
		}
		return bytes;
	}
	
	public void finishPage() {
		String userSettings = getUserSettings();
		File file = new File(userSettings);
		String outputString = getNewSettings();
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(file);
			byte[] bytes = outputString.getBytes(UTF_8);
			out.write(bytes);
			out.flush();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), ERROR_TITLE, e.getMessage());
			Activator.log(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

}
