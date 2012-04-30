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

import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author snjeza
 *
 */
public interface IJBossCentralConfigurator {

	String[] getMainToolbarCommandIds();
	String getJBossDiscoveryDirectory();
	String getTwitterLink();
	String getBlogsUrl();
	String getNewsUrl();
	String getDownloadRuntimesURL();
	List<String> getWizardIds();
	Image getHeaderImage();

}
