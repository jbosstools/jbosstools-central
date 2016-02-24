/*************************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.central.configurators.IJBossCentralConfigurator;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;

/**
 * 
 * @author snjeza, nboldt
 *
 */
public class DefaultJBossCentralConfigurator implements
		IJBossCentralConfigurator {
	
	private static final String TWITTER_LINK ="http://twitter.com/jbosstools"; //$NON-NLS-1$
	
	private static final String BUZZ_URL = "http://planet.jboss.org/feeds/buzz"; //$NON-NLS-1$
	
	private static final String DOCUMENTATION_URL = "http://www.jboss.org/tools/docs/reference"; //$NON-NLS-1$

	private Image headerImage;

	@Override
	public String[] getMainToolbarCommandIds() {
		return new String[] {"org.jboss.tools.central.openJBossToolsHome",  //$NON-NLS-1$
				"org.jboss.tools.central.favoriteAtEclipseMarketplace" //$NON-NLS-1$
				//,"org.jboss.tools.central.preferences"
				}; 
	}

	@Override
	public String getTwitterLink() {
		return TWITTER_LINK;
	}

	@Override
	public String getBuzzUrl() {
		return PropertiesHelper.getPropertiesProvider().getValue("buzz.feed.url", BUZZ_URL); //$NON-NLS-1$
	}

	@Override
	public String getDocumentationUrl() {
		return DOCUMENTATION_URL;
	}
	
	@Override
	public Image getHeaderImage() {
		if (headerImage == null) {
			headerImage = JBossCentralActivator.getDefault().getImage("/icons/shadowman.png"); //$NON-NLS-1$
		}
		return headerImage;
	}

}
