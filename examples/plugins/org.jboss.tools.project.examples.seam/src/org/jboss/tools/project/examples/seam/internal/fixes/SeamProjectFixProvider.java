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
package org.jboss.tools.project.examples.seam.internal.fixes;

import org.eclipse.core.runtime.Assert;
import org.jboss.tools.project.examples.fixes.IProjectFixProvider;
import org.jboss.tools.project.examples.fixes.UIHandler;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;

public class SeamProjectFixProvider implements IProjectFixProvider {

	private static final String SEAM_RUNTIME = "seam"; //$NON-NLS-1$

	public boolean accepts(String type) {
		return SEAM_RUNTIME.equals(type);
	}

	@Override
	public SeamRuntimeFix create(ProjectExample example, RequirementModel requirement) {
		Assert.isNotNull(requirement);
		if (accepts(requirement.getType())) {
			return new SeamRuntimeFix(example, requirement);
		}
		throw new IllegalArgumentException(requirement == null ? null : requirement.getType()
				+ " is not a valid seam requirement"); //$NON-NLS-1$
	}

	@Override
	public UIHandler createUIHandler() {
		return new SeamRuntimeFixUIHandler();
	}
}
