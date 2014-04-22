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
package org.jboss.tools.project.examples.configurators;

import static org.jboss.tools.project.examples.ProjectExamplesActivator.JBOSS_DISCOVERY_DIRECTORY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.foundation.core.properties.IPropertiesProvider;
import org.jboss.tools.foundation.core.properties.PropertiesHelper;
import org.jboss.tools.project.examples.ProjectExamplesActivator;

/**
 * 
 * @author snjeza, nboldt
 *
 */
public class DefaultJBossCentralConfigurator implements
		IJBossCentralConfigurator {

	private static final List<String> WIZARD_IDS;
	
	static {
		List<String> wizardIds = new ArrayList<String>();
	    //wizardIds.add("org.eclipse.jst.servlet.ui.project.facet.WebProjectWizard"); //$NON-NLS-1$
		wizardIds.add("org.jboss.tools.central.wizards.NewHtml5ProjectWizard"); //$NON-NLS-1$
		wizardIds.add("org.jboss.ide.eclipse.as.openshift.express.ui.wizard.createNewApplicationWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewJavaeeWarProjectWizard"); //$NON-NLS-1$
	    //wizardIds.add("org.jboss.tools.central.wizards.NewJavaeeEarProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewRichfacesProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewGwtProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewSpringMvcProjectWizard"); //$NON-NLS-1$
	    WIZARD_IDS = Collections.unmodifiableList(wizardIds);
	}
	
	private static final String TWITTER_LINK ="http://twitter.com/jbosstools"; //$NON-NLS-1$
	
	private static final String BLOGS_URL = "http://planet.jboss.org/feeds/blogs"; //$NON-NLS-1$
	
	private static final String BUZZ_URL = "http://planet.jboss.org/feeds/buzz"; //$NON-NLS-1$
	
	private static final String NEWS_URL = "http://planet.jboss.org/feeds/news"; //$NON-NLS-1$

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
	public String getJBossDiscoveryDirectory() {
		// use commandline override -Djboss.discovery.directory.url
		String directory = System.getProperty(JBOSS_DISCOVERY_DIRECTORY, null);
		if (directory == null) {
			IPropertiesProvider pp = PropertiesHelper.getPropertiesProvider();
			directory = pp.getValue(JBOSS_DISCOVERY_DIRECTORY);
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
	public String getBuzzUrl() {
		return BUZZ_URL;
	}

	@Override
	public List<String> getWizardIds() {
		return WIZARD_IDS;
	}

	@Override
	public String getDocumentationUrl() {
		return DOCUMENTATION_URL;
	}
	
	@Override
	public Image getHeaderImage() {
		if (headerImage == null) {
			headerImage = ProjectExamplesActivator.getDefault().getImage("/icons/jboss.png"); //$NON-NLS-1$
		}
		return headerImage;
	}

}
