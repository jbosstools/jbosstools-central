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
package org.jboss.tools.central.reddeer.perspective;

import org.jboss.reddeer.eclipse.ui.perspectives.AbstractPerspective;

/**
 * 
 * @author rhopp
 *
 */

public class JBossPerspective extends AbstractPerspective {

	/**
	 * Constructs the perspective with "JBoss".
	 * @deprecated Use org.jboss.tools.common.reddeer.perspectives.JBossPerspective instead
	 */
	public JBossPerspective() {
		super("JBoss");
	}

}
