/*******************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.cdi;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.jboss.tools.maven.cdi.internal.BeansXmlQuickPeek;
import org.junit.Test;

public class BeansXmlQuickPeekTest {

	@Test
	public void testGetVersion() {
		assertVersion("1.0",
				getInputStream("<beans version=\"1.0\"></beans>"));
		assertVersion("1.1",
				getInputStream("<beans version=\"1.1\"></beans>"));
		assertVersion(null,
				getInputStream("<beans version=\"\"></beans>"));
		assertVersion("99999", getInputStream("<beans version=\"99999\"></beans>"));
		assertVersion(null, getInputStream("<beans></beans>"));
		assertVersion(null, getInputStream(""));
	}

	@Test
	public void testGetVersionFromSchema() {
		assertVersion(
				"1.0",
				getInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
						"<beans xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
						"	xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd\">\r\n" + 
						"</beans>"));
		assertVersion(
				"1.0",
				getInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
						"<beans xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
						"	xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://docs.jboss.org/cdi/beans_1_0.xsd\">\r\n" + 
						"</beans>"));
		assertVersion(
				"1.1",
				getInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
						"<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\r\n" + 
						"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
						"       xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd\"\r\n" + 
						"       bean-discovery-mode=\"annotated\"></beans>"));

		assertVersion(
				null,
				getInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
						"<beans xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
						"	xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_9999_0.xsd\">\r\n" + 
						"</beans>"));
	}

	private InputStream getInputStream(String s) {
		try {
			return (s == null) ? null : new ByteArrayInputStream(
					s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void assertVersion(String expectedVersion, InputStream in) {
		BeansXmlQuickPeek qp = new BeansXmlQuickPeek(in);
		assertEquals(expectedVersion, qp.getVersion());
	}
}