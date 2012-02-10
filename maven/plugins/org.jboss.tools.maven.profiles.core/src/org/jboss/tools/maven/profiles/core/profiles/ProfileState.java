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
package org.jboss.tools.maven.profiles.core.profiles;

public enum ProfileState {
	Disabled(false), 
	Inactive(false), 
	Active(true); 
	
	private boolean active;
	
	ProfileState(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
	
}
