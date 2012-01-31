package org.jboss.tools.central.test.ui.bot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.awt.Button;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.ui.forms.widgets.Twistie;
import org.hamcrest.core.IsAnything;
import org.jboss.tools.ui.bot.ext.SWTBotFactory;
import org.jboss.tools.ui.bot.ext.SWTFormsBotExt;
import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.config.Annotations.ServerType;
import org.jboss.tools.ui.bot.ext.config.ConfiguredState.Server;
import org.jboss.tools.ui.bot.ext.gen.ActionItem;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.Perspective.JBOSSAS;
import org.jboss.tools.ui.bot.ext.gen.ActionItem.Server.JBossCommunityJBossAS71;
import org.jboss.tools.ui.bot.ext.parts.SWTBotEditorExt;
import org.jboss.tools.ui.bot.ext.parts.SWTBotTwistie;
import org.jboss.tools.ui.bot.ext.types.IDELabel;
import org.jboss.tools.ui.bot.ext.types.JobState;
import org.jboss.tools.ui.bot.ext.widgets.SWTBotSection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.org.apache.xml.internal.serializer.utils.Utils;

//@Require(server=@org.jboss.tools.ui.bot.ext.config.Annotations.Server(type=ServerType.JbossAS))
public class CreateProjectsTest extends SWTTestExt{
	
	private static final String JBOSS_INSTALL_PATH = "/tmp/jbossAS";
	
	@BeforeClass
	public static void setup(){
		log.info(configuredState.getServer().name);
		bot.menu("Help").menu(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).click();
		util.waitForAll();
	}
	@AfterClass
	public static void teardown(){
		deleteDirectory(new File(JBOSS_INSTALL_PATH));
	}
	
