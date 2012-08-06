package org.jboss.tools.maven.ui.bot.test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.tests.common.JobHelpers;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@Require(perspective="Java")
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public class MavenProfileSelectionTest extends AbstractMavenSWTBotTest {
	
	public static final String AUTOACTIVATED_PROFILE_IN_POM = "active-profile";
	public static final String[] AUTOACTIVATED_PROFILES_IN_USER_SETTINGS = {"profile.from.settings.xml", "jboss"};
	public static final String[] COMMON_PROFILES = {"common-profile"};
	public static final String[] ALL_PROFILES = {"inactive-profile", "common-profile", "active-profile"};
	
	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@BeforeClass
	public static void setup() {
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Package Explorer").click();
	}
	
	@After
	public void after() throws InterruptedException, CoreException, IOException{
		 doCleanWorkspace();
	}
	
	@Test
	public void testOpenMavenProfiles() throws IOException, InterruptedException, CoreException, ParseException{
		importMavenProject("projects/simple-jar/pom.xml");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("simple-jar");
		waitForJobsToComplete();
		testAutoActivatedProfiles();
		bot.viewByTitle("Package Explorer").bot().tree().select("simple-jar").pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));
		waitForShell(botUtil,"Select Maven profiles");
		
		//activate all profiles
		bot.button("Select All").click();
	    bot.button("OK").click();
	    waitForIdle();
	    testActivatedProfiles(project.getName(), ALL_PROFILES);
		bot.viewByTitle("Package Explorer").bot().tree().select("simple-jar").pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));

	    
	    //disable all profiles
	    waitForShell(botUtil,"Select Maven profiles");
	    bot.button("Deselect all").click();
	    bot.button("OK").click();
	    waitForIdle();
	    testActivatedProfiles(project.getName(), null);
	}
	
	//@Test
	public void testOpenMultipleMavenProfiles() throws IOException, InterruptedException, CoreException, ParseException{
		importMavenProject("projects/simple-jar/pom.xml");
		importMavenProject("projects/simple-jar1/pom.xml");
		importMavenProject("projects/simple-jar2/pom.xml");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("simple-jar");
		IProject project1 = ResourcesPlugin.getWorkspace().getRoot().getProject("simple-jar1");
		IProject project2 = ResourcesPlugin.getWorkspace().getRoot().getProject("simple-jar2");
		waitForJobsToComplete();
		bot.viewByTitle("Package Explorer").bot().tree().select("simple-jar","simple-jar1","simple-jar2").pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));
		SWTBot shell = bot.shell("Select Maven profiles").activate().bot();
		shell.button("Select All").click();
		shell.button("Activate").click();
		shell.button("OK").click();
		waitForIdle();
		testActivatedProfiles(project.getName(), COMMON_PROFILES);
		testActivatedProfiles(project1.getName(), COMMON_PROFILES);
		testActivatedProfiles(project2.getName(), COMMON_PROFILES);
	}
	
	private void openProfilesDialog(SWTBotTreeItem projectItem) throws ParseException{
		projectItem.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT,KeyStroke.getInstance("P"));
		final SWTBotShell selectDialogShell = bot.shell("Select Maven profiles");
	    assertEquals("Select Maven profiles", selectDialogShell.getText());
	}
	
	private void testActivatedProfiles(String projectName, String[] expectedProfiles) {	 
	    Set<String> setOfExpectedProfiles = new HashSet<String>();
	    if(expectedProfiles != null){
	    	Collections.addAll(setOfExpectedProfiles, expectedProfiles);
	    	for(String act: AUTOACTIVATED_PROFILES_IN_USER_SETTINGS){
		    	setOfExpectedProfiles.add(act);
	    	}
	    }
	    IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools.maven.tests", projectName, "1.0.0-SNAPSHOT");
		assertNotNull("facade is null",facade);
	    Set<String> setOfProfilesFacade = new HashSet<String>();
	    setOfProfilesFacade.addAll(MavenPlugin.getProjectConfigurationManager().getResolverConfiguration(facade.getProject()).getActiveProfileList());
	    setOfProfilesFacade.remove("");
	    assertEquals("Selected profiles in project " +projectName+ " doesn't match", setOfExpectedProfiles, setOfProfilesFacade);
	}
	
	private void testAutoActivatedProfiles(){
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getMavenProject("org.jboss.tools.maven.tests", "simple-jar", "1.0.0-SNAPSHOT");
	    assertNotNull("facade is null",facade);
	    assertEquals("Auto Activated profiles from pom.xml doesn't match", AUTOACTIVATED_PROFILE_IN_POM, facade.getMavenProject().getActiveProfiles().get(0).getId());
	    assertEquals("Auto Activated profiles from settings.xml doesn't match", AUTOACTIVATED_PROFILES_IN_USER_SETTINGS[0], facade.getMavenProject().getActiveProfiles().get(1).getId());
	    assertEquals("Auto Activated profiles from settings.xml doesn't match", AUTOACTIVATED_PROFILES_IN_USER_SETTINGS[1], facade.getMavenProject().getActiveProfiles().get(2).getId());
	}
	
	private void importMavenProject(String pomPath) throws IOException, InterruptedException{
		bot.menu("File").menu("Import...").click();
		waitForShell(botUtil, "Import");
		bot.tree().expandNode("Maven").select("Existing Maven Projects").click();
		bot.button("Next >").click();
		waitForShell(botUtil, "Import Maven Projects");
		bot.comboBoxWithLabel("Root Directory:").setText((new File(pomPath)).getParentFile().getCanonicalPath());
		bot.button("Refresh").click();
		waitForShell(botUtil, "Import Maven Projects");
		Thread.sleep(5000);
		bot.button("Finish").click();
		botUtil.waitForAll();
	}
	
	
	private static void doCleanWorkspace() throws InterruptedException, CoreException, IOException {
	    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    workspace.run(new IWorkspaceRunnable() {
	      public void run(IProgressMonitor monitor) throws CoreException {
	        IProject[] projects = workspace.getRoot().getProjects();
	        for(int i = 0; i < projects.length; i++ ) {
	          projects[i].delete(false, false, monitor);
	        }
	      }
	    }, new NullProgressMonitor());

	    JobHelpers.waitForJobsToComplete(new NullProgressMonitor());

	    File[] files = workspace.getRoot().getLocation().toFile().listFiles();
	    if(files != null) {
	      for(File file : files) {
	        if(!".metadata".equals(file.getName())) {
	          if(file.isDirectory()) {
	            FileUtils.deleteDirectory(file);
	          } else {
	            if(!file.delete()) {
	              throw new IOException("Could not delete file " + file.getCanonicalPath());
	            }
	          }
	        }
	      }
	    }
	  }
}
