/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.mylyn.internal.discovery.core.model.AbstractDiscoverySource;

/**
 * 
 * @author Fred Bricon
 *
 */
public class ProxyWizardExtensionReader {

	public ProxyWizard readProxyWizardElement(IConfigurationElement element, AbstractDiscoverySource discoverySource) {
		ProxyWizard pw = new ProxyWizard();
		List<String> requiredComponentIds = split(element.getAttribute("requiredComponentIds"));
		pw.setId(element.getAttribute("id"))
		  .setLabel(element.getAttribute("label"))
		  .setWizardId(element.getAttribute("wizardId"))
		  .setDescription(element.getAttribute("description"))
		  .setIconUrl(discoverySource.getResource(element.getAttribute("iconPath")))
		  .setRequiredComponentIds(requiredComponentIds)
		  .setPriority(Integer.parseInt(element.getAttribute("priority")))
		  ;
		
		return pw;
	}

	private List<String> split(String str) {
		if (str == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(str.split(","));
	}
}
