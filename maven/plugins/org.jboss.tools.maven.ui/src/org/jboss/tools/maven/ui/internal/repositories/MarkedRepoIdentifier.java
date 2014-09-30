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
package org.jboss.tools.maven.ui.internal.repositories;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jboss.tools.maven.ui.Activator;
import org.jboss.tools.maven.ui.wizard.RepositoryWrapper;

/**
 * Identifies a local folder as a Maven repository if that folder contains a .jboss-maven-repository marker
 * 
 * @author Fred Bricon
 *
 */
class MarkedRepoIdentifier extends AbstractRepositoryIdentifier {

	@Override
	protected boolean matches(File rootDirectory) {
		File marker = getMarker(rootDirectory);
		return marker.isFile() && marker.canRead();
	}
	
	private File getMarker(File directory) {
		File marker = new File(directory, ".jboss-maven-repository"); //$NON-NLS-1$
		return marker;
	}

	private Properties loadProperties(File marker) {
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(marker);
			props.load(fis);
		} catch (IOException e) {
			Activator.log("Can't load properties from "+ marker+ " : " + e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			props = null;
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return props;
	}


	@Override
	protected RepositoryWrapper getRepository(File rootDirectory) {
		String url = getUrl(rootDirectory);
		if (url == null) {
			return null;
		}

		Properties props = loadProperties(getMarker(rootDirectory));
		if (props == null) {
			return null;
		}
		String name = props.getProperty("name");//$NON-NLS-1$
		String id = props.getProperty("repository-id");//$NON-NLS-1$
		String profileId = props.getProperty("profile-id");//$NON-NLS-1$
		if (id == null || id.trim().isEmpty()) {
			if (name == null || name.trim().isEmpty()) {
				//nothing we can do here
				return null;
			}
			id = name.trim().toLowerCase().replace(' ', '-');
		}
		if (name == null || name.trim().isEmpty()) {
			name = id;
		}
		if (profileId == null || profileId.trim().isEmpty()) {
			profileId = id;
		}
		SettingsRepositoryBuilder  builder = new SettingsRepositoryBuilder()
		.setId(id.trim()) 
		.setName(name.trim())
		.setUrl(url);
		return new RepositoryWrapper(builder.get(), profileId.replace(' ', '-'));
	}
}
