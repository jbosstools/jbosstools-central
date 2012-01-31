package org.jboss.tools.central.test.ui.bot.helper;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.ui.IEditorReference;
import org.jboss.tools.ui.bot.ext.parts.SWTBotEditorExt;

public class SWTJBossCentralEditorExt extends SWTBotEditorExt{

	public SWTJBossCentralEditorExt(IEditorReference editorReference,
			SWTWorkbenchBot bot) throws WidgetNotFoundException {
		super(editorReference, bot);
	}
	
	public String getPages(){
		List<? extends CTabFolder> pokus = this.findWidgets(widgetOfType(CTabFolder.class));
		for (CTabFolder cTabFolder : pokus) {
			return Integer.toString(cTabFolder.getItemCount());
		}
		return "konec";
	}

}