	@Test
	public void createProjectsSectionTest(){
		//waitForAWhile();
		SWTFormsBotExt formsBot = SWTBotFactory.getFormsBot();
		//Dynamic web project
		bot.hyperlink(IDELabel.JBossCentralEditor.DYNAMIC_WEB_PROJECT).click();
		bot.waitForShell(IDELabel.JBossCentralEditor.NEW_DYNAMIC_WEB_PROJECT);
		assertTrue("New Dynamic Web Project should have appeared", bot.shell(IDELabel.JBossCentralEditor.NEW_DYNAMIC_WEB_PROJECT).isActive());
		bot.activeShell().close();
		//Openshift app
		bot.hyperlink(IDELabel.JBossCentralEditor.OPENSHIFT_APP).click();
		bot.waitForShell(IDELabel.JBossCentralEditor.OPENSHIFT_APP_WIZARD);
		assertTrue("New Dynamic Web Project should have appeared", bot.shell(IDELabel.JBossCentralEditor.OPENSHIFT_APP_WIZARD).isActive());
		bot.activeShell().close();
		
		//check Project example and detection of server
		formsBot.formTextWithText(IDELabel.JBossCentralEditor.JAVA_EE_WEB_PROJECT).click();
		SWTBotShell projectExampleShell = bot.waitForShell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE);
		assertTrue("Project Example window should have appeared", bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).isActive());
		try{
			bot.clickButton("Install");
			SWTBotShell shell = bot.waitForShell(IDELabel.Menu.PREFERENCES);
			if (shell == null){
				fail("Preferences shell should have appeared");
			}
			bot.activeShell().close();
		}catch(WidgetNotFoundException wnfex){
			fail("Missing Install button");
		}
		try{
			projectExampleShell.activate();
			bot.clickButton("Download and Install...");
		}catch(WidgetNotFoundException wnfex){
			fail("Missing \"Download and Install\" button");
		}
		
		//create direcotry where will be JBossAS downloaded
		if(!createDirectory(JBOSS_INSTALL_PATH)){
			fail("Unable to create direcory for JBoss - \""+JBOSS_INSTALL_PATH+"\"");
		}
		
		bot.textWithLabel("Install folder:").setText(JBOSS_INSTALL_PATH);
		bot.textWithLabel("Download folder:").setText("/tmp");
		bot.clickButton("OK");
		bot.waitForShell("Progress Information");
		bot.sleep(TIME_1S);
		util.waitForAll(Long.MAX_VALUE);
		//bot.waitUntil(Conditions.shellCloses(bot.activeShell()), Long.MAX_VALUE, TIME_5S);
		projectExampleShell.close();
		bot.sleep(TIME_1S);
		
		//server should be added.. check again
		formsBot.formTextWithText(IDELabel.JBossCentralEditor.JAVA_EE_WEB_PROJECT).click();
		projectExampleShell = bot.waitForShell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE);
		assertTrue("Project Example window should have appeared", bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).isActive());
		try{
			bot.clickButton("Install");
			fail("Button \"Install\" should not be enabled, because all requirements should have been met");
		}catch(WidgetNotFoundException wnfex){
			//ok
		}
		try{
			projectExampleShell.activate();
			bot.clickButton("Download and Install...");
			fail("Button \"Download and Install...\" should not be enabled, because all requirements should have been met");
		}catch(WidgetNotFoundException wnfex){
			//ok
		}
		projectExampleShell.close();
		
		//check the rest of project examples
		checkCreateProject(formsBot, IDELabel.JBossCentralEditor.JAVA_EE_WEB_PROJECT, IDELabel.JBossCentralEditor.NEW_JBOSS_PROJECT);
		checkCreateProject(formsBot, IDELabel.JBossCentralEditor.JAVA_EE_PROJECT, IDELabel.JBossCentralEditor.NEW_JBOSS_PROJECT);
		checkCreateProject(formsBot, IDELabel.JBossCentralEditor.HTML5_PROJECT, IDELabel.JBossCentralEditor.NEW_JBOSS_PROJECT);
		checkCreateProject(formsBot, IDELabel.JBossCentralEditor.SPRING_MVC_PROJECT, IDELabel.JBossCentralEditor.NEW_JBOSS_PROJECT);
		checkCreateProject(formsBot, IDELabel.JBossCentralEditor.RICHFACES_PROJECT, IDELabel.JBossCentralEditor.NEW_JBOSS_PROJECT);
		bot.toolbarDropDownButtonWithTooltip("New").click();
		bot.waitForShell("New");
		assertTrue("Shell \"New\" should have appeared", bot.shell("New").isActive());
		bot.activeShell().close();
	}
	
	@Test
	public void projectExamplesSectionTest(){
		//SWTBotSection section = bot.section("Project Examples");
		SWTBotTwistie twistieBot = bot.twistieByLabel("JBoss Quickstarts");
		if (!twistieBot.isExpanded()){
			twistieBot.toggle();
		}
		SWTFormsBotExt formsBot = SWTBotFactory.getFormsBot();
		formsBot.formTextWithText("Helloworld").click();
		bot.clickButton("Start");
		waitForAllMultipleTimes(3);
		formsBot.formTextWithText("Numberguess").click();
		bot.clickButton("Start");
		waitForAllMultipleTimes(3);
		log.info(bot.activeEditor().getTitle());
		formsBot.formTextWithText("Login").click();
		bot.clickButton("Start");
		waitForAllMultipleTimes(3);
		formsBot.formTextWithText("Kitchensink").click();
		bot.clickButton("Start");
		waitForAllMultipleTimes(3);
	}
	
	/**
	 * calls method waitForAll x times
	 * @param value how many times waitForAll should be called
	 */
	private void waitForAllMultipleTimes(int value){
		for (int i=0; i<value; i++){
			util.waitForAll();
		}
	}
	
	private void waitForAWhile(){
		bot.sleep(Long.MAX_VALUE);
	}
	
	private boolean createDirectory(String path){
		if (new File(path).mkdir()){
			return true;
		}
		return false;
	}
	
	private static boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }
	
	private void checkCreateProject(SWTFormsBotExt formsBot, String formText, String wizzardShellText){
		formsBot.formTextWithText(formText).click();
		bot.waitForShell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE);
		assertTrue("Project Example window should have appeared", bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).isActive());
		bot.button("Start").click();
		bot.waitForShell(wizzardShellText);
		assertTrue(wizzardShellText+"  should have appeared", bot.shell(wizzardShellText).isActive());
		bot.activeShell().close();
	}
}
