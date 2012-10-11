package org.jboss.tools.central.test.ui.bot;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.jboss.tools.ui.bot.ext.SWTFormsBotExt;
import org.jboss.tools.ui.bot.ext.condition.TaskDuration;
import org.jboss.tools.ui.bot.ext.view.ProblemsView;

public class CreateProjectsWithJava7Test extends CreateProjectsWithServerTest{
	
	/**
	 * Checks given example and tries to set compilation level to Java 1.7
	 */
	
	@Override
	protected void checkExample(SWTFormsBotExt formsBot, String formText,
			boolean readme, String projectName, String readmeFileName) {
		
		super.checkExample(formsBot, formText, readme, projectName, readmeFileName);
		projectExplorer.show();
		formText = formText.replaceAll("\\s", "");
		if (formText.equals("JavaEEProject")){
			setupMultiProject(formText);
		}else{
			setupProject((projectName==null) ? formText : projectName, true, true);
		}
		if (ProblemsView.getErrorsNode(bot) != null){
			for (String error : ProblemsView.getErrorsNode(bot).getNodes()) {
				log.error("Found error in Problems View: "+error);
			}
			fail("There should be no errors in Problems view");
		}
	}
	
	private void setupMultiProject(String formText){
		setupProject(formText+"-ear", false, true);
		setupProject(formText+"-ejb", true, true);
		setupProject(formText+"-web", true, true);
	}
	
	private void setupProject(String formText, boolean compiler, boolean facets){
		projectExplorer.bot().tree().getTreeItem(formText).select();
		projectExplorer.bot().tree().getTreeItem(formText).pressShortcut(SWT.ALT, SWT.CR, SWT.LF);
		bot.waitForShell("Properties for " + formText);
		if (compiler) setupCompiler(formText);
		if (facets) setupFacets(formText);
		bot.button("OK").click();
		bot.waitForShell("Compiler Settings Changed");
		if (bot.activeShell().getText().equals("Compiler Settings Changed")){
			bot.button("Yes").click();
		}
		util.waitForNonIgnoredJobs();
		bot.waitWhile(shellIsActive("Properties for " + formText), TaskDuration.VERY_LONG.getTimeout());
		util.waitForNonIgnoredJobs(TaskDuration.VERY_LONG.getTimeout());
	}
	
	private void setupFacets(String formText){
		bot.tree().getTreeItem("Project Facets").select();
		bot.tree(1).getTreeItem("Java").click(1);
		SWTBotShell facetsShell = null;
		for (SWTBotShell shell : bot.shells()) {
			if (shell.getText().isEmpty()){
				try{
					shell.bot().list();
					facetsShell = shell;
				}catch (WidgetNotFoundException ex){
					//nothing to do here
				}
			}
		}
		if (facetsShell == null){
			fail("Unable to select Java 1.7 in Project Facets");
		}else{
			facetsShell.bot().list().select("1.7");
			facetsShell.bot().list().pressShortcut(SWT.CR, SWT.LF);
		}
	}
	/**
	 * Properties window should be open by the time, this method is called
	 * @param formText
	 */
	private void setupCompiler(String formText){
		bot.tree().getTreeItem("Java Compiler").select();
		bot.checkBox(1).deselect();
		bot.comboBox().setSelection("1.7");
	}
}
