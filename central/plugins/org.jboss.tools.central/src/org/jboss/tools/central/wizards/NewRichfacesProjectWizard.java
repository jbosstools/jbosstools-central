/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.wizards;

public class NewRichfacesProjectWizard extends AbstractJBossCentralProjectWizard {

	public NewRichfacesProjectWizard() {
		super("richfaces-archetype-simpleapp");
	}

	protected String getWizardBackgroundImagePath() {
		return "icons/richfaces_background.png";
	}
}
