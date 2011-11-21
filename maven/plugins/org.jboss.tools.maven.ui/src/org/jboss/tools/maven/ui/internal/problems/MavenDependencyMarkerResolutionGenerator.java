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
package org.jboss.tools.maven.ui.internal.problems;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

@SuppressWarnings("nls")
public class MavenDependencyMarkerResolutionGenerator implements
		IMarkerResolutionGenerator2 {

	private static final String MSG_PREFIX = "Missing artifact "; //$NON-NLS-1$

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
					new JBossRepositoriesMarkerResolution(),
					new JBossRepositoriesMarkerResolution() {
						@Override
						public String getLabel() {
							return "Add JBoss repositories to pom.xml";
						}
					} };
		case EAP_REPO:
			return new IMarkerResolution[] { 
					new JBossRepositoriesMarkerResolution() {
						@Override
						public String getLabel() {
							return "Set up EAP repository";
						}
					} };
		}
		return new IMarkerResolution[0];
	}

	public boolean hasResolutions(IMarker marker) {
		return ResolutionType.UNSUPPORTED != getResolutionType(marker);
	}

	private ResolutionType getResolutionType(IMarker marker) {
		ArtifactKey key = getArtifactKey(marker);
		if (key.getVersion().contains("-redhat")) {
			return ResolutionType.EAP_REPO;
		} else if (key.getGroupId().startsWith("org.jboss")) {
			return ResolutionType.JBOSS_REPO;
		}
		return ResolutionType.UNSUPPORTED;
	}
 
	private ArtifactKey getArtifactKey(IMarker marker) {
		ArtifactKey key = null;
		try {
			key = (ArtifactKey) marker.getAttribute("artifactKey");
			if (key == null) {
				String message = (String) marker.getAttribute("message", null);
				key = parseMessage(message);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return key;
	}

	private ArtifactKey parseMessage(String message) {
		ArtifactKey key = null;
		if (message != null && message.startsWith(MSG_PREFIX)) {
			String keyString = message.substring(MSG_PREFIX.length());
			String[] keyAsArray = keyString.trim().split(":"); //$NON-NLS-1$
			if (keyAsArray.length > 3) {
				String artifactId = keyAsArray[0];
				String groupId 	  = keyAsArray[1];
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
		}
		return key;
	}

}
