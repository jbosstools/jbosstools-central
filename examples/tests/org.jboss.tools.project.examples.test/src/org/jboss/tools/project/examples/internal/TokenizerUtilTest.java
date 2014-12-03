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
package org.jboss.tools.project.examples.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.Test;

public class TokenizerUtilTest {

	@Test
	public void testSplitToList() {
		assertNull(TokenizerUtil.splitToList(null));
		assertEquals(Collections.emptyList(), TokenizerUtil.splitToList("  "));
		assertEquals(Arrays.asList("a","b","c","b"), TokenizerUtil.splitToList("a,b ,   c, b,"));
	}

	@Test
	public void testSplitToSet() {
		assertNull(TokenizerUtil.splitToSet(null));
		assertEquals(Collections.emptySet(), TokenizerUtil.splitToSet("  "));
		assertEquals(new LinkedHashSet<String>(Arrays.asList("a","b","c")), TokenizerUtil.splitToSet("a,b ,   c, b,"));
	}

}
