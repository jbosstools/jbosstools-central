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
 * Class representing archetype project in JBoss Central used in JBoss Central
 * tests.
 * 
 * @author rhopp
 *
 */

public class ArchetypeProject extends Project {

	boolean blank;

	public ArchetypeProject(String name, String projectName, boolean blank) {
		super(name, projectName);
		this.blank = blank;
	}

	public void setBlank(boolean blank) {
		this.blank = blank;
	}

	public boolean isBlank() {
		return blank;
	}

}
