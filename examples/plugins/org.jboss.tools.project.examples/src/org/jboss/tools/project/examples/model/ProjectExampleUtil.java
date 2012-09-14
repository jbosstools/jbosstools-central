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
package org.jboss.tools.project.examples.model;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.filetransfer.ECFExamplesTransport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author snjeza
 * 
 */
public class ProjectExampleUtil {

	private static final String SERVER_PROJECT_EXAMPLE_XML = ".project_example.xml"; //$NON-NLS-1$

	private static final String URL = "url"; //$NON-NLS-1$

	private static final String NAME = "name"; //$NON-NLS-1$

	private static final String SITES = "sites"; //$NON-NLS-1$

	private static final String SITE = "site"; //$NON-NLS-1$

	public static final String EDITOR = "editor"; //$NON-NLS-1$

	public static final String CHEATSHEETS = "cheatsheets"; //$NON-NLS-1$

	public static final String PROTOCOL_FILE = "file"; //$NON-NLS-1$

	public static final String PROTOCOL_PLATFORM = "platform"; //$NON-NLS-1$

	private static final String PROJECT_EXAMPLES_XML_EXTENSION_ID = "org.jboss.tools.project.examples.projectExamplesXml"; //$NON-NLS-1$

	private static final String PROJECT_EXAMPLES_CATEGORIES_EXTENSION_ID = "org.jboss.tools.project.examples.categories"; //$NON-NLS-1$

	private static String URL_EXT = URL;

	private static String EXPERIMENTAL_EXT = "experimental"; //$NON-NLS-1$

	private static Set<IProjectExampleSite> pluginSites;

	private static HashSet<IProjectExampleSite> invalidSites = new HashSet<IProjectExampleSite>();

	private static HashSet<URL> categoryUrls;

	private ProjectExampleUtil() {
	}

