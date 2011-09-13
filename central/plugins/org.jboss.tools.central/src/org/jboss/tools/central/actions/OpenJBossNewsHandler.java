package org.jboss.tools.central.actions;

import org.jboss.tools.central.JBossCentralActivator;

public class OpenJBossNewsHandler extends OpenWithBrowserHandler {

	@Override
	public String getLocation() {
		return JBossCentralActivator.NEWS_URL;
	}

}
