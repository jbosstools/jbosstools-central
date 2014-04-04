/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl.filters;

import org.eclipse.jface.viewers.ViewerFilter;
import org.jboss.tools.central.editors.xpl.DiscoveryViewer;

/**
 * A FilterEntry represent a user entry for a filter on {@link DiscoveryViewer}
 * @author mistria
 *
 */
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
