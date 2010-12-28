package org.jboss.tools.maven.ui.bot.test;

import org.jboss.tools.ui.bot.ext.RequirementAwareSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * This is a swtbot testcase for an eclipse application.
 * 
 */

@RunWith(RequirementAwareSuite.class)
@Suite.SuiteClasses({
	CreateMavenizedSeamProjectTest.class,
})
public class MavenAllBotTests {

}