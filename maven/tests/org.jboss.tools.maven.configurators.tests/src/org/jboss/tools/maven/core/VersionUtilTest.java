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
package org.jboss.tools.maven.core;

import static org.jboss.tools.maven.core.VersionUtil.getMajorMinorVersion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


public class VersionUtilTest {

	@Test
	public void testGetMajorMinorVersion() {
		assertNull(getMajorMinorVersion(null));
		assertEquals("1.0", getMajorMinorVersion("1.0-SP3"));
		assertEquals("1.0", getMajorMinorVersion("1.0"));
		assertEquals("1.1", getMajorMinorVersion("1.1.1"));
		assertEquals("1", getMajorMinorVersion("1"));
		assertEquals("x.y.z", getMajorMinorVersion("x.y.z"));
	}
}
