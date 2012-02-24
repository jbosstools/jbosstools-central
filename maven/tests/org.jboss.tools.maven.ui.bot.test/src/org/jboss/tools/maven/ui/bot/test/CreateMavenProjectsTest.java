package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("restriction")
@Require(perspective="Java")
public class CreateMavenProjectsTest extends AbstractMavenSWTBotTest{
	
	public static final String REPO_URL = "http://repo1.maven.org/maven2";
	protected static SWTWorkbenchBot bot = new SWTWorkbenchBot();
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
	}
	
	@Test
	public void updateRepositories() throws InterruptedException, CoreException {
		SWTBotExt botExt = new SWTBotExt();
		botExt.menu("Window").menu("Show View").menu("Other...").click();
		waitForIdle();
		botExt.tree().expandNode("Maven").select("Maven Repositories");
		botExt.button("OK").click();
		SWTBot b = botExt.viewByTitle("Maven Repositories").bot();
		AbstractMavenSWTBotTest.waitForIdle();
		b.tree().expandNode("Global Repositories").getNode("central (" + REPO_URL + ")").contextMenu("Rebuild Index").click();
		botExt.button("OK").click();
		SWTUtilExt util = new SWTUtilExt(botExt);
		util.waitForAll();
		util.waitForJobs(Long.MAX_VALUE,"Rebuilding Indexes");
	}

	@Test
	public void createSimpleJarProject() throws Exception {
		String projectName = "MavenJar";
		createSimpleMavenProject(projectName, "jar");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		assertTrue(Utils.isMavenProject(projectName));
		buildProject(projectName,"5 Maven build...", "jar","-0.0.1-SNAPSHOT");
	}
	
	
	
	@Test
	public void createSimpleJarMavenizedProject() throws Exception{
		String projectName = "JarMavenized";
		SWTBotExt botExt = new SWTBotExt();
		botExt.menu("File").menu("New").menu("Other...").click();
		waitForIdle();
		SWTBot shell = botExt.shell("New").activate().bot();
		shell.tree().expandNode("Java").select("Java Project");
		shell.button("Next >").click();
		shell.textWithLabel("Project name:").setText(projectName);
		shell.button("Finish").click();
		waitForIdle();
		SWTBot explorer = botExt.viewByTitle("Package Explorer").bot();
		explorer.tree().getTreeItem(projectName).select().pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		Thread.sleep(1000);
		botExt.tree().select("Project Facets");
		Thread.sleep(500);
		botExt.link(0).click("Convert to faceted form...");
		waitForIdle();
		botExt.tree(1).getTreeItem("JBoss Maven Integration").check();
	    Thread.sleep(500);
	    botExt.hyperlink("Further configuration required...").click();
	    botExt.comboBoxWithLabel("Packaging:").setSelection("jar");
	    botExt.button("OK").click();
	    botExt.button("OK").click();
	    waitForIdle();
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    assertNoErrors(project);
	    assertTrue(Utils.isMavenProject(projectName));
	    buildProject(projectName,"5 Maven build...", "jar","-0.0.1-SNAPSHOT");
		
	}
	
	
	@Test
	public void createSimpleWarMavenizedProject() throws Exception{
		String projectName = "WarMavenized";
		SWTBotExt botExt = new SWTBotExt();
		botExt.menu("File").menu("New").menu("Other...").click();
		waitForIdle();
		SWTBot shell = botExt.shell("New").activate().bot();
		shell.tree().expandNode("Web").select("Dynamic Web Project");
		shell.button("Next >").click();
		botExt.textWithLabel("Project name:").setText(projectName);
		botExt.button("Modify...").click();
		botExt.tree().getTreeItem("JBoss Maven Integration").check();
		botExt.button("OK").click();
		botExt.button("Next >").click();
		botExt.button("Next >").click();
		botExt.checkBox("Generate web.xml deployment descriptor").select();
		botExt.button("Next >").click();
		botExt.button("Finish").click();
		waitForIdle();
		botExt.button("No").click();
		waitForIdle();
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    assertNoErrors(project);
	    assertTrue(Utils.isMavenProject(projectName));
	    buildProject(projectName,"5 Maven build...", "war","-0.0.1-SNAPSHOT");
		
	}
	
	@Test
	public void createSimpleJSFProjectArchetype() throws Exception {
		String projectName = "JsfQuickstart";
		createSimpleMavenProjectArchetype(projectName,"weld-jsf-jee-minimal", "Nexus Indexer");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		assertTrue(Utils.isMavenProject(projectName));
		buildProject(projectName, "6 Maven build...", "war",""); //version is 1.0.0
	}
	
	@Test
	public void createSimpleJarProjectArchetype() throws Exception {
		String projectName = "ArchetypeQuickstart";
		createSimpleMavenProjectArchetype(projectName,"maven-archetype-quickstart", "Nexus Indexer");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		assertTrue(Utils.isMavenProject(projectName));
		buildProject(projectName, "6 Maven build...", "jar","-0.0.1-SNAPSHOT");
	}

	private void createSimpleMavenProjectArchetype(String projectName,String projectType, String catalog) throws InterruptedException,CoreException {
		SWTBotExt botExt = new SWTBotExt();
		botExt.menu("File").menu("New").menu("Other...").click();
		waitForIdle();
		SWTBot shell = botExt.shell("New").activate().bot();
		shell.tree().expandNode("Maven").select("Maven Project");
		shell.button("Next >").click();
		shell.checkBox("Create a simple project (skip archetype selection)").deselect();
		shell.button("Next >").click();
		Thread.sleep(2000);
		shell.comboBox().setSelection(catalog);
		SWTUtilExt botUtil = new SWTUtilExt(botExt);
		botUtil.waitForAll();
		int index = botExt.table(0).indexOf(projectType, "Artifact Id");
		if (index == -1) {
			fail(projectType + " not found");
		}
		shell.table(0).select(index);
		shell.button("Next >").click();
		shell.comboBoxWithLabel("Group Id:").setText(projectName);
		shell.comboBoxWithLabel("Artifact Id:").setText(projectName);
		shell.button("Finish").click();
		waitForJobsToComplete();
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

	private void buildProject(String projectName, String mavenBuild, String packaging, String version) throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBot explorer = bot.viewByTitle("Package Explorer").bot();
		Thread.sleep(500);
		SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
		SWTBotExt swtBot = new SWTBotExt();
		item.contextMenu("Run As").menu(mavenBuild).click();
		swtBot.textWithLabel("Goals:").setText("clean package");
		swtBot.button("Run").click();
		waitForIdle();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		project.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
		IFile jarFile = project.getFile("target/" + projectName + version+"."+packaging);
		assertTrue(jarFile + " is missing ", jarFile.exists());
	}

}
