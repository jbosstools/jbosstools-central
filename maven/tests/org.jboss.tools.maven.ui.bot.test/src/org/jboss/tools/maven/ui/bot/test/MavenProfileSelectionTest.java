package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class MavenProfileSelectionTest extends AbstractMavenSWTBotTest {
	
	public static final String AUTOACTIVATED_PROFILE_IN_POM = "active-profile";
	public static final String AUTOACTIVATED_PROFILE_IN_USER_SETTINGS = "environment";
	
	
	
	@Test
	public void testOpenMavenProfiles() throws Exception {
		IProject project = importProject("projects/simple-jar/pom.xml");
		waitForJobsToComplete();
		//Select the project
		testAutoActivatedProfiles();
		final SWTBotView packageExplorer = bot.viewByTitle("Project Explorer");
		SWTBot innerBot = packageExplorer.bot();
		innerBot.activeShell().activate();
		SWTBotTree tree = innerBot.tree();
		SWTBotTreeItem projectItem = tree.getTreeItem(project.getName());
		projectItem.select();
		openProfilesDialog(projectItem);
		Thread.sleep(2000);
	    //activate all profiles
		SWTBot shell = bot.shell("Select Maven profiles").activate().bot();
		shell.button("Select All").click();
	    String selectedProfiles = shell.textWithLabel("Active profiles for simple-jar :").getText();
	    shell.button("OK").click();
	   
	    testActivatedProfiles(project.getName(), selectedProfiles);
	    Thread.sleep(1000);
	    openProfilesDialog(projectItem);
	    
	    //disable all profiles
	    shell = bot.shell("Select Maven profiles").activate().bot();
	    shell.button("Deselect all").click();
	    selectedProfiles = bot.textWithLabel("Active profiles for simple-jar :").getText();
	    bot.button("OK").click();
	    
	    testActivatedProfiles(project.getName(), selectedProfiles);
	}
	
	private void openProfilesDialog(SWTBotTreeItem projectItem) throws ParseException, InterruptedException{
		projectItem.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));
		projectItem.pressShortcut(Keystrokes.DOWN);
		projectItem.pressShortcut(Keystrokes.LF);
		final SWTBotShell selectDialogShell = bot.shell("Select Maven profiles");
	    assertEquals("Select Maven profiles", selectDialogShell.getText());
	    Thread.sleep(1000);
	}
	
	private void testActivatedProfiles(String projectName, String profilesToCheck){
		
	    SWTBot explorer = bot.viewByTitle("Project Explorer").bot();
	    SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
	    item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
	    SWTBot shell = bot.shell("Properties for "+projectName).activate().bot();
	    shell.tree().select("Maven");
	    assertEquals("Selected profiles doesn't match", shell.textWithLabel("Active Maven Profiles (comma separated):").getText(),profilesToCheck);
	    shell.button("OK").click();
	    
	}
	
	private void testAutoActivatedProfiles(){
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools.maven.tests", "simple-jar", "1.0.0-SNAPSHOT");
	    assertNotNull("facade is null",facade);
	    facade.getMavenProject().getActiveProfiles().get(0);
	    assertEquals("Auto Activated profiles from pom.xml doesn't match", AUTOACTIVATED_PROFILE_IN_POM, facade.getMavenProject().getActiveProfiles().get(0).getId());
	    assertEquals("Auto Activated profiles from settings.xml doesn't match", AUTOACTIVATED_PROFILE_IN_USER_SETTINGS, facade.getMavenProject().getActiveProfiles().get(1).getId());
	}
}
