/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.internal.search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.classpath.core.jee.AbstractClasspathContainer;
import org.jboss.ide.eclipse.as.classpath.core.jee.AbstractClasspathContainerInitializer;
import org.jboss.ide.eclipse.as.classpath.core.xpl.ClasspathDecorations;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.containers.JBossSourceContainer;
import org.jboss.tools.maven.sourcelookup.internal.util.SourceLookupUtil;

/**
 * 
 * @author snjeza
 *
 */
public class JBossServerSearchInitializer extends
		AbstractClasspathContainerInitializer {

	public static final String ID = "org.jboss.tools.maven.sourcelookup.JBossServerJavaSearchInitializer"; //$NON-NLS-1$
	private static final String JBOSS_SERVERS_CONTAINER = "JBoss Servers Container";

	public String getDescription(IPath containerPath, IJavaProject project) {
		return JBOSS_SERVERS_CONTAINER;
	}

	@Override
	protected AbstractClasspathContainer createClasspathContainer(IPath path) {
		return new JBossServerSearchContainer(path, javaProject);
	}

	@Override
	protected String getClasspathContainerID() {
		return ID;
	}

	private class JBossServerSearchContainer extends AbstractClasspathContainer {

		public JBossServerSearchContainer(IPath path, IJavaProject project) {
			super(path, JBOSS_SERVERS_CONTAINER, null, project);
		}

		@Override
		protected IClasspathEntry[] computeEntries() {
			ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

			Set<File> jars = new HashSet<File>();
			List<IServer> servers = SourceLookupUtil.getServers();
			for (IServer server:servers) {
				if (server != null) {
					try {
						IJBossServer jbossServer = ServerConverter
								.checkedGetJBossServer(server);
						if (jbossServer != null) {
							IJBossServerRuntime runtime = jbossServer.getRuntime();
							if (runtime != null) {
								IPath location = runtime.getRuntime().getLocation();
								JBossSourceContainer container = new JBossSourceContainer(location.toOSString());
								jars.addAll(container.getJars());
							}
						}
					} catch (CoreException e) {
						//SourceLookupActivator.log(e);
					}
				}
			}

			for (File jarFile : jars) {
				IPath entryPath = new Path(jarFile.getAbsolutePath());

				IPath sourceAttachementPath = null;
				IPath sourceAttachementRootPath = null;

				final ClasspathDecorations dec = decorations.getDecorations(
						getDecorationManagerKey(getPath().toString()),
						entryPath.toString());

				IClasspathAttribute[] attrs = {};
				if (dec != null) {
					sourceAttachementPath = dec.getSourceAttachmentPath();
					sourceAttachementRootPath = dec
							.getSourceAttachmentRootPath();
					attrs = dec.getExtraAttributes();
				}

				IAccessRule[] access = {};
				IClasspathEntry entry = JavaCore.newLibraryEntry(entryPath,
						sourceAttachementPath, sourceAttachementRootPath,
						access, attrs, false);
				entries.add(entry);
			}
			

			return entries.toArray(new IClasspathEntry[entries.size()]);
		}

		@Override
		public void refresh() {
			new JBossServerSearchContainer(path,javaProject).install();
		}

	}
}
