/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.jboss.tools.maven.ui.Activator;

/**
 * 
 * @author snjeza
 *
 */
public class ConfiguratorPreferencesInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope().getNode(Activator.PLUGIN_ID);
		
		node.putBoolean(
				Activator.CONFIGURE_SEAM,
				Activator.CONFIGURE_SEAM_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_SEAM_RUNTIME,
				Activator.CONFIGURE_SEAM_RUNTIME_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_SEAM_ARTIFACTS,
				Activator.CONFIGURE_SEAM_ARTIFACTS_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_JSF,
				Activator.CONFIGURE_JSF_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_WEBXML_JSF20,
				Activator.CONFIGURE_WEBXML_JSF20_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_PORTLET,
				Activator.CONFIGURE_PORTLET_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_JSFPORTLET,
				Activator.CONFIGURE_JSFPORTLET_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_SEAMPORTLET,
				Activator.CONFIGURE_SEAMPORTLET_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_CDI,
				Activator.CONFIGURE_CDI_VALUE);
		node.putBoolean(
				Activator.CONFIGURE_HIBERNATE,
				Activator.CONFIGURE_HIBERNATE_VALUE);
	}

}
