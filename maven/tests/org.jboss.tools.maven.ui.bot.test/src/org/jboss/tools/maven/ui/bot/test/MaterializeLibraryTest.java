package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.bindings.keys.KeyStroke;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;

@Require(perspective="Java")
public class MaterializeLibraryTest extends AbstractMavenSWTBotTest{
	
	private String projectName = "example";
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
	}
	
	@Test
	public void testMaterializeLibrary() throws Exception{
		SWTBotExt ext = new SWTBotExt();
		ext.menu("New").menu("Example...").click();
		ext.tree().expandNode("JBoss Tools").select("Project Examples");
		waitForIdle();
		ext.button("Next >").click();
		waitForIdle();
		Thread.sleep(1000);
		ext.tree(0).expandNode("Java EE 6 Quickstarts").select("Spring MVC Project");
		ext.button("Finish").click();
		Thread.sleep(500);
		ext.comboBoxWithLabel("Project name").setText(projectName);
		ext.comboBoxWithLabel("Package").setText(projectName);
		ext.button("Next >").click();
		ext.button("Finish").click();
		waitForIdle();
		final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBotTree tree = packageExplorer.bot().tree();
		SWTBotTreeItem item = tree.expandNode(project.getName());
		item = item.getNode("Maven Dependencies");
		item.select().pressShortcut(Keystrokes.SHIFT,Keystrokes.F10);
		KeyStroke k = KeyStroke.getInstance("M");
		item.pressShortcut(k);
		waitForIdle();
		SWTBotExt botExt = new SWTBotExt();
		botExt.button("OK").click();
		botExt.activeShell().bot().button("OK").click();
		waitForIdle();
		assertFalse(project.getName()+" is still a maven project!",Utils.isMavenProject(project.getName()));
		testExcludedResources(project);
	}
	
	public void testExcludedResources(IProject project) throws Exception{
		final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		packageExplorer.bot().tree().getTreeItem(project.getName()).select().pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		SWTBotExt botExt = new SWTBotExt();
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
