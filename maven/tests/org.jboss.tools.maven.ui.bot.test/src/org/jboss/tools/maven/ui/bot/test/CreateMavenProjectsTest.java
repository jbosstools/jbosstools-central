package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

@SuppressWarnings("restriction")
@Require(perspective="Java")
public class CreateMavenProjectsTest extends AbstractMavenSWTBotTest{

	public static final String REPO_URL = "http://repo1.maven.org/maven2";
	protected static SWTWorkbenchBot bot = new SWTWorkbenchBot();
	
	@Test
	public void updateRepositories() throws InterruptedException {
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
	}

	@Test
	public void createSimpleJarProject() throws Exception {
		String projectName = "MavenJar";
		createSimpleMavenProject(projectName, "jar");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		Utils.isMavenProject(projectName);
		buildProject(projectName,"5 Maven build...");
	}

	@Test
	public void createSimpleWarProject() throws Exception {
		String projectName = "MavenWar";
		createSimpleMavenProject(projectName, "war");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		Utils.isMavenProject(projectName);
		buildProject(projectName,"5 Maven build...");
	}

	@Test
	public void createSimpleJarProjectArchetype() throws Exception {
		String projectName = "ArchetypeQuickstart";
		createSimpleMavenProjectArchetype(projectName,"maven-archetype-quickstart", "Nexus Indexer");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
		Utils.isMavenProject(projectName);
		buildProject(projectName, "6 Maven build...");
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
		Thread.sleep(1000);
		shell.comboBox().setSelection(catalog);
		Thread.sleep(1000);
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

	private void buildProject(String projectName, String mavenBuild) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBot explorer = bot.viewByTitle("Package Explorer").bot();
		SWTBotTreeItem item = explorer.tree().getTreeItem(projectName).select();
		SWTBotExt swtBot = new SWTBotExt();
		item.contextMenu("Run As").menu(mavenBuild).click();
		swtBot.textWithLabel("Goals:").setText("clean package");
		swtBot.button("Run").click();
		waitForIdle();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		project.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
		IFile jarFile = project.getFile("target/" + projectName + "-0.0.1-SNAPSHOT.jar");
		assertTrue(jarFile + " is missing ", jarFile.exists());
	}

}
