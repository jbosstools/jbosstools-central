/*************************************************************************************
 * Copyright (c) 2008-2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.sourcelookup.internal.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.jboss.tools.maven.sourcelookup.SourceLookupActivator;
import org.jboss.tools.maven.sourcelookup.internal.util.SourceLookupUtil;

/**
 * 
 * @author snjeza
 *
 */
public class ServerLifecycleListener implements IRuntimeLifecycleListener, IServerLifecycleListener{

	@Override
	public void serverAdded(IServer server) {
		updateSearchContainer();
	}

	@Override
	public void serverChanged(IServer server) {
		updateSearchContainer();
	}

	@Override
	public void serverRemoved(IServer server) {
		updateSearchContainer();
	}

	@Override
	public void runtimeAdded(IRuntime runtime) {
		updateSearchContainer();
	}

	@Override
	public void runtimeChanged(IRuntime runtime) {
		updateSearchContainer();
	}

	@Override
	public void runtimeRemoved(IRuntime runtime) {
		updateSearchContainer();
	}

	private void updateSearchContainer() {
		try {
			SourceLookupUtil.updateClasspath();
		} catch (CoreException e) {
			SourceLookupActivator.log(e);
		}
	}
}
