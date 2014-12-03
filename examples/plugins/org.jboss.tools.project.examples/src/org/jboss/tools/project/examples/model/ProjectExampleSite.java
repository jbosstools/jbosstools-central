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

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType (XmlAccessType.FIELD)
@XmlRootElement(name="site")
public class ProjectExampleSite implements IProjectExampleSite {
	@XmlAttribute
	private URI url;
	
	@XmlAttribute
	private String name;
	
	@XmlAttribute
	private boolean experimental;
	
	@XmlAttribute
	private boolean editable = false;
	
	@Override
	public URI getUrl() {
		return url;
	}
	
	@Override
	public void setUrl(URI url) {
		this.url = url;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isExperimental() {
		return experimental;
	}
	
	@Override
	public void setExperimental(boolean experimental) {
		this.experimental = experimental;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (editable ? 1231 : 1237);
		result = prime * result + (experimental ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectExampleSite other = (ProjectExampleSite) obj;
		if (editable != other.editable)
			return false;
		if (experimental != other.experimental)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "ProjectExampleSite [url=" + url + ", name=" + name
				+ ", experimental=" + experimental + ", editable=" + editable
				+ "]";
	}

}
