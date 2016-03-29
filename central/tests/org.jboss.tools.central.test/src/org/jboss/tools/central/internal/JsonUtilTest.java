/*************************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class JsonUtilTest {

	@Test
	public void testJsonifyStrings() {
		assertEquals("[\"foo\",\"bar\",\"yee haa\"]", JsonUtil.jsonify(Arrays.asList("foo", "bar", "yee haa")));
	}
}
