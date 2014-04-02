package org.jboss.tools.central.editors.xpl.filters;

import org.eclipse.jface.viewers.ViewerFilter;

public class FilterEntry {

	private ViewerFilter filter;
	private String label;
	private boolean enabled;

	public FilterEntry(ViewerFilter filter, String label, boolean defaultEnabled) {
		this.filter = filter;
		this.label = label;
		this.enabled = defaultEnabled;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public ViewerFilter getFilter() {
		return this.filter;
	}
	
	public void setEnabled(boolean enable) {
		this.enabled = enable; 
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
}
