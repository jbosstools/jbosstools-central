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
package org.jboss.tools.maven.ui.internal.repositories;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.maven.ui.wizard.RepositoryWrapper;

/**
 * Identifies a local folder as a Maven repository. Searches, in order, for :
 * <ul>
 *  <li>a .maven-repository marker at the root, containing repository and profile informations</li>
 *  <li>a "com/sun/faces/jsf-impl" folder containing -redhat jars , that makes it an EAP repository</li>
 *  <li>a "com/redhat/jboss/wfk/boms/" folder, that makes it a WFK repository</li>
 * </ul>
 * 
 * @author Fred Bricon
 */
public class RepositoryIdentificationManager {
	
	List<ILocalRepositoryIdentifier> identifiers = new ArrayList<>(3);
	
	public RepositoryIdentificationManager() {
		identifiers.add(new MarkedRepoIdentifier());
		identifiers.add(new EAPRepoIdentifier());
		identifiers.add(new WFKRepoIdentifier());
		
	}
	
	public RepositoryWrapper identifyRepository(File directory, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		for (ILocalRepositoryIdentifier identifier : identifiers) {
			if (monitor.isCanceled()) {
				return null;
			}
			RepositoryWrapper repo = identifier.identifyRepository(directory, monitor);
			if (repo != null) {
				return repo;
			}
		}
		return null;
	}
	
	static class EAPRepoIdentifier extends AbstractRepositoryIdentifier {

		static final String JBOSS_EAP_MAVEN_REPOSITORY = "JBoss EAP Maven Repository"; //$NON-NLS-1$
		static final String JBOSS_EAP_MAVEN_REPOSITORY_ID = "jboss-eap-maven-repository";; //$NON-NLS-1$
		private static final String JSF_IMPL = "com" + File.separator + "sun" + File.separator + "faces" + File.separator + "jsf-impl";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		@Override
		protected boolean matches(File rootDirectory) {
			File jsfDir = new File(rootDirectory, JSF_IMPL);
			if (jsfDir.isDirectory()) {
				final boolean[] found = new boolean[1];
			
				jsfDir.listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File pathname) {
						if (pathname != null && pathname.getName() != null && pathname.getName().contains("redhat")) { //$NON-NLS-1$
							found[0] = true;
							return true;
						}
						return false;
					}
				});
				return found[0];
			}
			return false;
		}
	
		@Override
		protected RepositoryWrapper getRepository(File rootDirectory) {
			String url = getUrl(rootDirectory);
			if (url == null) {
				return null;
			}
			
			SettingsRepositoryBuilder  builder = new SettingsRepositoryBuilder()
			.setId(JBOSS_EAP_MAVEN_REPOSITORY_ID)
			.setName(JBOSS_EAP_MAVEN_REPOSITORY)
			.setUrl(url);
			
			return new RepositoryWrapper(builder.get());
		}
	}

    static class WFKRepoIdentifier extends AbstractRepositoryIdentifier {

		static final String JBOSS_WFK_MAVEN_REPOSITORY = "JBoss WFK Maven Repository"; //$NON-NLS-1$
		static final String JBOSS_WFK_MAVEN_REPOSITORY_ID = "jboss-wfk-maven-repository"; //$NON-NLS-1$
		private static final String WFK_BOMS = "com" + File.separator + "redhat" + File.separator + "jboss" + File.separator + "wfk" + File.separator + "boms";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		@Override
		protected boolean matches(File rootDirectory) {
			return new File(rootDirectory, WFK_BOMS).isDirectory();
		}


		@Override
		protected RepositoryWrapper getRepository(File rootDirectory) {
			String url = getUrl(rootDirectory);
			if (url == null) {
				return null;
			}
	
			SettingsRepositoryBuilder  builder = new SettingsRepositoryBuilder()
			.setId(JBOSS_WFK_MAVEN_REPOSITORY_ID)
			.setName(JBOSS_WFK_MAVEN_REPOSITORY)
			.setUrl(url);
			return new RepositoryWrapper(builder.get());
		}
		
	}
}
