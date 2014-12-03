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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jboss.tools.foundation.ui.xpl.taskwizard.TaskWizardDialog;
import org.jboss.tools.project.examples.internal.ProjectExamplesActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.ui.wizard.DownloadRuntimesTaskWizard;

public abstract class AbstractRuntimeFixUIHandler extends AbstractUIHandler {

	@Override
	public void decorateDownloadButton(Button button, IProjectExamplesFix fix, IProgressMonitor monitor) {
		if (!(fix instanceof IDownloadRuntimeProvider) || button.isDisposed()) {
			return;
		}
		super.decorateDownloadButton(button, fix, monitor);
		
		IDownloadRuntimeProvider runtimeFix = (IDownloadRuntimeProvider) fix;
		List<DownloadRuntime> downloadRuntimes = new ArrayList<>(runtimeFix.getDownloadRuntimes(monitor));
		if (!downloadRuntimes.isEmpty()) {
			StringBuilder tooltip = new StringBuilder("Download and install "); //$NON-NLS-1$
			if (downloadRuntimes.size() > 1) {
				tooltip.append("a runtime"); //$NON-NLS-1$
			} else {
				tooltip.append(downloadRuntimes.get(0).getName());
			}
			button.setToolTipText(tooltip.toString());
		}
	}
	
	@Override
	public void handleInstallRequest(Shell shell, IRunnableContext context, IProjectExamplesFix fix) {
		openPreferencesPage(shell, getPreferencePageId());
	}

	abstract protected String getPreferencePageId();

	protected void openPreferencesPage(Shell shell, String preferenceId) {
		PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(shell, preferenceId, null, null);
		preferenceDialog.open();
	}
	
	@Override
	public void handleDownloadRequest(final Shell shell, IRunnableContext context, IProjectExamplesFix fix) {
		final IDownloadRuntimeProvider runtimeFix = (IDownloadRuntimeProvider) fix;
		List<DownloadRuntime> runtimes = new ArrayList<>(runtimeFix.getDownloadRuntimes(new NullProgressMonitor()));
		WizardDialog dialog = new TaskWizardDialog(shell, new DownloadRuntimesTaskWizard(runtimes));
		dialog.open();
	}
}
