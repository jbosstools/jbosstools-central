/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;



/**
 * @author snjeza
 * 
 */

public interface IJBossMavenConstants {

	static final String M2_FACET_ID="jboss.m2";
	static final String ARTIFACT_ID = "artifactId";
	static final String GROUP_ID = "groupId";
	static final String VERSION = "version";
	static final String NAME = "name";
	static final String DESCRIPTION = "description";
	static final String PACKAGING = "packaging";
	static final String MAVEN_MODEL_VERSION = "4.0.0";
	static final String PROJECT_VERSION = "project.version";
	static final String SEAM_MAVEN_VERSION = "seamMavenVersion";
	static final String SEAM_VERSION = "seam.version";
	static final String REMOVE_WTP_CLASSPATH_CONTAINERS = "removeWTPClasspathContainers";
}