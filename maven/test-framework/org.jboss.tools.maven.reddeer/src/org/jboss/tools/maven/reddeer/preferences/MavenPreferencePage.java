/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.reddeer.preferences;

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.impl.button.CheckBox;

public class MavenPreferencePage extends PreferencePage{
	
	public MavenPreferencePage(ReferencedComposite referencedComposite){
		super(referencedComposite, "Maven");
	}
	
	public void updateIndexesOnStartup(boolean update){
		CheckBox cb = new CheckBox("Download repository index updates on startup");
		cb.toggle(update);
	}
}
