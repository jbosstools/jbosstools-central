/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.filetransfer.ECFExamplesTransport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author snjeza
 * 
 */
public class ProjectUtil {

	private static final String EDITOR = "editor"; //$NON-NLS-1$

	public static final String CHEATSHEETS = "cheatsheets"; //$NON-NLS-1$

	private static final String PROTOCOL_FILE = "file"; //$NON-NLS-1$

	private static final String PROJECT_EXAMPLES_XML_EXTENSION_ID = "org.jboss.tools.project.examples.projectExamplesXml"; //$NON-NLS-1$
	private static List<URL> URLs;

	private ProjectUtil() {
	}

	private static List<URL> getURLs() {
		if (URLs == null) {
			URLs = new ArrayList<URL>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry
					.getExtensionPoint(PROJECT_EXAMPLES_XML_EXTENSION_ID);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurationElements = extension
						.getConfigurationElements();
				for (int j = 0; j < configurationElements.length; j++) {
					IConfigurationElement configurationElement = configurationElements[j];
					String urlString = configurationElement.getValue();
					URL url = getURL(urlString);
					if (url != null) {
						URLs.add(url);
					}
				}
			}
			URL url = getURL(getProjectExamplesXml());
			if (url != null) {
				URLs.add(url);
			}
		}
		return URLs;
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
		getURLs();
		List<Category> list = new ArrayList<Category>();
		Category other = Category.OTHER;
		try {
			for (URL url : URLs) {
				File file = getProjectExamplesFile(url,
						"projectExamples", ".xml", null); //$NON-NLS-1$ //$NON-NLS-2$
				if (file == null || !file.exists() || !file.isFile()) {
					ProjectExamplesActivator.log(NLS.bind(Messages.ProjectUtil_Invalid_URL,url.toString()));
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
								if (nodeName.equals("name")) { //$NON-NLS-1$
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
								if (nodeName.equals("url")) { //$NON-NLS-1$
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
									attribute = child.getAttribute("url"); //$NON-NLS-1$
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
					category.getProjects().add(project);
				}
			}
		} catch (Exception e) {
			ProjectExamplesActivator.log(e);
		}
		list.add(other);
		return list;
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
}
