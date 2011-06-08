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
package org.jboss.tools.project.examples.fixes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.Project;
import org.jboss.tools.project.examples.model.ProjectFix;
import org.osgi.framework.Bundle;

/**
 * 
 * @author snjeza
 *
 */
public class PluginFix implements ProjectExamplesFix {

	public boolean canFix(Project project, ProjectFix fix) {
		if (!ProjectFix.PLUGIN_TYPE.equals(fix.getType())) {
			return false;
		}
		String symbolicName = fix.getProperties().get(ProjectFix.ID);
		if (symbolicName == null) {
			ProjectExamplesActivator.log(NLS.bind(Messages.PluginFix_Invalid_plugin_fix, project.getName()));
			return true;
		}
		Bundle bundle = Platform.getBundle(symbolicName);
		return bundle != null;
	}

	public boolean fix(Project project, ProjectFix fix,
			IProgressMonitor monitor) {
		// can't be fixed
		return false;
	}

}
