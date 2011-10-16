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

import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author snjeza
 *
 */
public class TutorialCategory implements Comparable<TutorialCategory> {

	private String id;
	private String name;
	private int priority;
	private Set<Tutorial> tutorials = new TreeSet<Tutorial>();

	public TutorialCategory() {
	}
	
	public TutorialCategory(String id, String name, int priority) {
		this.id = id;
		this.name = name;
		this.priority = priority;
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

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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
		TutorialCategory other = (TutorialCategory) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TutorialCategory [id=" + id + ", name=" + name + ", priority="
				+ priority + "]";
	}

	@Override
	public int compareTo(TutorialCategory o) {
		if (o == null) 
			return 1;
		int other = o.getPriority();
		if (other < this.priority) 
			return 1;
		else if (other > this.priority)
			return -1;
		return id.compareTo(o.getId());
	}

	public Set<Tutorial> getTutorials() {
		return tutorials;
	}

}
