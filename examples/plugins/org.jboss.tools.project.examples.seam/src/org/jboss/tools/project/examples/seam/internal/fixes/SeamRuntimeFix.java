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
package org.jboss.tools.project.examples.seam.internal.fixes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.project.examples.fixes.AbstractRuntimeFix;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.jboss.tools.project.examples.seam.internal.Messages;
import org.jboss.tools.project.examples.seam.internal.SeamProjectExamplesActivator;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.core.project.facet.SeamRuntime;
import org.jboss.tools.seam.core.project.facet.SeamRuntimeManager;
import org.jboss.tools.seam.internal.core.project.facet.ISeamFacetDataModelProperties;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Configures required Seam runtime to a project, if available.
 *
 * @author snjeza
 * @author Fred Bricon
 */
public class SeamRuntimeFix extends AbstractRuntimeFix {

	SeamRuntimeFix(ProjectExample project, RequirementModel requirement) {
		super(project, requirement);
	}

	private SeamRuntime getBestRuntime() {
		Collection<String> allowedVersions = getAllowedVersions();
		if (allowedVersions.isEmpty()) {
			SeamProjectExamplesActivator.log(NLS.bind(Messages.SeamRuntimeFix_Invalid_seam_runtime_fix, project.getName()));
			return null;
		}
		SeamRuntime[] seamRuntimes = SeamRuntimeManager.getInstance().getRuntimes();
		if (seamRuntimes == null || seamRuntimes.length == 0) {
			return null;
		}
		if (ANY.equals(allowedVersions.iterator().next())) {
			return seamRuntimes[0];
		}
		for (String allowedVersion : allowedVersions) {
			for (SeamRuntime seamRuntime : seamRuntimes) {
				if (seamRuntime.getVersion().toString().equals(allowedVersion.substring(0, 3))) {
					return seamRuntime;
				}
			}
		}
		return null;
	}

	@Override
	public boolean fix(IProgressMonitor monitor) {
		IProject[] offsprings = ProjectExamplesActivator.getEclipseProject(project, requirement);
		if (offsprings.length == 0) {
			return false;
		}
		SeamRuntime[] seamRuntimes = SeamRuntimeManager.getInstance().getRuntimes();
		SeamRuntime bestRuntime = getBestRuntime();
		boolean ret = true;
		for (IProject eclipseProject : offsprings) {
			if (!fix(eclipseProject, seamRuntimes, bestRuntime)) {
				ret = false;
			}
		}
		return ret;
	}

	private boolean fix(IProject eclipseProject, SeamRuntime[] seamRuntimes, SeamRuntime bestRuntime) {
		IEclipsePreferences prefs = SeamCorePlugin.getSeamPreferences(eclipseProject);
		String seamRuntimeName = prefs.get(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME, null);
		if (seamRuntimeName != null) {
			for (SeamRuntime seamRuntime : seamRuntimes) {
				if (seamRuntimeName.equals(seamRuntime.getName())) {
					return true;
				}
			}
		}
		if (bestRuntime != null) {
			prefs.put(ISeamFacetDataModelProperties.SEAM_RUNTIME_NAME, bestRuntime.getName());
			try {
				prefs.flush();
				return true;
			} catch (BackingStoreException e) {
				SeamProjectExamplesActivator.log(e);
			}
		}
		return false;
	}

	public Collection<String> getAllowedVersions() {
		return splitProperty(RequirementModel.ALLOWED_VERSIONS);
	}

	@Override
	public boolean isSatisfied() {
		return getBestRuntime() != null;
	}

	public String getDownloadId() {
		return requirement.getProperties().get(RequirementModel.DOWNLOAD_ID);
	}

	@Override
	public List<DownloadRuntime> getDownloadRuntimes(IProgressMonitor monitor) {
		String downloadId = getDownloadId();
		if (downloadId != null && !downloadId.trim().isEmpty()) {
			DownloadRuntime dr = RuntimeCoreActivator.getDefault().findDownloadRuntime(downloadId, monitor);
			if (dr != null) {
				return Collections.singletonList(dr);
			}
		}
		return Collections.emptyList();
	}
}
