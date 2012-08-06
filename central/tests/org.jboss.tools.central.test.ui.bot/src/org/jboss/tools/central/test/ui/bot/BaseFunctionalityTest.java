package org.jboss.tools.central.test.ui.bot;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.jboss.tools.ui.bot.ext.RequirementAwareSuite;
import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.types.IDELabel;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(RequirementAwareSuite.class)
@SuiteClasses({CentralAllBotTests.class})
public class BaseFunctionalityTest extends SWTTestExt {
	
	/**
	 * Close usage report window
	 */
	@BeforeClass
	public static void setup(){
		util.closeAllEditors(false);
		util.closeAllViews();
	}
	/**
	 * Tests whether JBoss central is accessible from Help menu
	 */
	@Test
	public void testIsInstalled(){
		try {
			bot.menu("Help").menu(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).click();
		} catch (WidgetNotFoundException e) {
			e.printStackTrace(System.out);
			assertTrue("JBoss Cenral isn't in menu Help", false);
		}
		//JBoss Central should be visible right now
		assertTrue("JBoss Central is not active",bot.editorByTitle(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).isActive());
		bot.editorByTitle(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).close();
		try {
			bot.toolbarButtonWithTooltip(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).click();
		}catch (WidgetNotFoundException e) {
			assertTrue("JBoss Central isn't accessible through toolbar", false);
		}
		assertTrue("JBoss Central is not active",bot.editorByTitle(IDELabel.JBossCentralEditor.JBOSS_CENTRAL).isActive());
	}
	
	
	//TODO Refactor search Test
//	@Test
//	public void testSearch(){
//		assertTrue("JBoss Central is not active",bot.editorByTitle("JBoss Central").isActive());
//		//SWTBotEditor editor = bot.editorByTitle("JBoss Central");
//		SWTBotCTabItem cTabItem = bot.cTabItem("Software/Update");
//		cTabItem.activate();
//		util.waitForJobs("Discovering...");
//		//captureScreenshot("pokus.jpg");
//		//features available is ready
//		//bot.checkBox("Show installed").click();
//		SWTBotToolbarPushButton button = (SWTBotToolbarPushButton) bot.toolbarButtonWithTooltip("JBoss Tools Home");
//		button.click();
//		SWTBotBrowser browser = new SWTBotExt().browser();
//		while (!browser.getUrl().equals("http://www.jboss.org/tools")){
//		}
//		browser.setUrl("http://www.jboss.org/tools");
//		if (browser.isPageLoaded()){
//			log.info("Uz je pry nactena");
//		}
//		log.info(browser.getText());
//		bot.editorByTitle(bot.activeEditor().getTitle()).close();
//		
//		
//		/*Matcher matcher = allOf(widgetOfType(Button.class), withLabel("JBoss Maven CDI Configurator."), withStyle(SWT.CHECK, "SWT.CHECK"));
//		//SWTBotCheckBox box = new SWTBotCheckBox((Button) bot.widgets(matcher, matcher);
//		List<Button> boxy =  bot.widgets(matcher);
//		for (Button btn : boxy) {
//			SWTBotCheckBox box = new SWTBotCheckBox(btn);
//			box.click();
//		}*/
//		/*asyncExec(new VoidResult() {
//			
//			@Override
//			public void run() {
//				for (Control but : bot.checkBox(5).widget.getParent().getChildren()){
//					log.info(but.getClass());
//					if (but instanceof Button){
//						log.info(but.getData("connectorId"));
//					}
//					if (but instanceof Label){
//						Label lab = (Label) but;
//						log.info(lab.toString());
//					}
//				}
//			}
//		});*/
//		//bot.checkBox(1).select();
//		//bot.checkBox(5).toString();
//		/*button = (SWTBotToolbarPushButton) bot.toolbarButtonWithTooltip("Install");
//		button.click();
//		log.info("Kliknul jsem na "+button.toString());
//		//bot.sleep(80000);
//		bot.waitUntil(Conditions.shellIsActive("Install"), 80000);
//		log.info("Cil by mel byt otevreny Install");*/
//		//bot.shell("Install").close();
//		/*SWTBotCheckBox box = bot.checkBoxWithLabel("JBoss Maven CDI Configurator.");
//		log.info(box.toString());
//		box.select();*/
//		//log.info(box.widget.getData("connectorId"));
//		//browser.refresh();
//		/*while (!browser.isPageLoaded()){
//			bot.sleep(TIME_500MS);
//		}*/
//		//log.info(browser.getText());
//		/*log.info(bot.activeEditor().getTitle());
//		assertTrue("The main page wasn't opened",bot.activeEditor().getTitle() == "JBoss Tools | Overview - JBoss Community");
//		bot.editorByTitle("JBoss Tools | Overview - JBoss Community").close();*/
//		bot.sleep(TIME_5S);
//		/*bot.sleep(TIME_5S);
//		bot.sleep(TIME_5S);
//		bot.sleep(TIME_5S);
//		bot.sleep(TIME_5S);
//		bot.sleep(TIME_5S);*/
//	}
}
