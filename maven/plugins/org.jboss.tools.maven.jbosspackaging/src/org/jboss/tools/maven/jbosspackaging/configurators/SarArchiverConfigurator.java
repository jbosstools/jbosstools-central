package org.jboss.tools.maven.jbosspackaging.configurators;


public class SarArchiverConfigurator extends JBossPackagingArchiverConfigurator {

	@Override
	protected String getGoal() {
		return "sar";
	}
}
