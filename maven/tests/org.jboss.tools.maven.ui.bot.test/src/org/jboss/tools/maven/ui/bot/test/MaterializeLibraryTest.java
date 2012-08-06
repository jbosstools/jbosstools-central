package org.jboss.tools.maven.ui.bot.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.jboss.tools.ui.bot.ext.SWTBotExt;
import org.jboss.tools.ui.bot.ext.SWTUtilExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;

@Require(perspective="Java")
public class MaterializeLibraryTest extends AbstractMavenSWTBotTest{
	
	private String projectName = "example";
	
	private SWTUtilExt botUtil= new SWTUtilExt(bot);
	
	@BeforeClass
	public static void setup(){
		SWTBotExt setup = new SWTBotExt();
		setup.menu("Window").menu("Show View").menu("Other...").click();
		setup.tree().expandNode("Java").select("Package Explorer").click();
		setup.button("OK").click();
		setup.menu("Window").menu("Preferences").click();
		setup.waitForShell("Preferences");
		setup.tree().expandNode("JBoss Tools").select("Project Examples");
		setup.checkBox("Show Project Ready wizard").deselect();
		setup.checkBox("Show readme/cheatsheet file").deselect();
		setup.button("OK").click();
	}
	
	@SuppressWarnings("restriction")
	@Test
	public void testMaterializeLibrary() throws Exception{
		bot.menu("New").menu("Example...").click();
		bot.tree().expandNode("JBoss Tools").select("Project Examples");
		waitForIdle();
		bot.button("Next >").click();
		waitForShell(botUtil, "New Project Example");
		bot.tree().expandNode("JBoss Maven Archetypes").select("Spring MVC Project");
		bot.button("Next >").click();
		bot.button("Next >").click();
		bot.comboBoxWithLabel("Project name").setText(projectName);
		bot.comboBoxWithLabel("Package").setText(projectName);
		bot.button("Finish").click();
		botUtil.waitForAll(Long.MAX_VALUE);

		final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		SWTBotTree tree = packageExplorer.bot().tree();
		SWTBotTreeItem item = tree.expandNode(project.getName());
		item = item.getNode("Maven Dependencies");
		item.select().pressShortcut(Keystrokes.SHIFT,Keystrokes.F10);
		KeyStroke k = KeyStroke.getInstance("M");
		item.pressShortcut(k);
		waitForShell(botUtil, "Materialize Classpath Library");
		bot.button("OK").click();
		Thread.sleep(1000);
		bot.activeShell().activate();
		bot.button("OK").click();
		waitForIdle();
		assertFalse(project.getName()+" is still a maven project!",isMavenProject(project.getName()));
		testExcludedResources(project);
		assertNoErrors(project);
	}
	
	private void testExcludedResources(IProject project) throws Exception{
		final SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
		packageExplorer.bot().tree().getTreeItem(project.getName()).select().pressShortcut(Keystrokes.ALT,Keystrokes.LF);
		bot.tree().select("Java Build Path");
		bot.tabItem("Source").activate();
		for(SWTBotTreeItem item: bot.tree(1).getAllItems()){
			for(SWTBotTreeItem itemToCheck: item.getItems()){
				if(itemToCheck.getText().startsWith("Included")){
					assertTrue("(All) expected in Included patterns",itemToCheck.getText().endsWith("(All)"));
				} else if (itemToCheck.getText().startsWith("Excluded")){
					assertTrue("(None) expected in Excluded patterns",itemToCheck.getText().endsWith("(None)"));
				}
			}
		}
		bot.button("OK").click();
	}
	
	
}
