/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.core.settings;

/**
 * A listener which is notified when the Maven Settings have been changed by 
 * JBoss Tools (the Maven Repository Wizard). 
 * 
 * @author Fred Bricon
 *
 */
public interface MavenSettingsChangeListener {
	
	/**
	 * Notifies that the Maven Settings have changed.
	 */
	void onSettingsChanged();

}
