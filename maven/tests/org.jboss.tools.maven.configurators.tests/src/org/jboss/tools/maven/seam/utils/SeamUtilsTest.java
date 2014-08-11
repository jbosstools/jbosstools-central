/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.seam.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SeamUtilsTest {

	@Test
	public void testConversionSupported() {
		
		assertFalse(SeamUtils.isSeamConversionSupported("1.1"));
		assertTrue(SeamUtils.isSeamConversionSupported("2.0.Alpha1"));
		assertTrue(SeamUtils.isSeamConversionSupported("2.2.999-Final"));
		assertFalse(SeamUtils.isSeamConversionSupported("2.3.0-Alpha1"));
		assertFalse(SeamUtils.isSeamConversionSupported("2.3.0.Final"));
		assertFalse(SeamUtils.isSeamConversionSupported("3.0"));
	}
}
