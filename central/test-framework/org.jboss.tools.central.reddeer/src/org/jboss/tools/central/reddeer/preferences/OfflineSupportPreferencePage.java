/******************************************************************************* 
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.central.reddeer.preferences;

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.impl.group.DefaultGroup;
import org.eclipse.reddeer.swt.impl.styledtext.DefaultStyledText;

/**
 * 
 * @author rhopp
 *
 */

public class OfflineSupportPreferencePage extends PreferencePage {

	public OfflineSupportPreferencePage(ReferencedComposite referencedComposite) {
		super(referencedComposite, new String[] {"JBoss Tools", "Project Examples", "Offline Support"});
	}
	
	public String getCommand(){
		DefaultGroup defaultGroup = new DefaultGroup("Prepare offline data");
		DefaultStyledText defaultStyledText = new DefaultStyledText(defaultGroup);
		return defaultStyledText.getText();
	}
	
}
