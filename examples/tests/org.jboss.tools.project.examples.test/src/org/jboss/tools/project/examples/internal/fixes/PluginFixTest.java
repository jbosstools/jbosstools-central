/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.internal.fixes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.tools.project.examples.model.RequirementModel;
import org.junit.Test;

public class PluginFixTest {
	
	@Test
	public void testIsSatisfied() {
		RequirementModel requirement = createRequirement("org.jboss.tools.project.examples", null);
		PluginFix fix = new PluginFixProvider().create(null, requirement);
		assertTrue(fix.isSatisfied());
	}
	
	@Test
	public void testIsNotSatisfied() {
		RequirementModel requirement = createRequirement("foo.bar.examples.test", null);
		PluginFix fix = new PluginFixProvider().create(null, requirement);
		assertFalse(fix.isSatisfied());

		requirement = createRequirement("org.jboss.tools.project.examples", "[9999,10000)");
		fix = new PluginFixProvider().create(null, requirement);
		assertFalse(fix.isSatisfied());
	}
	
	
	protected RequirementModel createRequirement(String pluginId, String versions) {
		RequirementModel requirement = new RequirementModel("plugin");
		requirement.getProperties().put(RequirementModel.ID, pluginId);
		requirement.getProperties().put("versions", versions);
		return requirement;
	}
}
