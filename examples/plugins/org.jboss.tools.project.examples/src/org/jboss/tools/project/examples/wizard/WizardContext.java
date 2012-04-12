/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.wizard;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class WizardContext {

	private Map<String, Object> context;
	
	private Set<IWizardContextChangeListener> listeners;
	
	public WizardContext() {
		context = new HashMap<String, Object>();
		listeners = new LinkedHashSet<IWizardContextChangeListener>();
	}
	
	public void addListener(IWizardContextChangeListener newListener) {
		listeners.add(newListener);
	}
	
	public void removeListener(IWizardContextChangeListener listener) {
		listeners.remove(listener);
	}
	
	public void setProperty(String key, Object value) {
		Object previousValue = context.get(key);
		if ((previousValue != null && !previousValue.equals(value))
				|| (previousValue == null && value != null)) {
			context.put(key, value);
			fireChangeEvent(key, value);
		}
	}

	public Object getProperty(String key) {
		return context.get(key);
	}
	
	private void fireChangeEvent(String key, Object value) {
		for (IWizardContextChangeListener listener : listeners) {
			listener.onWizardContextChange(key, value);
		}
	}
}
