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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.project.examples.fixes.AbstractUIHandler;
import org.jboss.tools.project.examples.fixes.IProjectExamplesFix;
import org.jboss.tools.project.examples.fixes.IProjectFixProvider;
import org.jboss.tools.project.examples.fixes.UIHandler;
import org.jboss.tools.project.examples.model.AbstractProjectFix;
import org.jboss.tools.project.examples.model.ProjectExample;
import org.jboss.tools.project.examples.model.RequirementModel;

public class UnsupportedFixProvider implements IProjectFixProvider {

	@Override
	public IProjectExamplesFix create(ProjectExample example, RequirementModel requirement) {
		return new NoopFix(example, requirement);
	}

	@Override
	public UIHandler createUIHandler() {
		return new NoOpUIHandler();
	}

	public static class NoopFix extends AbstractProjectFix {

		public NoopFix(ProjectExample project, RequirementModel requirement) {
			super(project, requirement);
		}

		@Override
		public boolean isSatisfied() {
			return false;
		}

		@Override
		public boolean fix(IProgressMonitor monitor) {
			return false;
		}
	}
	
	public static final class NoOpUIHandler extends AbstractUIHandler {

		@Override
		public void handleInstallRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix) {
		}

		@Override
		public void handleDownloadRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix) {
		}

		@Override
		public void decorateInstallButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor) {
			disable(button);
		}

		@Override
		public void decorateDownloadButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor) {
			disable(button);
		}
		
		private void disable(Button button) {
			if (button != null && !button.isDisposed()) {
				button.setEnabled(false);
			}
		}
		
	}
	
}
