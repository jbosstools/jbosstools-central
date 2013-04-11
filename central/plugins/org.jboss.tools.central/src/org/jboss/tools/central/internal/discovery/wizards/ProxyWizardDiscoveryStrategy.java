/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards;

import java.util.List;

public interface ProxyWizardDiscoveryStrategy {

	String PROXY_WIZARD_EXTENSION_POINT = "org.jboss.tools.central.proxyWizard";
	
	String PROXY_WIZARD_EXTENSION_POINT_NAME = "proxyWizard";
	
	List<ProxyWizard> getProxyWizards();
}
