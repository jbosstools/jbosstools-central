/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.fixes;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * @author snjeza
 *
 */
public class SeamRuntimeFix implements ProjectExamplesFix {

	public boolean canFix(ProjectExample project, ProjectFix fix) {
		if (!ProjectFix.SEAM_RUNTIME.equals(fix.getType())) {
			return false;
		}
		return getBestRuntime(project, fix) != null;
	}

	private SeamRuntime getBestRuntime(ProjectExample project, ProjectFix fix) {
		String allowedVersions = fix.getProperties().get(ProjectFix.ALLOWED_VERSIONS);
		if (allowedVersions == null) {
			ProjectExamplesActivator.log(NLS.bind(Messages.SeamRuntimeFix_Invalid_seam_runtime_fix, project.getName())); 
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(allowedVersions,","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String allowedVersion = tokenizer.nextToken().trim();
			if (allowedVersion.length() <= 0) {
				continue;
			}
			SeamRuntime[] seamRuntimes = SeamRuntimeManager.getInstance().getRuntimes();
			if (seamRuntimes == null) {
				return null;
			}
			if (seamRuntimes.length > 0 && ProjectFix.ANY.equals(allowedVersion)) {
				return seamRuntimes[0];
			}
			for (int i = 0; i < seamRuntimes.length; i++) {
				SeamRuntime seamRuntime = seamRuntimes[i];
				if (seamRuntime.getVersion().toString().equals(allowedVersion.substring(0, 3))) {
					return seamRuntime;
				}
			}
		}
		return null;
	}

	public boolean fix(ProjectExample project, ProjectFix fix,
			IProgressMonitor monitor) {
		if (!canFix(project, fix)) {
			return false;
		}
		IProject[] eclipseProjects = ProjectExamplesActivator.getEclipseProject(project, fix);
		if (eclipseProjects.length == 0) {
			return false;
		}
		boolean ret = true;
		for (int i = 0; i < eclipseProjects.length; i++) {
			IProject eclipseProject = eclipseProjects[i];
			if (!fix(project, fix, eclipseProject)) {
				ret = false;
			}
		}
		return ret;
	}

	private boolean fix(ProjectExample project, ProjectFix fix,
			IProject eclipseProject) {
		IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(eclipseProject);
		String seamRuntimeName = prefs.get(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME, null);
		SeamRuntime[] seamRuntimes = SeamRuntimeManager.getInstance().getRuntimes();
		if (seamRuntimeName != null) {
			for (int i1 = 0; i1 < seamRuntimes.length; i1++) {
				if (seamRuntimeName.equals(seamRuntimes[i1].getName())) {
					return true;
				}
			}
		}
		SeamRuntime seamRuntime = getBestRuntime(project, fix);
		if (seamRuntime != null) {
			prefs.put(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME, seamRuntime.getName());
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				ProjectExamplesActivator.log(e);
				return false;
			}
			return true;
		}
		return false;
	}

}
