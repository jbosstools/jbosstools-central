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
package org.jboss.tools.maven.core.identification;

import static org.jboss.tools.maven.core.identification.IdentificationUtil.getSHA1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Test;

public class IdentificationUtilTest {

	@Test
	public void testSHA1() throws Exception {

		assertNull(getSHA1(null));

		assertEquals("e4f1766ce7404a08f45d859fb9c226fc9e41a861", 
				      getSHA1(new File("resources/junit_4_10.jar")));
	}
	
}
