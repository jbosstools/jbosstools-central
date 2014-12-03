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

import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.tools.project.examples.internal.model.XmlUnMarshallers.ArchetypePropertyUnMarshaller;

/**
 * @author snjeza
 * 
 */
@XmlAccessorType (XmlAccessType.FIELD)
@XmlRootElement(name = "mavenArchetype")
public class ArchetypeModel implements Cloneable {
	private String groupId;
	private String artifactId;
	private String version;

	private String archetypeGroupId;
	private String archetypeArtifactId;
	private String archetypeVersion;
	private String archetypeRepository;
	private String javaPackage;
	
	@XmlJavaTypeAdapter(ArchetypePropertyUnMarshaller.class)
	@XmlElement(name="properties")
	private Properties archetypeProperties = new Properties();

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getArchetypeGroupId() {
		return archetypeGroupId;
	}

	public void setArchetypeGroupId(String archetypeGroupId) {
		this.archetypeGroupId = archetypeGroupId;
	}

	public String getArchetypeArtifactId() {
		return archetypeArtifactId;
	}

	public void setArchetypeArtifactId(String archetypeArtifactId) {
		this.archetypeArtifactId = archetypeArtifactId;
	}

	public String getArchetypeVersion() {
		return archetypeVersion;
	}

	public void setArchetypeVersion(String archetypeVersion) {
		this.archetypeVersion = archetypeVersion;
	}

	public String getArchetypeRepository() {
		return archetypeRepository;
	}

	public void setArchetypeRepository(String archetypeRepository) {
		this.archetypeRepository = archetypeRepository;
	}

	public String getJavaPackage() {
		return javaPackage;
	}

	public void setJavaPackage(String javaPackage) {
		this.javaPackage = javaPackage;
	}

	public Properties getArchetypeProperties() {
		return archetypeProperties;
	}

	public void addProperty(String key, String value) {
		archetypeProperties.put(key, value);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ArchetypeModel clone = (ArchetypeModel)super.clone();
		if (archetypeProperties != null) {
			clone.archetypeProperties = (Properties)archetypeProperties.clone();
		}
		return clone;
	}
	
	/**
	 * Returns the archetype model's coordinates as groupId:artifactId:version
	 * @return groupId:artifactId:version
	 * @since 2.0.0
	 */
	public String getGAV() {
		StringBuilder sb = new StringBuilder();
		sb.append(getArchetypeGroupId())
		.append(":") //$NON-NLS-1$
		.append(getArchetypeArtifactId())
		.append(":") //$NON-NLS-1$
		.append(getArchetypeVersion());
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((archetypeArtifactId == null) ? 0 : archetypeArtifactId
						.hashCode());
		result = prime
				* result
				+ ((archetypeGroupId == null) ? 0 : archetypeGroupId.hashCode());
		result = prime
				* result
				+ ((archetypeProperties == null) ? 0 : archetypeProperties
						.hashCode());
		result = prime
				* result
				+ ((archetypeRepository == null) ? 0 : archetypeRepository
						.hashCode());
		result = prime
				* result
				+ ((archetypeVersion == null) ? 0 : archetypeVersion.hashCode());
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result
				+ ((javaPackage == null) ? 0 : javaPackage.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		ArchetypeModel other = (ArchetypeModel) obj;
		if (archetypeArtifactId == null) {
			if (other.archetypeArtifactId != null)
				return false;
		} else if (!archetypeArtifactId.equals(other.archetypeArtifactId))
			return false;
		if (archetypeGroupId == null) {
			if (other.archetypeGroupId != null)
				return false;
		} else if (!archetypeGroupId.equals(other.archetypeGroupId))
			return false;
		if (archetypeProperties == null) {
			if (other.archetypeProperties != null)
				return false;
		} else if (!archetypeProperties.equals(other.archetypeProperties))
			return false;
		if (archetypeRepository == null) {
			if (other.archetypeRepository != null)
				return false;
		} else if (!archetypeRepository.equals(other.archetypeRepository))
			return false;
		if (archetypeVersion == null) {
			if (other.archetypeVersion != null)
				return false;
		} else if (!archetypeVersion.equals(other.archetypeVersion))
			return false;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (javaPackage == null) {
			if (other.javaPackage != null)
				return false;
		} else if (!javaPackage.equals(other.javaPackage))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
