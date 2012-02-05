/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
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

import org.jboss.tools.project.examples.Messages;

/**
 * @author snjeza
 * 
 */
public class ProjectExampleCategory implements ProjectModelElement, Comparable<ProjectExampleCategory> {

	private String name;
	private List<ProjectExample> projects = new ArrayList<ProjectExample>();
	private IProjectExampleSite site;
	private String description;
	private int priority;
	public static ProjectExampleCategory OTHER = new ProjectExampleCategory(Messages.Category_Other);

	public ProjectExampleCategory(String name) {
		super();
		this.name = name;
	}

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

	public String getDescription() {
		return description;
	}

	public String getShortDescription() {
		return getName();
	}

	public IProjectExampleSite getSite() {
		return site;
	}

	@Override
	public void setSite(IProjectExampleSite site) {
		this.site = site;
	}

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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((site == null) ? 0 : site.hashCode());
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
		if (site == null) {
			if (other.site != null)
				return false;
		} else if (!site.equals(other.site))
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
		return name.compareTo(o.getName());
	}

	@Override
	public String toString() {
		return "ProjectExampleCategory [name=" + name + ", description=" //$NON-NLS-1$ //$NON-NLS-2$
				+ description + ", site=" + site + ", priority=" + priority  //$NON-NLS-1$//$NON-NLS-2$
				+ "]"; //$NON-NLS-1$
	}

}
