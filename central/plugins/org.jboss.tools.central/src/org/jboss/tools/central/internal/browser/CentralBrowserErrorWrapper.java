/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.part.StatusPart;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.Messages;

public class CentralBrowserErrorWrapper {

	/**
	 * Logs given {@code throwable} (may be wrapped) and shows error message in 
	 * the {@code parent} composite.
	 */
	public void showError(Composite parent,
			Throwable originalThrowable) {
		Throwable throwable = wrapError(originalThrowable);
		String errorMessage = throwable.getMessage();
		
		parent.setLayout(new GridLayout());
		Composite statusComposite = new Composite(parent, SWT.NONE);
		Color bgColor= parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		Color fgColor= parent.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		parent.setBackground(bgColor);
		parent.setForeground(fgColor);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 0;
		gridData.heightHint = 0;
		statusComposite.setLayoutData(gridData);

		IStatus displayStatus = new Status(IStatus.ERROR, JBossCentralActivator.PLUGIN_ID, errorMessage, throwable);
		new StatusPart(statusComposite, displayStatus);
		
		final Link link = new Link(parent, SWT.WRAP);
		link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		link.setBackground(bgColor);
		link.setText(Messages.visualEditorFaq);
		link.setToolTipText(Messages.visualEditorFaqLink);
		link.setForeground(link.getDisplay().getSystemColor(SWT.COLOR_BLUE));

		link.addMouseListener(new MouseAdapter () {
			public void mouseDown(MouseEvent e) {
				BusyIndicator.showWhile(link.getDisplay(), new Runnable() {
					public void run() {
						JBossCentralActivator.openUrl(Messages.visualEditorFaqLink, Display.getDefault().getActiveShell(), true);
					}
				});
			}
		});
		
		parent.getParent().layout(true, true);
	}
	
	private Throwable wrapError(Throwable originalThrowable) {
		if (originalThrowable instanceof SWTError && originalThrowable.getMessage() != null) {
			String message = originalThrowable.getMessage(); 
			if (message.contains("No more handles")) {//$NON-NLS-1$
				/*running under GTK3 and no webkit installed
				or Xulrunner disbaled by -Dorg.jboss.tools.vpe.loadxulrunner=false flag and no webkit installed
				this error can be only under Linux. On Windows and OSX default browser always can be created.*/
				return new Exception(Messages.noEngineError_message, originalThrowable);
			}
		}
		return originalThrowable;
	}
	
}
