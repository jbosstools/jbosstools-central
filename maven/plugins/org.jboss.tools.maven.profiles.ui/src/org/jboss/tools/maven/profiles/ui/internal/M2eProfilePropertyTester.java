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
package org.jboss.tools.maven.profiles.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Platform;

/**
 * 
 * @author Fred Bricon
 * 
 */
public class M2eProfilePropertyTester extends PropertyTester {

	
	private static final Boolean hasConflict;

	static {
		hasConflict = Platform.getBundle("org.eclipse.m2e.profiles.ui") != null;
	}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (!"hasConflict".equals(property)) {
			return false;
		} 
		
		return hasConflict.equals(expectedValue);
	}
	
	public static boolean isConflicting() {
		return hasConflict.booleanValue();
	}
	
		
}
