/*************************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc. and others.
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
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.UnmarshalException;

import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.project.examples.model.IProjectExampleSite;
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fred Bricon
 */
public class ProjectExampleSiteParserTest {

	private ProjectExampleSiteParser parser;

	@Before
	public void setup() {
		parser = new ProjectExampleSiteParser();
	}
	
	@Test
	public void serialize_user_sites() throws Exception {
		Set<IProjectExampleSite> sites = buildSites();
		String result = parser.serialize(sites );
		String expected = buildXmlSites();
		assertEquals(expected, result);
	}
	
	@Test
	public void serialize_empty_user_sites() throws Exception {
		Set<IProjectExampleSite> sites = Collections.emptySet();
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sites/>";

		String result = parser.serialize(sites );
		assertEquals(expected, result);
		
		result = parser.serialize(null);
		assertEquals(expected, result);
	}

	@Test
	public void parse_broken_user_sites() {
		try {
			parser.parse("<sites>");
		} catch (CoreException e) {
			assertEquals("Unable to parse user example sites",e.getMessage().replace('\\', '/'));
			assertEquals(UnmarshalException.class, e.getCause().getClass());
		}
	}
	
	private String buildXmlSites() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<sites>"
				+ "<site url=\"file://foo/\" name=\"foo\" experimental=\"true\" editable=\"false\"/>"
				+ "<site url=\"file://bar/\" name=\"bar\" experimental=\"false\" editable=\"true\"/>"
				+ "</sites>";
	}

	@Test
	public void parse_user_sites() throws Exception {
		String sites = buildXmlSites();
		Set<IProjectExampleSite> result = parser.parse(sites);
		Set<IProjectExampleSite> expected = buildSites();
		assertEquals(expected, result);
	}
	
	@Test
	public void parse_empty_user_sites() throws Exception {
		assertNull(parser.parse(null));
		assertNull(parser.parse("  "));
	}
	
	private Set<IProjectExampleSite> buildSites() throws URISyntaxException {
		Set<IProjectExampleSite> sites = new LinkedHashSet<>();
		sites.add(createSite("foo", "file://foo/", true, false));
		sites.add(createSite("bar", "file://bar/", false, true));
		return sites;
	}
	
	
	private ProjectExampleSite createSite(String name, String uri, boolean experimental, boolean editable) throws URISyntaxException {
		ProjectExampleSite site = new ProjectExampleSite();
		site.setUrl(new URI(uri));
		site.setName(name);
		site.setExperimental(experimental);
		site.setEditable(editable);
		return site;
	}
}
