/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.configurators.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.eclipse.core.resources.IProject;
import org.hibernate.eclipse.console.properties.HibernatePropertiesConstants;
import org.junit.Test;

@SuppressWarnings("restriction")
public class HibernateConfiguratorTest extends AbstractMavenConfiguratorTest {

	@Test
	public void testJBIDE11570_constraintViolations() throws Exception {
		IProject project = importProject("projects/hibermate/war-hibernate/pom.xml");
		waitForJobsToComplete();
		assertNoErrors(project);
		assertIsNotHibernateProject(project);
		
		updateProject(project, "enable-hibernate.pom");
		assertIsHibernateProject(project);
	}

	private void assertIsHibernateProject(IProject project) throws Exception {
		assertTrue(project.getName() +" should have the Hibernate nature", project.hasNature(HibernatePropertiesConstants.HIBERNATE_NATURE));
	}

	private void assertIsNotHibernateProject(IProject project) throws Exception {
		assertFalse(project.getName() +" should not have the Hibernate nature", project.hasNature(HibernatePropertiesConstants.HIBERNATE_NATURE));		
	}
}
