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
package org.jboss.tools.project.examples.configurators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.graphics.Image;
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
	    wizardIds.add("org.eclipse.jst.servlet.ui.project.facet.WebProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.ide.eclipse.as.openshift.express.ui.wizard.createNewApplicationWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewJavaeeWarProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewJavaeeEarProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewHtml5ProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewRichfacesProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewSpringMvcProjectWizard"); //$NON-NLS-1$
	    wizardIds.add("org.jboss.tools.central.wizards.NewGwtProjectWizard"); //$NON-NLS-1$
	    WIZARD_IDS = Collections.unmodifiableList(wizardIds);
	}
	
	// TODO: for GA, change this from /development/juno/ to /stable/juno/
	private static final String JBOSS_DIRECTORY_URL_DEFAULT = "http://download.jboss.org/jbosstools/updates/development/juno/jbosstools-directory.xml"; //$NON-NLS-1$
	
	// see pom.xml for actual value -- this is passed it at build-time via Maven
	private static final String JBOSS_DIRECTORY_URL;

	private static final String JBOSS_RUNTIME_URL_DEFAULT = "http://download.jboss.org/jbosstools/examples/download_runtimes.xml"; //$NON-NLS-1$
	
	private static final String JBOSS_RUNTIME_DIRECTORY = "jboss.runtime.directory.url"; //$NON-NLS-1$
	
	private static final String JBOSS_RUNTIME_URL;

	static {
		ResourceBundle rb = ResourceBundle.getBundle("org.jboss.tools.project.examples.configurators.discovery"); //$NON-NLS-1$
		String url = rb.getString("discovery.url").trim(); //$NON-NLS-1$
		if ("".equals(url) || "${jboss.discovery.directory.url}".equals(url)){  //$NON-NLS-1$//$NON-NLS-2$
			//was not filtered, fallback to default value
			JBOSS_DIRECTORY_URL = JBOSS_DIRECTORY_URL_DEFAULT;
		} else {
			JBOSS_DIRECTORY_URL = url;
		}
		url = rb.getString("runtime.url").trim(); //$NON-NLS-1$
		if ("".equals(url) || "${jboss.runtime.directory.url}".equals(url)){  //$NON-NLS-1$//$NON-NLS-2$
			//was not filtered, fallback to default value
			JBOSS_RUNTIME_URL = JBOSS_RUNTIME_URL_DEFAULT;
		} else {
			JBOSS_RUNTIME_URL = url;
		}
	}

	private static final String TWITTER_LINK ="http://twitter.com/#!/jbosstools"; //$NON-NLS-1$
	
	private static final String BLOGS_URL = "http://planet.jboss.org/feeds/blogs"; //$NON-NLS-1$

	private static final String NEWS_URL = "http://planet.jboss.org/feeds/news"; //$NON-NLS-1$

	private Image headerImage;

	@Override
	public String[] getMainToolbarCommandIds() {
		return new String[] {"org.jboss.tools.central.openJBossToolsHome",  //$NON-NLS-1$
				"org.jboss.tools.central.favoriteAtEclipseMarketplace", //$NON-NLS-1$
				"org.jboss.tools.central.preferences"}; //$NON-NLS-1$
	}

	@Override
	public String getJBossDiscoveryDirectory() {
		// use commandline override -Djboss.discovery.directory.url
		String directory = System.getProperty(ProjectExamplesActivator.JBOSS_DISCOVERY_DIRECTORY, null);
		if (directory == null) {
			// else use Maven-generated value (or fall back to default)
			return JBOSS_DIRECTORY_URL;
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
		return WIZARD_IDS;
	}

	@Override
	public Image getHeaderImage() {
		if (headerImage == null) {
			headerImage = ProjectExamplesActivator.getDefault().getImage("/icons/jboss.png"); //$NON-NLS-1$
		}
		return headerImage;
	}

	@Override
	public String getDownloadRuntimesURL() {
		// use commandline override -Djboss.runtime.directory.url
		String directory = System.getProperty(JBOSS_RUNTIME_DIRECTORY, null);
		if (directory == null) {
			// else use Maven-generated value (or fall back to default)
			return JBOSS_RUNTIME_URL;
		}
		return directory;		
	}
}
