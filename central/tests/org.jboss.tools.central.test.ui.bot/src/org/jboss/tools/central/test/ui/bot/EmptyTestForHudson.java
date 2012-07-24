package org.jboss.tools.central.test.ui.bot;

import java.io.FileNotFoundException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.ui.bot.ext.SWTBotFactory;
import org.jboss.tools.ui.bot.ext.SWTFormsBotExt;
import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.config.Annotations.ServerType;
import org.jboss.tools.ui.bot.ext.types.IDELabel;
import org.jboss.tools.ui.bot.ext.wizards.SWTBotWizard;
import org.junit.BeforeClass;
import org.junit.Test;


@Require
public class EmptyTestForHudson extends SWTTestExt {
	
	
	@BeforeClass
	public static void setup() throws FileNotFoundException{
		util.closeAllEditors(false);
		util.closeAllViews();
		bot.menu("Window").menu("Preferences").click();
		bot.waitForShell("Preferences");
		bot.tree().getTreeItem("Maven").select();
		bot.checkBox("Download repository index updates on startup").deselect();
		bot.clickButton("OK");
		bot.menu("Help").menu(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).click();
		util.waitForAll();
	}
	
	@Test
	public void testTest(){
		checkExample(null, IDELabel.JBossCentralEditor.RICHFACES_PROJECT, true);
		checkExample(null, IDELabel.JBossCentralEditor.SPRING_MVC_PROJECT, true);
		SWTFormsBotExt formsBot = SWTBotFactory.getFormsBot();
		formsBot.formTextWithText("Helloworld").click();
		bot.sleep(TIME_10S);
	}
	
	
	
	/**
	 * 
	 * @param formsBot formBot==null => link is of type HyperLink else it is of type FormText
	 * @param formText
	 * @param readme true if readme should be shown
	 */
	
	private void checkExample(SWTFormsBotExt formsBot, String formText, boolean readme){
		checkExample(formsBot, formText, readme, null);
	}
	
	/**
	 * Checks example
	 * @param formsBot bot for Forms
	 * @param formText text to be clicked at
	 * @param readme true if readme is supposed to show, false otherwise
	 * @param readmeFileName 
	 */
	
	private void checkExample(SWTFormsBotExt formsBot, String formText, boolean readme, String readmeFileName){
		problems.show();
		if (formsBot==null){
			bot.hyperlink(formText).click();
		}else{
			try{
				formsBot.formTextWithText(formText).click();
			}catch(WidgetNotFoundException wnfex){
				throw new WidgetNotFoundException("Could not found widget of type Hyperlink and text " +
						formText, wnfex);
			}
		}
		bot.waitForShell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE);
		/*SWTBotWizard wizard = new SWTBotWizard(bot.shell(IDELabel.JBossCentralEditor.PROJECT_EXAMPLE).widget);
		wizard.next();
		if (wizard.canNext()){
			bot.comboBox(2).setSelection(1);
			try{
				bot.link();
				fail("There is something wrong with maven repo. Message: \n"+bot.link().getText());
			}catch (WidgetNotFoundException ex){
				//everything fine
			}
			wizard.next();
		}
		wizard.finishWithWait();
		String readmeText = bot.checkBox(1).getText();
		assertFalse("Quick fix should not be enabled (Everything should be fine)", bot.checkBox(0).isEnabled());
		if (readme){
			assertTrue("Show readme checkbox should be enabled", bot.checkBox(1).isEnabled());
			assertTrue("Show readme checkbox should be checked by default", bot.checkBox(1).isChecked());
			if (readmeFileName != null){
				assertTrue(readmeText.toLowerCase().contains(readmeFileName));
				bot.clickButton("Finish");
				assertTrue("Cheat Sheets view should be opened right now", bot.activeView().getTitle().equals("Cheat Sheets"));
				bot.activeView().close();
			}else if (readmeText.contains("cheatsheet.xml")){
				bot.clickButton("Finish");
				assertTrue("Cheat Sheets view should be opened right now", bot.activeView().getTitle().equals("Cheat Sheets"));
				bot.activeView().close();
			}else if (readmeText.toLowerCase().contains("readme.md") || readmeText.toLowerCase().contains("readme.txt")){
				bot.clickButton("Finish");
				assertTrue("Readme should have opened in Text Editor", bot.activeEditor().getReference().getEditor(false).getClass().getName().contains("org.eclipse.ui.editors.text.TextEditor")); //because readmes are opening in browser now.. It's a bug. Jira is created.
				bot.activeEditor().close();
			}else if (readmeText.toLowerCase().contains("readme.htm")){
				bot.clickButton("Finish");
				assertTrue("Readme should have opened in Internal Browser", bot.activeEditor().getReference().getEditor(false).getClass().getName().contains("org.eclipse.ui.internal.browser.WebBrowserEditor"));
			}
		}else{
			bot.clickButton("Finish");
		}*/
		bot.activeShell().close();
	}
	
}
