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
