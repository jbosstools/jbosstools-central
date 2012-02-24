package org.jboss.tools.central.test.ui.bot;

import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.condition.BrowserIsLoaded;
import org.jboss.tools.ui.bot.ext.condition.TaskDuration;
import org.jboss.tools.ui.bot.ext.parts.SWTBotBrowserExt;
import org.jboss.tools.ui.bot.ext.types.IDELabel;
import org.junit.BeforeClass;
import org.junit.Test;

public class DocumentationSectionTest extends SWTTestExt {

	@BeforeClass
	public static void setup(){
		bot.menu("Help").menu(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).click();
		util.waitForAll();
	}
	
	@Test
	public void documentationSectionTest(){
		testHyperlinkToBrowser("New and Noteworthy");
		testHyperlinkToBrowser("User Forum");
		testHyperlinkToBrowser("Reference");
		testHyperlinkToBrowser("Developer Forum");
		testHyperlinkToBrowser("FAQ");
		testHyperlinkToBrowser("Wiki");
		testHyperlinkToBrowser("Screencasts");
		testHyperlinkToBrowser("Issue Tracker");
		bot.sleep(TIME_10S);
	}
	
	private void testHyperlinkToBrowser(String hyperlinkText){
		bot.hyperlink(hyperlinkText).click();
		SWTBotBrowserExt browser = bot.browserExt();
		bot.waitUntil(new BrowserIsLoaded(browser), TaskDuration.LONG.getTimeout());
		assertFalse("JBoss Central sould not be active editor right now", bot.activeEditor().getTitle().equals("JBoss Central"));
		System.out.println(browser.getText());
		bot.activeEditor().close();
	}
	
	
}
