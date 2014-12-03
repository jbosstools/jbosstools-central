/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;
import org.jboss.tools.project.examples.internal.TokenizerUtil;

public class XmlUnMarshallers {
	
	private XmlUnMarshallers(){}
	
	@XmlType(name="property") 
	public static class XmlProperty {
	    @XmlAttribute(name="name") public String key;
	    @XmlAttribute public String value;
	    @XmlValue public String body;
	}

	//@XmlType(name="property") 
	public static class XmlProperties {
	    @XmlElement(name="property") 
	    public Collection<XmlProperty> properties = new ArrayList<XmlProperty>();
	}

	private static abstract class AbstractUnMarshaller<ValueType,BoundType> extends XmlAdapter<ValueType,BoundType> {
		@Override
		public ValueType marshal(BoundType v) throws Exception {
			throw new UnsupportedOperationException();
		}	
	}
	
	public static class StringToListUnMarshaller extends AbstractUnMarshaller<String, List<String>> {

		@Override
		public List<String> unmarshal(String s) throws Exception {
			return TokenizerUtil.splitToList(s);
		}
	}

	public static class StringToSetUnMarshaller extends AbstractUnMarshaller<String, Set<String>> {

		@Override
		public Set<String> unmarshal(String s) throws Exception {
			return TokenizerUtil.splitToSet(s);
		}
	}
	
	public static class ArchetypePropertyUnMarshaller extends AbstractUnMarshaller<XmlProperties, Properties> {
		
		public Properties unmarshal(XmlProperties xml) {
			Properties props = new Properties();
			for (XmlProperty entry: xml.properties) {
				props.setProperty(entry.key, entry.value);
			}
			return props;
		}
	}
	
	public static class StringTrimXmlAdapter extends XmlAdapter<String, String> {
	    @Override
	    public String unmarshal(String text) throws Exception {
	        return StringUtils.trimToNull(text);
	    }

		@Override
		public String marshal(String text) throws Exception {
			return StringUtils.trimToEmpty(text);
		}
	}
	
}
