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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jpt.jpa.core.resource.persistence.XmlPersistenceUnit;
import org.eclipse.jpt.jpa.core.resource.persistence.XmlProperty;

/**
 * JPA Platform manager, used to identify a JPA Platform from the content of a persistence.xml
 * 
 * @author Fred Bricon
 */
public class PlatformIdentifierManager {

	List<IPlatformIdentifier> platformIdentifiers = new ArrayList<IPlatformIdentifier>();
	
	public PlatformIdentifierManager() {
		platformIdentifiers.add(new ReallySimplePlatformIdentifer("hibernate"));
		platformIdentifiers.add(new ReallySimplePlatformIdentifer("eclipse"));
	} 
	
	public String identify(XmlPersistenceUnit xmlPersistenceUnit) {
		String platformId = null;
		for (IPlatformIdentifier identifier : platformIdentifiers) {
			platformId = identifier.getPlatformId(xmlPersistenceUnit);
			if (platformId != null) {
				return platformId;
			}
		}
		return null;
	}
	
	private class ReallySimplePlatformIdentifer extends AbstractPlatformIdentifier {
		
		private final String platformName;

		ReallySimplePlatformIdentifer(String platformName) {
			this.platformName = platformName;
		}
		
		@Override
		protected String identifyProvider(String provider) {
			if (provider != null && provider.contains(platformName)) {
				return platformName;
			}
			return null;
		}

		@Override
		protected String identifyProperty(XmlProperty property) {
			return identifyProvider(property.getName());
		}
	}
}
