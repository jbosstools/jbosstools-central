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
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.jdf.stacks.parser.Parser;


public class StacksManager {

	private static final String STACKS_URL;
	
	static {
		String defaultUrl = "http://raw.github.com/jboss-jdf/jdf-stack/1.0.0.CR1/stacks.yaml"; //$NON-NLS-1$
		STACKS_URL = System.getProperty("org.jboss.examples.stacks.url", defaultUrl); //$NON-NLS-1$
	}
	
	public Stacks getStacks(IProgressMonitor monitor) throws MalformedURLException {
		Stacks stacks = null;
		File f = getProjectExamplesFile(new URL(STACKS_URL), "stacks", "yaml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		if (f != null && f.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				Parser p  = new Parser();
				stacks =  p.parse(fis);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					fis.close();
				} catch(Exception e) {
					//ignore
				}
			}
		}
		return stacks;
		
	}
}
