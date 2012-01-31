package org.jboss.tools.maven.ui.bot.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.helper.ContextMenuHelper;
import org.junit.BeforeClass;
import org.junit.Test;


@Require(perspective="Java EE")
public class CreateMavenizedEARProjectTest {
	
	public static final String WAR_PROJECT_NAME="earWeb";
	public static final String EJB_PROJECT_NAME="earEJB";
	public static final String EAR_PROJECT_NAME="ear";
	
	private SWTBotExt botext = new SWTBotExt();
	private SWTUtilExt botUtil = new SWTUtilExt(botext);
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
	}

	
	@Test
	public void createEARProject() throws Exception{
		createWarProject(WAR_PROJECT_NAME);
		Thread.sleep(500);
		createEJBProject(EJB_PROJECT_NAME);
		Thread.sleep(500);
		botext.menu("File").menu("Enterprise Application Project").click();
		botext.textWithLabel("Project name:").setText(EAR_PROJECT_NAME);
		botext.button("Modify...").click();
		botext.tree().getTreeItem("JBoss Maven Integration").check();
		botext.button("OK").click();
		botext.button("Next >").click();
		botext.button("Select All").click();
		botext.button("Next >").click();
		botext.comboBoxWithLabel("Packaging:").setSelection("ear");
		botext.button("Finish").click();
		Utils.waitForIdle();
		assertTrue(Utils.isMavenProject(EAR_PROJECT_NAME));
		installProject(WAR_PROJECT_NAME);
		installProject(EJB_PROJECT_NAME);
		botext.viewByTitle("Project Explorer").bot().tree().getTreeItem(EAR_PROJECT_NAME).contextMenu("Run As").menu("5 Maven build...").click();
		Utils.waitForIdle();
		botext.textWithLabel("Goals:").setText("clean package");
		botext.button("Run").click();
		Utils.waitForIdle();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(EAR_PROJECT_NAME);
		project.getFolder("target").refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		IFolder earFolder = project.getFolder("target/" + EAR_PROJECT_NAME + "-0.0.1-SNAPSHOT");
		assertTrue(earFolder +" is missing ", earFolder.exists());
	}
	
	private void createWarProject(String projectName) throws CoreException, InterruptedException{
		botext.menu("File").menu("Dynamic Web Project").click();
		botext.textWithLabel("Project name:").setText(projectName);
		botext.button("Next >").click();
		botext.button("Next >").click();
		botext.checkBox("Generate web.xml deployment descriptor").select();
		botext.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		SWTBotTreeItem item = botext.viewByTitle("Package Explorer").bot().tree().getTreeItem(projectName).select();
		item.pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		SWTBot shellProperties = botext.shell("Properties for "+projectName).activate().bot();
	    shellProperties.tree().select("Project Facets");
	    shellProperties.tree(1).getTreeItem("JBoss Maven Integration").check();
	    Utils.waitForIdle();
	    Thread.sleep(500);
	    botext.hyperlink("Further configuration required...").click();
	    botext.button("OK").click();
	    botext.button("OK").click();
	    botUtil.waitForAll(Long.MAX_VALUE);
		assertTrue("Web project doesn't have maven nature",Utils.isMavenProject(projectName));
		
	}
	
	private void createEJBProject(String projectName) throws CoreException, InterruptedException{
		botext.menu("File").menu("EJB Project").click();
		botext.textWithLabel("Project name:").setText(projectName);
		botext.button("Modify...").click();
		botext.tree().getTreeItem("JBoss Maven Integration").check();
		botext.button("OK").click();
		botext.button("Next >").click();
		botext.button("Next >").click();
		botext.button("Next >").click();
		botext.comboBoxWithLabel("Packaging:").setSelection("ejb");
		botext.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);
		assertTrue("EJB project doesn't have maven nature", Utils.isMavenProject(projectName));
	}
	
	private void installProject(String projectName) throws InterruptedException{
		botext.menu("Window").menu("Show View").menu("Other...").click();
		botext.tree().expandNode("Java").select("Package Explorer").click();
		botext.button("OK").click();
		SWTBotTree innerBot = botext.viewByTitle("Package Explorer").bot().tree().select(projectName);
		ContextMenuHelper.clickContextMenu(innerBot,"Run As","5 Maven build...");
		botext.textWithLabel("Goals:").setText("clean package install");
		botext.button("Run").click();
		Utils.waitForIdle();
		botUtil.waitForAll();
	}
	
	
}
