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
package org.jboss.tools.maven.seam;

import org.eclipse.m2e.model.edit.pom.Model;
import org.eclipse.m2e.model.edit.pom.Parent;
import org.eclipse.m2e.model.edit.pom.PomFactory;
import org.jboss.tools.maven.core.xpl.ProjectUpdater;

/**
 * 
 * @author snjeza
 *
 */
public class ParentAdder extends ProjectUpdater {

	private static final PomFactory POM_FACTORY = PomFactory.eINSTANCE;
    private final String groupId;
    private final String artifactId;
    private final String version;
	private String relativePath;

    public ParentAdder(String groupId, String artifactId, String version, String relativePath) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.version = version;
      this.relativePath = relativePath;
    }

    public void update(Model model) {
      Parent parent = model.getParent();
      if(parent==null) {
        parent = POM_FACTORY.createParent();
        parent.setArtifactId(artifactId);
        parent.setGroupId(groupId);
        parent.setVersion(version);
        if (relativePath != null) {
        	parent.setRelativePath(relativePath);
        }
        model.setParent(parent);
      }
    }
  }