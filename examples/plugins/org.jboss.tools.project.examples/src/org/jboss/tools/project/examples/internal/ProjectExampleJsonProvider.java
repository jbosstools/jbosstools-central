/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal;

import static org.jboss.tools.foundation.core.properties.PropertiesHelper.getPropertiesProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.foundation.core.ecf.URLTransportUtility;
import org.jboss.tools.project.examples.IProjectExampleProvider;
import org.jboss.tools.project.examples.internal.model.RequirementModelUtil;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;

@SuppressWarnings("nls")
public class ProjectExampleJsonProvider implements IProjectExampleProvider {
	
	private IProjectExampleParser parser ;
	
	public ProjectExampleJsonProvider() {
		this(new ProjectExampleJsonParser());
	}
	
	public ProjectExampleJsonProvider(IProjectExampleParser projectExampleParser) {
		Assert.isNotNull(projectExampleParser);
		parser = projectExampleParser;
	}

	@Override
	public Collection<ProjectExample> getExamples(IProgressMonitor monitor) throws CoreException {
		IPath cacheRoot = ProjectExamplesActivator.getDefault().getStateLocation().append("quickstarts-json");
		File jsonPayload = new URLTransportUtility().getCachedFileForURL(getExamplesUrl(), "Searching for Quickstarts", URLTransportUtility.CACHE_FOREVER, cacheRoot , monitor);
		if (jsonPayload == null || jsonPayload.length() == 0) {
			return Collections.emptyList();
		}
			
		Collection<ProjectExample> examples = null;
		try (InputStream json = new BufferedInputStream(new FileInputStream(jsonPayload))){
			examples = parser.parse(json, monitor);
			if (examples != null) {
				for (ProjectExample example : examples) {
					inferRequirements(example);
				}
			}
		} catch (IOException e) {
			if (monitor.isCanceled()) {
				ProjectExamplesActivator.log("Quickstart search was cancelled. Returning an empty list.");
			} else {
				IStatus status = new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
						"Unable to get project examples", e);
				throw new CoreException(status);
			}
		}
		if (examples == null) {
			examples =  Collections.emptyList();
		}
		return examples;
	}

	private void inferRequirements(ProjectExample example) {
		Collection<RequirementModel> requirements = RequirementModelUtil.getAsRequirements(example.getTags());
		if (!requirements.isEmpty()) {
			example.setRequirements(new ArrayList<>(requirements));
		}
	}

	protected String getExamplesUrl() {
		String defaultQuery = "http://dcpbeta-searchisko.rhcloud.com/v1/rest/search?content_provider=jboss-developer&content_provider=rht&field=sys_author&field=target_product&field=contributors&field=duration&field=github_repo_url&field=git_download&field=level&field=sys_contributors&field=sys_created&field=sys_description&field=sys_title&field=sys_tags&field=sys_url_view&field=thumbnail&field=sys_type&field=sys_rating_num&field=sys_rating_avg&field=experimental&field=prerequisites&field=quickstart_id&field=git_tag&field=git_commit&query=sys_type:(jbossdeveloper_quickstart)&size=500";
		String searchQuery = getPropertiesProvider().getValue("quickstarts.search.query", defaultQuery);
		return searchQuery;
	}

}
