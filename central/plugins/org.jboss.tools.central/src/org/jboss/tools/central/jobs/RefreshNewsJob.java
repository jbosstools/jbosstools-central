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
package org.jboss.tools.central.jobs;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza
 *
 */
public class RefreshNewsJob extends AbstractRefreshJob {

	private static final String CACHE_FILE = "news.xml";
	public static RefreshNewsJob INSTANCE = new RefreshNewsJob();

	
	private RefreshNewsJob() {
		super("Refreshing JBoss News...", JBossCentralActivator.NEWS_URL);
	}

	@Override
	public File getCacheFile() {
		IPath location = JBossCentralActivator.getDefault().getStateLocation();
		File file = new File(location.toFile(), CACHE_FILE);
		return file;
	}
}
