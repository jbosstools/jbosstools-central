/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.fixes;

import org.eclipse.core.runtime.Assert;
import org.jboss.tools.project.examples.fixes.IProjectFixProvider;
import org.jboss.tools.project.examples.fixes.UIHandler;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;

public class PluginFixProvider implements IProjectFixProvider {

	public final static String PLUGIN_TYPE = "plugin"; //$NON-NLS-1$
	
	public boolean accepts(String type) {
	  return PLUGIN_TYPE.equals(type);
	}

	@Override
	public PluginFix create(ProjectExample project, RequirementModel requirement) {
		Assert.isNotNull(requirement);
		if (accepts(requirement.getType())) {
			return new PluginFix(project, requirement);
		}
		throw new IllegalArgumentException(requirement==null?null:requirement.getType() + " is not a valid plugin requirement");
	}

	@Override
	public UIHandler createUIHandler() {
		return new PluginFixUIHandler();
	}

}
