/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.seam.internal.fixes;

import org.jboss.tools.project.examples.fixes.AbstractRuntimeFixUIHandler;

public class SeamRuntimeFixUIHandler extends AbstractRuntimeFixUIHandler {

	@Override
	protected String getPreferencePageId() {
		return "org.jboss.tools.common.model.ui.seam"; //$NON-NLS-1$
	}
}
