package org.jboss.tools.central.test.ui.bot;

import org.jboss.tools.ui.bot.ext.SWTTestExt;
import org.jboss.tools.ui.bot.ext.config.Annotations.Require;
import org.jboss.tools.ui.bot.ext.config.Annotations.ServerType;
import org.junit.Test;


@Require(server=@org.jboss.tools.ui.bot.ext.config.Annotations.Server(type=ServerType.JbossAS))
public class EmptyTestForHudson extends SWTTestExt {
	
	
	@Test
	public void isRuntimeTest(){
		assertTrue(jbt.isServerRuntimeDefined());
	}
}
