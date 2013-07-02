/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.configurators.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.hibernate.eclipse.console.properties.HibernatePropertiesConstants;
import org.hibernate.eclipse.console.utils.LaunchHelper;
import org.hibernate.eclipse.launch.IConsoleConfigurationLaunchConstants;
import org.junit.Test;


@SuppressWarnings("restriction")
public class HibernateConfiguratorTest extends JpaConfiguratorTest {

	@Test
	public void testJBDS2452_multiplePersistences() throws Exception {
		IProject project = importProject( "projects/hibernate/hibernate-3.5/pom.xml");
		waitForJobsToComplete();
		waitForJobsToComplete();
		assertIsJpaProject(project, JPA_FACET_VERSION_1_0);
		assertIsHibernateProject(project, "primary");
		assertNoErrors(project);
	}	
	
	private void assertIsHibernateProject(IProject project, String persistenceUnitName) throws CoreException {
		assertEquals(true, project.hasNature(HibernatePropertiesConstants.HIBERNATE_NATURE));
		ILaunchConfiguration[] launchConfigs = LaunchHelper.findProjectRelatedHibernateLaunchConfigs(project.getName());
		assertEquals(1, launchConfigs.length);
		assertEquals(persistenceUnitName, launchConfigs[0].getAttribute(IConsoleConfigurationLaunchConstants.PERSISTENCE_UNIT_NAME, ""));
	}
	
 }
