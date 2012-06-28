package org.jboss.tools.central.test.ui.bot;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotFactory;
import org.jboss.tools.ui.bot.ext.SWTFormsBotExt;
import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.Timing;
import org.jboss.tools.ui.bot.ext.condition.NonSystemJobRunsCondition;
import org.jboss.tools.ui.bot.ext.condition.TaskDuration;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.config.Annotations.ServerType;
import org.jboss.tools.ui.bot.ext.parts.SWTBotFormTextExt;
import org.jboss.tools.ui.bot.ext.parts.SWTBotTwistie;
import org.jboss.tools.ui.bot.ext.types.IDELabel;
import org.jboss.tools.ui.bot.ext.view.ProblemsView;
import org.jboss.tools.ui.bot.ext.wizards.SWTBotWizard;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;


//TODO When testing new build try it with type=ServerType.EAP !!!!
@Require(clearProjects=false,server=@org.jboss.tools.ui.bot.ext.config.Annotations.Server(type=ServerType.JbossAS))
public class CreateProjectsWithServerTest extends SWTTestExt{
	
	@BeforeClass
	public static void setup() throws FileNotFoundException{
		util.closeAllEditors(false);
		util.closeAllViews();
		bot.menu("Window").menu("Preferences").click();
		bot.waitForShell("Preferences");
		bot.tree().getTreeItem("Maven").select();
		bot.checkBox("Download repository index updates on startup").deselect();
		bot.clickButton("OK");
		bot.menu("Help").menu(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).click();
		util.waitForAll();
		if (configuredState.getServer().type.equalsIgnoreCase("EAP")){
			setupEAP();
		}
	}
	/**
	 * Sets up maven configuration file with configured EAP and WFK repository and clears ~/.m2/clean-repository 
	 * Clears ~/.m2/clean-repository if exists
	 * @throws FileNotFoundException 
	 */
	private static void setupEAP() throws FileNotFoundException{
		String mvnConfigFileName = System.getProperty("eap.maven.config.file");
		File mvnConfigFile;
		try {
			mvnConfigFile = new File(mvnConfigFileName);
		} catch (NullPointerException e) {
			throw new NullPointerException("eap.maven.config.file wasn't set");
		}
		if (!mvnConfigFile.exists()) throw new FileNotFoundException("File configured in eap.maven.config.file " +
				"property does not exist");
		File mvnLocalRepo = new File(System.getProperty("user.home")+"/.m2/clean-repository");
		if (mvnLocalRepo.exists()){
			deleteDirectory(mvnLocalRepo);
		}
		//Now is ~/.m2/clean-repository deleted and settings.xml exists. Next step is to tell eclipse to use our settings.xml
		bot.menu("Window").menu("Preferences").click();
		bot.waitForShell("Preferences");
		bot.tree().expandNode("Maven").select("User Settings");
		bot.text(1).setText(mvnConfigFileName);
		bot.clickButton("Update Settings");
		util.waitForNonIgnoredJobs();
		bot.clickButton("Apply");
		bot.clickButton("OK");
		util.waitForNonIgnoredJobs();
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
	
	@After
	public void teardown(){
		servers.removeAllProjectsFromServer("AS-7.0");
		/*SWTBotTreeItem[] items = ProblemsView.getErrorsNode(bot).getItems();
		for (SWTBotTreeItem swtBotTreeItem : items) {
			log.error(swtBotTreeItem.toString());
		}*/
	}
	
	@Test
	public void dummyTest(){
		
	}
	
	@Test
	public void createProjectsSectionTest(){
		//Dynamic web project
		bot.hyperlink(IDELabel.JBossCentralEditor.DYNAMIC_WEB_PROJECT).click();
		bot.waitForShell(IDELabel.JBossCentralEditor.NEW_DYNAMIC_WEB_PROJECT);
		assertTrue("New Dynamic Web Project should have appeared", bot.shell(IDELabel.JBossCentralEditor.NEW_DYNAMIC_WEB_PROJECT).isActive());
		bot.activeShell().close();
		//Openshift app
		log.info(bot.activeShell().getText());
		
		//TODO will put back when IDELabel.JBossCentralEditor.OPENSHIFT_APP_WIZARD will be correct
		bot.hyperlink(IDELabel.JBossCentralEditor.OPENSHIFT_APP).click();
		bot.waitForShell(IDELabel.JBossCentralEditor.OPENSHIFT_APP_WIZARD);
		bot.waitWhile(new NonSystemJobRunsCondition());
		assertTrue("New OpenShift Express Application window should have appeared", bot.activeShell().getText().equals(IDELabel.JBossCentralEditor.OPENSHIFT_APP_WIZARD));
		//assertTrue("New OpenShift Express Application window should have appeared", bot.shell(IDELabel.JBossCentralEditor.OPENSHIFT_APP_WIZARD).isActive());
		bot.activeShell().close();
		bot.waitWhile(new NonSystemJobRunsCondition());
		bot.hyperlink(IDELabel.JBossCentralEditor.JAVA_EE_WEB_PROJECT).click();
		SWTBotShell projectExampleShell = bot.waitForShell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE);
		assertTrue("Project Example window should have appeared", bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).isActive());
		try{
			bot.clickButton("Install");
			fail("Button \"Install\" should not be enabled, because all requirements should have been met");
		}catch(WidgetNotFoundException wnfex){
			//ok
		}
		projectExampleShell.activate();
		assertFalse("Button \"Download and Install...\" should not be enabled, because all requirements should have been met, condition", bot.button("Download and Install...").isEnabled());
		projectExampleShell.close();
		
