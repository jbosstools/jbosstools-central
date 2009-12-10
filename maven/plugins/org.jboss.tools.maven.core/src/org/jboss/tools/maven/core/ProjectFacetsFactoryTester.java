package org.jboss.tools.maven.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.common.project.facet.core.internal.ProjectFacetVersion;

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
