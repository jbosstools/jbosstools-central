/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.jboss.tools.maven.ui.Messages;

@SuppressWarnings("nls")
public class MavenDependencyMarkerResolutionGenerator implements
		IMarkerResolutionGenerator2 {

	private static final String ORG_JBOSS_PREFIX = "org.jboss";

	private static final String REDHAT_SUFFIX = "-redhat";

	private static final String JBOSS_ENTERPRISE_REPO_SETUP_GUIDE_URL = "http://community.jboss.org/wiki/SettingUpTheJBossEnterpriseRepositories";

	private static final String JBOSS_COMMUNITY_REPO_SETUP_GUIDE_URL = "http://community.jboss.org/wiki/MavenGettingStarted-Users";

	private static final String MISSING_ARTIFACT_PREFIX = "Missing artifact "; //$NON-NLS-1$

	private static final String FAILURE_TO_FIND_STRING = "Failure to find "; //$NON-NLS-1$

	enum ResolutionType {
		JBOSS_REPO, EAP_REPO, UNSUPPORTED
	}

	
	public MavenDependencyMarkerResolutionGenerator() {
	}
	
	public IMarkerResolution[] getResolutions(IMarker marker) {
		ResolutionType type = getResolutionType(marker);
		switch (type) {
		case JBOSS_REPO:
			return new IMarkerResolution[] {
					new ConfigureMavenRepositoriesMarkerResolution(),
					new OpenPageInBrowserMarkerResolution(Messages.Quickfix_setupCommunityRepo, 
							JBOSS_COMMUNITY_REPO_SETUP_GUIDE_URL)
					};
		case EAP_REPO:
			return new IMarkerResolution[] { 
					new ConfigureMavenRepositoriesMarkerResolution(),
					new OpenPageInBrowserMarkerResolution(Messages.Quickfix_setupEnterpriseRepo, 
							JBOSS_ENTERPRISE_REPO_SETUP_GUIDE_URL)
					};
		}
		return new IMarkerResolution[0];
	}

	public boolean hasResolutions(IMarker marker) {
		return ResolutionType.UNSUPPORTED != getResolutionType(marker);
	}

	private ResolutionType getResolutionType(IMarker marker) {
		ArtifactKey key = getArtifactKey(marker);
		if (key != null) {
			if (key.getVersion().contains(REDHAT_SUFFIX)) {
				return ResolutionType.EAP_REPO;
			} else if (key.getGroupId().startsWith(ORG_JBOSS_PREFIX)) {
				return ResolutionType.JBOSS_REPO;
			}
		}
		return ResolutionType.UNSUPPORTED;
	}
 
	private ArtifactKey getArtifactKey(IMarker marker) {
		ArtifactKey key = null;
		try {
			key = (ArtifactKey) marker.getAttribute("artifactKey");
			if (key == null) {
				String message = (String) marker.getAttribute("message", null);
				if (message != null) {
					String markerType = marker.getType();
					if (IMavenConstants.MARKER_DEPENDENCY_ID.equals(markerType)) {
						key = parseDependencyErrorMessage(message);
					} else if (IMavenConstants.MARKER_POM_LOADING_ID.equals(markerType)) {
						key = parsePomLoadingErrorMessage(message);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return key;
	}

	private ArtifactKey parseDependencyErrorMessage(String message) {
		ArtifactKey key = null;
		if (message.startsWith(MISSING_ARTIFACT_PREFIX)) {
			String keyString = message.substring(MISSING_ARTIFACT_PREFIX.length());
			key = extractKey(key, keyString);
		}
		return key;
	}

	private ArtifactKey parsePomLoadingErrorMessage(String message) {
		ArtifactKey key = null;
		int start = message.indexOf(FAILURE_TO_FIND_STRING);
		if (start > -1) {
			int from = FAILURE_TO_FIND_STRING.length()+start;
			String keyString = message.substring(from, message.indexOf(" ", from));
			key = extractKey(key, keyString);
		}
		return key;
	}

	private ArtifactKey extractKey(ArtifactKey key, String keyString) {
		String[] keyAsArray = keyString.trim().split(":"); //$NON-NLS-1$
		if (keyAsArray.length > 3) {
			String groupId 	  = keyAsArray[0];
			String artifactId = keyAsArray[1];
			String classifier = null;
			String version;
			if (keyAsArray.length > 4) {
				classifier = keyAsArray[3];
				version = keyAsArray[4];
			} else {
				version = keyAsArray[3];
			}
			key = new ArtifactKey(groupId, artifactId, version, classifier);
		}
		return key;
	}

}
