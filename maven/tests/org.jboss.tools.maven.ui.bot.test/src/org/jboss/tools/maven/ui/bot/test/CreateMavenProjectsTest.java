package org.jboss.tools.maven.ui.bot.test;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;


import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.Test;

@SuppressWarnings("restriction")
public class CreateMavenProjectsTest extends AbstractMavenSWTBotTest{
	
	public static final String JBOSS6_AS_HOME=System.getProperty("jbosstools.test.jboss.home.6.1", "/home/eiden/Java/RedHat/JBossASs/jboss-6.1.0.Final");
	public static final String JBOSS7_AS_HOME=System.getProperty("jbosstools.test.jboss.home.7.0", "/home/eiden/Java/RedHat/JBossASs/jboss-as-7.0.1.Final1");
	public static final String POM_FILE = "pom.xml";
	public static final String PROJECT_NAME6="JSFProject6";
	public static final String PROJECT_NAME7="JSFProject7";
	public static final String SERVER_RUNTIME6="JBoss 6.x Runtime";
	public static final String SERVER_RUNTIME7="JBoss 7.x Runtime";
	public static final String SERVER6="JBoss AS 6.x";
	public static final String SERVER7="JBoss AS 7.x";
	public static final String GROUPID ="javax.faces";
	public static final String ARTIFACTID ="jsf-api";
	public static final String JSF_VERSION_1_1_02 ="1.1.02";
	public static final String JSF_VERSION_1_2 ="2.0";
	public static final String JSF_VERSION_2 ="2.0";
	

	
	@Test
	public void createSimpleJarProject() throws Exception{
		String projectName = "MavenJar";
		createSimpleMavenProject(projectName,"jar");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
	}
	
	@Test
	public void createSimpleWarProject() throws Exception{
		String projectName = "MavenWar";
		createSimpleMavenProject(projectName, "war");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
	}
	
	
	@Test
	public void createSimpleJarProjectArchetype() throws Exception{
		String projectName = "ArchetypeQuickstart";
		createSimpleMavenProjectArchetype(projectName, "maven-archetype-quickstart", "All Catalogs");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
	}
	
	@Test
	public void createSimpleJsfProjectArchetype() throws Exception{
		String projectName = "ArchetypeJSF";
		createSimpleMavenProjectArchetype(projectName, "appfuse-basic-jsf", "All Catalogs");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertNoErrors(project);
	}
	
	
	
	
	private void createSimpleMavenProjectArchetype(String projectName, String projectType, String catalog) throws InterruptedException, CoreException{
		bot.menu("File").menu("New").menu("Other...").click();
		SWTBot shell = bot.shell("New").activate().bot();
		shell.tree().expandNode("Maven").select("Maven Project");
		shell.button("Next >").click();
		shell.checkBox("Create a simple project (skip archetype selection)").deselect();
		shell.button("Next >").click();
		Thread.sleep(1000);
		shell.comboBox().setSelection(catalog);
		Thread.sleep(1000);
		int index = bot.table(0).indexOf(projectType,"Artifact Id");
		if(index == -1){
			fail(projectType + " not found");
		}
		shell.table(0).select(index);
		shell.button("Next >").click();
		shell.comboBoxWithLabel("Group Id:").setText(projectName);
		shell.comboBoxWithLabel("Artifact Id:").setText(projectName);
		shell.button("Finish").click();
		waitForJobsToComplete();
	}
	
	private void createSimpleMavenProject(String projectName, String projectType) throws InterruptedException, CoreException{
		bot.menu("File").menu("New").menu("Other...").click();
		SWTBot shell = bot.shell("New").activate().bot();
		shell.tree().expandNode("Maven").select("Maven Project").click() ;
		shell.button("Next >").click();
		shell.checkBox("Create a simple project (skip archetype selection)").select();
		shell.button("Next >").click();
		shell.comboBoxInGroup("Artifact", 0).setText(projectName);
		shell.comboBoxInGroup("Artifact", 1).setText(projectName);
		shell.comboBoxInGroup("Artifact", 4).setText(projectType);
		shell.button("Finish").click();
		waitForJobsToComplete();

	}
	
	
	
	
}
