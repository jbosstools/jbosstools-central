/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.central.reddeer.projects;

/**
 * Class representing general project used in JBoss Central Tests
 * 
 * @author rhopp
 *
 */


public class Project {

	protected String name;
	
	protected String projectName;
	
	protected String packageName;
	
	public Project(String name, String projectName, String packageName) {
		this.name = name;
		this.projectName = projectName;
		this.packageName = packageName;
	}
	
	public Project(String name, String projectName){
		this(name, projectName, "");
	}
	
	public String getName() {
		return name;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getReadmeString(){
		return "README.md";
	}
	
}
