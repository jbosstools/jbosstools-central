package org.jboss.tools.maven.jsf.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class FacesConfigQuickPeekTest extends TestCase {

	public void testGetVersion() {
		assertVersion("2.0", getInputStream("<faces-config version=\"2.0\"></faces-config>"));
		assertVersion("1.2", getInputStream("<faces-config version=\"1.2\"></faces-config>"));
		assertVersion(null, getInputStream("<faces-config version=\"\"></faces-config>"));
		assertVersion(null, getInputStream("<faces-config></faces-config>"));
		assertVersion(null, getInputStream("<faces-config version=\"2.0\""));
		assertVersion(null, getInputStream(""));
		assertVersion(null, getInputStream("<dummy version=\"3.0\">"));
	}

	public void testGetVersionFromDTD() {
		assertVersion("1.0", getInputStream("<!DOCTYPE faces-config PUBLIC \"-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN\" \"http://java.sun.com/dtd/web-facesconfig_1_0.dtd\"><faces-config></faces-config>"));
		assertVersion("1.1", getInputStream("<!DOCTYPE faces-config PUBLIC \"-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN\" \"http://java.sun.com/dtd/web-facesconfig_1_1.dtd\"><faces-config></faces-config>"));
		assertVersion("1.2", getInputStream("<faces-config xmlns=\"http://java.sun.com/xml/ns/javaee\""
										              +  " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
										              +  " xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_1_2.xsd\" >"));
		assertVersion("2.0", getInputStream("<faces-config xmlns=\"http://java.sun.com/xml/ns/javaee\""
													  +  " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
													  +  " xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_2_0.xsd\" >"));
	}

	private InputStream getInputStream(String s) {
		try {
			return (s == null)? null :new ByteArrayInputStream(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void assertVersion(String expectedVersion, InputStream in) {
		FacesConfigQuickPeek qp = new FacesConfigQuickPeek(in);
		assertEquals(expectedVersion, qp.getVersion());
	}
}
