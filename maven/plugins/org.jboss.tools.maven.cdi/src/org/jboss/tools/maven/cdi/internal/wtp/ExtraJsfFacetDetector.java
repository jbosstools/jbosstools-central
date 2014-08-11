/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.cdi.internal.wtp;

import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.wtp.facets.AbstractFacetDetector;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

/**
 * Detects the JSF Facet Version for some known CDI dependencies
 * 
 * @author Fred Bricon
 *
 */
public class ExtraJsfFacetDetector extends AbstractFacetDetector {

	private static final String JSF_FACET_ID = "jst.jsf";
	
	private static final IProjectFacet JSF_FACET;
	
	static {
		IProjectFacet jsfFacet = null;
		if (ProjectFacetsManager.isProjectFacetDefined(JSF_FACET_ID)) {
			jsfFacet = ProjectFacetsManager.getProjectFacet(JSF_FACET_ID);
		}
		JSF_FACET = jsfFacet;
	}
	
	@Override
	public IProjectFacetVersion findFacetVersion(
			IMavenProjectFacade mavenProjectFacade, Map<?, ?> context,
			IProgressMonitor monitor) throws CoreException {
		if (JSF_FACET == null) {
			return null;
		}
		if (mavenProjectFacade != null && mavenProjectFacade.getMavenProject() != null) {
			for (Artifact artifact : mavenProjectFacade.getMavenProject().getArtifacts()) {
				if (isKnownJsf2BasedArtifact(artifact)) {
					return JSF_FACET.getVersion("2.0");
				}
			}
		}
		return null;
	}

	private boolean isKnownJsf2BasedArtifact(Artifact artifact) {
		return (artifact.getGroupId().startsWith("org.jboss.seam.") 	//$NON-NLS-1$ 
				&& artifact.getArtifactId().startsWith("seam-faces") 	//$NON-NLS-1$
				&& artifact.getVersion().startsWith("3."))
				||
				("org.apache.deltaspike.modules".equals(artifact.getGroupId()) 	//$NON-NLS-1$ 
						&& artifact.getArtifactId().startsWith("deltaspike-jsf-module"));//$NON-NLS-1$			
	}

}
