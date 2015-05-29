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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.tools.as.runtimes.integration.util.RuntimeMatcher;
import org.jboss.tools.project.examples.model.AbstractProjectFix;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;

/**
 * 
 * @author snjeza
 * @author Fred Bricon
 */
public abstract class AbstractRuntimeFix extends AbstractProjectFix implements IDownloadRuntimeProvider {

	protected final static String ANY = "any"; //$NON-NLS-1$
	private static final Pattern RUNTIMES_PATTERN = Pattern.compile("[A-Za-z0-9._-]*(\\{[-A-Za-z0-9._:,\\(\\[\\]\\)]+\\})?"); //$NON-NLS-1$
	
	public AbstractRuntimeFix(ProjectExample project, RequirementModel requirement) {
		super(project, requirement);
	}
	
	
	/** Not an API. Public for testing purposes only. */
	public static List<String> parseRuntimeKeys(String allRuntimeKeys) {
		if (allRuntimeKeys == null || allRuntimeKeys.trim().isEmpty()) {
			return Collections.emptyList();
		}
		List<String> runtimeKeys = new ArrayList<String>();
		Matcher m = RUNTIMES_PATTERN.matcher(allRuntimeKeys);
		while (m.find()) {
			String group = m.group(0);
			if (!group.isEmpty()) {
				runtimeKeys.add(group);
			}
		}
		return runtimeKeys;
	}

	public static IRuntime[] getRuntimesFromPattern(String allRuntimeIds) {
		List<String> runtimeIds = parseRuntimeKeys(allRuntimeIds);
		if (runtimeIds.isEmpty()) {
			return new IRuntime[0];
		}
		RuntimeMatcher runtimeMatcher = new RuntimeMatcher();
		List<IRuntime> runtimes = new ArrayList<>(runtimeIds.size());
		for (String key : runtimeIds) {
			for (IRuntime r : runtimeMatcher.findExistingRuntimes(key)){
			   runtimes.add(r);
			}
		}
		IRuntime[] aRuntimes = new IRuntime[runtimes.size()];
		return runtimes.toArray(aRuntimes );
	}
	
	public static DownloadRuntime[] getDownloadRuntimesFromPattern(String allDownloadRuntimeIds, IProgressMonitor monitor) {
		List<String> runtimeIds = parseRuntimeKeys(allDownloadRuntimeIds);
		if (runtimeIds.isEmpty()) {
			return new DownloadRuntime[0];
		}
		RuntimeMatcher runtimeMatcher = new RuntimeMatcher();
		List<DownloadRuntime> runtimes = new ArrayList<>(runtimeIds.size());
		for (String key : runtimeIds) {
			for (DownloadRuntime dr : runtimeMatcher.findDownloadRuntimes(key, monitor)){
			   runtimes.add(dr);
			}
		}
		DownloadRuntime[] aRuntimes = new DownloadRuntime[runtimes.size()];
		return runtimes.toArray(aRuntimes );
	}

	public Collection<DownloadRuntime> getDownloadRuntimes(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		final String downloadId = requirement.getProperties().get(RequirementModel.DOWNLOAD_ID);
		DownloadRuntime preferredDownload = null;
		if (downloadId != null) {
			preferredDownload = RuntimeCoreActivator.getDefault().getDownloadRuntimes(monitor).get(downloadId);

		}
		
		String allDownloadRuntimeIds = requirement.getProperties().get(RequirementModel.ALLOWED_TYPES);
		
		List<DownloadRuntime> runtimes = new ArrayList<>();
		if (allDownloadRuntimeIds != null && !ANY.equals(allDownloadRuntimeIds)) {
			DownloadRuntime[] downloadableRuntimes = getDownloadRuntimesFromPattern(allDownloadRuntimeIds, monitor);
			if (downloadableRuntimes != null && downloadableRuntimes.length > 0) {
				runtimes.addAll(Arrays.asList(downloadableRuntimes));
			}
			//FIXME : ANY not supported!!!
		}
		if (preferredDownload != null) {
			int i = runtimes.indexOf(preferredDownload);
			if (i < -1) {
				runtimes.add(preferredDownload);
			}else if (i > 0 ) {//move at the top
				runtimes.add(0, runtimes.remove(i));
			}
		}
		return runtimes;
	}
}
