/*************************************************************************************
 * Copyright (c) 2009 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.internal.project.facet;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.componentcore.datamodel.FacetInstallDataModelProvider;
import org.jboss.tools.maven.core.IJBossMavenConstants;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.Messages;

/**
 * @author snjeza
 * 
 */
public class MavenFacetInstallDataModelProvider extends
		FacetInstallDataModelProvider {

	@Override
	public Object getDefaultProperty(String propertyName) {
		if(propertyName.equals(FACET_ID)){
			return IJBossMavenConstants.M2_FACET_ID;
		}
		if (propertyName.equals(IJBossMavenConstants.ARTIFACT_ID)) {
			return getDefaultProperty(FACET_PROJECT_NAME);
		}
		if (propertyName.equals(IJBossMavenConstants.GROUP_ID)) {
			return "org.jboss.tools"; //$NON-NLS-1$
		}
		if (propertyName.equals(IJBossMavenConstants.VERSION)) {
			return "0.0.1-SNAPSHOT"; //$NON-NLS-1$
		}
		if (propertyName.equals(IJBossMavenConstants.NAME)) {
			return getDefaultProperty(FACET_PROJECT_NAME);
		}
		if (propertyName.equals(IJBossMavenConstants.DESCRIPTION)) {
			return ""; //$NON-NLS-1$
		}
		if (propertyName.equals(IJBossMavenConstants.PACKAGING)) {
			return "war"; //$NON-NLS-1$
		}
		if (propertyName.equals(IJBossMavenConstants.SEAM_MAVEN_VERSION)) {
			return ""; //$NON-NLS-1$
		}
		if (propertyName.equals(IJBossMavenConstants.REMOVE_WTP_CLASSPATH_CONTAINERS)) {
			return Boolean.TRUE;
		}
		if (propertyName.equals(IJBossMavenConstants.MAVEN_PROJECT_EXISTS)) {
			return Boolean.FALSE;
		}
		return super.getDefaultProperty(propertyName);
	}
	
	@Override
	public Set<String> getPropertyNames() {
		Set<String> propertyNames = super.getPropertyNames();
		propertyNames.add(IJBossMavenConstants.ARTIFACT_ID);
		propertyNames.add(IJBossMavenConstants.GROUP_ID);
		propertyNames.add(IJBossMavenConstants.VERSION);
		propertyNames.add(IJBossMavenConstants.NAME);
		propertyNames.add(IJBossMavenConstants.DESCRIPTION);
		propertyNames.add(IJBossMavenConstants.PACKAGING);
		propertyNames.add(IJBossMavenConstants.SEAM_MAVEN_VERSION);
		propertyNames.add(IJBossMavenConstants.REMOVE_WTP_CLASSPATH_CONTAINERS);
		propertyNames.add(IJBossMavenConstants.MAVEN_PROJECT_EXISTS);
		return propertyNames;
	}

	@Override
	public IStatus validate(String propertyName) {
		IStatus status = OK_STATUS;
        if(propertyName.equals(IJBossMavenConstants.GROUP_ID)){
        	String groupId = getStringProperty(IJBossMavenConstants.GROUP_ID);
        	if (groupId == null || groupId.trim().length() <= 0) {
        		status = new Status(IStatus.ERROR, MavenCoreActivator.PLUGIN_ID	, Messages.MavenFacetInstallPage_The_groupId_field_is_required);
        	}
        }
        if(propertyName.equals(IJBossMavenConstants.ARTIFACT_ID)){
        	String groupId = getStringProperty(IJBossMavenConstants.ARTIFACT_ID);
        	if (groupId == null || groupId.trim().length() <= 0) {
        		status = new Status(IStatus.ERROR, MavenCoreActivator.PLUGIN_ID	, Messages.MavenFacetInstallPage_The_artifactId_field_is_required);
        	}
        }
        if(propertyName.equals(IJBossMavenConstants.VERSION)){
        	String groupId = getStringProperty(IJBossMavenConstants.VERSION);
        	if (groupId == null || groupId.trim().length() <= 0) {
        		status = new Status(IStatus.ERROR, MavenCoreActivator.PLUGIN_ID	, Messages.MavenFacetInstallPage_The_version_field_is_required);
        	}
        }
        if(propertyName.equals(IJBossMavenConstants.PACKAGING)){
        	String groupId = getStringProperty(IJBossMavenConstants.PACKAGING);
        	if (groupId == null || groupId.trim().length() <= 0) {
        		status = new Status(IStatus.ERROR, MavenCoreActivator.PLUGIN_ID	, Messages.MavenFacetInstallPage_The_packaging_field_is_required);
        	}
        }
        return status;
	}

	
}
