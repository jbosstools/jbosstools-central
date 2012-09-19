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
 * 
 * @author snjeza
 *
 */
public class FavoriteAtEclipseMarketplaceHandler extends OpenWithBrowserHandler {

	private static final String MARKETPLACE_ECLIPSE_URL = "http://marketplace.eclipse.org/node/420896";

	@Override
	public String getLocation() {
		return MARKETPLACE_ECLIPSE_URL;
	}

}
