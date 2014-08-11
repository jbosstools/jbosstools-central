/*******************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.maven.conversion.core.internal;

import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.conversion.AbstractProjectConversionParticipant;

/**
 * Adds the maven-acr-plugin extension to projects having at least one app-client dependency
 * 
 * @author Fred Bricon
 */
public class AppClientDependencyConversionParticipant extends
		AbstractProjectConversionParticipant {
	@Override
	public boolean accept(IProject project) throws CoreException {
		return true;
	}

	@Override
	public void convert(IProject project, Model model, IProgressMonitor monitor)
			throws CoreException {
		if (hasAppClientDependency(model.getDependencies())) {
			setAcrPlugin(model);
		}
	}

	private boolean hasAppClientDependency(List<Dependency> deps) {
		if (deps == null || deps.isEmpty()) {
			return false;
		}
		for (Dependency d : deps) {
			if ("app-client".equals(d.getType())) {
				return true;
			}
		}
		return false;
	}

	private void setAcrPlugin(Model model)
			throws CoreException {
		Build build = getCloneOrCreateBuild(model);
		Plugin acrPlugin = setupPlugin(build, "org.apache.maven.plugins", "maven-acr-plugin", "1.0");
		acrPlugin.setExtensions(true);
		model.setBuild(build);
	}

	protected Build getCloneOrCreateBuild(Model model) {
		Build build;
		if (model.getBuild() == null) {
			build = new Build();
		} else {
			build = model.getBuild().clone();
		}
		return build;
	}

	protected Plugin setupPlugin(Build build, String pluginGroupId, String pluginArtifactId, String pluginVersion) {
		build.flushPluginMap();// We need to force the re-generation of the
								// plugin map as it may be stale
		Plugin plugin = build.getPluginsAsMap().get(pluginGroupId + ":" + pluginArtifactId); //$NON-NLS-1$  
		if (plugin == null) {
			plugin = build.getPluginsAsMap().get(pluginArtifactId);
		}
		if (plugin == null) {
			plugin = new Plugin();
			plugin.setGroupId(pluginGroupId);
			plugin.setArtifactId(pluginArtifactId);
			plugin.setVersion(pluginVersion);
			build.addPlugin(plugin);
		}
		return plugin;
	}

}
