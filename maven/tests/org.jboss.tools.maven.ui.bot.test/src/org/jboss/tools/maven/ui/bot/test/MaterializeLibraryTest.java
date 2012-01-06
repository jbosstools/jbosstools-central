package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.Test;

@Require(perspective="Seam 2")
public class MaterializeLibraryTest extends AbstractMavenSWTBotTest {
	
	private String projectName = "exampleHTML5";
	
	@Test
	public void testMaterializeLibrary() throws Exception{
		SWTBotExt ext = new SWTBotExt();
		ext.menu("New").menu("Example...").click();
		ext.tree().expandNode("JBoss Tools").select("Project Examples");
		waitForIdle();
		ext.button("Next >").click();
		waitForIdle();
		Thread.sleep(1000);
		ext.tree(0).expandNode("Java EE 6 Quickstarts").select("Java EE 6 HTML5 Mobile Webapp");
		ext.button("Finish").click();
		ext.comboBoxWithLabel("Project name").setText(projectName);
		ext.comboBoxWithLabel("Package").setText(projectName);
		ext.button("Next >").click();
		ext.button("Finish").click();
		waitForIdle();
		final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBotTree tree = packageExplorer.bot().tree();
		tree.expandNode(project.getName()).getNode("Maven Dependencies").contextMenu("Materialize Library...").click();
		waitForIdle();
		SWTBotExt botExt = new SWTBotExt();
		botExt.button("OK").click();
		botExt.button("OK").click();
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
