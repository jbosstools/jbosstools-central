/*************************************************************************************
 * Copyright (c) 2014-2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Button;
import org.jboss.tools.foundation.core.ecf.URLTransportUtility;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;
import org.jboss.tools.project.examples.IProjectExampleManager;
import org.jboss.tools.project.examples.fixes.ProjectFixManager;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;

public class ProjectExampleManager implements IProjectExampleManager {

	private ProjectFixManager projectFixManager;

	public ProjectExampleManager(ProjectFixManager projectFixManager) {
		this.projectFixManager = projectFixManager;
	}

	@Override
	public ProjectExampleWorkingCopy createWorkingCopy(ProjectExample example) {
		ProjectExampleWorkingCopy workingCopy = new ProjectExampleWorkingCopy(example);
		projectFixManager.loadFixes(workingCopy);
		return workingCopy;
	}

	@SuppressWarnings("nls")
	public Collection<ProjectExample> getExamples(IProgressMonitor monitor) throws CoreException {
		String defaultQuery = "http://dcpbeta-searchisko.rhcloud.com/v1/rest/search?content_provider=jboss-developer&content_provider=rht&field=sys_author&field=target_product&field=contributors&field=duration&field=github_repo_url&field=git_download&field=level&field=sys_contributors&field=sys_created&field=sys_description&field=sys_title&field=sys_tags&field=sys_url_view&field=thumbnail&field=sys_type&field=sys_rating_num&field=sys_rating_avg&field=experimental&field=prerequisites&field=quickstart_id&field=git_tag&field=git_commit&query=sys_type:(jbossdeveloper_quickstart)&size=500";
		String searchQuery = PropertiesHelper.getPropertiesProvider().getValue("quickstarts.search.query",defaultQuery);
		ByteArrayOutputStream response = new ByteArrayOutputStream(64);
		//TODO needs to survive offline status
		new URLTransportUtility().download("Searching for quickstarts", searchQuery, response, monitor);
		InputStream json = new ByteArrayInputStream(response.toByteArray());
		ProjectExampleParser parser = new ProjectExampleParser();
		try {
			return parser.parse(json, monitor);
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
					"Unable to get project examples", e);
			throw new CoreException(status);
		}
	}

	/*
	 public Collection<ProjectExample> searchExamples(String query, Collection<ProjectExample> examples, IProgressMonitor monitor) throws CoreException {
	   if (examples == null || examples.isEmpty() || query == null || query.isEmpty()) {
		   return examples;
	   }
	   List<ProjectExample> results = new ArrayList<>();
	   query = query.toLowerCase();
	   for (ProjectExample ex : examples) {
		   if (matches(ex, query)) {
			   results.add(ex);
		   }
	   }
	   return results;
	  }

	private boolean matches(ProjectExample ex, String query) {
		boolean match = ex.getName().toLowerCase().contains(query);
		match = match || ex.getDescription().toLowerCase().contains(query);
		match = match || ex.hasTags(query.split(" ")); //$NON-NLS-1$
		return match;
	}
	*/
}
