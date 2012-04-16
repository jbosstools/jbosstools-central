/*************************************************************************************
 * Copyright (c) 2008-2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.runtimes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;

public class RuntimeUtils {

	/**
	 * Returns an unmodifiable {@link Set} of all {@link IRuntimeType} having at least one corresponding {@link IServer} instance 
	 * configured in this workspace.<br/>
	 *  
	 * @return all configured {@link IRuntimeType}
	 */
	public static Set<IRuntimeType> getInstalledRuntimeTypes() {
		Set<IRuntimeType> runtimeTypes = new HashSet<IRuntimeType>();
		for (IServer server : ServerCore.getServers()) {
			IRuntime runtime = server.getRuntime();
			if (runtime != null && runtime.getRuntimeType() != null) {
				runtimeTypes.add(runtime.getRuntimeType());
			}
		}
		return Collections.unmodifiableSet(runtimeTypes);
	}
}
