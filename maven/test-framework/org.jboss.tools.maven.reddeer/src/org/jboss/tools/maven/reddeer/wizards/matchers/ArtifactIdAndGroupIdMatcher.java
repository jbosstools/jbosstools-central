/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.reddeer.wizards.matchers;

import org.eclipse.reddeer.swt.api.TableItem;
import org.eclipse.reddeer.swt.impl.table.DefaultTableItem;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * This class implements a matcher of groupId and artifactId for 
 * class org.jboss.tools.maven.reddeer.wizards.MavenProjectWizardSecondPage
 * 
 * @author vprusa
 *
 */
public class ArtifactIdAndGroupIdMatcher extends BaseMatcher<TableItem> {

	protected String groupId = null;
	protected String artifactId = "";

	public ArtifactIdAndGroupIdMatcher(String groupId, String artifactId) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	@Override
	public boolean matches(Object arg0) {
		DefaultTableItem ti = (DefaultTableItem) arg0;
		// archetype is necessary, groupId is optional but if set it has to match
		return ((groupId != null && ti.getText(0).matches(".*" + groupId + ".*")) || groupId == null)
				&& ti.getText(1).matches(".*" + artifactId + ".*");
	}

	@Override
	public void describeTo(Description arg0) {
	}
}
