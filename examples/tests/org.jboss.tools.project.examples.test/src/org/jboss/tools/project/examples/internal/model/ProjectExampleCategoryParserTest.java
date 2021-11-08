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
package org.jboss.tools.project.examples.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.project.examples.model.ProjectExampleCategory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fred Bricon
 */
public class ProjectExampleCategoryParserTest {

	private ProjectExampleCategoryParser parser;

	@Before
	public void setup() {
		parser = new ProjectExampleCategoryParser();
	}
	
	@Test
	public void parse_categories() throws Exception {
		File file = new File("resources/categories.xml");
		List<ProjectExampleCategory> result = parser.parse(file);
		assertNotNull(result);
		assertEquals(4, result.size());
		Collections.sort(result);
		assertEquals("Web Applications", result.get(0).getName());		
		assertEquals("Mobile Applications", result.get(1).getName());
		assertEquals("Portal Examples", result.get(2).getName());
		assertEquals("Other", result.get(3).getName());
	}	
	
	@Test
	public void parse_broken_categories() {
		File file = new File("resources/broken-categories.xml");
		try {
			parser.parse(file);
			fail("Should not be able to parse file");
		} catch (CoreException e) {
			assertEquals("Unable to parse categories in resources/broken-categories.xml",e.getMessage().replace('\\', '/'));
			assertEquals("javax.xml.bind.UnmarshalException", e.getCause().getClass().getName());
		}
	}
}
