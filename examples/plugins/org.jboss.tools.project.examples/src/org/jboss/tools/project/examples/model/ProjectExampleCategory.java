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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.tools.project.examples.internal.Messages;

/**
 * @author snjeza
 * 
 */
@XmlRootElement(name = "category")
@XmlAccessorType (XmlAccessType.PROPERTY)
public class ProjectExampleCategory implements ProjectModelElement, Comparable<ProjectExampleCategory> {

	public static String OTHER = Messages.Category_Other;

	private String name;
	
	@XmlTransient
	private List<ProjectExample> projects = new ArrayList<>();
	
	private String description;
	
	private int priority = Integer.MAX_VALUE -1 ;

	public ProjectExampleCategory() {
		super();
	}
	
	public ProjectExampleCategory(String name) {
		this();
		setName(name);
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProjectExample> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectExample> projects) {
		this.projects = projects;
	}

	@XmlAttribute
	public String getDescription() {
		return description;
	}

	@XmlAttribute
	public String getShortDescription() {
		return getName();
	}

	@XmlAttribute
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + priority;
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
		ProjectExampleCategory other = (ProjectExampleCategory) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}

	@Override
	public int compareTo(ProjectExampleCategory o) {
		if (o == null)
			return 1;
		int other = o.getPriority();
		if (other < this.priority)
			return 1;
		else if (other > this.priority)
			return -1;
		if (name == null)
			return -1;
		
		if (OTHER.equals(name)) {
			return 1;
		}
		
		return name.compareTo(o.getName());
	}

	@Override
	public String toString() {
		return "ProjectExampleCategory [name=" + name + ", description=" //$NON-NLS-1$ //$NON-NLS-2$
				+ description + ", priority=" + priority  //$NON-NLS-1$//$NON-NLS-2$
				+ "]"; //$NON-NLS-1$
	}

}
