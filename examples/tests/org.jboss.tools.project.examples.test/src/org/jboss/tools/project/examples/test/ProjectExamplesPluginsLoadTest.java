package org.jboss.tools.project.examples.test;

import org.eclipse.core.runtime.Platform;
import org.jboss.tools.tests.AbstractPluginsLoadTest;

public class ProjectExamplesPluginsLoadTest extends AbstractPluginsLoadTest {
	public void testOrgJbossToolsProjectExamplesAreResolvedAndActivated() {
		assertPluginResolved(Platform.getBundle("org.jboss.tools.project.examples"));
	}
}
