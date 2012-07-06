package org.jboss.tools.maven.jbosspackaging.configurators;

import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.sonatype.m2e.mavenarchiver.internal.JarArchiverConfigurator;

public abstract class JBossPackagingArchiverConfigurator extends JarArchiverConfigurator {

	@Override
	protected MojoExecutionKey getExecutionKey() {
		MojoExecutionKey key = new MojoExecutionKey("org.codehaus.mojo", "jboss-packaging-maven-plugin", "", getGoal(),
				null, null);
		return key;
	}

	protected abstract String getGoal();

}
