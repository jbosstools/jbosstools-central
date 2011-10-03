package org.jboss.tools.project.examples.cheatsheet;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.project.examples.cheatsheet.messages"; //$NON-NLS-1$
	public static String OpenFileInEditor_Cannot_open;
	public static String LaunchJunitTest_The_project_does_not_exist;
	public static String RunProjectExample_Invalid_project_example;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