	public static Set<IProjectExampleSite> getPluginSites() {
		if (pluginSites == null) {
			pluginSites = new HashSet<IProjectExampleSite>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(PROJECT_EXAMPLES_XML_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				IProjectExampleSite site = new ProjectExampleSite();
				site.setName(extension.getLabel());
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					if (URL_EXT.equals(configurationElement.getName())) {
						String urlString = configurationElement.getValue();
						URL url = getURL(urlString);
						if (url != null) {
							site.setUrl(url);
						}
					} else if (EXPERIMENTAL_EXT.equals(configurationElement
							.getName())) {
						String experimental = configurationElement.getValue();
						if ("true".equals(experimental)) { //$NON-NLS-1$
							site.setExperimental(true);
						}
					}
				}
				if (site.getUrl() != null) {
					pluginSites.add(site);
				}
			}

		}
		return pluginSites;
	}

	public static Set<IProjectExampleSite> getRuntimeSites() {
		return getRuntimeSites(false);
	}
	
	public static Set<IProjectExampleSite> getRuntimeSites(boolean force) {
		Set<IProjectExampleSite> sites = new HashSet<IProjectExampleSite>();
		if (!force) {
			IPreferenceStore store = ProjectExamplesActivator.getDefault()
					.getPreferenceStore();
			if (!store.getBoolean(ProjectExamplesActivator.SHOW_RUNTIME_SITES)) {
				return sites;
			}
		}
		IServer[] servers = ServerCore.getServers();
		for (IServer server:servers) {
			IRuntime runtime = server.getRuntime();
			if (runtime == null) {
				continue;
			}
			IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			if (jbossRuntime == null) {
				continue;
			}
			IPath jbossLocation = runtime.getLocation();
			if (jbossRuntime.getRuntime() == null) {
				continue;
			}
			String name = jbossRuntime.getRuntime().getName() + " Project Examples";
			File serverHome = jbossLocation.toFile();
			File file = getFile(serverHome, true);
			if (file != null) {
				ProjectExampleSite site = new ProjectExampleSite();
				site.setExperimental(false);
				site.setName(name);
				try {
					site.setUrl(file.toURI().toURL());
					sites.add(site);
				} catch (MalformedURLException e) {
					ProjectExamplesActivator.log(e.getMessage());
				}
			}
		}
		return sites;
	}
	private static File getFile(File serverHome, boolean b) {
		if (!serverHome.isDirectory()) {
			return null;
		}
		File[] directories = serverHome.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		if (directories != null && directories.length > 0) {
			for (File directory:directories) {
				File projectExampleFile = new File(directory, SERVER_PROJECT_EXAMPLE_XML);
				if (projectExampleFile.isFile()) {
					return projectExampleFile;
				}
			}
		}
		return null;
	}

	public static Set<IProjectExampleSite> getUserSites() {
		Set<IProjectExampleSite> sites = new HashSet<IProjectExampleSite>();
		ProjectExampleSite site = getSite(getProjectExamplesXml());
		if (site != null) {
			sites.add(site);
		}
		IPreferenceStore store = ProjectExamplesActivator.getDefault()
				.getPreferenceStore();
		String sitesAsXml = store
				.getString(ProjectExamplesActivator.USER_SITES);
		if (sitesAsXml != null && sitesAsXml.trim().length() > 0) {
			Element rootElement = parseDocument(sitesAsXml);
			if (!rootElement.getNodeName().equals(SITES)) {
				ProjectExamplesActivator
						.log(Messages.ProjectUtil_Invalid_preferences);
				return sites;
			}
			NodeList list = rootElement.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element entry = (Element) node;
					if (entry.getNodeName().equals(SITE)) {
						String name = entry.getAttribute(NAME);
						String urlString = entry.getAttribute(URL);
						if (name != null && name.trim().length() > 0
								&& urlString != null
								&& urlString.trim().length() > 0) {
							URL url = null;
							try {
								url = new URL(urlString);
							} catch (MalformedURLException e) {
								ProjectExamplesActivator
										.log(Messages.ProjectUtil_Invalid_preferences);
								continue;
							}
							site = new ProjectExampleSite();
							site.setName(name);
							site.setUrl(url);
							site.setExperimental(true);
							site.setEditable(true);
							sites.add(site);
						}
					}
				}
			}
		}
		return sites;
	}

	private static Set<IProjectExampleSite> getSites() {
		Set<IProjectExampleSite> sites = new HashSet<IProjectExampleSite>();
		sites.addAll(getPluginSites());
		sites.addAll(getUserSites());
		sites.addAll(getRuntimeSites());
		return sites;
	}

	private static ProjectExampleSite getSite(String url) {
		if (url != null) {
			ProjectExampleSite site = new ProjectExampleSite();
			try {
				site.setUrl(new URL(url));
			} catch (MalformedURLException e) {
				ProjectExamplesActivator.log(e);
				return null;
			}
			site.setExperimental(true);
			site.setName(Messages.ProjectUtil_Test);
			return site;
		}
		return null;
	}

	private static URL getURL(String urlString) {
		if (urlString != null && urlString.trim().length() > 0) {
			urlString = urlString.trim();
			try {
				URL url = new URL(urlString);
				return url;
			} catch (MalformedURLException e) {
				ProjectExamplesActivator.log(e);
			}
		}
		return null;
	}

	public static List<ProjectExampleCategory> getProjects(
			IProgressMonitor monitor) {
		return getProjects(getSites(), monitor);
	}

	public static List<ProjectExampleCategory> getProjects(
			Set<IProjectExampleSite> sites, IProgressMonitor monitor) {
		monitor.setTaskName(Messages.ProjectUtil_Parsing_project_description_files);
		List<ProjectExampleCategory> list = new ArrayList<ProjectExampleCategory>();
		invalidSites.clear();
		ProjectExampleCategory other = ProjectExampleCategory.OTHER;
		int threads = Runtime.getRuntime().availableProcessors();
		//threads = 1;
		ExecutorService service = Executors.newFixedThreadPool(threads);
		CompletionService<Tuple<IProjectExampleSite, Document>> pool = new ExecutorCompletionService<Tuple<IProjectExampleSite, Document>>(service);
		try {
			boolean showExperimentalSites = ProjectExamplesActivator
					.getDefault()
					.getPreferenceStore()
					.getBoolean(
							ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES);
			
			
		    int count = 0;
			for (IProjectExampleSite site : sites) {
				if (!showExperimentalSites && site.isExperimental()) {
					continue;
				}
				if (monitor.isCanceled()) {
					invalidSites.add(site);
					continue;
				}
				
				pool.submit(new FetchProjectExampleDocumentTask(site));
				count++;
			}
			
			for (int k=0; k <count; k++) {
				//Handle the next finished task first
				Tuple<IProjectExampleSite, Document> tuple = pool.take().get();
				IProjectExampleSite site = tuple.key;
				Document doc = tuple.value;
				if (doc == null) {
					invalidSites.add(site);
					continue;
				}
				NodeList projects = doc.getElementsByTagName("project"); //$NON-NLS-1$
				int len = projects.getLength();
				for (int i = 0; i < len; i++) {
					Node node = projects.item(i);
					ProjectExample project = new ProjectExample();
					project.setSite(site);
					ProjectExampleCategory category = other;
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						NodeList children = element.getChildNodes();
						int cLen = children.getLength();
						for (int j = 0; j < cLen; j++) {
							Node cNode = children.item(j);
							if (cNode.getNodeType() == Node.ELEMENT_NODE) {
								Element child = (Element) cNode;
								String nodeName = child.getNodeName();
								if (nodeName.equals("fixes")) { //$NON-NLS-1$
									parseFixes(project, child);
								}

								else if (nodeName.equals("category")) { //$NON-NLS-1$
									String value = getContent(child);
									boolean found = false;
									for (ProjectExampleCategory cat : list) {
										if (cat.getSite() == null) {
											cat.setSite(site);
										}
										if (cat.getName().equals(value)
												&& site.getName()
														.equals(cat.getSite()
																.getName())) {
											category = cat;
											found = true;
											break;
										}
									}
									if (!found) {
										category = new ProjectExampleCategory(
												value);
										category.setSite(site);
										list.add(category);
									}
									project.setCategory(category);
									category.getProjects().add(project);
								} else if (nodeName.equals(NAME)) {
									project.setName(getContent(child));
								} else if (nodeName.equals("priority")) { //$NON-NLS-1$
									String value = getContent(child);
									if (value != null) {
										try {
											project.setPriority(new Integer(
													value).intValue());
										} catch (Exception e) {
											ProjectExamplesActivator.log(e);
										}
									}
								} else if (nodeName.equals("shortDescription")) { //$NON-NLS-1$
									project.setShortDescription(getContent(child));
								} else if (nodeName.equals("description")) { //$NON-NLS-1$
									project.setDescription(getContent(child));
								} else if (nodeName.equals(URL)) {
									project.setUrl(getContent(child));
								} else if (nodeName.equals("source-location")) { //$NON-NLS-1$
									project.setSourceLocation(getContent(child));
								} else if (nodeName.equals("perspectiveId")) { //$NON-NLS-1$
									project.setPerspectiveId(getContent(child));
								} else if (nodeName.equals("importType")) { //$NON-NLS-1$
									project.setImportType(getContent(child));
								} else if (nodeName
										.equals("importTypeDescription")) { //$NON-NLS-1$
									project.setImportTypeDescription(getContent(child));
								} else if (nodeName.equals("size")) { //$NON-NLS-1$
									long size = 0;
									try {
										size = new Long(getContent(child));
									} catch (Exception ignored) {
									}
									project.setSize(size);
								} else if (nodeName.equals("included-projects")) { //$NON-NLS-1$
									String includedProjects = getContent(child);
									if (includedProjects != null) {
										includedProjects = includedProjects
												.trim();
										StringTokenizer tokenizer = new StringTokenizer(
												includedProjects, ","); //$NON-NLS-1$
										List<String> projectList = new ArrayList<String>();
										while (tokenizer.hasMoreTokens()) {
											projectList.add(tokenizer
													.nextToken().trim());
										}
										project.setIncludedProjects(projectList);
									}
								} else if (nodeName
										.equals("defaultMavenProfiles")) { //$NON-NLS-1$
									project.setDefaultProfiles(getContent(child));
								} else if (nodeName.equals("welcome")) { //$NON-NLS-1$
									project.setWelcome(true);
									String attribute = child
											.getAttribute("type"); //$NON-NLS-1$
									if (attribute != null
											&& CHEATSHEETS.equals(attribute
													.trim())) {
										project.setType(attribute.trim());
									} else {
										project.setType(EDITOR);
									}
									attribute = child.getAttribute(URL);
									if (attribute == null
											|| attribute.trim().length() <= 0) {
										project.setWelcome(false);
										ProjectExamplesActivator
												.log(Messages.ProjectUtil_Invalid_welcome_element);
									} else {
										project.setWelcomeURL(attribute.trim());
										project.setWelcomeFixRequired(false);
									}
								} else if (nodeName.equals("mavenArchetype")) { //$NON-NLS-1$
									parseMavenArchetype(project, child);
								} else if (nodeName.equals("tags")) { //$NON-NLS-1$
									parseTags(project, child);
								} else if (nodeName.equals("icon")) { //$NON-NLS-1$
									String path = child.getAttribute("path"); //$NON-NLS-1$
									if (path != null) {
										project.setIconPath(path);
									}
								} else if (nodeName.equals("essentialEnterpriseDependencies")) { //$NON-NLS-1$
									parseEssentialEnterpriseDependencies(project, child);
								} else if (nodeName.equals("stacksId")) { //$NON-NLS-1$
									String stacksId = child.getAttribute("stacksId"); //$NON-NLS-1$
									if (stacksId != null) {
										project.setStacksId(stacksId);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		} finally {
			service.shutdown();
		}
		list.add(other);
		handleCategories(list, monitor);
		return list;
	}

	private static void parseEssentialEnterpriseDependencies(
			ProjectExample project, Element enterpriseDependenciesElement) {

		String tagsValue = enterpriseDependenciesElement.getTextContent();
		if (tagsValue != null) {
			StringTokenizer tokenizer = new StringTokenizer(tagsValue.trim(),",");//$NON-NLS-1$
			Set<String> gavs = new HashSet<String>();
			while (tokenizer.hasMoreTokens()) {
				String gav = tokenizer.nextToken().trim();
				if (gav.length() > 0) {
					gavs.add(gav);
				}
			}
			project.setEssentialEnterpriseDependencyGavs(gavs);
		}

	}

	public static Set<URL> getCategoryURLs() {
		if (categoryUrls == null) {
			categoryUrls = new HashSet<URL>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(PROJECT_EXAMPLES_CATEGORIES_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					if (URL_EXT.equals(configurationElement.getName())) {
						String urlString = configurationElement.getValue();
						URL url = getURL(urlString);
						if (url != null) {
							categoryUrls.add(url);
						}
					} 
				}
			}

		}
		return categoryUrls;
	}
	
	private static void handleCategories(List<ProjectExampleCategory> list, IProgressMonitor monitor) {
		Set<URL> urls = getCategoryURLs();
		for (URL url:urls) {
			File file = getProjectExamplesFile(url,
					"categories", ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
			if (monitor.isCanceled()) {
				return;
			}
			if (file == null || !file.exists() || !file.isFile()) {
				ProjectExamplesActivator.log(NLS.bind(
						Messages.ProjectUtil_Invalid_URL, url
								.toString()));
				continue;
			}

			DocumentBuilderFactory dbf = DocumentBuilderFactory
					.newInstance();
			Document doc;
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(file);
			} catch (Exception e1) {
				ProjectExamplesActivator.log(e1.getMessage());
				continue;
			} 
			NodeList categoryNodes = doc.getElementsByTagName("category"); //$NON-NLS-1$
			int len = categoryNodes.getLength();
			for (int i = 0; i < len; i++) {
				Node node = categoryNodes.item(i);
				NamedNodeMap attributes = node.getAttributes();
				if (attributes == null) {
					continue;
				}
				String name = getAttributeValue(attributes, NAME);
				if (name == null) {
					continue;
				}
				String description = getAttributeValue(attributes, "description"); //$NON-NLS-1$
				int priority = 0;
				try {
					priority = new Integer(getAttributeValue(attributes, "priority")).intValue(); //$NON-NLS-1$
				} catch (Exception e) {
					ProjectExamplesActivator.log(e);
				}
				if (description != null || priority > 0) {
					for (ProjectExampleCategory projectExampleCategory:list) {
						if (name.equals(projectExampleCategory.getName())) {
							projectExampleCategory.setDescription(description);
							projectExampleCategory.setPriority(priority);
							continue;
						}
					}
				}
			}
		}
	}

	private static String getAttributeValue(NamedNodeMap attributes, String name) {
		String value = null;
		Node node = attributes.getNamedItem(name);
		if (node != null) {
			value = node.getNodeValue();
		}
		return value;
	}

	private static void parseTags(ProjectExample project, Element tagElement) {
		String tagsValue = tagElement.getTextContent();
		if (tagsValue != null) {
			StringTokenizer tokenizer = new StringTokenizer(tagsValue.trim(),
					",");//$NON-NLS-1$
			Set<String> tags = new HashSet<String>();
			while (tokenizer.hasMoreTokens()) {
				String tag = tokenizer.nextToken().trim();
				if (tag.length() > 0) {
					tags.add(tag);
				}
			}
			project.setTags(tags);
		}
	}

	private static void parseFixes(ProjectExample project, Element node) {
		NodeList children = node.getChildNodes();
		int cLen = children.getLength();
		for (int i = 0; i < cLen; i++) {
			Node cNode = children.item(i);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) cNode;
				String nodeName = child.getNodeName();
				if (nodeName.equals("fix")) { //$NON-NLS-1$
					parseFix(project, child);
				}
			}
		}
	}

	private static void parseMavenArchetype(ProjectExample project, Element node) {
		NodeList children = node.getChildNodes();
		int cLen = children.getLength();
		ArchetypeModel archetypeModel = project.getArchetypeModel();
		for (int i = 0; i < cLen; i++) {
			Node cNode = children.item(i);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) cNode;
				String nodeName = child.getNodeName();
				if (nodeName.equals("archetypeGroupId")) { //$NON-NLS-1$
					archetypeModel.setArchetypeGroupId(getContent(child));
				} else if (nodeName.equals("archetypeArtifactId")) { //$NON-NLS-1$
					archetypeModel.setArchetypeArtifactId(getContent(child));
				} else if (nodeName.equals("archetypeVersion")) { //$NON-NLS-1$
					archetypeModel.setArchetypeVersion(getContent(child));
				} else if (nodeName.equals("archetypeRepository")) { //$NON-NLS-1$
					archetypeModel.setArchetypeRepository(getContent(child));
				} else if (nodeName.equals("groupId")) { //$NON-NLS-1$
					archetypeModel.setGroupId(getContent(child));
				} else if (nodeName.equals("artifactId")) { //$NON-NLS-1$
					archetypeModel.setArtifactId(getContent(child));
				} else if (nodeName.equals("version")) { //$NON-NLS-1$
					archetypeModel.setVersion(getContent(child));
				} else if (nodeName.equals("javaPackage")) { //$NON-NLS-1$
					archetypeModel.setJavaPackage(getContent(child));
				} else if (nodeName.equals("properties")) { //$NON-NLS-1$
					parseProperties(project, child);
				}
			}
		}
	}

	private static void parseProperties(ProjectExample project, Element node) {
		NodeList children = node.getChildNodes();
		int cLen = children.getLength();
		for (int i = 0; i < cLen; i++) {
			Node cNode = children.item(i);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) cNode;
				String nodeName = child.getNodeName();
				if (nodeName.equals("property")) { //$NON-NLS-1$
					String key = child.getAttribute("name"); //$NON-NLS-1$
					if (key == null || key.trim().length() <= 0) {
						ProjectExamplesActivator
								.log(Messages.ProjectUtil_Invalid_property);
						return;
					}
					String value = child.getAttribute("value"); //$NON-NLS-1$
					if (value == null || value.trim().length() <= 0) {
						ProjectExamplesActivator
								.log(Messages.ProjectUtil_Invalid_property);
						return;
					}
					project.getArchetypeModel().addProperty(key, value);
				}
			}
		}
	}

	private static void parseFix(ProjectExample project, Element node) {
		String type = node.getAttribute("type"); //$NON-NLS-1$
		if (type == null || type.trim().length() <= 0) {
			ProjectExamplesActivator.log(Messages.ProjectUtil_Invalid_fix);
			return;
		}
		ProjectFix fix = new ProjectFix();
		fix.setType(type);
		NodeList children = node.getChildNodes();
		int cLen = children.getLength();
		for (int i = 0; i < cLen; i++) {
			Node cNode = children.item(i);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) cNode;
				String nodeName = child.getNodeName();
				if (nodeName.equals("property")) { //$NON-NLS-1$
					String name = child.getAttribute("name"); //$NON-NLS-1$
					if (name == null || name.trim().length() <= 0) {
						ProjectExamplesActivator
								.log(Messages.ProjectUtil_Invalid_property);
						return;
					}
					String value = getContent(child);
					fix.getProperties().put(name, value);
				}
			}
		}
		project.getFixes().add(fix);
	}

	private static String getProjectExamplesXml() {
		String projectXML = System
				.getProperty("org.jboss.tools.project.examples.xml"); //$NON-NLS-1$
		if (projectXML != null && projectXML.length() > 0) {
			return projectXML;
		}
		return null;
	}

	private static String getContent(Element child) {
		String value = child.getTextContent();
		if (value == null) {
			value = ""; //$NON-NLS-1$
		}
		return value.trim();
	}

	public static File getProjectExamplesFile(URL url, String prefix,
			String suffix, IProgressMonitor monitor) {
		File file = null;
		if (PROTOCOL_FILE.equals(url.getProtocol())
				|| PROTOCOL_PLATFORM.equalsIgnoreCase(url.getProtocol())) {
			try {
				// assume all illegal characters have been properly encoded, so
				// use URI class to unencode
				file = new File(new URI(url.toExternalForm()));
			} catch (Exception e) {
				// URL contains unencoded characters
				file = new File(url.getFile());
			}
			if (!file.exists())
				return null;
		} else {
			try {
				if (monitor.isCanceled()) {
					return null;
				}
				long urlModified = -1;
				file = getFile(url);
				try {
					urlModified = ECFExamplesTransport.getInstance()
							.getLastModified(url);
				} catch (CoreException e) {
					if (file.exists()) {
						return file;
					}
				}
				if (urlModified == 0) {
					if (file.exists()) {
						return file;
					}
				}
				if (file.exists()) {
					long modified = file.lastModified();
					if (urlModified == modified) {
						return file;
					}
				}
				// file = File.createTempFile(prefix, suffix);
				// file.deleteOnExit();
				file.getParentFile().mkdirs();
				if (monitor.isCanceled()) {
					return null;
				}
				if (monitor.isCanceled()) {
					return null;
				}
				BufferedOutputStream destination = new BufferedOutputStream(
						new FileOutputStream(file));
				IStatus result = getTransport().download(prefix,
						url.toExternalForm(), destination, monitor);
				if (!result.isOK()) {
					ProjectExamplesActivator.getDefault().getLog().log(result);
					return null;
				} else {
					if (file.exists()) {
						file.setLastModified(urlModified);
					}
				}
			} catch (FileNotFoundException e) {
				ProjectExamplesActivator.log(e);
				return null;
			}
		}
		return file;
	}

	private static File getFile(URL url) {
		IPath location = ProjectExamplesActivator.getDefault()
				.getStateLocation();
		File root = location.toFile();
		String urlFile = url.getFile();
		File file = new File(root, urlFile);
		return file;
	}

	private static ECFExamplesTransport getTransport() {
		return ECFExamplesTransport.getInstance();
	}

	public static Document getDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		return doc;
	}

	public static String getAsXML(Set<IProjectExampleSite> sites)
			throws ParserConfigurationException, TransformerException,
			UnsupportedEncodingException {
		if (sites == null || sites.size() == 0) {
			return ""; //$NON-NLS-1$
		}
		Document doc = getDocument();
		Element sitesElement = doc.createElement(SITES);
		doc.appendChild(sitesElement);
		for (IProjectExampleSite site : sites) {
			Element siteElement = doc.createElement(SITE);
			siteElement.setAttribute(NAME, site.getName());
			siteElement.setAttribute(URL, site.getUrl().toString());
			sitesElement.appendChild(siteElement);
		}
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		DOMSource source = new DOMSource(doc);
		StreamResult outputTarget = new StreamResult(s);
		transformer.transform(source, outputTarget);
		return s.toString("UTF8"); //$NON-NLS-1$			
	}

	public static Element parseDocument(String document) {
		Element root = null;
		InputStream stream = null;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			stream = new ByteArrayInputStream(document.getBytes("UTF8")); //$NON-NLS-1$
			root = parser.parse(stream).getDocumentElement();
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				ProjectExamplesActivator.log(e);
			}
		}
		return root;
	}

	public static HashSet<IProjectExampleSite> getInvalidSites() {
		return invalidSites;
	}

	public static List<ProjectExample> getProjectsByTags(
			Collection<ProjectExampleCategory> categories, String... tags) {
		if (categories == null) {
			return null;
		}
		List<ProjectExample> selection = new ArrayList<ProjectExample>();
		for (ProjectExampleCategory c : categories) {
			for (ProjectExample p : c.getProjects()) {
				if (p.hasTags(tags) && !selection.contains(p)) {
					selection.add(p);
				}
			}
		}
		return selection;
	}
	
	private static class Tuple<X, Y> {
		
		X key;
		Y value;

		public Tuple(X key) {
			this.key = key;
		}
		
	}
	
	private static class FetchProjectExampleDocumentTask implements Callable<Tuple<IProjectExampleSite, Document>> {

		Tuple<IProjectExampleSite, Document> tuple; 
		
		public FetchProjectExampleDocumentTask(IProjectExampleSite site) {
			 tuple = new Tuple<IProjectExampleSite, Document>(site);
		}

		@Override
		public Tuple<IProjectExampleSite, Document> call() throws Exception {
			URL url = tuple.key.getUrl();
			File file = getProjectExamplesFile(url, "projectExamples", ".xml", new NullProgressMonitor());  //$NON-NLS-1$ //$NON-NLS-2$
			if(file == null || !file.exists() || !file.isFile()) {
				ProjectExamplesActivator.log(NLS.bind(Messages.ProjectUtil_Invalid_URL, url.toString()));
				return tuple;
			}
			
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(file);
				tuple.value = doc;
				
			} catch (Exception e) {
				ProjectExamplesActivator.log(e);
			}
			return tuple;
		}

	}	
}
