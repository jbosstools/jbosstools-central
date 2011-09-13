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
package org.jboss.tools.central;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author snjeza
 *
 */
public class ShowJBossCentral implements IStartup {

	@Override
	public void earlyStartup() {
		boolean showJBossCentral = JBossCentralActivator.getDefault().showJBossCentralOnStartup();
		if (!showJBossCentral) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				JBossCentralActivator.getJBossCentralEditor();
			}
		});
		
	}

}
