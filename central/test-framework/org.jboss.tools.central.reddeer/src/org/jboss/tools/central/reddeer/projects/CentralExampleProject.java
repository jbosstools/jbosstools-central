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
 * Class representing example project in JBoss Central in "Start from a sample"
 * section used in JBoss Central tests.
 * 
 * @author rhopp
 *
 */

public class CentralExampleProject extends Project {

	private String category;

	public CentralExampleProject(String name, String projectName,
			String category) {
		super(name, projectName);
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
