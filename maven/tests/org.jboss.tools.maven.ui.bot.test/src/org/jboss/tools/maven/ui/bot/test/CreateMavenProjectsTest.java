package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

@Require(perspective="Java")
public class CreateMavenProjectsTest extends AbstractMavenSWTBotTest{
	
	
	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@Test
	public void createSimpleJarProject() throws Exception {
		String projectName = "MavenJar";
		createSimpleMavenProject(projectName, "jar");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		assertTrue(isMavenProject(projectName));
		buildProject(projectName,"5 Maven build...", "jar","-0.0.1-SNAPSHOT");
	}
	
	
	
	@Test
	public void createSimpleJarMavenizedProject() throws Exception{
		String projectName = "JarMavenized";
		bot.menu("File").menu("New").menu("Other...").click();
		waitForShell(botUtil, "New");
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
		waitForIdle();
		bot.viewByTitle("Package Explorer").setFocus();
		bot.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select().pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		waitForShell(botUtil, "Properties for "+projectName);
		bot.tree().select("Project Facets");
		Thread.sleep(500);
		bot.link(0).click("Convert to faceted form...");
		Thread.sleep(1000);
		bot.tree(1).getTreeItem("JBoss Maven Integration").check();
	    Thread.sleep(500);
	    bot.hyperlink("Further configuration required...").click();
	    bot.comboBoxWithLabel("Packaging:").setSelection("jar");
	    bot.button("OK").click();
	    bot.button("OK").click();
	    waitForIdle();
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    assertNoErrors(project);
	    assertTrue(isMavenProject(projectName));
	    buildProject(projectName,"5 Maven build...", "jar","-0.0.1-SNAPSHOT");
		
	}
	
	
	@Test
	public void createSimpleWarMavenizedProject() throws Exception{
		String projectName = "WarMavenized";
		bot.menu("File").menu("New").menu("Other...").click();
		waitForShell(botUtil,"New");
		bot.tree().expandNode("Web").select("Dynamic Web Project");
		bot.button("Next >").click();
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Modify...").click();
		bot.tree().getTreeItem("JBoss Maven Integration").check();
		bot.button("OK").click();
		bot.button("Next >").click();
		bot.button("Next >").click();
		bot.checkBox("Generate web.xml deployment descriptor").select();
		bot.button("Next >").click();
		bot.button("Finish").click();
		//waitForIdle();
		//bot.button("No").click();
		waitForIdle();
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    assertNoErrors(project);
	    assertTrue(isMavenProject(projectName));
	    buildProject(projectName,"5 Maven build...", "war","-0.0.1-SNAPSHOT");
		
	}
	
	private void createSimpleMavenProject(String projectName, String projectType) throws InterruptedException, CoreException {
		SWTBotExt botExt = new SWTBotExt();
		botExt.menu("File").menu("New").menu("Other...").click();
		SWTBot shell = botExt.shell("New").activate().bot();
		shell.tree().expandNode("Maven").select("Maven Project").click();
		shell.button("Next >").click();
		shell.checkBox("Create a simple project (skip archetype selection)").select();
		shell.button("Next >").click();
		shell.comboBoxInGroup("Artifact", 0).setText(projectName);
		shell.comboBoxInGroup("Artifact", 1).setText(projectName);
		shell.comboBoxInGroup("Artifact", 4).setText(projectType);
		shell.button("Finish").click();
		waitForIdle();

	}

}
