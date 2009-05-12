package org.jboss.tools.project.examples.model;

import java.net.URL;

public class ProjectExampleSite implements IProjectExampleSite {
	private URL url;
	private String name;
	private boolean experimental;
	private boolean editable = false;
	
	public ProjectExampleSite() {		
	}
	
	public URL getUrl() {
		return url;
	}
	
	public void setUrl(URL url) {
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isExperimental() {
		return experimental;
	}
	
	public void setExperimental(boolean experimental) {
		this.experimental = experimental;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ProjectExampleSite other = (ProjectExampleSite) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProjectExampleSite [name=" + name + ", url=" + url + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