		//check the rest of project examples
		checkExample(null, IDELabel.JBossCentralEditor.JAVA_EE_WEB_PROJECT, true);
		checkExample(null, IDELabel.JBossCentralEditor.JAVA_EE_PROJECT, true);
		checkExample(null, IDELabel.JBossCentralEditor.HTML5_PROJECT, true);
		checkExample(null, IDELabel.JBossCentralEditor.RICHFACES_PROJECT, true);
		checkExample(null, IDELabel.JBossCentralEditor.SPRING_MVC_PROJECT, true);
		
		bot.toolbarDropDownButtonWithTooltip("New").click();
		bot.waitForShell("New");
		assertTrue("Shell \"New\" should have appeared", bot.shell("New").isActive());
		bot.activeShell().close();
		canBeDeployedTest();
		projectExplorer.deleteAllProjects();
	}
	
	@Test
	public void projectExamplesSectionTest(){
		SWTBotTwistie twistieBot = bot.twistieByLabel("JBoss Quickstarts");
		while (!twistieBot.isExpanded()){
			twistieBot.toggle();
		}
		/*if (!twistieBot.isExpanded()){
			twistieBot.toggle();
		}*/
		SWTFormsBotExt formsBot = SWTBotFactory.getFormsBot();
		checkExample(formsBot, "Helloworld", true);
		checkExample(formsBot, "Numberguess", true);
		checkExample(formsBot, "Login", true, "login.xml"); //Login example ma nejaky divny login.xml cheatsheet
		checkExample(formsBot, "Kitchensink", true);
		checkExample(formsBot, "HTML5", true);
		canBeDeployedTest();
		projectExplorer.deleteAllProjects();
	}
	
	/**
	 * Tries to deploy all projects
	 */
	public void canBeDeployedTest(){
		servers.show();
		try{
			bot.viewByTitle("Project Explorer").close();
		}catch (WidgetNotFoundException ex){
			//do nothing
			log.info("Project Explorer is already closed");  
		}
		try {
			bot.viewByTitle("Problems").close();
		}catch (WidgetNotFoundException ex){
			//let be...
		}
		String serverName = bot.tree().getAllItems()[0].getText().substring(0, bot.tree().getAllItems()[0].getText().indexOf(' '));
		servers.findServerByName(servers.bot().tree(), serverName).contextMenu("Add and Remove...").click();
		bot.shell("Add and Remove...").activate();
		for (SWTBotTreeItem treeItem : bot.tree().getAllItems()) {
			treeItem.select();
			log.info("Adding "+treeItem.getText()+" to server");
			bot.clickButton("Add >");
				log.info("Succesfully added");
		}
		bot.clickButton("Finish");
		servers.show();
		bot.waitWhile(new NonSystemJobRunsCondition(), TaskDuration.LONG.getTimeout());
		assertNull("Errors node should be null", ProblemsView.getErrorsNode(bot));
		servers.show();
		bot.waitWhile(new NonSystemJobRunsCondition());
		SWTBotTreeItem serverTreeItem = servers.findServerByName(servers.bot().tree(), serverName).expand();
		bot.sleep(TIME_1S);
		for (SWTBotTreeItem projectName : projectExplorer.show().bot().tree().getAllItems()) {
			try{
				log.info("Testing project "+projectName.getText());
				serverTreeItem.getNode(projectName.getText()+"  [Started, Synchronized]");
				log.info("Project: "+projectName.getText()+" is properly deployed.");
			}catch (WidgetNotFoundException wnfe){
				//exception for Java EE Web project. It hase 4 projects, multi, multi-ear, multi-ejb and multi-web.
				if (!projectName.getText().contains("multi")){
					fail("Project <"+projectName.getText()+"> is not deployed on server correctly");
				}
			}
		}
		servers.removeProjectFromServers(serverName);
	}
	
	private void waitForAWhile(){
		bot.sleep(Long.MAX_VALUE);
	}
	

	/**
	 * 
	 * @param formsBot formBot==null => link is of type HyperLink else it is of type FormText
	 * @param formText
	 * @param readme true if readme should be shown
	 */
	
	private void checkExample(SWTFormsBotExt formsBot, String formText, boolean readme){
		checkExample(formsBot, formText, readme, null);
	}
	
	/**
	 * Checks example
	 * @param formsBot bot for Forms
	 * @param formText text to be clicked at
	 * @param readme true if readme is supposed to show, false otherwise
	 * @param readmeFileName 
	 */
	
	private void checkExample(SWTFormsBotExt formsBot, String formText, boolean readme, String readmeFileName){
		problems.show();
		if (formsBot==null){
			bot.hyperlink(formText).click();
		}else{
			try{
				formsBot.formTextWithText(formText).click();
			}catch(WidgetNotFoundException wnfex){
				throw new WidgetNotFoundException("Could not found widget of type Hyperlink and text " +
						formText, wnfex);
			}
		}
		bot.waitForShell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE);
		SWTBotWizard wizard = new SWTBotWizard(bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).widget);
		wizard.next();
		if (wizard.canNext()){
			bot.comboBox(2).setSelection(1);
			try{
				bot.link();
				fail("There is something wrong with maven repo. Message: \n"+bot.link().getText());
			}catch (WidgetNotFoundException ex){
				//everything fine
			}
			wizard.next();
		}
		wizard.finishWithWait();
		String readmeText = bot.checkBox(1).getText();
		assertFalse("Quick fix should not be enabled (Everything should be fine)", bot.checkBox(0).isEnabled());
		if (readme){
			assertTrue("Show readme checkbox should be enabled", bot.checkBox(1).isEnabled());
			assertTrue("Show readme checkbox should be checked by default", bot.checkBox(1).isChecked());
			if (readmeFileName != null){
				assertTrue(readmeText.toLowerCase().contains(readmeFileName));
				bot.clickButton("Finish");
				assertTrue("Cheat Sheets view should be opened right now", bot.activeView().getTitle().equals("Cheat Sheets"));
				bot.activeView().close();
			}else if (readmeText.contains("cheatsheet.xml")){
				bot.clickButton("Finish");
				assertTrue("Cheat Sheets view should be opened right now", bot.activeView().getTitle().equals("Cheat Sheets"));
				bot.activeView().close();
			}else if (readmeText.toLowerCase().contains("readme.md") || readmeText.toLowerCase().contains("readme.txt")){
				bot.clickButton("Finish");
				assertTrue("Readme should have opened in Text Editor", bot.activeEditor().getReference().getEditor(false).getClass().getName().contains("org.eclipse.ui.editors.text.TextEditor")); //because readmes are opening in browser now.. It's a bug. Jira is created.
				bot.activeEditor().close();
			}else if (readmeText.toLowerCase().contains("readme.htm")){
				bot.clickButton("Finish");
				assertTrue("Readme should have opened in Internal Browser", bot.activeEditor().getReference().getEditor(false).getClass().getName().contains("org.eclipse.ui.internal.browser.WebBrowserEditor"));
			}
		}else{
			bot.clickButton("Finish");
		}
	}
	
//	private void checkCreateProject(String formText){
//		//formsBot.formTextWithText(formText).click();
//		bot.hyperlink(formText).click();
//		bot.waitForShell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE);
//		SWTBotWizard wizard = new SWTBotWizard(bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).widget);
//		wizard.next();
//		wizard.finishWithWait();
//		bot.activeShell().close();
//		if (bot.activeEditor().getTitle().equalsIgnoreCase("cheat sheets")){
//			bot.activeEditor().close();
//		}
////		assertTrue("Project Example window should have appeared", bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).isActive());
////		bot.button("Start").click();
////		bot.waitForShell(wizzardShellText);
////		assertTrue(wizzardShellText+"  should have appeared", bot.shell(wizzardShellText).isActive());
////		bot.activeShell().close();
//	}
}
