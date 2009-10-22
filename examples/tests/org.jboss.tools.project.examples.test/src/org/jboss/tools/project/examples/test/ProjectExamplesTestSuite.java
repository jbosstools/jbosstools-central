package org.jboss.tools.project.examples.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ProjectExamplesTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ProjectExamplesPluginsLoadTest.class);
		return suite;
	}
}
