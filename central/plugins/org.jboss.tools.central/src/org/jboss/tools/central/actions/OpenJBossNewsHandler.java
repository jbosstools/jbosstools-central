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

package org.jboss.tools.central.actions;

import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza
 *
 */
public class OpenJBossNewsHandler extends OpenWithBrowserHandler {

	@Override
	public String getLocation() {
		return JBossCentralActivator.NEWS_URL;
	}

}
