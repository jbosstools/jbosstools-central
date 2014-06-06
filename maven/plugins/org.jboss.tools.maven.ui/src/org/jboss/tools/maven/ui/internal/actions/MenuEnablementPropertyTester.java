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
package org.jboss.tools.maven.ui.internal.actions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jboss.tools.maven.ui.Activator;


/**
 * Property tester to check if Maven menus are enabled
 * 
 * @author Fred Bricon
 */
public class MenuEnablementPropertyTester extends PropertyTester {

  private static final String IS_CLEAN_VERIFY_MENU_ENABLED_NODE = "isCleanVerifyMenuEnabled"; //$NON-NLS-1$

  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

	boolean enabled = true;
	if(IS_CLEAN_VERIFY_MENU_ENABLED_NODE.equals(property)) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		enabled = store.getBoolean(Activator.ENABLE_MAVEN_CLEAN_VERIFY_MENU);
    }

	return enabled;
  }

}
