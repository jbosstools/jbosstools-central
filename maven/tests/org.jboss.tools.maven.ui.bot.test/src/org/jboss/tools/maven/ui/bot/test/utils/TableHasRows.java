package org.jboss.tools.maven.ui.bot.test.utils;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

public class TableHasRows extends DefaultCondition {
	  private final String projectType;
	  private final SWTBotTable table;

	  // initialize
	  public TableHasRows(SWTBotTable table, String projectType) {
	    this.table = table;
	    this.projectType = projectType;
	  }

	  // return true if the condition matches, false otherwise
	  public boolean test() {
		return table.indexOf(projectType, "Artifact Id")!=-1;
	  }

	  // provide a human readable error message
	  public String getFailureMessage() {
	    return "Timed out waiting for " + table + " to contain rows.";
	  }
	}