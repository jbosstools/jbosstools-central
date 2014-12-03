/*************************************************************************************
 * Copyright (c) 2008-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.fixes;


/**
 * Copied from  org.jboss.tools.portlet.core.IPortletConstants
 * 
 * @author snjeza
 * 
 */

interface TemporaryIPortletConstantsFork {

	static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR = "deploy/jboss-portal.sar"; //$NON-NLS-1$
	
	static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR = "deploy/jboss-portal-ha.sar"; //$NON-NLS-1$

	static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL = "deploy/simple-portal"; //$NON-NLS-1$

	static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR = "deploy/simple-portal.sar"; //$NON-NLS-1$
	
	static final String SERVER_DEFAULT_DEPLOY_GATEIN = "deploy/gatein.ear"; //$NON-NLS-1$
	
	static final String SERVER_DEFAULT_DEPLOY_GATEIN33 = "standalone/deployments/gatein.ear"; //$NON-NLS-1$

	static final String SERVER_DEFAULT_DEPLOY_JPP60 = "gatein/gatein.ear"; //$NON-NLS-1$

	static final String TOMCAT_LIB = "lib"; //$NON-NLS-1$

	static final String JAR = ".jar"; //$NON-NLS-1$

	static final String PORTLET_API = "portlet-api"; //$NON-NLS-1$
}