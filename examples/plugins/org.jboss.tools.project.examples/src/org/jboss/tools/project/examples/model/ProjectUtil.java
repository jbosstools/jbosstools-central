/*************************************************************************************
 * Copyright (c) 2008-2009 JBoss by Red Hat and others.
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.filetransfer.ECFExamplesTransport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author snjeza
 * 
 */
public class ProjectUtil {

	private static final String URL = "url"; //$NON-NLS-1$

	private static final String NAME = "name"; //$NON-NLS-1$

	private static final String SITES = "sites"; //$NON-NLS-1$

	private static final String SITE = "site"; //$NON-NLS-1$
	
	private static final String EDITOR = "editor"; //$NON-NLS-1$

	public static final String CHEATSHEETS = "cheatsheets"; //$NON-NLS-1$

	public static final String PROTOCOL_FILE = "file"; //$NON-NLS-1$

	private static final String PROJECT_EXAMPLES_XML_EXTENSION_ID = "org.jboss.tools.project.examples.projectExamplesXml"; //$NON-NLS-1$
	
	private static String URL_EXT = URL;
	
	private static String EXPERIMENTAL_EXT = "experimental"; //$NON-NLS-1$

	private static Set<ProjectExampleSite> pluginSites;
	
	private ProjectUtil() {
	}

