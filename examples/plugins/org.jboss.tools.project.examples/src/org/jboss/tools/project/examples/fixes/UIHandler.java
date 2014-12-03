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

public interface UIHandler {

	void decorateInstallButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor);
	
	void handleInstallRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix);

	void decorateDownloadButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor);

	void handleDownloadRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix);
}
