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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.dmr.ModelNode;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.ProjectExampleWorkingCopy;

@SuppressWarnings("nls")
public class ProjectExampleParser implements IProjectExampleParser {

	private static final String HITS_NODE = "hits"; //$NON-NLS-1$

	@Override
	public Collection<ProjectExample> parse(InputStream json, IProgressMonitor monitor) throws IOException {

		ModelNode results = ModelNode.fromJSONStream(json);
		List<ProjectExample> examples = null;
		if (results.isDefined()) {
			ModelNode hitsWrapper = results.get(HITS_NODE);
			if (hitsWrapper.isDefined()) {
				ModelNode hitsNode = hitsWrapper.get(HITS_NODE);
				if (hitsNode.isDefined()) {
					List<ModelNode> hits = hitsNode.asList();
					examples = new ArrayList<>(hits.size());
					for (ModelNode hit : hits) {
						if (monitor.isCanceled()) {
							break;
						}
						ProjectExample example = parse(hit);
						if (example != null) {
							examples.add(example);
						}
					}
				}
			}
		}

		return examples == null ? Collections.<ProjectExample> emptyList() : Collections.unmodifiableList(examples);
	}

	private ProjectExample parse(ModelNode hit) {
		String id = hit.get("_id").asString();
		ModelNode fields = hit.get("fields");
		String name = fields.get("quickstart_id").asString();
		try {
			// String github_repo_url =
			// fields.get("github_repo_url").asString();
			String downloadUrl = fields.get("git_download").asString();
			String title = fields.get("sys_title").asString();
			String description = fields.get("sys_description").asString();

			ProjectExampleWorkingCopy example = new ProjectExampleWorkingCopy();
			example.setId(id);
			example.setImportType("maven");
			example.setName(name);
			example.setHeadLine(title);
			example.setDescription(description);

			// example.setGitRepo(new URI(github_repo_url));
			example.setUrl(downloadUrl);
			
			Set<String> sys_tags = new LinkedHashSet<>();
			if (fields.get("target_product").isDefined()) {
				StringBuilder productTag = new StringBuilder("product:").append(fields.get("target_product").asString());
				if (fields.get("git_tag").isDefined()) {
					productTag.append("-").append(fields.get("git_tag").asString());
				}
				sys_tags.add(productTag.toString());
			}
			sys_tags.addAll(asSet(fields.get("sys_tags")));
			example.setTags(sys_tags);
			
			Set<String> importFilter = new HashSet<>();
			importFilter.add(name);
			Set<String> prerequisites = asSet(fields.get("prerequisites"));
			importFilter.addAll(prerequisites);
			example.setImportFilter(importFilter);
			return example;
		} catch (Exception O_o) {
			System.err.println("Error parsing " + name + " : " + O_o.getMessage());
		}
		return null;
	}

	private Set<String> asSet(ModelNode node) {
		if (!node.isDefined()) {
			return Collections.emptySet();
		}
		String asString = node.asString();
		Set<String> results = Collections.emptySet();
		if (asString.startsWith("[") && asString.endsWith("]")) {
			results = new HashSet<>();
			for (ModelNode n : node.asList()) {
				String value = n.asString();
				if (isValid(value)) {
					results.add(value);
				}
			}
		} else if (isValid(asString)) {
			results = Collections.singleton(asString);
		}
		return results;
	}

	private boolean isValid(String value) {
		return !"".equals(value) && !"none".equalsIgnoreCase(value);
	}
}
