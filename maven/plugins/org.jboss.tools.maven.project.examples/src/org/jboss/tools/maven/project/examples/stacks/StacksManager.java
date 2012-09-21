/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.stacks;

import static org.jboss.tools.project.examples.model.ProjectExampleUtil.getProjectExamplesFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.codehaus.plexus.util.IOUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.jdf.stacks.client.StacksClient;
import org.jboss.jdf.stacks.client.StacksClientConfiguration;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.jdf.stacks.parser.Parser;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;

public class StacksManager {

	private static final String STACKS_URL;

	static {
		String defaultUrl = getStacksUrlFromJar(); //$NON-NLS-1$
		STACKS_URL = System.getProperty(
				"org.jboss.examples.stacks.url", defaultUrl); //$NON-NLS-1$
	}

	public Stacks getStacks(IProgressMonitor monitor) {
		Stacks stacks = null;
		try {
			File f = getProjectExamplesFile(new URL(STACKS_URL),
					"stacks", "yaml", monitor);//$NON-NLS-1$ //$NON-NLS-2$
			if (f != null && f.exists()) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(f);
					Parser p = new Parser();
					stacks = p.parse(fis);
				} finally {
					IOUtil.close(fis);
				}
			}
		} catch (Exception e) {
			MavenProjectExamplesActivator.log(e,
					"Can't access or parse  " + STACKS_URL //$NON-NLS-1$
							+ ", falling back on default Stacks Client values"); //$NON-NLS-1$
			StacksClient client = new StacksClient();
			stacks = client.getStacks();
		}

		return stacks;

	}

	private static String getStacksUrlFromJar() {
		InputStream is = null;
		try {
			is = StacksManager.class.getResourceAsStream("/org/jboss/jdf/stacks/client/config.properties");
			Properties p = new Properties();
			p.load(is);
			return p.getProperty(StacksClientConfiguration.REPO_PROPERTY);
		} catch (Exception e) {
			System.err.println("Can't read stacks url from the stacks-client.jar");
			e.printStackTrace();
		} finally {
			IOUtil.close(is);
		}
		return null;
	}
	
	
}
