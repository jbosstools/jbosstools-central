package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.bindings.keys.KeyStroke;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.helper.ContextMenuHelper;
import org.junit.BeforeClass;
import org.junit.Test;

@Require(perspective="Java")
public class MaterializeLibraryTest extends AbstractMavenSWTBotTest{
	
	private String projectName = "example";
	
	private SWTBotExt botExt = new SWTBotExt();
	private SWTUtilExt botUtil = new SWTUtilExt(botExt);
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
	}
	
	@Test
	public void testMaterializeLibrary() throws Exception{
		botExt.menu("New").menu("Example...").click();
		botExt.tree().expandNode("JBoss Tools").select("Project Examples");
		waitForIdle();
		botExt.button("Next >").click();
		waitForIdle();
		while(!botUtil.isShellActive("New Project Example")){
			Thread.sleep(500);
		}
		botExt.tree().expandNode("JBoss Maven Archetypes").select("Spring MVC Project");
		botExt.button("Next >").click();
		botExt.button("Next >").click();
		botExt.button("Next >").click();
		botExt.comboBoxWithLabel("Group Id:").setText(projectName);
		botExt.comboBoxWithLabel("Artifact Id:").setText(projectName);
		botExt.button("Finish").click();
		
		
		
		/* old wizard
		botExt.button("Finish").click();
		while(!botUtil.isShellActive("New JBoss Project")){
			Thread.sleep(500);
		}
		botExt.comboBoxWithLabel("Project name").setText(projectName);
		botExt.comboBoxWithLabel("Package").setText(projectName);
		botExt.button("Next >").click();
		botExt.button("Finish").click();
		*/
		botUtil.waitForNonIgnoredJobs();
		Thread.sleep(5000);
		botExt.activeShell().bot().button("Finish").click();
		final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBotTree tree = packageExplorer.bot().tree();
		SWTBotTreeItem item = tree.expandNode(project.getName());
		item = item.getNode("Maven Dependencies");
		item.select().pressShortcut(Keystrokes.SHIFT,Keystrokes.F10);
		KeyStroke k = KeyStroke.getInstance("M");
		item.pressShortcut(k);
		waitForIdle();
		botExt.button("OK").click();
		Thread.sleep(500);
		botExt.button("OK").click();
		waitForIdle();
		assertFalse(project.getName()+" is still a maven project!",Utils.isMavenProject(project.getName()));
		testExcludedResources(project);
	}
	
	private void testExcludedResources(IProject project) throws Exception{
		final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		packageExplorer.bot().tree().getTreeItem(project.getName()).select().pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		botExt.tree().select("Java Build Path");
		botExt.tabItem("Source").activate();
		for(SWTBotTreeItem item: botExt.tree(1).getAllItems()){
			for(SWTBotTreeItem itemToCheck: item.getItems()){
				if(itemToCheck.getText().startsWith("Included")){
					assertTrue("(All) expected in Included patterns",itemToCheck.getText().endsWith("(All)"));
				} else if (itemToCheck.getText().startsWith("Excluded")){
					assertTrue("(None) expected in Excluded patterns",itemToCheck.getText().endsWith("(None)"));
				}
			}
		}
	}
	
	
}
