/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.jsf.configurators;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.codehaus.plexus.util.IOUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.jboss.tools.common.util.EclipseJavaUtil;
import org.jboss.tools.maven.core.ProjectUtil;
import org.jboss.tools.maven.jsf.utils.FacesConfigQuickPeek;
import org.jboss.tools.maven.jsf.utils.xpl.JSFAppConfigUtils;
import org.jboss.tools.maven.ui.Activator;
import org.w3c.dom.Document;

public class JSFUtils {

	public static final String FACES_SERVLET = "javax.faces.webapp.FacesServlet";

	private static final String FACES_SERVLET_XPATH = "//servlet[servlet-class=\"" + FACES_SERVLET + "\"]";

	public static final String JSF_VERSION_2_1 = "2.1";

	public static final String JSF_VERSION_2_0 = "2.0";
	
	public static final String JSF_VERSION_1_2 = "1.2";
	
	public static final String JSF_VERSION_1_1 = "1.1";
	
	private JSFUtils() {
		// no public constructor
	}

	/**
	 * Return the faces-config.xml of the given project, or null if faces-config.xml doesn't exist
	 */
	public static IFile getFacesconfig(IProject project) {
		IFile facesConfig = null;
		@SuppressWarnings("unchecked")
		List<String> configFiles = JSFAppConfigUtils.getConfigFilesFromContextParam(project);
		for (String configFile : configFiles) {
			facesConfig = ProjectUtil.getWebResourceFile(project, configFile);
			if (facesConfig != null && facesConfig.exists()) {
				return facesConfig;
			}
		}
		facesConfig = ProjectUtil.getWebResourceFile(project, "WEB-INF/faces-config.xml");
		
		return facesConfig;
	}
	
	/**
	 * Return the faces config version of the given project, or null if faces-config.xml doesn't exist
	 */
	public static  String getVersionFromFacesconfig(IProject project) {
		IFile facesConfig = getFacesconfig(project);
	    String version = null;
		if (facesConfig != null) {
			InputStream in = null;
			try {
				facesConfig.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
				in = facesConfig.getContents();
				FacesConfigQuickPeek peek = new FacesConfigQuickPeek(in);
				version = peek.getVersion();
			} catch (CoreException e) {
				// ignore
				Activator.log(e);
			} finally {
				IOUtil.close(in);
			}
		}
		return version;
	}
	
	public static boolean hasFacesServlet(IFile webXml) {
		//We look for javax.faces.webapp.FacesServlet in web.xml
		if (webXml == null || !webXml.isAccessible()) {
			return false;
		}
		
		InputStream is = null;
		try {
			is = webXml.getContents();
			return hasFacesServlet(is);
		} catch (Exception e) {
			Activator.log("An error occured trying to find to "+FACES_SERVLET+" in "+ webXml.getLocation().toOSString()+":"+e.getMessage());
		} finally {
			IOUtil.close(is);
		}
		return false;
	}	
	
	public static boolean hasFacesServlet(InputStream input) {
		if (input == null) {
			return false;
		}
		boolean hasFacesServlet = false;
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(false); // never forget this!
			domFactory.setValidating(false);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(input);

			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(FACES_SERVLET_XPATH);
			hasFacesServlet = null != expr.evaluate(doc, XPathConstants.NODE);
		} catch (Exception e) {
			Activator.log("An error occured trying to find to "+FACES_SERVLET+" :"+e.getMessage());
		}
		return hasFacesServlet;
	}		
	
	/**
	 * Determines the JSF version by searching for the methods of javax.faces.application.Application 
	 * in the project's classpath.
	 * @param project : the java project to analyze
	 * @return the JSF version (1.1, 1.2, 2.0, 2.1) found in the classpath, 
	 * or null if the project doesn't depend on JSF 
	 */
	public static String getJSFVersionFromClasspath(IProject project) {
		String version = null;
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject != null) {
			IType type = null;
			try {
				type = EclipseJavaUtil.findType(javaProject, "javax.faces.context.FacesContext");//$NON-NLS-1$ 
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			if (type != null) {
				String[] emptyParams = new String[0];
				if (type.getMethod("isReleased", emptyParams).exists()) {
					return JSF_VERSION_2_1;					
				}
				if (type.getMethod("getAttributes", emptyParams).exists() &&    
					type.getMethod("getPartialViewContext", emptyParams).exists()) {      
					return JSF_VERSION_2_0;
			    }
				if (type.getMethod("getELContext", emptyParams).exists()) { 
					return JSF_VERSION_1_2;
				} 
				version = JSF_VERSION_1_1;
			}
		}
		return version;
	}
		
}
