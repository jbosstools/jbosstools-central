package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.jboss.tools.maven.ui.bot.test.utils.TableHasRows;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;

@Require(perspective="Java")
public class ArchetypesTest extends AbstractMavenSWTBotTest{

	public static final String REPO_URL = "http://repo1.maven.org/maven2";
	public static final String NEXUS_URL = "https://repository.jboss.org/nexus/content/repositories/releases/";
	
	@BeforeClass
	public static void setup() throws InterruptedException, CoreException{
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
		updateRepositories();
		
	}

	public static void updateRepositories() throws InterruptedException, CoreException {
		SWTBotExt bot = new SWTBotExt();
		SWTUtilExt botUtil= new SWTUtilExt(bot);
		bot.menu("Window").menu("Show View").menu("Other...").click();
		bot.tree().expandNode("Maven").select("Maven Repositories");
		bot.button("OK").click();
		bot.viewByTitle("Maven Repositories");
		bot.sleep(1000);
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
	public void createSimpleJSFProjectArchetype() throws Exception {
		String projectName = "JsfQuickstart";
		createSimpleMavenProjectArchetype(projectName,"maven-archetype-jsfwebapp", "Nexus Indexer");
		//IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		//assertNoErrors(project);
		assertTrue(isMavenProject(projectName));
		buildProject(projectName, "5 Maven build...", "war",""); //version is 1.0.0
	}
	
	@SuppressWarnings("restriction")
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
}
