/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;

import org.apache.maven.model.Plugin;
import org.eclipse.m2e.model.edit.pom.Build;
import org.eclipse.m2e.model.edit.pom.Model;
import org.eclipse.m2e.model.edit.pom.PomFactory;
import org.jboss.tools.maven.core.xpl.ProjectUpdater;

/**
* @author snjeza
* 
*/
public class PluginUpdater extends ProjectUpdater {

	private static final PomFactory POM_FACTORY = PomFactory.eINSTANCE;
	private Plugin plugin;

    public PluginUpdater(Plugin plugin) {
      this.plugin = plugin;
    }

    public void update(Model model) {
    	Build build = model.getBuild();
        if(build==null) {
          build = POM_FACTORY.createBuild();
          model.setBuild(build);
        }
        org.eclipse.m2e.model.edit.pom.Plugin newPlugin = POM_FACTORY.createPlugin();
        newPlugin.setArtifactId(plugin.getArtifactId());
        newPlugin.setGroupId(plugin.getGroupId());
        newPlugin.setVersion(plugin.getVersion());
        newPlugin.setExtensions(plugin.getExtensions());
        // FIXME 
    }
  }
