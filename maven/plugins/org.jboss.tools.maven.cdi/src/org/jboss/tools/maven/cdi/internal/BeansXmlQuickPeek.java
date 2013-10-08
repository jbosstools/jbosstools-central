/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
  *     Fred Bricon (Red Hat, Inc.) - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.cdi.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
import org.eclipse.jst.jee.util.internal.XMLRootHandler;
import org.xml.sax.InputSource;

@SuppressWarnings("restriction")
public class BeansXmlQuickPeek {

	private static final String CDI_SCHEMA_ID_1_0 = "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd"; //$NON-NLS-1$
	private static final String CDI_SCHEMA_ID_1_0_JBOSS = "http://java.sun.com/xml/ns/javaee http://docs.jboss.org/cdi/beans_1_0.xsd"; //$NON-NLS-1$
	private static final String CDI_SCHEMA_ID_1_1 = "http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"; //$NON-NLS-1$
	private static final String CDI_ID_1_0 = "1.0"; //$NON-NLS-1$
	private static final String CDI_ID_1_1 = "1.1"; //$NON-NLS-1$
	
	private XMLRootHandler handler;

	private String storedVersion = null;
	
	private boolean versionSet = false;

	
	public BeansXmlQuickPeek(IFile file) {
		if (file == null) {
			throw new IllegalArgumentException("The file mustn't be null");
		}
		InputStream is = null;
		try {
			is = file.getContents();
			parse(is);
		} catch (Exception ioe){
			if (is != null) {
				try {
					is.close();
				} catch (Exception ignore) {
					//ignore
				}
			}
		}
	}
	
	public BeansXmlQuickPeek(InputStream in) {
		parse(in);
	}
	
	private void parse(InputStream in) {
		if (in != null) {
			try {
				InputSource inputSource = new InputSource(in);
				handler = new XMLRootHandler();
				handler.parseContents(inputSource);
			} catch (Exception ex) {
				// ignore
			} finally {
				try {
					in.reset();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}

	public String getVersion() {
		if (!versionSet) {
			if (handler != null && "beans".equals(handler.getRootName())) { //$NON-NLS-1$
				String version = null;
				if (handler.getRootAttributes() != null) {
					version = handler.getRootAttributes().getValue("version"); //$NON-NLS-1$
				}
				if (version == null || version.trim().length() == 0) {
					version = getVersionFromSchema();
				}
				storedVersion = version;
				versionSet = true;
			}
			
		}
		return storedVersion;
	}

	private String getVersionFromSchema() {
		//Algorithm copied from org.eclipse.jst.jee.util.internal.JavaEEQuickPeek
		if (handler == null) {
			return null;
		}
		String schemaName = null;
		if (handler.getRootAttributes() != null) {
			schemaName  = JavaEEQuickPeek.normalizeSchemaLocation(handler.getRootAttributes().getValue("xsi:schemaLocation")); //$NON-NLS-1$
		}
		if (schemaName == null) {
			return null;
		}
		String version = null;
		if (schemaName.equals(CDI_SCHEMA_ID_1_0) || schemaName.equals(CDI_SCHEMA_ID_1_0_JBOSS)) {
			version  = CDI_ID_1_0;
		} else if (schemaName.equals(CDI_SCHEMA_ID_1_1)) {
			version = CDI_ID_1_1;
		} 
		return version;
	}
}