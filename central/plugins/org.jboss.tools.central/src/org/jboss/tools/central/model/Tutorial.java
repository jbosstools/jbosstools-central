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
package org.jboss.tools.central.model;

import org.jboss.tools.project.examples.model.Project;

/**
 * 
 * @author snjeza
 *
 */
public class Tutorial implements Comparable<Tutorial> {
	private String id;
	private String name;
	private String type;
	private String reference;
	private int priority;
	private TutorialCategory category;
	private Project projectExamples;
	private String description;
	private String iconPath;

	public Tutorial(String id, String name, String type, String reference,
			int priority, TutorialCategory category, String description, String iconPath) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.reference = reference;
		this.priority = priority;
		this.category = category;
		this.description = description;
		this.iconPath = iconPath;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public TutorialCategory getCategory() {
		return category;
	}

	public void setCategory(TutorialCategory category) {
		this.category = category;
	}

	public Project getProjectExamples() {
		return projectExamples;
	}

	public void setProjectExamples(Project projectExamples) {
		this.projectExamples = projectExamples;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Tutorial other = (Tutorial) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(Tutorial o) {
		if (o == null) {
			return -1;
		}
		TutorialCategory otherCategory = o.getCategory();
		if (otherCategory == null && this.category == null) {
			return 0;
		}
		if (this.category != null) {
			if (this.category.compareTo(otherCategory) != 0) {
				return this.category.compareTo(otherCategory);
			}
			int other = o.getPriority();
			if (other < this.priority) 
				return 1;
			else if (other > this.priority)
				return -1;
			return this.getId().compareTo(o.getId());
		}
		return -1;
	}

	@Override
	public String toString() {
		return "Tutorial [id=" + id + ", name=" + name + ", type=" + type
				+ ", reference=" + reference + ", priority=" + priority
				+ ", category=" + category + ", projectExamples="
				+ projectExamples + ", description=" + description
				+ ", iconPath=" + iconPath + "]";
	}

}
