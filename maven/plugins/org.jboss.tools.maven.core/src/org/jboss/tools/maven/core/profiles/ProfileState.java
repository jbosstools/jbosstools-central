package org.jboss.tools.maven.core.profiles;

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
