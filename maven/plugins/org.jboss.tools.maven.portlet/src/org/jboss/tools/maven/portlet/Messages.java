package org.jboss.tools.maven.portlet;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.jsf.messages"; //$NON-NLS-1$
	public static String PortletProjectConfigurator_The_project_does_not_contain_the_Web_Module_facet;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
