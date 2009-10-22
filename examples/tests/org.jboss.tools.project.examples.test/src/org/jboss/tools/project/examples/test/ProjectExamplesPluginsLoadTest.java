package org.jboss.tools.project.examples.test;

import org.jboss.tools.tests.AbstractPluginsLoadTest;

public class ProjectExamplesPluginsLoadTest extends AbstractPluginsLoadTest {
	public void testProjectExamplesPluginsAreResolvedAndActivated() {
		testBundlesAreLoadedFor("org.jboss.tools.project.examples.feature");
	}
}
