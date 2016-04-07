/*************************************************************************************
 * Copyright (c) 2010-2014 Red Hat, Inc. and others.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenConfiguration;
import org.eclipse.m2e.core.internal.index.IndexManager;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.ui.Activator;
import org.jboss.tools.maven.ui.internal.repositories.SettingsRepositoryBuilder;
import org.jboss.tools.maven.ui.preferences.AutoResizeTableLayout;
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
@SuppressWarnings("nls")
public class ConfigureMavenRepositoriesWizardPage extends WizardPage implements IPageChangedListener {

	private static final String ACTIVE_PROFILE = "activeProfile"; //$NON-NLS-1$

	private static final String ACTIVE_PROFILES = "activeProfiles"; //$NON-NLS-1$

	private static final String REPOSITORY_APACHE_ORG_ID = "repository-apache-org"; //$NON-NLS-1$
 
	private static final String COM_SPRINGSOURCE_REPOSITORY_BUNDLES_EXTERNAL_ID = "com-springsource-repository-bundles-external"; //$NON-NLS-1$

	private static final String COM_SPRINGSOURCE_REPOSITORY_BUNDLES_RELEASE_ID = "com-springsource-repository-bundles-release"; //$NON-NLS-1$

	private static final String BINTRAY_ID = "bintray"; //$NON-NLS-1$

	private static final String JAVA_NET_PUBLIC_ID = "java-net-public"; //$NON-NLS-1$

	private static final String JBOSS_PUBLIC_REPOSITORY_ID = "jboss-public-repository"; //$NON-NLS-1$

	private static final String RED_HAT_TECHPREVIEW_ALL_REPOSITORY_ID = "redhat-techpreview-all-repository";//$NON-NLS-1$
	
	private static final String RED_HAT_GA_REPOSITORY_ID = "redhat-ga-repository";//$NON-NLS-1$

	private static final String RED_HAT_GA_REPOSITORY_URL = "http://maven.repository.redhat.com/ga/";//$NON-NLS-1$
	
	private static final String ERROR_TITLE = "Error";

	private static final String SNAPSHOTS_ELEMENT = "snapshots"; //$NON-NLS-1$

	private static final String UPDATE_POLICY_ELEMENT = "updatePolicy"; //$NON-NLS-1$

	private static final String ENABLED_ELEMENT = "enabled"; //$NON-NLS-1$

	private static final String RELEASES_ELEMENT = "releases"; //$NON-NLS-1$

	private static final String LAYOUT_ELEMENT = "layout"; //$NON-NLS-1$

	private static final String NAME_ELEMENT = "name"; //$NON-NLS-1$

	private static final String URL_ELEMENT = "url"; //$NON-NLS-1$

	private static final String REPOSITORY_ELEMENT = "repository"; //$NON-NLS-1$

	private static final String PLUGIN_REPOSITORIES_ELEMENT = "pluginRepositories"; //$NON-NLS-1$

	private static final String PLUGIN_REPOSITORY_ELEMENT = "pluginRepository"; //$NON-NLS-1$

	private static final String REPOSITORIES_ELEMENT = "repositories"; //$NON-NLS-1$

	private static final String ID_ELEMENT = "id"; //$NON-NLS-1$

	private static final String PROFILE_ELEMENT = "profile"; //$NON-NLS-1$

	private static final String PROFILES_ELEMENT = "profiles"; //$NON-NLS-1$

	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	
	private static final String PAGE_NAME = "org.jboss.tools.maven.ui.wizard.page"; //$NON-NLS-1$
	private static final String ADD_REPOSITORY = " Add Repository...";
	private static final String EDIT_REPOSITORY = " Edit Repository...";
	private static final String REMOVE_ALL = " Remove All ";
	private static final String REMOVE = " Remove ";

	private Button removeButton;
	private Button removeAllButton;
	private Button addRepositoryButton;
	private Button editRepositoryButton;
	private IMaven maven;
	private TableViewer includedRepositoriesViewer;
	private Set<RepositoryWrapper> includedRepositories;
	private Set<RepositoryWrapper> availableRepositories;
	private Set<RepositoryWrapper> selectedIncludedRepositories = new HashSet<>();

	private Document document;

	private CompareConfiguration compareConfiguration;

	private TextMergeViewer previewViewer;

	private String newSettings;

	private String oldSettings;

	private ArtifactKey artifactKey;

	private String preSelectedProfileId;

	private Map<String, String> preconfiguredRepositoryUrls;

	public ConfigureMavenRepositoriesWizardPage(ArtifactKey artifactKey) {
		this(artifactKey, null);
	}

	public ConfigureMavenRepositoriesWizardPage(ArtifactKey artifactKey, String profileId) {
		super(PAGE_NAME);
		setTitle("Configure Maven Repositories");
		maven = MavenPlugin.getMaven();
		this.artifactKey = artifactKey;
		if (RED_HAT_TECHPREVIEW_ALL_REPOSITORY_ID.equals(profileId)) {
			profileId = RED_HAT_GA_REPOSITORY_ID;
		}
		this.preSelectedProfileId = profileId;
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
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
		
	    Group repositoriesGroup = new Group(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        GridLayout layout = new GridLayout(3, false);
        repositoriesGroup.setLayout(layout);
        repositoriesGroup.setLayoutData(gd);
        repositoriesGroup.setText("Repositories"); //$NON-NLS-1$

        final ScrolledComposite sc = new ScrolledComposite(repositoriesGroup, SWT.H_SCROLL
                | SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true,false);
        sc.setLayoutData(gd);
        sc.setLayout(new GridLayout());

        final Composite includedRepositoriesComposite = new Composite(sc, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        includedRepositoriesComposite.setLayoutData(gd);
        includedRepositoriesComposite.setLayout(new GridLayout(1, false));
        
        sc.setContent(includedRepositoriesComposite);
        sc.setAlwaysShowScrollBars(false);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        includedRepositoriesComposite.addControlListener(new ControlListener() {

                @Override
                public void controlResized(ControlEvent e) {
                        sc.setMinSize(includedRepositoriesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                }

                @Override
                public void controlMoved(ControlEvent e) {
                }
        });

	    includedRepositoriesViewer = new TableViewer(includedRepositoriesComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL|SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.widthHint = 500;
        gd.heightHint = 150;
        includedRepositoriesViewer.getTable().setLayoutData(gd);
        includedRepositoriesViewer.getTable().setLinesVisible(false);
        includedRepositoriesViewer.getTable().setHeaderVisible(false);
        TableViewerColumn c = new TableViewerColumn(includedRepositoriesViewer, SWT.NONE);
        c.getColumn().setText("Repository"); //$NON-NLS-1$
        c.getColumn().setResizable(true);
        TableLayout includedLayout = new AutoResizeTableLayout(includedRepositoriesViewer.getTable());
        ColumnLayoutData columnLayoutData = new ColumnWeightData(350,350);
        includedLayout.addColumnData(columnLayoutData);
        
        includedRepositoriesViewer.setContentProvider(new ArrayContentProvider());
        includedRepositoriesViewer.setLabelProvider(new RepositoryLabelProvider());
        ColumnViewerToolTipSupport.enableFor(includedRepositoriesViewer, ToolTip.NO_RECREATE);
        
        createButtons(repositoriesGroup);
        
		includedRepositories = getIncludedRepositories();
		availableRepositories = getAvailableRepositories();
		List<RepositoryWrapper> remove = new ArrayList<>();
		for (RepositoryWrapper availableRepository:availableRepositories) {
			if (includedRepositories.contains(availableRepository)) {
				remove.add(availableRepository);
			}
		}
		for (RepositoryWrapper repository:remove) {
			availableRepositories.remove(repository);
		}
		
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
				boolean ok = getMessageDialog(selectedIncludedRepositories);
				if (ok) {
					for (RepositoryWrapper wrapper : selectedIncludedRepositories) {
						includedRepositories.remove(wrapper);
						availableRepositories.add(wrapper);
						removeRepository(wrapper);
					}
					setPageComplete(true);
					refreshRepositories();
				}
			}
		
        });
		removeAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean ok = getMessageDialog(includedRepositories);
				if (!ok) {
					return;
				}
				List<RepositoryWrapper> list = new ArrayList<>();
				for (RepositoryWrapper wrapper : includedRepositories) {
					list.add(wrapper);
					removeRepository(wrapper);
				}
				includedRepositories.removeAll(list);
				availableRepositories.addAll(list);
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		addRepositoryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				openAddRepositoryDialog();
			}
		
        });
		
		editRepositoryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				openEditRepositoryDialog();
			}
		
        });

		
		Label separator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setVisible(false);
		
		Label previewLabel= new Label(composite, SWT.NULL);
		previewLabel.setText("Preview:"); //$NON-NLS-1$
		
		createPreviewer(composite);
		
		RepositoryWrapper[] newRepos = createPreselectedRepositories();
		
		setPageComplete(newRepos.length > 0);

		refreshRepositories();
		
		includedRepositoriesViewer.setSelection(new StructuredSelection(newRepos));
	}

	private RepositoryWrapper[] createPreselectedRepositories() {
		if (preconfiguredRepositoryUrls == null || preconfiguredRepositoryUrls.isEmpty()) {
			return new RepositoryWrapper[0];
		}
		Set<RepositoryWrapper> allRepos = new HashSet<>();
		allRepos.addAll(availableRepositories);
		allRepos.addAll(includedRepositories);
		
		List<RepositoryWrapper> newRepos = new ArrayList<>();
		
		for (Map.Entry<String, String> entry : preconfiguredRepositoryUrls.entrySet()) {
			String repoId = entry.getKey();
			String preConfUrl = entry.getValue();
			if (RED_HAT_TECHPREVIEW_ALL_REPOSITORY_ID.equals(repoId)) {
				repoId = RED_HAT_GA_REPOSITORY_ID;
				preConfUrl = RED_HAT_GA_REPOSITORY_URL;
			}
			
			RepositoryWrapper preconfiguredRepository = null;
			for (RepositoryWrapper repo : allRepos) {
				if ((preConfUrl == null && repo.getProfileId().equals(repoId))
				    || repo.getRepository().getUrl().equals(preConfUrl)) {
					preconfiguredRepository = repo;
					break;
				}
			}
			if(preconfiguredRepository == null) {
				if (preConfUrl == null) {
					continue;
				}
				
				SettingsRepositoryBuilder r = new SettingsRepositoryBuilder();
				String name = StringUtils.capitaliseAllWords(repoId.replace("redhat", "Red Hat").replace("-", " "));
				if (!name.endsWith(" Repository")) {
					name += " Repository";
				}
				r.setName(getUniqueName(name, allRepos));
				r.setUrl(preConfUrl);
				String id = getUniqueProfileId(repoId, allRepos);
				r.setId(id);
				preconfiguredRepository = new RepositoryWrapper(r.get());
			}
			includedRepositories.add(preconfiguredRepository);
			removeRepository(preconfiguredRepository);
			availableRepositories.remove(preconfiguredRepository);
			addRepository(preconfiguredRepository, true);
			newRepos.add(preconfiguredRepository);
		}
		RepositoryWrapper[] result = new RepositoryWrapper[newRepos.size()];
		newRepos.toArray(result);
		return result;
	}

	private String getUniqueName(String name, Collection<RepositoryWrapper> repos) {
		return getUniqueName(name, 1, repos);
	}

	private String getUniqueName(String name, int iteration, Collection<RepositoryWrapper> repos) {
		String candidateName = (iteration > 1)? name + " (" + iteration + ")" : name;
		for (RepositoryWrapper rw : repos) {
			if (candidateName.equals(rw.getRepository().getName())) {
				return getUniqueName(candidateName, ++iteration, repos);
			}
		}
		return candidateName;
	}


	private String getUniqueProfileId(String profileId, Collection<RepositoryWrapper> repos) {
		return getUniqueProfileId(profileId, 1, repos);
	}

	private String getUniqueProfileId(String profileId, int iteration, Collection<RepositoryWrapper> repos) {
		String candidateId = (iteration > 1)? profileId + "-" + iteration : profileId;
		for (RepositoryWrapper rw : repos) {
			if (candidateId.equals(rw.getProfileId()) || candidateId.equals(rw.getRepository().getId())) {
				return getUniqueProfileId(candidateId, ++iteration, repos);
			}
		}
		return candidateId;
	}

	
	protected void createButtons(Composite parent) {
		GridData gd;
		Composite buttonsComposite = new Composite(parent, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.TOP, false, false);
        buttonsComposite.setLayoutData(gd);
        buttonsComposite.setLayout(new GridLayout(1, false));
        
        removeButton = createButton(buttonsComposite, REMOVE);
        removeAllButton = createButton(buttonsComposite, REMOVE_ALL);
        addRepositoryButton = createButton(buttonsComposite, ADD_REPOSITORY);
        editRepositoryButton = createButton(buttonsComposite, EDIT_REPOSITORY);
	}

	private void createDefaultSettings() throws CoreException,
			UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		maven.writeSettings(new Settings(), out);
		newSettings = new String(out.toByteArray(), UTF_8);
	}

	private void addRepository(RepositoryWrapper wrapper, boolean activeByDefault) {
		if (wrapper == null || wrapper.getProfileId() == null || wrapper.getRepository() == null) {
			return;
		}
		String profileId = wrapper.getProfileId();
		Element profile = getProfile(profileId);
		Element repositoriesElement = getElement(profile, REPOSITORIES_ELEMENT);
		if (repositoriesElement != null) {
			addRepository(wrapper, repositoriesElement, false);
		}
		Element pluginRepositoriesElement = getElement(profile, PLUGIN_REPOSITORIES_ELEMENT);
		if (pluginRepositoriesElement != null) {
			addRepository(wrapper, pluginRepositoriesElement, true);
		}
		
		configureActiveByDefault(activeByDefault, profileId);
	}

	private void configureActiveByDefault(boolean activeByDefault,
			String profileId) {
		if (activeByDefault) {

			NodeList activeProfilesList = document
					.getElementsByTagName(ACTIVE_PROFILES);
			Element activeProfiles = null;
			if (activeProfilesList.getLength() > 0) {
				activeProfiles = (Element) activeProfilesList.item(0);
			}
			if (activeProfiles == null) {
				activeProfiles = document.createElement(ACTIVE_PROFILES);
				document.getDocumentElement().appendChild(activeProfiles);
			}
			NodeList activeProfileList = activeProfiles.getChildNodes();
			boolean activated = false;
			for (int i = 0; i < activeProfileList.getLength(); i++) {
				Node node = activeProfileList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& ACTIVE_PROFILE.equals(node.getNodeName())) {
					String id = node.getTextContent();
					if (id != null) {
						id = id.trim();
					}
					if (profileId.equals(id)) {
						activated = true;
						break;
					}
				}
			}
			if (!activated) {
				addNewElement(activeProfiles, ACTIVE_PROFILE, profileId);
			}
		}
	}
	
	protected Element addNewElement(Element element, String name, String value) {
		Element child = document.createElement(name);
		if (value != null) {
			Text textNode = document.createTextNode(value);
			child.appendChild(textNode);
		}
		element.appendChild(child);
		return child;
	}

	private Element getElement(Element element, String name) {
		NodeList elements = element.getChildNodes();
		int len = elements.getLength();
		for (int i = 0; i < len; i++) {
			Node node = elements.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())) {
				return (Element) node;
			}
		}
		return null;
	}

	private Element getProfile(String profileId) {
		NodeList profilesList = document.getElementsByTagName(PROFILES_ELEMENT);
		Node profiles;
		Element profileElement = null;
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
							if (profileId.equals(id)) {
								profileElement = (Element) profile;
								break;
							}
						}
					}
				}
				if (profileElement != null) {
					break;
				}
			}
		} else {
			profiles = document.createElement(PROFILES_ELEMENT);
			document.getDocumentElement().appendChild(profiles);
		}

		if (profileElement == null) {
			profileElement = createProfile(profiles, profileId);
		}
		configureProfile(profileElement);
		return profileElement;

	}

	private Element createProfile(Node profiles, String profileId) {
		Element profile = document.createElement(PROFILE_ELEMENT); 
		profiles.appendChild(profile);
		Element id = document.createElement(ID_ELEMENT);
		id.setTextContent(profileId);
		profile.appendChild(id);
		return profile;
	}

	private void configureProfile(Element profileElement) {
		NodeList nodeList = profileElement.getChildNodes();
		int len = nodeList.getLength();
		Element repositoriesElement = null;
		Element pluginRepositoriesElement = null;
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
			profileElement.appendChild(repositoriesElement);
		}
		if (pluginRepositoriesElement == null) {
			pluginRepositoriesElement = document.createElement(PLUGIN_REPOSITORIES_ELEMENT);
			profileElement.appendChild(pluginRepositoriesElement);
		}
	}

	private void addRepository(RepositoryWrapper wrapper, Element repos, boolean isPluginRepository) {
		Element repository;
		if (isPluginRepository) {
			repository = document.createElement(PLUGIN_REPOSITORY_ELEMENT);
		} else {
			repository = document.createElement(REPOSITORY_ELEMENT);
		}
		repos.appendChild(repository);
		populateRepository(repository, wrapper);
	}

	private void populateRepository(Element repository,
			RepositoryWrapper wrapper) {
		addElement(repository, ID_ELEMENT, wrapper.getRepository().getId());
		String name = wrapper.getRepository().getName();
		if(name != null){
			addElement(repository, NAME_ELEMENT, wrapper.getRepository().getName());
		}
		addElement(repository, URL_ELEMENT, wrapper.getRepository().getUrl());
		String layout = wrapper.getRepository().getLayout();
		if(layout != null){
			addElement(repository, LAYOUT_ELEMENT, wrapper.getRepository().getLayout());
		}
		RepositoryPolicy policy = wrapper.getRepository().getReleases();
		if (policy != null) {
			Element releases = addElement(repository, RELEASES_ELEMENT, null);
			addElement(releases, ENABLED_ELEMENT, policy.isEnabled() ? "true" : "false");  //$NON-NLS-1$//$NON-NLS-2$
			addElement(releases, UPDATE_POLICY_ELEMENT, policy.getUpdatePolicy());
		}
		policy = wrapper.getRepository().getSnapshots();
		if (policy != null) {
			Element snapshots = addElement(repository, SNAPSHOTS_ELEMENT, null);
			addElement(snapshots, ENABLED_ELEMENT, policy.isEnabled() ? "true" : "false");  //$NON-NLS-1$//$NON-NLS-2$
			addElement(snapshots, UPDATE_POLICY_ELEMENT,  policy.getUpdatePolicy());
		}
	}

	protected Element addElement(Element element, String name, String value) {
		Element child = getElementsByTagName(element, name);
		boolean isNew = false;
		if (child == null) {
			child = document.createElement(name);
			isNew = true;
		}
		if (value != null) {
			Text textNode = document.createTextNode(value);
			if (!isNew) {
				Node oldTextNode = child.getFirstChild();
				if (oldTextNode != null) {
					String oldValue = oldTextNode.getNodeValue();
					if (!value.equals(oldValue)) {
						child.replaceChild(textNode, oldTextNode);
					}
				} else {
					child.appendChild(textNode);
				}
			} else {
				child.appendChild(textNode);
			}
		}
		if (isNew) {
			element.appendChild(child);
		}
		return child;
	}

	private Element getElementsByTagName(Element element, String name) {
		NodeList children = element.getElementsByTagName(name);
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) node;
			}
		}
		return null;
	}

	protected void removeRepository(RepositoryWrapper wrapper) {
		if (wrapper == null || wrapper.getProfileId() == null || wrapper.getRepository() == null || wrapper.getRepository().getUrl() == null) {
			return;
		}
		String url = wrapper.getRepository().getUrl();
		String profileId = wrapper.getProfileId();
		Element profile = getProfile(profileId);
		if (profile == null) {
			return;
		}
		Element repositoriesElement = getElement(profile, REPOSITORIES_ELEMENT);
		if (repositoriesElement != null) {
			removeRepository(url, repositoriesElement, false);
		}
		Element pluginRepositoriesElement = getElement(profile, PLUGIN_REPOSITORIES_ELEMENT);
		if (pluginRepositoriesElement != null) {
			removeRepository(url, pluginRepositoriesElement, true);
		}
		
		// remove profile ?
		if (repositoriesElement != null) {
			NodeList nodeList = repositoriesElement.getChildNodes();
			int len = nodeList.getLength();
			for (int i = 0; i < len; i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& REPOSITORY_ELEMENT.equals(node.getNodeName())) {
					return;
				}
			}
		}
		if (pluginRepositoriesElement != null) {
			NodeList nodeList = pluginRepositoriesElement.getChildNodes();
			int len = nodeList.getLength();
			for (int i = 0; i < len; i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& PLUGIN_REPOSITORY_ELEMENT.equals(node.getNodeName())) {
					return;
				}
			}
		}
		removeActiveProfileId(profileId, profile);
	}

	private void removeActiveProfileId(String profileId, Element profile) {
		NodeList profilesList = document.getElementsByTagName(PROFILES_ELEMENT);
		Element profiles = (Element) profilesList.item(0);
		profiles.removeChild(profile);
		
		NodeList activeProfilesList = document
				.getElementsByTagName(ACTIVE_PROFILES);
		Element activeProfiles = null;
		if (activeProfilesList.getLength() > 0) {
			activeProfiles = (Element) activeProfilesList.item(0);
		}
		if (activeProfiles != null) {
			NodeList activeProfileList = activeProfiles.getChildNodes();
			Node profileNode = null;
			for (int i = 0; i < activeProfileList.getLength(); i++) {
				Node node = activeProfileList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& ACTIVE_PROFILE.equals(node.getNodeName())) {
					String id = node.getTextContent();
					if (id != null) {
						id = id.trim();
					}
					if (profileId.equals(id)) {
						profileNode = node;
						break;
					}
				}
			}
			if (profileNode != null) {
				activeProfiles.removeChild(profileNode);
			}
		}
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
		if (url != null && !url.endsWith(RepositoryWrapper.SEPARATOR)) {
			url = url + RepositoryWrapper.SEPARATOR;
		}
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

	private void createPreviewer(Composite composite) {
		compareConfiguration= new CompareConfiguration();
		compareConfiguration.setAncestorLabel("Preview:"); //$NON-NLS-1$
		
		compareConfiguration.setLeftLabel("Old settings"); //$NON-NLS-1$
		compareConfiguration.setLeftEditable(false);
		
		compareConfiguration.setRightLabel("New settings"); //$NON-NLS-1$
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


	private String readFile(File file) throws FileNotFoundException {
	    StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator"); //$NON-NLS-1$
	    Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"); //$NON-NLS-1$
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine()).append(NL);
	      }
	    }
	    finally{
	      scanner.close();
	    }
	    return text.toString();
	  }
	
	public static String getUserSettings() {
		String userSettings = MavenPlugin.getMavenConfiguration().getUserSettingsFile();
	    if(userSettings == null || userSettings.length() == 0) {
	    	userSettings = MavenCli.DEFAULT_USER_SETTINGS_FILE.getAbsolutePath();
	    }
		return userSettings;
	}
	
	private void configureButtons() {
		removeAllButton.setEnabled(includedRepositories.size() > 0);
		removeButton.setEnabled(selectedIncludedRepositories.size() > 0);
		editRepositoryButton.setEnabled(selectedIncludedRepositories.size() == 1);
	}

	private void refreshRepositories() {
		includedRepositoriesViewer.setInput(includedRepositories.toArray(new RepositoryWrapper[0]));
		previewViewer.refresh();
        selectedIncludedRepositories.clear();
        includedRepositoriesViewer.setSelection(new StructuredSelection(selectedIncludedRepositories.toArray(new RepositoryWrapper[0])));
		configureButtons();
	}

	private Set<RepositoryWrapper> getIncludedRepositories() {
		Set<RepositoryWrapper> repositories = new TreeSet<>();
        try {
			List<Profile> profiles = getProfiles();
			for (Profile profile:profiles) {
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
		Set<RepositoryWrapper> repositories = new TreeSet<>();
        
		SettingsRepositoryBuilder repoBuilder = new SettingsRepositoryBuilder() 
		 											.setId(JBOSS_PUBLIC_REPOSITORY_ID)
		 											.setName("JBoss Public") //$NON-NLS-1$
		 											.setUrl("https://repository.jboss.org/nexus/content/groups/public-jboss/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repoBuilder.get()));
		
		repoBuilder = new SettingsRepositoryBuilder()
						.setId(RED_HAT_GA_REPOSITORY_ID)
						.setName("Red Hat GA repository") //$NON-NLS-1$
						.setUrl(RED_HAT_GA_REPOSITORY_URL); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repoBuilder.get()));

		repoBuilder = new SettingsRepositoryBuilder()
				.setId(BINTRAY_ID)
				.setName("Bintray JCenter") //$NON-NLS-1$
				.setUrl("https://jcenter.bintray.com"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repoBuilder.get()));

		
		repoBuilder = new SettingsRepositoryBuilder()
        				.setId(JAVA_NET_PUBLIC_ID)
						.setName("Java Net Public") //$NON-NLS-1$
						.setUrl("https://maven.java.net/content/groups/public/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repoBuilder.get()));
		
		repoBuilder = new SettingsRepositoryBuilder()
        			.setId(COM_SPRINGSOURCE_REPOSITORY_BUNDLES_RELEASE_ID)
					.setName("EBR Spring Release") //$NON-NLS-1$
					.setUrl("http://repository.springsource.com/maven/bundles/release/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repoBuilder.get()));
		
		repoBuilder = new SettingsRepositoryBuilder()
        			.setId(COM_SPRINGSOURCE_REPOSITORY_BUNDLES_EXTERNAL_ID)
					.setName("EBR External Release") //$NON-NLS-1$
					.setUrl("http://repository.springsource.com/maven/bundles/external/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repoBuilder.get()));
		
		repoBuilder = new SettingsRepositoryBuilder()
        			.setId(REPOSITORY_APACHE_ORG_ID)
					.setName("Apache Repository") //$NON-NLS-1$
					.setUrl("https://repository.apache.org/content/groups/public/"); //$NON-NLS-1$
		repositories.add(new RepositoryWrapper(repoBuilder.get()));
		
		return repositories;
	}
	

	private List<Profile> getProfiles() throws CoreException {
		return maven.getSettings().getProfiles();
	}
	
	private Button createButton(Composite parent, String text) {
		GridData gd;
		Button button = new Button(parent, SWT.NONE | SWT.LEFT);
        gd = new GridData(GridData.FILL, GridData.FILL, false, false);
        button.setLayoutData(gd);
        button.setText(text);
        return button;
	}

	class RepositoryLabelProvider extends StyledCellLabelProvider {
		
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
				RepositoryWrapper wrapper = (RepositoryWrapper) element;
				StyledString text = new StyledString();
				String displayName;
				if (isActive(wrapper)) {
					displayName = wrapper.getDisplayName();
				} else {
					displayName = wrapper.getDisplayName() + " (Inactive)";
				}
				StyleRange styledRange = new StyleRange(0,
						displayName.length(), null, null);
				text.append(displayName, StyledString.DECORATIONS_STYLER);
				cell.setText(text.toString());
				if (isActive(wrapper)) {
					styledRange.font = JFaceResources.getFontRegistry()
							.get(JFaceResources.DEFAULT_FONT);
				} else {
					styledRange.font = JFaceResources.getFontRegistry()
							.getItalic(JFaceResources.DEFAULT_FONT);
				}
				StyleRange[] range = { styledRange };
				cell.setStyleRanges(range);
			}
			super.update(cell);
		}
	}

	class StringPreviewerInput extends PreviewerInput {
		String fContent;
		
		StringPreviewerInput(String content) {
			if (content == null) {
				content="<null>"; //$NON-NLS-1$
			}
			fContent= content;
		}
		
		public InputStream getContents() {
			return new ByteArrayInputStream(getBytes(fContent, UTF_8));
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
	
	private boolean isActive(RepositoryWrapper wrapper) {
		if (wrapper != null) {
			String profileId = wrapper.getProfileId();
			NodeList activeProfilesList = document
					.getElementsByTagName(ACTIVE_PROFILES);
			Element activeProfiles = null;
			if (activeProfilesList.getLength() > 0) {
				activeProfiles = (Element) activeProfilesList.item(0);
			}
			if (activeProfiles == null) {
				return false;
			}
			NodeList activeProfileList = activeProfiles.getChildNodes();
			for (int i = 0; i < activeProfileList.getLength(); i++) {
				Node node = activeProfileList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& ACTIVE_PROFILE.equals(node.getNodeName())) {
					String id = node.getTextContent();
					if (id != null) {
						id = id.trim();
					}
					if (profileId.equals(id)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean finishPage() {
		String userSettings = getUserSettings();
		File file = new File(userSettings);
		boolean ok = MessageDialog.openQuestion(getShell(), "Confirm File Update", "Are you sure you want to update the file '" + userSettings + "'?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (!ok) {
			return false;
		}
		String outputString = getNewSettings();
		FileOutputStream out = null;
		
		try {
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			
			out = new FileOutputStream(file);
			byte[] bytes = outputString.getBytes(UTF_8);
			out.write(bytes);
			out.flush();
			updateSettings();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), ERROR_TITLE, e.getMessage());
			Activator.log(e);
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return true;
	}

	protected void updateSettings() {
		final String userSettings = getUserSettings();

		Job job = new Job("Updating Maven settings...") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				try {
					MavenPlugin.getMaven().reloadSettings();
					final File localRepositoryDir = new File(maven
							.getLocalRepository().getBasedir());

					IMavenConfiguration mavenConfiguration = MavenPlugin
							.getMavenConfiguration();
					if (userSettings.length() > 0) {
						mavenConfiguration.setUserSettingsFile(userSettings);
					} else {
						mavenConfiguration.setUserSettingsFile(null);
					}
					
					MavenCoreActivator.getDefault().notifyMavenSettingsChanged();

					File newRepositoryDir = new File(maven.getLocalRepository()
							.getBasedir());
					if (!newRepositoryDir.equals(localRepositoryDir)) {
						IndexManager indexManager = MavenPlugin
								.getIndexManager();
						indexManager.getWorkspaceIndex().updateIndex(true, monitor);
					}

					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					if (projects != null && projects.length > 0) {
						MavenUpdateRequest updateRequest = new MavenUpdateRequest(projects, mavenConfiguration.isOffline(),true);
						MavenPlugin.getMavenProjectRegistry().refresh(updateRequest);
					}
					return Status.OK_STATUS;
				} catch (CoreException e) {
					Activator.log(e);
					return e.getStatus();
				}
			}
		};
		job.schedule();
	}
	
	private boolean getMessageDialog(Set<RepositoryWrapper> repos) {
		if (repos.size() == 0) {
			return false;
		}
		StringBuilder builder = new StringBuilder();
		if (repos.size() == 1) {
			builder.append("Are you sure you want to delete the '");
			builder.append(repos.iterator().next().getRepository().getUrl());
			builder.append("' repository?");
		} else {
			builder.append("Are you sure you want to delete the following repositories:\n\n");
			for (RepositoryWrapper wrapper:repos) {
				builder.append(wrapper.getRepository().getUrl());
				builder.append("\n"); //$NON-NLS-1$
			}
			builder.append("\n"); //$NON-NLS-1$
		}
		return MessageDialog.openQuestion(getShell(), "Question?", builder.toString()); 
	}

	@Override
	public void pageChanged(PageChangedEvent event) {
		//When page is selected, if a profileId was preselected, then open the "Add Repository..." dialog 
		if ( this.equals(event.getSelectedPage())) {
			if (preSelectedProfileId == null) {
				openEditRepositoryDialog();
			} else {
				for (RepositoryWrapper repo : availableRepositories) {
					if (preSelectedProfileId.equals(repo.getProfileId())){
						openAddRepositoryDialog();
						break;
					}
				}
				preSelectedProfileId = null;
			}
		}
	}

	private void openEditRepositoryDialog() {
		if (selectedIncludedRepositories.size() != 1) {
			return;
		}
		RepositoryWrapper selectedWrapper = selectedIncludedRepositories.iterator().next();
		if (selectedWrapper == null || selectedWrapper.getProfileId() == null) {
			return;
		}
		RepositoryWrapper editWrapper;
		try {
			editWrapper = selectedWrapper.clone();
		} catch (CloneNotSupportedException e) {
			Activator.log(e);
			editWrapper = selectedWrapper;
		}
		AddRepositoryDialog dialog = new AddRepositoryDialog(getShell(), availableRepositories, 
				includedRepositories, maven, artifactKey, editWrapper, isActive(editWrapper));
		
		dialog.setPreSelectedProfile(selectedWrapper.getProfileId());;
		int ok = dialog.open();
		if (ok == Window.OK) {
			RepositoryWrapper wrapper = dialog.getRepositoryWrapper();
			includedRepositories.remove(selectedWrapper);
			//removeRepository(selectedWrapper);
			//addRepository(wrapper, dialog.isActiveByDefault());
			editRepository(selectedWrapper, wrapper, dialog.isActiveByDefault());
			includedRepositories.add(wrapper);
			setPageComplete(true);
			refreshRepositories();
		}
	}
	
	private void editRepository(RepositoryWrapper selectedWrapper,
			RepositoryWrapper wrapper, boolean activeByDefault) {
		if (wrapper == null || wrapper.getProfileId() == null || wrapper.getRepository() == null) {
			return;
		}
		String profileId = wrapper.getProfileId();
		Element profile = getProfile(profileId);
		Node repository = getRepository(selectedWrapper, REPOSITORIES_ELEMENT);
		if (repository == null) {
			removeRepository(selectedWrapper);
			addRepository(wrapper, activeByDefault);
			return;
		} 
		populateRepository((Element) repository, wrapper);
		Node pluginRepository = getRepository(selectedWrapper, PLUGIN_REPOSITORIES_ELEMENT);
		if (pluginRepository == null) {
			Element repos = getElement(profile, PLUGIN_REPOSITORIES_ELEMENT);
			addRepository(wrapper, repos, true);
		} else {
			populateRepository((Element) pluginRepository, wrapper);
		}
		if (!activeByDefault) {
			removeActiveProfileId(profileId);
		}
		configureActiveByDefault(activeByDefault, profileId);
	}

	private void removeActiveProfileId(String profileId) {
		NodeList activeProfilesList = document
				.getElementsByTagName(ACTIVE_PROFILES);
		Element activeProfiles = null;
		if (activeProfilesList.getLength() > 0) {
			activeProfiles = (Element) activeProfilesList.item(0);
		}
		if (activeProfiles != null) {
			NodeList activeProfileList = activeProfiles.getChildNodes();
			Node profileNode = null;
			for (int i = 0; i < activeProfileList.getLength(); i++) {
				Node node = activeProfileList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& ACTIVE_PROFILE.equals(node.getNodeName())) {
					String id = node.getTextContent();
					if (id != null) {
						id = id.trim();
					}
					if (profileId.equals(id)) {
						profileNode = node;
						break;
					}
				}
			}
			if (profileNode != null) {
				activeProfiles.removeChild(profileNode);
			}
		}
	}

	private Node getRepository(RepositoryWrapper wrapper, String type) {
		if (wrapper == null || wrapper.getProfileId() == null || wrapper.getRepository() == null || wrapper.getRepository().getUrl() == null) {
			return null;
		}
		String url = wrapper.getRepository().getUrl();
		String profileId = wrapper.getProfileId();
		Element profile = getProfile(profileId);
		Element repositoriesElement = getElement(profile, type);
		if (repositoriesElement != null) {
			return getRepository(url, repositoriesElement, PLUGIN_REPOSITORIES_ELEMENT.equals(type));
		}
		return null;
	}

	private Node getRepository(String url, Element repos,
			boolean isPluginRepository) {
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
				if (url != null && !url.endsWith(RepositoryWrapper.SEPARATOR)) {
					url = url + RepositoryWrapper.SEPARATOR;
				}
				if (urlNode != null && urlNode.equals(url)) {
					repository = node;
					break;
				}
			}
		}
		return repository;
	}

	private void openAddRepositoryDialog() {
		AddRepositoryDialog dialog = new AddRepositoryDialog(getShell(), availableRepositories, includedRepositories, maven, artifactKey);
		dialog.setPreSelectedProfile(preSelectedProfileId);
		int ok = dialog.open();
		if (ok == Window.OK) {
			RepositoryWrapper wrapper = dialog.getRepositoryWrapper();
			includedRepositories.add(wrapper);
			availableRepositories.remove(wrapper);
			addRepository(wrapper, dialog.isActiveByDefault());
			setPageComplete(true);
			refreshRepositories();
		}
	}


	/**
	 * @since 1.5.3
	 */
	public void addPreconfiguredRepositories(
			Map<String, String> preconfiguredRepositories) {
				this.preconfiguredRepositoryUrls = preconfiguredRepositories;
	}
	
}

