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

import java.util.Properties;

/**
 * @author snjeza
 * 
 */
public class ArchetypeModel implements Cloneable {
	private String groupId;
	private String artifactId;
	private String version;

	private String archetypeGroupId;
	private String archetypeArtifactId;
	private String archetypeVersion;
	private String archetypeRepository;
	private String javaPackage;
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
		return super.clone();
	}
}
