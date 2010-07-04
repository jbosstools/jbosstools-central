package org.jboss.tools.maven.seam;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.maven.seam.messages"; //$NON-NLS-1$
	public static String MavenSeamActivator_Cannot_get_seam_runtime;
	public static String MavenSeamActivator_The_file_does_not_exist;
	public static String MavenSeamActivator_The_folder_does_not_exist;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
