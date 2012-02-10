/*************************************************************************************
 * Copyright (c) 2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.configurators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.central.JBossCentralActivator;

/**
 * 
 * @author snjeza, nboldt
 *
 */
public class DefaultJBossCentralConfigurator implements
		IJBossCentralConfigurator {

	// TODO: for GA, change this from /development/indigo/ to /stable/indigo/
	private static final String JBOSS_DIRECTORY_URL_DEFAULT = "http://download.jboss.org/jbosstools/updates/development/indigo/jbosstools-directory.xml";
	
	// see pom.xml for actual value -- this is passed it at build-time via Maven
	private static final String JBOSS_DIRECTORY_URL = "${jboss.discovery.directory.url}";  

	private static final String TWITTER_LINK ="http://twitter.com/#!/jbosstools";
	
	private static final String BLOGS_URL = "http://planet.jboss.org/feeds/blogs";

	private static final String NEWS_URL = "http://planet.jboss.org/feeds/news";

	private Image headerImage;

	@Override
	public String[] getMainToolbarCommandIds() {
		return new String[] {"org.jboss.tools.central.openJBossToolsHome", 
				"org.jboss.tools.central.favoriteAtEclipseMarketplace",
				"org.jboss.tools.central.preferences"};
	}

	@Override
	public String getJBossDiscoveryDirectory() {
		// use commandline override -Djboss.discovery.directory.url
		String directory = System.getProperty(JBossCentralActivator.JBOSS_DISCOVERY_DIRECTORY, null);
		if (directory == null) {
			// else use Maven-generated value; fall back to default
			return JBOSS_DIRECTORY_URL.equals("${" + "jboss.discovery.directory.url" + "}") ? JBOSS_DIRECTORY_URL_DEFAULT : JBOSS_DIRECTORY_URL;
		}
		return directory;
	}

	@Override
	public String getTwitterLink() {
		return TWITTER_LINK;
	}

	@Override
	public String getBlogsUrl() {
		return BLOGS_URL;
	}

	@Override
	public String getNewsUrl() {
		return NEWS_URL;
	}

	@Override
	public List<String> getWizardIds() {
		List<String> wizardIds = new ArrayList<String>();
	    wizardIds.add("org.eclipse.jst.servlet.ui.project.facet.WebProjectWizard");
	    wizardIds.add("org.jboss.ide.eclipse.as.openshift.express.ui.wizard.createNewApplicationWizard");
		return wizardIds;
	}

	@Override
	public Image getHeaderImage() {
		if (headerImage == null) {
			headerImage = JBossCentralActivator.getDefault().getImage("/icons/jboss.png");
		}
		return headerImage;
	}
}
