/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.internal.identification;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;

public class MavenPropertiesIdentifier extends AbstractArtifactIdentifier {

	public MavenPropertiesIdentifier() {
		super("Maven Properties identifier");
	}

	public ArtifactKey identify(File file) throws CoreException {
		return identify(file, null);
	}
	
	public ArtifactKey identify(File file, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (monitor.isCanceled()) {
			return null;
		}
		ZipFile jar = null;
		try {
			jar = new ZipFile(file);
			monitor.setTaskName("Checking for maven properties in " + file.getName());
			return getArtifactFromMetaInf(jar);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	@SuppressWarnings("nls")
	protected static ArtifactKey getArtifactFromMetaInf(ZipFile jar) throws IOException {
		ZipEntry mavenEntry = jar.getEntry("META-INF/maven");//$NON-NLS-1$
		if (mavenEntry == null) {
			return null;
		}
		String entryName = mavenEntry.getName();
		Enumeration<? extends ZipEntry> zipEntries = jar.entries();
		ArtifactKey artifact = null;
		
		
		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = zipEntries.nextElement();
			if (zipEntry.getName().endsWith("pom.properties")
					&& zipEntry.getName().startsWith(entryName)) {
				if (artifact != null) {
					//org.fusesource.jansi:jansi:1.6 is an OSGi bundle containing several maven pom files.
					//The first properties being found is wrong.
					//So for the moment we bail but should try to look at the MANIFEST.MF
					return null;
				}
				Properties props = new Properties();
				props.load(jar.getInputStream(zipEntry));
				String groupId = props.getProperty("groupId");
				String artifactId = props.getProperty("artifactId");
				String version = props.getProperty("version");
				String classifier = props.getProperty("classifier");
				if (groupId != null && artifactId != null && version != null) {
					artifact = new ArtifactKey(groupId, artifactId, version,
							classifier);
				}
			}
		}
		
		return artifact;
	}
}
