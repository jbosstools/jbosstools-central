/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.fixes;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * 
 * @author Fred Bricon
 * 
 */
public class WTPRuntimeFixTest {

	@Test
	public void parseRuntimeKeys() {

		String[] values = new String[] { "fo.o.o{bar:[a,b)}", "f.o.o{GATEIN:(3.6)}", "f.o.o", "azdea{az:1.2}" };

		StringBuilder input = new StringBuilder();
		int k = 0;
		for (String s : values) {
			input.append(s).append(",");
			if (++k % 2 == 1) {
				input.append(" ");
			}
		}

		List<String> result = WTPRuntimeFix.parseRuntimeKeys(input.toString());

		assertEquals(values.length, result.size());
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[i], result.get(i));
		}
	}
}
