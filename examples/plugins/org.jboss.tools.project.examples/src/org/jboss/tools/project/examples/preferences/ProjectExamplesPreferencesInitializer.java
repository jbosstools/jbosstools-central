package org.jboss.tools.project.examples.preferences;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.jboss.tools.project.examples.ProjectExamplesActivator;

public class ProjectExamplesPreferencesInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope().getNode("org.jboss.tools.project.examples"); //$NON-NLS-1$
		
		node.putBoolean(
				ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES,
				ProjectExamplesActivator.SHOW_EXPERIMENTAL_SITES_VALUE);
	}

}
