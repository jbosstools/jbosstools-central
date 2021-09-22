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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.project.examples.IProjectExampleProvider;
import org.jboss.tools.project.examples.internal.model.RequirementModelUtil;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleSite;
import org.jboss.tools.project.examples.model.RequirementModel;

@SuppressWarnings("nls")
public class ProjectExampleJsonProvider implements IProjectExampleProvider {

	private IProjectExampleParser parser;

	public ProjectExampleJsonProvider() {
		this(new ProjectExampleJsonParser());
	}

	public ProjectExampleJsonProvider(IProjectExampleParser projectExampleParser) {
		Assert.isNotNull(projectExampleParser);
		parser = projectExampleParser;
	}

	@Override
	public Collection<ProjectExample> getExamples(IProgressMonitor monitor) throws CoreException {
		URL jsonPayloadUrl;
		try {
			jsonPayloadUrl = FileLocator.toFileURL(ProjectExamplesActivator.class.getResource("/quickstarts.json"));
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, ProjectExamplesActivator.PLUGIN_ID,
					"Unable to get project examples", e);
			throw new CoreException(status);
		}
		File jsonPayload = new File(jsonPayloadUrl.getFile());
		return getExamples(jsonPayload, monitor);
	}

	public Collection<ProjectExample> getExamples(File jsonPayload, IProgressMonitor monitor) throws CoreException{
		if (jsonPayload == null || jsonPayload.length() == 0) {
			return Collections.emptyList();
		}
		Collection<ProjectExample> examples = null;
		try (InputStream json = new BufferedInputStream(new FileInputStream(jsonPayload))) {
			examples = parseStream(json, monitor);
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
			examples = Collections.emptyList();
		}
		return examples;
	}

	public Collection<ProjectExample> parseStream(InputStream json, IProgressMonitor monitor) throws IOException {
		Collection<ProjectExample> examples;
		ProjectExampleSite site = new ProjectExampleSite();
		site.setEditable(false);
		site.setExperimental(false);
		site.setUrl(null);
		site.setName("JBoss Developer Examples");
		examples = parser.parse(json, monitor);
		if (examples != null) {
			for (ProjectExample example : examples) {
				example.setSite(site);
				inferCategory(example);
				inferRequirements(example);
			}
		}
		return examples;
	}

	private void inferCategory(ProjectExample example) {
		String name = example.getName().toLowerCase();
		if (name.contains("portlet")) {
			example.setCategory("Portal Applications");
			return;
		}
		if (name.contains("cordova") || name.contains("android") || name.contains("-ios")) {
			example.setCategory("Mobile Applications");
			return;
		}
		if (name.contains("jsf") || name.contains("html") || name.contains("servlet")) {
			example.setCategory("Web Applications");
			return;
		}
		Set<String> tags = example.getTags();
		if (tags != null) {
			for (String tag : tags) {
				if (tag.startsWith("cordova") || tag.startsWith("android") || tag.startsWith("ios")) {
					example.setCategory("Mobile Applications");
					return;
				} else if (tag.startsWith("angular") || tag.startsWith("servlet") || tag.startsWith("jsf")) {
					example.setCategory("Web Applications");
					return;
				}
			}
		}
		example.setCategory("JBoss Developer Framework");
	}

	private void inferRequirements(ProjectExample example) {
		Collection<RequirementModel> requirements = RequirementModelUtil.getAsRequirements(example.getTags());
		if (!requirements.isEmpty()) {
			example.setRequirements(new ArrayList<>(requirements));
		}
	}

}