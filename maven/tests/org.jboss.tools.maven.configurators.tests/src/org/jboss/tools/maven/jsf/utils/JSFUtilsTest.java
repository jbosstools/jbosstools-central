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
package org.jboss.tools.maven.jsf.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.jboss.tools.maven.jsf.configurators.JSFUtils;

public class JSFUtilsTest extends TestCase {

	private static final String webXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<web-app xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:web=\"http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\" version=\"2.5\">\r\n" + 
			"  <display-name>a</display-name>\r\n" + 
			"  <welcome-file-list>\r\n" + 
			"    <welcome-file>index.html</welcome-file>\r\n" + 
			"  </welcome-file-list>\r\n" + 
			"  <servlet>\r\n" + 
			"  <servlet-name>FacesServlet</servlet-name>\r\n" + 
			"  <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>\r\n" + 
			"  </servlet>\r\n" + 
			"</web-app>";

	private static final String webXmlNoFaces = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<web-app xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:web=\"http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\" version=\"2.5\">\r\n" + 
			"  <servlet>\r\n" + 
			"  <servlet-name>FacesServlet</servlet-name>\r\n" + 
			"  <servlet-class>javax.faces.webapp.NoFacesServlet</servlet-class>\r\n" + 
			"  </servlet>\r\n" + 
			"</web-app>";
	

	public void testHasFacesServlet() {
		InputStream is = null; 
		assertFalse(JSFUtils.hasFacesServlet(is));
				
		is = new ByteArrayInputStream(webXml.getBytes());
		assertTrue(JSFUtils.hasFacesServlet(is));

		is = new ByteArrayInputStream(webXmlNoFaces.getBytes());
		assertFalse(JSFUtils.hasFacesServlet(is));
	}
	
}