	public static Set<ProjectExampleSite> getPluginSites() {
		if (pluginSites == null) {
			pluginSites = new HashSet<ProjectExampleSite>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(PROJECT_EXAMPLES_XML_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				ProjectExampleSite site = new ProjectExampleSite();
				site.setName(extension.getLabel());
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					if (URL_EXT.equals(configurationElement.getName())) {
						String urlString = configurationElement.getValue();
						URL url = getURL(urlString);
						if (url != null) {
							site.setUrl(url);
						}
					} else if (EXPERIMENTAL_EXT.equals(configurationElement.getName())) {
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
	
	public static Set<ProjectExampleSite> getUserSites() {
		Set<ProjectExampleSite> sites = new HashSet<ProjectExampleSite>();
		ProjectExampleSite site = getSite(getProjectExamplesXml());
		if (site != null) {
			sites.add(site);
		}
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		String sitesAsXml = store.getString(ProjectExamplesActivator.USER_SITES);
		if (sitesAsXml != null && sitesAsXml.trim().length() > 0) {
			Element rootElement = parseDocument(sitesAsXml);
			if (!rootElement.getNodeName().equals(SITES)) { 
				ProjectExamplesActivator.log(Messages.ProjectUtil_Invalid_preferences);
				return sites;
			}
			NodeList list = rootElement.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element entry = (Element) node;
					if(entry.getNodeName().equals(SITE)){
						String name = entry.getAttribute(NAME);
						String urlString = entry.getAttribute(URL);
						if (name != null && name.trim().length() > 0 && urlString != null && urlString.trim().length() > 0) {
							URL url = null;
							try {
								url = new URL(urlString);
							} catch (MalformedURLException e) {
								ProjectExamplesActivator.log(Messages.ProjectUtil_Invalid_preferences);
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
	
	private static Set<ProjectExampleSite> getSites() {
		Set<ProjectExampleSite> sites = new HashSet<ProjectExampleSite>();
		sites.addAll(getPluginSites());
		sites.addAll(getUserSites());
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

	public static List<Category> getProjects() {
		Set<ProjectExampleSite> sites = getSites();
		List<Category> list = new ArrayList<Category>();
		Category other = Category.OTHER;
		try {
			for (ProjectExampleSite site : sites) {
				boolean showExperimentalSites = ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES);
				if (!showExperimentalSites && site.isExperimental()) {
					continue;
				}
				File file = getProjectExamplesFile(site.getUrl(),
						"projectExamples", ".xml", null); //$NON-NLS-1$ //$NON-NLS-2$
				if (file == null || !file.exists() || !file.isFile()) {
					ProjectExamplesActivator.log(NLS.bind(Messages.ProjectUtil_Invalid_URL,site.getUrl().toString()));
					continue;
				}
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(file);
				NodeList projects = doc.getElementsByTagName("project"); //$NON-NLS-1$
				int len = projects.getLength();
				for (int i = 0; i < len; i++) {
					Node node = projects.item(i);
					Project project = new Project();
					Category category = other;
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
								
								if (nodeName.equals("category")) { //$NON-NLS-1$
									String value = getContent(child);
									boolean found = false;
									for (Category cat : list) {
										if (cat.getName().equals(value)) {
											category = cat;
											found = true;
											break;
										}
									}
									if (!found) {
										category = new Category(value);
										list.add(category);
									}
									project.setCategory(category);
								}
								if (nodeName.equals(NAME)) { 
									project.setName(getContent(child));
								}
								if (nodeName.equals("site")) { //$NON-NLS-1$
									project.setSite(getContent(child));
								}
								if (nodeName.equals("shortDescription")) { //$NON-NLS-1$
									project
											.setShortDescription(getContent(child));
								}
								if (nodeName.equals("description")) { //$NON-NLS-1$
									project.setDescription(getContent(child));
								}
								if (nodeName.equals(URL)) { 
									project.setUrl(getContent(child));
								}
								if (nodeName.equals("size")) { //$NON-NLS-1$
									long size = 0;
									try {
										size = new Long(getContent(child));
									} catch (Exception ignored) {
									}
									project.setSize(size);
								}
								if (nodeName.equals("included-projects")) { //$NON-NLS-1$
									String includedProjects = getContent(child);
									if (includedProjects != null) {
										includedProjects = includedProjects
												.trim();
										StringTokenizer tokenizer = new StringTokenizer(
												includedProjects, ","); //$NON-NLS-1$
										List<String> projectList = new ArrayList<String>();
										while (tokenizer.hasMoreTokens()) {
											projectList.add(tokenizer
													.nextToken());
										}
										project
												.setIncludedProjects(projectList);
									}
								}
								if (nodeName.equals("welcome")) { //$NON-NLS-1$
									project.setWelcome(true);
									String attribute = child.getAttribute("type"); //$NON-NLS-1$
									if (attribute != null && CHEATSHEETS.equals(attribute.trim())) {
										project.setType(attribute.trim());
									} else {
										project.setType(EDITOR);
									}
									attribute = child.getAttribute(URL); 
									if (attribute == null || attribute.trim().length() <= 0) {
										project.setWelcome(false);
										ProjectExamplesActivator.log(Messages.ProjectUtil_Invalid_welcome_element);
									} else {
										project.setWelcomeURL(attribute.trim());
									}
								}
							}
						}
					}
					if (project.getSite() == null) {
						String siteName = site.getName();
						if (siteName == null) {
							siteName = Messages.Project_Unknown;
						}
						project.setSite(siteName);
					}
					category.getProjects().add(project);
				}
			}
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		}
		list.add(other);
		return list;
	}

	private static void parseFixes(Project project, Element node) {
		NodeList children = node.getChildNodes();
		int cLen = children.getLength();
		for (int i = 0; i < cLen; i++) {
			Node cNode = children.item(i);
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) cNode;
				String nodeName = child.getNodeName();
				if (nodeName.equals("fix")) { //$NON-NLS-1$
					parseFix(project,child);
				}
			}
		}
	}

	private static void parseFix(Project project, Element node) {
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
						ProjectExamplesActivator.log(Messages.ProjectUtil_Invalid_property);
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
		String projectXML = System.getProperty("org.jboss.tools.project.examples.xml"); //$NON-NLS-1$
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
		if (PROTOCOL_FILE.equals(url.getProtocol())) {
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
				file = File.createTempFile(prefix, suffix);
				file.deleteOnExit();
				BufferedOutputStream destination = new BufferedOutputStream(
						new FileOutputStream(file));
				IStatus result = getTransport().download(prefix,
						url.toExternalForm(), destination, monitor);
				if (!result.isOK()) {
					ProjectExamplesActivator.getDefault().getLog().log(result);
					return null;
				}
			} catch (FileNotFoundException e) {
				ProjectExamplesActivator.log(e);
				return null;
			} catch (IOException e) {
				ProjectExamplesActivator.log(e);
				return null;
			}
		}
		return file;
	}

	private static ECFExamplesTransport getTransport() {
		return ECFExamplesTransport.getInstance();
	}
	
	public static Document getDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dfactory= DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder= dfactory.newDocumentBuilder();
		Document doc= docBuilder.newDocument();
		return doc;
	}
	
	public static String getAsXML(Set<ProjectExampleSite> sites)
			throws ParserConfigurationException, TransformerException,
			UnsupportedEncodingException {
		if (sites == null || sites.size() == 0) {
			return ""; //$NON-NLS-1$
		}
		Document doc = getDocument();
		Element sitesElement = doc.createElement(SITES); 
		doc.appendChild(sitesElement);
		for (ProjectExampleSite site : sites) {
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
		try{		
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			stream = new ByteArrayInputStream(document.getBytes("UTF8")); //$NON-NLS-1$
			root = parser.parse(stream).getDocumentElement();
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		} finally { 
			try{
                if (stream != null) {
                    stream.close();
                }
			} catch(IOException e) {
				ProjectExamplesActivator.log(e);
			}
		}		
		return root;
	}	
}
