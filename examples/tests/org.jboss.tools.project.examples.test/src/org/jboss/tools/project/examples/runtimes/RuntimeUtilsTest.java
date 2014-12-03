package org.jboss.tools.project.examples.runtimes;

import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_HOME;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_RUNTIME_ID;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_RUNTIME_NAME;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_SERVER_ID;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.JBOSS_AS_SERVER_NAME;
import static org.jboss.tools.project.examples.tests.ProjectExamplesTestUtil.createJBossServer;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class RuntimeUtilsTest {

	@Before
	public void setUp() throws Exception {
		createJBossServer(new File(JBOSS_AS_HOME), JBOSS_AS_SERVER_ID, JBOSS_AS_RUNTIME_ID, JBOSS_AS_SERVER_NAME, JBOSS_AS_RUNTIME_NAME);
	}
	
	@Test
	public void testGetInstalledRuntimeTypes() {
		assertEquals(1, RuntimeUtils.getInstalledRuntimeTypes().size());
	}
}
