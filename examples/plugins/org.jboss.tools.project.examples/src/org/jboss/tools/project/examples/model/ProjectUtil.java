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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.filetransfer.ECFExamplesTransport;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author snjeza
 * 
 */
public class ProjectUtil {

	private final static String PROJECT_EXAMPLES_XML = "http://anonsvn.jboss.org/repos/jbosstools/workspace/examples/projectExamples.xml";
	private static final String PROTOCOL_FILE = "file";
	
	private ProjectUtil() {

	}

	public static List<Category> getTestProjects() {
		Category seamCategory = new Category("Seam");
		Project project = new Project();
		project.setName("dvdstore");
		project.setShortDescription("Seam DVD Store Example");
		project
				.setDescription("This example demonstrates the use of Seam with jBPM pageflow and business process management.  It runs on JBoss AS and Tomcat.");
		project.setSize(10000);
		project
				.setUrl("http://anonsvn.jboss.org/repos/jbosstools/workspace/snjeza/portlet-examples/dvdstore.zip");
		project.setCategory(seamCategory);
		seamCategory.getProjects().add(project);

		Category portletCategory = new Category("Portlet");

		project = new Project();
		project.setName("TestJavaPortlet");
		project.setShortDescription("JBoss Java Portlet Example");
		project
				.setDescription("This example demonstrates the use of JBoss Java Portlet. It runs on JBoss Portal 2.7.0");
		project.setSize(10000);
		project
				.setUrl("http://anonsvn.jboss.org/repos/jbosstools/workspace/snjeza/portlet-examples/TestJavaPortlet.zip");
		project.setCategory(portletCategory);
		portletCategory.getProjects().add(project);

		project = new Project();
		project.setName("TestJSFPortlet");
		project.setShortDescription("JBoss JSF Portlet Example");
		project
				.setDescription("This example demonstrates the use of JBoss JSF Portlet. It runs on JBoss Portal 2.7.0");
		project.setSize(4000000);
		project
				.setUrl("http://anonsvn.jboss.org/repos/jbosstools/workspace/snjeza/portlet-examples/TestJSFPortlet.zip");
		project.setCategory(portletCategory);
		portletCategory.getProjects().add(project);

		project = new Project();
		project.setName("TestSeamPortlet");
		project.setShortDescription("JBoss Seam Portlet Example");
		project
				.setDescription("This example demonstrates the use of JBoss Seam Portlet. It runs on JBoss Portal 2.7.0");
		project.setSize(10000000);
		project
				.setUrl("http://anonsvn.jboss.org/repos/jbosstools/workspace/snjeza/portlet-examples/TestSeamPortlet.zip");
		project.setCategory(portletCategory);
		portletCategory.getProjects().add(project);

		Category otherCategory = new Category("Other");

		List<Category> list = new ArrayList<Category>();
		list.add(seamCategory);
		list.add(portletCategory);
		list.add(otherCategory);

		return list;
	}

	public static List<Category> getProjects() {
		List<Category> list = new ArrayList<Category>();
		Category other = Category.OTHER;
		try {
			// TODO add a progress monitor
			File file = getProjectExamplesFile(getProjectExamplesXml(),"projectExamples", ".xml",null);
			if (file.exists() && file.isFile()) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(file);
				NodeList projects = doc.getElementsByTagName("project");
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
								if (nodeName.equals("category")) {
									String value = getContent(child);
									boolean found = false;
									for (Category cat:list) {
										if (cat.getName().equals(value)) {
											category=cat;
											found=true;
											break;
										}
									}
									if (!found) {
										category = new Category(value);
										list.add(category);
									}
									project.setCategory(category);
								}
								if (nodeName.equals("name")) {
									project.setName(getContent(child));
								}
								if (nodeName.equals("shortDescription")) {
									project.setShortDescription(getContent(child));
								}
								if (nodeName.equals("description")) {
									project.setDescription(getContent(child));
								}
								if (nodeName.equals("url")) {
									project.setUrl(getContent(child));
								}
								if (nodeName.equals("size")) {
									long size = 0;
									try {
										size = new Long(getContent(child));
									} catch (Exception ignored) {
									}
									project.setSize(size);
								}
								if (nodeName.equals("included-projects")) {
									String includedProjects = getContent(child);
									if (includedProjects != null) {
										includedProjects = includedProjects.trim();
										StringTokenizer tokenizer = new StringTokenizer(includedProjects,",");
										List<String> projectList = new ArrayList<String>();
										while (tokenizer.hasMoreTokens()) {
											projectList.add(tokenizer.nextToken());
										}
										project.setIncludedProjects(projectList);
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
		String projectXML = System.getProperty("org.jboss.tools.project.examples.xml");
		if (projectXML != null && projectXML.length() > 0) {
			return projectXML;
		}
		return PROJECT_EXAMPLES_XML;
	}

	private static String getContent(Element child) {
		String value = child.getTextContent();
		if (value == null) {
			value="";
		}
		return value.trim();
	}

	private static File getProjectExamplesXmlTest() throws Exception {
		Bundle bundle = Platform.getBundle(ProjectExamplesActivator.PLUGIN_ID);
		URL examplesURL = bundle.getEntry("/projectExamples.xml");
		URL url = FileLocator.resolve(examplesURL);
		File file = new File(FileLocator.toFileURL(url).getFile());
		return file;
	}
	
	public static File getProjectExamplesFile(String urlString,String prefix, String suffix, IProgressMonitor monitor) {
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			ProjectExamplesActivator.log(e);
			return null;
		}
		File file = null;
		if (PROTOCOL_FILE.equals(url.getProtocol())) {
			try {
				//assume all illegal characters have been properly encoded, so use URI class to unencode
				file = new File(new URI(url.toExternalForm()));
			} catch (Exception e) {
				//URL contains unencoded characters
				file = new File(url.getFile());
			}
			if (!file.exists())
				return null;
		} else {
			try {
				file = File.createTempFile(prefix,suffix);
				file.deleteOnExit();
				BufferedOutputStream destination = new BufferedOutputStream(new FileOutputStream(file));
				IStatus result = getTransport().download(prefix,url.toExternalForm(), destination, monitor);
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
