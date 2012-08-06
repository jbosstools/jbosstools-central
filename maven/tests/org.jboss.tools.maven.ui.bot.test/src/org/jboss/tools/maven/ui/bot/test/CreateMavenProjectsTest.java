package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.bindings.keys.KeyStroke;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.maven.ui.bot.test.utils.TableHasRows;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("restriction")
@Require(perspective="Java")
public class CreateMavenProjectsTest extends AbstractMavenSWTBotTest{
	
	public static final String REPO_URL = "http://repo1.maven.org/maven2";
	public static final String NEXUS_URL = "https://repository.jboss.org/nexus/content/repositories/releases/";
	
	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
		
	}
	
	@Test
	public void updateRepositories() throws InterruptedException, CoreException {
		bot.menu("Window").menu("Show View").menu("Other...").click();
		waitForShell(botUtil, "Show View");
		bot.tree().expandNode("Maven").select("Maven Repositories");
		bot.button("OK").click();
		bot.viewByTitle("Maven Repositories");
		waitForIdle();
		bot.tree().expandNode("Global Repositories").getNode("central (" + REPO_URL + ")").contextMenu("Rebuild Index").click();
		bot.button("OK").click();
		botUtil.waitForAll();
		botUtil.waitForJobs(Long.MAX_VALUE,"Rebuilding Indexes");
		bot.tree().expandNode("Global Repositories").getNode("jboss (" + NEXUS_URL + ")").contextMenu("Rebuild Index").click();
		bot.button("OK").click();
		botUtil.waitForAll();
		botUtil.waitForJobs(Long.MAX_VALUE,"Rebuilding Indexes");
		bot.tree().expandNode("Global Repositories").getNode("jboss (" + NEXUS_URL + ")").contextMenu("Update Index").click();
		botUtil.waitForAll();
		botUtil.waitForJobs(Long.MAX_VALUE,"Updating Indexes");
	}

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
		bot.viewByTitle("Package Explorer");
		bot.tree().getTreeItem(projectName).select().pressShortcut(Keystrokes.ALT,Keystrokes.LF);
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
		waitForIdle();
		bot.button("No").click();
		waitForIdle();
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	    assertNoErrors(project);
	    assertTrue(isMavenProject(projectName));
	    buildProject(projectName,"5 Maven build...", "war","-0.0.1-SNAPSHOT");
		
	}

	@Test
	public void createSimpleJSFProjectArchetype() throws Exception {
		String projectName = "JsfQuickstart";
		createSimpleMavenProjectArchetype(projectName,"maven-archetype-jsfwebapp", "Nexus Indexer");
		//IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		//assertNoErrors(project);
		assertTrue(isMavenProject(projectName));
		buildProject(projectName, "5 Maven build...", "war",""); //version is 1.0.0
	}
	
	@Test
	public void createSimpleJarProjectArchetype() throws Exception {
		String projectName = "ArchetypeQuickstart";
		createSimpleMavenProjectArchetype(projectName,"maven-archetype-quickstart", "Internal");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		assertTrue(isMavenProject(projectName));
		buildProject(projectName, "4 Maven build...", "jar","-0.0.1-SNAPSHOT");
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
		botExt.waitUntil(new TableHasRows(botExt.table(),projectType),100000);
		Thread.sleep(10000);
		int index = botExt.table(0).indexOf(projectType, "Artifact Id");
		if (index == -1) {
			fail(projectType + " not found");
		}
		shell.table(0).select(index);
		shell.button("Next >").click();
		shell.comboBoxWithLabel("Group Id:").setText(projectName);
		shell.comboBoxWithLabel("Artifact Id:").setText(projectName);
		shell.button("Finish").click();
		waitForIdle();
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
		item.pressShortcut(Keystrokes.SHIFT,Keystrokes.ALT,KeyStroke.getInstance("X"));
		Thread.sleep(1000);
		item.pressShortcut(KeyStroke.getInstance("M"));
		swtBot.waitForShell("Edit Configuration");
		swtBot.textWithLabel("Goals:").setText("clean package");
		swtBot.button("Run").click();
		waitForIdle();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		project.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
		IFile jarFile = project.getFile("target/" + projectName + version+"."+packaging);
		assertTrue(jarFile + " is missing ", jarFile.exists());
	}

}
