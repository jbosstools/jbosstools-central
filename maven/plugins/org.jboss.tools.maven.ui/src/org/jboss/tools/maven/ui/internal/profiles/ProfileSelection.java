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
package org.jboss.tools.maven.ui.internal.profiles;

import org.jboss.tools.maven.core.profiles.ProfileState;

public class ProfileSelection {
	private String id;
	private Boolean autoActive;
	private Boolean selected;
	private ProfileState activationState;
	private String source;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Boolean getAutoActive() {
		return autoActive;
	}
	public void setAutoActive(Boolean autoActive) {
		this.autoActive = autoActive;
	}
	public Boolean getSelected() {
		return selected;
	}
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	public ProfileState getActivationState() {
		return activationState;
	}
	public void setActivationState(ProfileState activationState) {
		this.activationState = activationState;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public String toMavenString() {
		String s = id;
		if (ProfileState.Disabled == activationState) {
			s = "!"+id;
		}
		return s;
	}
}
