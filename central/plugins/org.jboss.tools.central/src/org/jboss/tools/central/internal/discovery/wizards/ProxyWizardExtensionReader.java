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
import org.eclipse.equinox.internal.p2.discovery.AbstractCatalogSource;

/**
 * 
 * @author Fred Bricon
 *
 */
@SuppressWarnings("restriction")
public class ProxyWizardExtensionReader {

	public ProxyWizard readProxyWizardElement(IConfigurationElement element, AbstractCatalogSource discoverySource) {
		ProxyWizard pw = new ProxyWizard();
		List<String> requiredComponentIds = split(element.getAttribute("requiredComponentIds"));
		List<String> requiredPluginIds = split(element.getAttribute("requiredPluginIds"));
		pw.setId(element.getAttribute("id"))
		  .setLabel(element.getAttribute("label"))
		  .setWizardId(element.getAttribute("wizardId"))
		  .setDescription(element.getAttribute("description"))
		  .setIconUrl(discoverySource.getResource(element.getAttribute("iconPath")))
		  .setRequiredComponentIds(requiredComponentIds)
		  .setRequiredPluginIds(requiredPluginIds)
		  .setPriority(Integer.parseInt(element.getAttribute("priority")))
		  .setTags(split(element.getAttribute("tags")))
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
