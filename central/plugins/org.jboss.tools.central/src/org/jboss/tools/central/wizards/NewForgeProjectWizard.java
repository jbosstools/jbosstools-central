/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.wizards;

public class NewForgeProjectWizard extends AbstractJBossCentralProjectWizard {

	public NewForgeProjectWizard() {
		super("jboss-forge-html5-archetype");
	}

	protected String getWizardBackgroundImagePath() {
		return "icons/forge.png";
	}
}
