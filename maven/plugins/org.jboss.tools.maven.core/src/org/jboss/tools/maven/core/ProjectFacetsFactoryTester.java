/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.common.project.facet.core.internal.ProjectFacetVersion;

/**
 * @author snjeza
 * 
 */
public class ProjectFacetsFactoryTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		
		if (! (receiver instanceof Collection)) {
			return false;
		}
		if (! (expectedValue instanceof String)) {
			return false;
		}
		String requiredFacets = (String) expectedValue;
		Collection collection = (Collection) receiver;
		List<String> facets = new ArrayList<String>();
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if ( ! (object instanceof ProjectFacetVersion)) {
				return false;
			}
			ProjectFacetVersion projectFacetVersion = (ProjectFacetVersion) object;
			facets.add(projectFacetVersion.getProjectFacet().getId());
		}
		StringTokenizer tokenizer = new StringTokenizer(requiredFacets,","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String requiredFacet = tokenizer.nextToken().trim();
			if (!facets.contains(requiredFacet)) {
				return false;
			}
		}
		return true;
	}

}
