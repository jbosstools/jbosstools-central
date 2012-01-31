package org.jboss.tools.maven.ui.bot.test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.Profile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@Require(perspective="Java")
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class MavenProfileSelectionTest extends AbstractMavenSWTBotTest {
	
	public static final String AUTOACTIVATED_PROFILE_IN_POM = "active-profile";
	public static final String AUTOACTIVATED_PROFILE_IN_USER_SETTINGS = "profile.from.settings.xml";
	public static final String COMMON_PROFILE = "common-profile";
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Package Explorer").click();
	}
	
	@Test
	public void testOpenMavenProfiles() throws Exception {
		setUserSettings();
		IProject project = importProject("projects/simple-jar/pom.xml");
		waitForJobsToComplete();
		testAutoActivatedProfiles();
		bot.menu("Window").menu("Show View").menu("Project Explorer").click();
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
	    System.out.println("+++++++++++++++++++++"+selectedProfiles);
	    shell.button("OK").click();
	   
	    testActivatedProfiles(project.getName(), selectedProfiles, false);
	    Thread.sleep(1000);
	    openProfilesDialog(projectItem);
	    
	    //disable all profiles
	    shell = bot.shell("Select Maven profiles").activate().bot();
	    shell.button("Deselect all").click();
	    selectedProfiles = bot.textWithLabel("Active profiles for simple-jar :").getText();
	    bot.button("OK").click();
	    
	    testActivatedProfiles(project.getName(), selectedProfiles, true);
	}
	
	@Test
	public void testOpenMultipleMavenProfiles() throws Exception{
		IProject project = importProject("projects/simple-jar/pom.xml");
		IProject project1 = importProject("projects/simple-jar1/pom.xml");
		IProject project2 = importProject("projects/simple-jar2/pom.xml");
		waitForJobsToComplete();
		final SWTBotView packageExplorer = bot.viewByTitle("Project Explorer");
		SWTBot innerBot = packageExplorer.bot();
		innerBot.activeShell().activate();
		SWTBotTree tree = innerBot.tree();
		tree.select("simple-jar","simple-jar1","simple-jar2").pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));
		SWTBot shell = bot.shell("Select Maven profiles").activate().bot();
		shell.button("Select All").click();
		shell.button("Activate").click();
		shell.button("OK").click();
		//testActivatedProfiles(project.getName(), COMMON_PROFILE+", "+AUTOACTIVATED_PROFILE_IN_USER_SETTINGS, false);
		testActivatedProfiles(project1.getName(), COMMON_PROFILE+", "+AUTOACTIVATED_PROFILE_IN_USER_SETTINGS, false);
		testActivatedProfiles(project2.getName(), COMMON_PROFILE+", "+AUTOACTIVATED_PROFILE_IN_USER_SETTINGS, false);
	}
	
	private void setUserSettings() throws InterruptedException, IOException, CoreException{
		SWTBotExt botExt = new SWTBotExt();
		botExt.menu("Window").menu("Preferences").click();
		botExt.tree().expandNode("Maven").select("User Settings").click();
		File f = new File("usersettings/settings.xml");
		botExt.text(1).setText(f.getAbsolutePath());
		botExt.button("OK").click();
	}
	
	private void openProfilesDialog(SWTBotTreeItem projectItem) throws ParseException, InterruptedException{
		projectItem.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));
		//projectItem.pressShortcut(Keystrokes.DOWN);
		//projectItem.pressShortcut(Keystrokes.LF);
		final SWTBotShell selectDialogShell = bot.shell("Select Maven profiles");
	    assertEquals("Select Maven profiles", selectDialogShell.getText());
	    Thread.sleep(1000);
	}
	
	private void testActivatedProfiles(String projectName, String expectedProfiles, boolean defaultProfile){	    
	    String[] parsedexpectedProfiles = expectedProfiles.split(", ");
	    String empty = "";
	    Set<String> setOfExpectedProfiles = new HashSet<String>();
	    Collections.addAll(setOfExpectedProfiles, parsedexpectedProfiles);
	    setOfExpectedProfiles.add(AUTOACTIVATED_PROFILE_IN_USER_SETTINGS);
	    if(defaultProfile){
	    	setOfExpectedProfiles.add(AUTOACTIVATED_PROFILE_IN_POM);
	    }
	    setOfExpectedProfiles.remove(empty);
	    
	    IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools.maven.tests", projectName, "1.0.0-SNAPSHOT");
		assertNotNull("facade is null",facade);
	    Set<String> setOfProfilesFacade = new HashSet<String>();
	    for(Profile profile : facade.getMavenProject().getActiveProfiles()){
	    	    setOfProfilesFacade.add(profile.getId());
	    }
	    assertEquals("Selected profiles in project " +projectName+ " doesn't match", setOfExpectedProfiles, setOfProfilesFacade);
	}
	
	private void testAutoActivatedProfiles(){
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools.maven.tests", "simple-jar", "1.0.0-SNAPSHOT");
	    assertNotNull("facade is null",facade);
	    assertEquals("Auto Activated profiles from pom.xml doesn't match", AUTOACTIVATED_PROFILE_IN_POM, facade.getMavenProject().getActiveProfiles().get(0).getId());
	    assertEquals("Auto Activated profiles from settings.xml doesn't match", AUTOACTIVATED_PROFILE_IN_USER_SETTINGS, facade.getMavenProject().getActiveProfiles().get(1).getId());
	}
}
