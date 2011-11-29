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
 * @author snjeza
 *
 */
public class DefaultJBossCentralConfigurator implements
		IJBossCentralConfigurator {
	
	private static final String JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML = "http://download.jboss.org/jbosstools/updates/nightly/core/trunk/jbosstools-directory.xml";

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
		return JBOSS_DISCOVERY_DIRECTORY_3_3_0_XML;
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
	    //wizardIDs.add("org.jboss.ide.eclipse.as.openshift.express.ui.wizard.NewServerAdapter");
	    wizardIds.add("org.eclipse.jst.servlet.ui.project.facet.WebProjectWizard");
	    wizardIds.add("org.jboss.tools.seam.ui.wizards.SeamProjectWizard");
	    wizardIds.add("org.eclipse.m2e.core.wizards.Maven2ProjectWizard");
	    wizardIds.add(JBossCentralActivator.NEW_PROJECT_EXAMPLES_WIZARD_ID);
		return wizardIds;
	}

	@Override
	public Image getHeaderImage() {
		if (headerImage == null) {
			headerImage = JBossCentralActivator.getDefault().getImage("/icons/jboss.gif");
		}
		return headerImage;
	}
}
