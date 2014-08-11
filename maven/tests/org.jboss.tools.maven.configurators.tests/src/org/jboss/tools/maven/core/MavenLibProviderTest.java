/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MavenLibProviderTest {
	
	@Parameters
	public static Collection<?> loadLibProviders() {
	
		List<ILibraryProvider[]> mavenProviders = new ArrayList<ILibraryProvider[]>();
		for (ILibraryProvider lp : LibraryProviderFramework.getProviders() ) {
            if (!lp.isAbstract() && "maven-library-provider".equals(lp.getRootProvider().getId())) {
            	mavenProviders.add(new ILibraryProvider[]{lp});
            }
		}
		return mavenProviders;
	}
	
	ILibraryProvider mavenProvider;
	
	public MavenLibProviderTest(ILibraryProvider provider) {
		mavenProvider = provider;
	}

	@Test
	public void testTemplateLoaded() {
			Map<String, String> params = mavenProvider.getParams();
			String pomURLString = params.get("template"); //$NON-NLS-1$
			assertNotNull("null template for "+mavenProvider.getLabel() , pomURLString);
			System.out.println("testing " + mavenProvider.getLabel() + " : "+pomURLString);
		try {
			URL platformURL = new URL(pomURLString);
			URL url = FileLocator.resolve(platformURL);
			assertNotNull(pomURLString + " can not be read", MavenCoreActivator.loadResource(url));
		} catch (Throwable t) {
			t.printStackTrace();
			fail("provider " + mavenProvider.getId() + " template can not be loaded. Cause : "+t.getMessage());
		}
	}
}
