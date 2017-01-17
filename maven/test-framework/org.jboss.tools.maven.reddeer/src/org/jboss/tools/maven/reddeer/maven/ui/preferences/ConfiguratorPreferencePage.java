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
package org.jboss.tools.maven.reddeer.maven.ui.preferences;

import org.jboss.reddeer.jface.preference.PreferencePage;
import org.jboss.tools.maven.reddeer.wizards.ConfigureMavenRepositoriesWizard;

public class ConfiguratorPreferencePage extends PreferencePage{
	
	public ConfiguratorPreferencePage(){
		super("JBoss Tools","JBoss Maven Integration");
	}
	
	public ConfigureMavenRepositoriesWizard configureRepositories(){
		ConfigureMavenRepositoriesWizard cw = new ConfigureMavenRepositoriesWizard();
		cw.open();
		return cw;
	}

}
