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
 * Represents example project which can be imported using File->New->Project Examples
 * 
 * @author rhopp
 *
 */

public class ExampleProject extends Project {

	private String[] path;
	
	public ExampleProject(String name, String projectName, String... path) {
		super(name, projectName);
		this.path = path;
	}

	public String[] getPath() {
		return path;
	}

	public void setPath(String[] path) {
		this.path = path;
	}

}
