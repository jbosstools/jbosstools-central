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
package org.jboss.tools.project.examples.internal.fixes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.project.examples.internal.Messages;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.AbstractProjectFix;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.VersionRange;

/**
 * 
 * @author snjeza
 *
 */
public class PluginFix extends AbstractProjectFix {

	PluginFix(ProjectExample project, RequirementModel fix) {
		super(project, fix);
	}
	
	public boolean fix(IProgressMonitor monitor) {
		// can't be fixed
		return false;
	}


	@Override
	public boolean isSatisfied() {
		String symbolicName = requirement.getProperties().get(RequirementModel.ID);
		if (symbolicName == null) {
			ProjectExamplesActivator.log(NLS.bind(Messages.PluginFix_Invalid_plugin_fix, project.getName()));
			return true;
		}
		Bundle bundle = Platform.getBundle(symbolicName);
		if( bundle == null) {
			return false;
		}
		String versions = requirement.getProperties().get("versions"); //$NON-NLS-1$
		if (versions != null && !versions.isEmpty()) {
			VersionRange versionRange = new VersionRange(versions);
			return versionRange.includes(bundle.getVersion());
		}
		return true;
	}
}
