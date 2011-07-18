package org.jboss.tools.maven.core.profiles;

public class ProfileStatus {
	private String id;
	private boolean autoActive;
	private boolean userSelected;
	private ProfileState activationState;
	private String source;
	
	public ProfileStatus(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isAutoActive() {
		return autoActive;
	}

	public void setAutoActive(boolean autoActive) {
		this.autoActive = autoActive;
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

	public boolean isUserSelected() {
		return userSelected;
	}

	public void setUserSelected(boolean userSelected) {
		this.userSelected = userSelected;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((activationState == null) ? 0 : activationState.hashCode());
		result = prime * result + (autoActive ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + (userSelected ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProfileStatus other = (ProfileStatus) obj;
		if (activationState != other.activationState)
			return false;
		if (autoActive != other.autoActive)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (userSelected != other.userSelected)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProfileStatus [id=" + id + ", autoActive=" + autoActive
				+ ", userSelected=" + userSelected + ", activationState="
				+ activationState + ", source=" + source + "]";
	}

	
	
}
