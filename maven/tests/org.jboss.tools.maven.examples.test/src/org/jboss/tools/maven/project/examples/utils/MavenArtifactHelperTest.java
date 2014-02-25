/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.junit.Test;

public class MavenArtifactHelperTest {

	@Test
	public void testCheckRequirementsAvailable() {
		ProjectExample project = new ProjectExample();
		IStatus status = MavenArtifactHelper.checkRequirementsAvailable(project);
		assertEquals(Status.OK_STATUS, status);

		Set<String> deps = new LinkedHashSet<String>(2);
		deps.add("org.wildfly.bom:jboss-javaee-7.0-with-tools:pom:8.0.0.Final");
		deps.add("foo.bar.bom.eap:jboss-javaee-6.0-with-tools:pom:6.2.0-build-5");
		
		project.setEssentialEnterpriseDependencyGavs(deps);
		status = MavenArtifactHelper.checkRequirementsAvailable(project);

		assertTrue(status.matches(Status.ERROR));
		assertEquals("This project has a dependency on foo.bar.bom.eap:jboss-javaee-6.0-with-tools:pom:6.2.0-build-5, which cannot be found. This indicates you do not have access to the proper Maven repository or that repository is incomplete.\nThis can cause build problems. This can be fixed by adding the recommended <a>repository</a> in your settings.xml.", status.getMessage());
	}
	
}
