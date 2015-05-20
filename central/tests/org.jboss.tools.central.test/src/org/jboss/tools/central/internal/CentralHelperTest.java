package org.jboss.tools.central.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.central.ShowJBossCentral;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CentralHelperTest {
	
	private static String openCentral;
	
	@BeforeClass
	public static void beforeClass() {
		openCentral = System.getProperty(ShowJBossCentral.ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW);
		System.setProperty(ShowJBossCentral.ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW, Boolean.TRUE.toString());
		System.setProperty("usage_reporting_enabled", Boolean.FALSE.toString());
	}
	
	@AfterClass
	public static void afterClass() {
		if (openCentral != null) {
			System.setProperty(ShowJBossCentral.ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW, openCentral);
		} else {
			System.clearProperty(ShowJBossCentral.ORG_JBOSS_TOOLS_CENTRAL_DONOTSHOW);
			
		}
	}
	
	@Test
	public void testGetCentralSyspropUrl() throws Exception {
		String url;
		try {
			url = "/foo/bar/central";
			System.setProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY, url);
			assertEquals(url+"/index.html", CentralHelper.getCentralUrl(null));

			url = "file:///foo/bar/central/index.html";
			System.setProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY, url);
			assertEquals(url, CentralHelper.getCentralUrl(null));

		
			url = "http://central.jboss.org/";
			System.setProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY, url);
			assertEquals(url+"index.html", CentralHelper.getCentralUrl(null));
		} finally {
			System.clearProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY);
		}
	}

	@Test
	public void testGetCentralUrlZip() throws Exception {
		Path zip = Paths.get("test-resources", "central.zip");
		String url = zip.toUri().toString();
		String sha1 = "566caf";
		String expectedEnd = ".metadata"+File.separator+".plugins"+File.separator+"org.jboss.tools.central"+File.separator+"versions"+File.separator+sha1+File.separator+"index.html";
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			String resolvedUrl = CentralHelper.getCentralUrl(url, monitor);
			assertTrue("Unexpected resolvedUrl" + resolvedUrl, resolvedUrl.endsWith(expectedEnd));
			
			//different url, same file, returns same sha1 based url
			zip = Paths.get("test-resources", "same-central.zip");
		    url = zip.toUri().toString();
		    String resolvedUrl2 = CentralHelper.getCentralUrl(url, monitor);
			
		    assertEquals(resolvedUrl, resolvedUrl2);
			
		} finally {
			System.clearProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY);
		}
	}

	@Test
	public void testGetCentralFallbackSyspropUrl() throws Exception {
		String url;
		try {
			url = "/foo/bar/central";
			System.setProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY, url);
			assertEquals(url+"/legacy.html", CentralHelper.getCentralFallbackUrl(null));

			url = "file:///foo/bar/central/index.html";
			System.setProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY, url);
			assertEquals("file:///foo/bar/central/legacy.html", CentralHelper.getCentralFallbackUrl(null));

		
			url = "http://central.jboss.org/";
			System.setProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY, url);
			assertEquals(url+"legacy.html", CentralHelper.getCentralFallbackUrl(null));
		} finally {
			System.clearProperty(CentralHelper.JBOSS_CENTRAL_WEBPAGE_URL_KEY);
		}
	}
	
	//TODO setup jetty server to server remote URL and test caching capabilities
}
