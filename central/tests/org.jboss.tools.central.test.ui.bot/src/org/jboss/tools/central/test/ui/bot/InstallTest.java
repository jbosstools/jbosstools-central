package org.jboss.tools.central.test.ui.bot;

import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.jboss.tools.ui.bot.ext.RequirementAwareSuite;
import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.condition.TaskDuration;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(RequirementAwareSuite.class)
@SuiteClasses({CentralAllBotTests.class})
public class InstallTest extends SWTTestExt{
	
	@BeforeClass
	public static void setup(){
		bot.menu("Help").menu("JBoss Central").click();
		assertEquals(bot.activeEditor().getTitle(), bot.editorByTitle("JBoss Central").getTitle());
	}
	
	@Test
	public void triggerInstall(){
		bot.cTabItem("Software/Update").activate();
		util.waitForAllExcept(TaskDuration.LONG.getTimeout(), "Usage Data Event consumer");
		List<SWTBotCheckBox> checkBoxy = bot.checkBoxes();
		for (SWTBotCheckBox swtBotCheckBox : checkBoxy) {
			if (swtBotCheckBox.getText().equals(" ")){
				swtBotCheckBox.click();
			}
		}
		bot.clickButton("Install");
		bot.sleep(2000);
	}
	
}
