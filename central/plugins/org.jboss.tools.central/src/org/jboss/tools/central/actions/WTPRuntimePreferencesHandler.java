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


/**
* @author snjeza
* 
*/
public class WTPRuntimePreferencesHandler extends AbstractPreferencesHandler {

	@Override
	public String getPreferenceId() {
		return "org.eclipse.wst.server.ui.runtime.preferencePage";
	}

}
