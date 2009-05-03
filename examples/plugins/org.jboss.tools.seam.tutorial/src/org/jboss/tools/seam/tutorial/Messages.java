package org.jboss.tools.seam.tutorial;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.seam.tutorial.messages"; //$NON-NLS-1$
	public static String OpenFileInEditor_Cannot_open;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
