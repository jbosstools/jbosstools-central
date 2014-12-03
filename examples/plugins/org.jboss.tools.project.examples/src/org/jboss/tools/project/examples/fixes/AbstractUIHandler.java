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
package org.jboss.tools.project.examples.fixes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractUIHandler implements UIHandler {

	@Override
	public void decorateInstallButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor) {
		enableButton(button, fix);
	}

	@Override
	public void handleInstallRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix) {
	}

	@Override
	public void decorateDownloadButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor) {
		enableButton(button, fix);
	}

	@Override
	public void handleDownloadRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix) {
	}

	protected void enableButton(Button button, IProjectExamplesFix fix) {
		if (!button.isDisposed()) {
			button.setEnabled(!fix.isSatisfied());
		}
	}

}
