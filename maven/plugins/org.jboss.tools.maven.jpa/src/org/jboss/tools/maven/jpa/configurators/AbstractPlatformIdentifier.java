/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.jpa.configurators;

import org.eclipse.jpt.jpa.core.resource.persistence.XmlPersistenceUnit;
import org.eclipse.jpt.jpa.core.resource.persistence.XmlProperties;
import org.eclipse.jpt.jpa.core.resource.persistence.XmlProperty;

public abstract class AbstractPlatformIdentifier implements IPlatformIdentifier {

	public String getPlatformId(XmlPersistenceUnit xmlPersistenceUnit) {
		if (xmlPersistenceUnit == null) {
			return null;
		}
		String platformId = identifyProvider(xmlPersistenceUnit.getProvider());
		if (platformId != null) {
			return platformId;
		}
		XmlProperties properties = xmlPersistenceUnit.getProperties();
		if (properties != null && properties.getProperties() != null) {
			for (XmlProperty property : properties.getProperties()){
				platformId = identifyProperty(property);
				if (platformId != null) {
					return platformId;
				}
			}
		}
		return null;
	}
	
	protected abstract String identifyProvider(String provider); 
	
	protected abstract String identifyProperty(XmlProperty property);

}
