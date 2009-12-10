package org.jboss.tools.maven.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.core.messages"; //$NON-NLS-1$
	public static String MavenFacetInstallDelegate_Internal_Error_creating_JBoss_Maven_Facet;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
