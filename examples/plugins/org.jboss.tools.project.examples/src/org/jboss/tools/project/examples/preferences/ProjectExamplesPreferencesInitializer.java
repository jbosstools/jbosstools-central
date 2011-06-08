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

package org.jboss.tools.project.examples.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.jboss.tools.project.examples.ProjectExamplesActivator;

/**
 * 
 * @author snjeza
 *
 */
public class ProjectExamplesPreferencesInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope().getNode("org.jboss.tools.project.examples"); //$NON-NLS-1$
		
		node.putBoolean(
				ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES,
				ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES_VALUE);
		
		node.putBoolean(
				ProjectExamplesActivator.SHOW_INVALID_SITES,
				ProjectExamplesActivator.SHOW_INVALID_SITES_VALUE);
	}

}
