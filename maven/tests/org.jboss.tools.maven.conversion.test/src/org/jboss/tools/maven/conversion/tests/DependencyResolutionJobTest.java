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
package org.jboss.tools.maven.conversion.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.maven.model.Dependency;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.maven.conversion.ui.internal.jobs.DependencyResolutionJob;
import org.jboss.tools.maven.conversion.ui.internal.jobs.IdentificationJob.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DependencyResolutionJobTest {

	private DependencyResolutionJob job;

	@Before
	public void setUp() {
		job = new DependencyResolutionJob("resolution");
		job.setRequestedProcess(Task.RESOLUTION_ONLY);
	}
	
	@After
	public void tearDown() {
		job = null;
	}
	
	@Test
	public void testIdentifyValidDependency() throws Exception {
		Dependency d = new Dependency();
		d.setArtifactId("javax.inject");
		d.setGroupId("javax.inject");
		d.setVersion("1");
		job.setDependency(d);
		job.schedule();
		job.join();
		assertEquals(Status.OK_STATUS, job.getResult());
		assertTrue(job.isResolvable());
	}

	@Test
	public void testIdentifyUnknownDependency() throws Exception {
		Dependency d = new Dependency();
		d.setType(null);
		//Dependency with null values
		job.setDependency(d);
		job.schedule();
		//shouldn't crash
		job.join();
		assertEquals(Status.OK_STATUS, job.getResult());
		assertFalse(job.isResolvable());

		d.setArtifactId("foo");
		d.setGroupId("gradel-core");
		d.setVersion("1");
		job.setDependency(d);
		job.schedule();
		job.join();
		assertEquals(Status.OK_STATUS, job.getResult());
		assertFalse(job.isResolvable());
	}

}
