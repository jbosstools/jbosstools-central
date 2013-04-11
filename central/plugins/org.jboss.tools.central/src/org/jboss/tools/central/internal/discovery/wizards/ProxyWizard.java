/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards;

import java.net.URL;
import java.util.List;

/**
 * 
 * @author Fred Bricon
 */
public class ProxyWizard implements Comparable<ProxyWizard> {

	String id;
	String wizardId;
	String label;
	String description;
	URL iconUrl;
	List<String> requiredComponentIds;
	int priority;

	public String getId() {
		return id;
	}

	public ProxyWizard setId(String id) {
		this.id = id;
		return this;		
	}

	public String getWizardId() {
		return wizardId;
	}

	public ProxyWizard setWizardId(String wizardId) {
		this.wizardId = wizardId;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public ProxyWizard setLabel(String label) {
		this.label = label;
		return this;		
	}

	public String getDescription() {
		return description;
	}

	public ProxyWizard setDescription(String description) {
		this.description = description;
		return this;		
	}

	public URL getIconUrl() {
		return iconUrl;
	}

	public ProxyWizard setIconUrl(URL iconUrl) {
		this.iconUrl = iconUrl;
		return this;		
	}

	public List<String> getRequiredComponentIds() {
		return requiredComponentIds;
	}

	public ProxyWizard setRequiredComponentIds(List<String> requiredComponentIds) {
		this.requiredComponentIds = requiredComponentIds;
		return this;		
	}
	
	public int getPriority() {
		return priority;
	}

	public ProxyWizard setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	@Override
	public int compareTo(ProxyWizard other) {
		return (other == null)?1:(getPriority()-other.getPriority());
	}

	@Override
	public String toString() {
		return priority + " - "+label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((iconUrl == null) ? 0 : iconUrl.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + priority;
		result = prime
				* result
				+ ((requiredComponentIds == null) ? 0 : requiredComponentIds
						.hashCode());
		result = prime * result
				+ ((wizardId == null) ? 0 : wizardId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProxyWizard other = (ProxyWizard) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (iconUrl == null) {
			if (other.iconUrl != null) {
				return false;
			}
		} else if (!iconUrl.equals(other.iconUrl)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (priority != other.priority) {
			return false;
		}
		if (requiredComponentIds == null) {
			if (other.requiredComponentIds != null) {
				return false;
			}
		} else if (!requiredComponentIds.equals(other.requiredComponentIds)) {
			return false;
		}
		if (wizardId == null) {
			if (other.wizardId != null) {
				return false;
			}
		} else if (!wizardId.equals(other.wizardId)) {
			return false;
		}
		return true;
	}
}
