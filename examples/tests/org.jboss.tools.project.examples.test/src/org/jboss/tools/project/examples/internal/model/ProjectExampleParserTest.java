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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import javax.xml.bind.UnmarshalException;

import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.project.examples.model.ArchetypeModel;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fred Bricon
 */
public class ProjectExampleParserTest {

	private ProjectExampleJaxbParser parser;

	@Before
	public void setup() {
		parser = new ProjectExampleJaxbParser();
	}
	
	@Test
	public void parse_single_example() throws Exception {
		File file = new File("resources/requirements.xml");
		List<ProjectExample> result = parser.parse(file);
		assertNotNull(result);
		assertEquals(1, result.size());
		ProjectExample errai = result.get(0);		
		assertEquals("errai", errai.getName());	
		List<RequirementModel> reqs = errai.getRequirements();
		assertEquals(1, reqs.size());
		RequirementModel wtpReq = reqs.get(0);
		assertEquals("wtpruntime", wtpReq.getType());
		assertEquals(3, wtpReq.getProperties().size());
	}
	
	
	@Test
	public void parse_examples() throws Exception {
		File file = new File("resources/examples.xml");
		List<ProjectExample> result = parser.parse(file);
		assertNotNull(result);
		assertEquals(5, result.size());
		assertEquals("errai", result.get(0).getName());	
		{
			ProjectExample numberguess = result.get(3);
			assertEquals("richfaces-archetype-simpleapp", numberguess.getName());
			assertEquals("", numberguess.getUrl());
		}
		{
			ProjectExample numberguess = result.get(4);	
			assertEquals("numberguess", numberguess.getName());
			assertEquals(" 2.10MB", numberguess.getSizeAsText());
			List<RequirementModel> reqs = numberguess.getRequirements();
			assertEquals(2, reqs.size());
			RequirementModel wtpReq = reqs.get(0);
			assertEquals("wtpruntime", wtpReq.getType());
			assertEquals("org.jboss.ide.eclipse.as.runtime.eap.43, org.jboss.ide.eclipse.as.runtime.42", wtpReq.getProperties().get("allowed-types"));
			assertEquals("http://anonsvn.jboss.org/repos/jbosstools/workspace/snjeza/seam-examples/numberguess.zip", numberguess.getUrl());
		}

	}	
	
	@Test
	public void parse_broken_examples() {
		File file = new File("resources/broken-examples.xml");
		try {
			parser.parse(file);
			fail("Should not be able to parse file");
		} catch (CoreException e) {
			assertEquals("Unable to parse examples in resources/broken-examples.xml",e.getMessage().replace('\\', '/'));
			assertEquals(UnmarshalException.class, e.getCause().getClass());
		}
	}	
}
