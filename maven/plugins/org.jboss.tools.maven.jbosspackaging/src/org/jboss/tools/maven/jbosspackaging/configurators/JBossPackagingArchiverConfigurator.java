/*************************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
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
