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
package org.jboss.tools.maven.sourcelookup.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;

public class SourceLookupPreferencesInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = SourceLookupActivator.getDefault().getPreferenceStore();
		store.setDefault(
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT,
				SourceLookupActivator.AUTO_ADD_JBOSS_SOURCE_ATTACHMENT_PROMPT);
	}

}
