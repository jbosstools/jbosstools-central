/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.tools.project.examples.internal.model.XmlUnMarshallers.XmlProperty;

/**
 * 
 * @author snjeza
 *
 */
@XmlRootElement(name = "fix")
@XmlAccessorType (XmlAccessType.FIELD)
public class RequirementModel {
	
	public final static String ALLOWED_VERSIONS = "allowed-versions"; //$NON-NLS-1$
	public final static String ECLIPSE_PROJECTS = "eclipse-projects"; //$NON-NLS-1$
	public final static String ALLOWED_TYPES = "allowed-types"; //$NON-NLS-1$
	public final static String ID = "id"; //$NON-NLS-1$
	public final static String VERSION = "VERSION"; //$NON-NLS-1$
	public final static String DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String DOWNLOAD_ID = "downloadId"; //$NON-NLS-1$
	public static final String CONNECTOR_ID = "connectorIds"; //$NON-NLS-1$
	
	@XmlAttribute
	private String type;
	
	@XmlAttribute
	private boolean required = false;
	
	@XmlElement(name="property")
	private List<XmlProperty> xmlProperties;

	//Required for JAXB support only
	@SuppressWarnings("unused")
	private void setXmlProperties(List<XmlProperty> xmlProperties) {
		this.xmlProperties = xmlProperties;
	}
	@SuppressWarnings("unused")
	private List<XmlProperty> getXmlProperties() {
		return xmlProperties;
	}
	
	@XmlTransient
	private Map<String,String> properties;

	public RequirementModel() {
		super();
	}
	
	public RequirementModel(String type) {
		this();
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<>();
			if (xmlProperties != null) {
				for (XmlProperty property : xmlProperties) {
					properties.put(property.key, property.body);
				}
			}
		}
		return properties;
	}
	
	public void setProperties(Map<String, String> properties) {
		this.properties = properties ==null? null : new LinkedHashMap<>(properties);
	} 
	
	public void setRequired(boolean required) {
		this.required = required;
	} 
	
	public boolean isRequired() {
		return required;
	} 
}
